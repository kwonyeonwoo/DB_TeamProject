package com.academicshare.backend.post.service;

import com.academicshare.backend.auth.session.CurrentUserProvider;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.common.response.PageResponse;
import com.academicshare.backend.post.domain.Like;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.domain.PostFile;
import com.academicshare.backend.post.dto.LikeResponse;
import com.academicshare.backend.post.dto.PostCreateRequest;
import com.academicshare.backend.post.dto.PostListQuery;
import com.academicshare.backend.post.dto.PostResponse;
import com.academicshare.backend.post.dto.PostUpdateRequest;
import com.academicshare.backend.post.repository.LikeRepository;
import com.academicshare.backend.post.repository.PostFileRepository;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserStatus;
import com.academicshare.backend.user.repository.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostService {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_CATEGORY_LENGTH = 100;
    private static final String ANONYMOUS_DISPLAY_NAME = "\uC775\uBA85_1";
    private static final String DELETED_USER_DISPLAY_NAME = "\uD0C8\uD1F4\uD55C \uC720\uC800";

    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PostFileStorage postFileStorage;

    public PostService(
            PostRepository postRepository,
            PostFileRepository postFileRepository,
            LikeRepository likeRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            PostFileStorage postFileStorage
    ) {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.postFileStorage = postFileStorage;
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getPostList(PostListQuery query) {
        validatePage(query.page(), query.size());
        validateSingleFilter(query);

        Pageable pageable = PageRequest.of(
                query.page() - 1,
                query.size(),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<Post> posts = searchPosts(query, pageable);
        List<PostResponse> items = toResponses(posts.getContent());

        return new PageResponse<>(
                items,
                query.page(),
                query.size(),
                posts.getTotalElements(),
                posts.getTotalPages()
        );
    }

    @Transactional
    public PostResponse getPostDetail(Integer postId) {
        Post post = findPost(postId);
        post.increaseViewCount();

        return toResponse(post);
    }

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        validateCreateRequest(request);

        Post post = postRepository.saveAndFlush(new Post(
                currentUserProvider.getCurrentUserId(),
                request.title(),
                request.content(),
                request.mainCategory(),
                request.subCategory(),
                request.isAnonymous()
        ));
        saveFiles(post.getId(), request.files());

        return toResponse(post);
    }

    @Transactional
    public PostResponse updatePost(Integer postId, PostUpdateRequest request) {
        Post post = findPost(postId);
        requireAuthor(post);
        validateUpdateRequest(request);

        if (request.titleProvided()) {
            post.changeTitle(request.title());
        }
        if (request.contentProvided()) {
            post.changeContent(request.content());
        }
        if (request.mainCategoryProvided()) {
            post.changeMainCategory(request.mainCategory());
        }
        if (request.subCategoryProvided()) {
            post.changeSubCategory(request.subCategory());
        }
        if (request.isAnonymousProvided()) {
            post.changeIsAnonymous(request.isAnonymous());
        }
        if (request.hasUploadedFiles()) {
            postFileRepository.deleteAllByPostId(post.getId());
            saveFiles(post.getId(), request.files());
        }
        post.markUpdated();

        return toResponse(post);
    }

    @Transactional
    public void deletePost(Integer postId) {
        Post post = findPost(postId);
        requireAuthor(post);
        postRepository.delete(post);
    }

    @Transactional
    public LikeResponse createLike(Integer postId) {
        if (!postRepository.existsById(postId)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        Integer currentUserId = currentUserProvider.getCurrentUserId();
        if (likeRepository.existsByUserIdAndPostId(currentUserId, postId)) {
            throw new ApiException(ErrorCode.CONFLICT);
        }

        try {
            return LikeResponse.from(likeRepository.saveAndFlush(new Like(currentUserId, postId)));
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
    }

    @Transactional
    public void deleteLike(Integer postId) {
        if (!postRepository.existsById(postId)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        Integer currentUserId = currentUserProvider.getCurrentUserId();
        Like like = likeRepository.findByUserIdAndPostId(currentUserId, postId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        likeRepository.delete(like);
    }

    private Page<Post> searchPosts(PostListQuery query, Pageable pageable) {
        if (StringUtils.hasText(query.keyword())) {
            return postRepository.searchByKeyword(query.keyword(), pageable);
        }
        if (StringUtils.hasText(query.author())) {
            return postRepository.searchByAuthor(query.author(), UserStatus.ACTIVE, pageable);
        }
        if (StringUtils.hasText(query.mainCategory()) || StringUtils.hasText(query.subCategory())) {
            return postRepository.searchByCategory(
                    blankToNull(query.mainCategory()),
                    blankToNull(query.subCategory()),
                    pageable
            );
        }
        return postRepository.findAll(pageable);
    }

    private Post findPost(Integer postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void requireAuthor(Post post) {
        if (!post.isOwnedBy(currentUserProvider.getCurrentUserId())) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void saveFiles(Integer postId, List<MultipartFile> files) {
        List<PostFile> postFiles = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> new PostFile(postId, postFileStorage.store(postId, file)))
                .toList();

        if (!postFiles.isEmpty()) {
            postFileRepository.saveAll(postFiles);
        }
    }

    private List<PostResponse> toResponses(List<Post> posts) {
        Map<Integer, List<PostFile>> filesByPostId = filesByPostId(posts.stream().map(Post::getId).toList());
        Map<Integer, User> usersById = usersById(posts.stream().map(Post::getUserId).toList());
        Integer currentUserId = currentUserProvider.getCurrentUserId();

        return posts.stream()
                .map(post -> PostResponse.from(
                        post,
                        authorDisplayName(post, usersById.get(post.getUserId())),
                        filesByPostId.getOrDefault(post.getId(), List.of()),
                        likeRepository.existsByUserIdAndPostId(currentUserId, post.getId()),
                        likeRepository.countByPostId(post.getId())
                ))
                .toList();
    }

    private PostResponse toResponse(Post post) {
        User author = userRepository.findById(post.getUserId()).orElse(null);
        return PostResponse.from(
                post,
                authorDisplayName(post, author),
                postFileRepository.findByIdOrderByFileUrlAsc(post.getId()),
                likeRepository.existsByUserIdAndPostId(currentUserProvider.getCurrentUserId(), post.getId()),
                likeRepository.countByPostId(post.getId())
        );
    }

    private Map<Integer, List<PostFile>> filesByPostId(Collection<Integer> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        return postFileRepository.findByIdInOrderByIdAscFileUrlAsc(postIds)
                .stream()
                .collect(Collectors.groupingBy(PostFile::getId));
    }

    private Map<Integer, User> usersById(Collection<Integer> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private String authorDisplayName(Post post, User author) {
        if (author == null || author.getStatus() == UserStatus.DELETED) {
            return DELETED_USER_DISPLAY_NAME;
        }
        if (Boolean.TRUE.equals(post.getIsAnonymous())) {
            return ANONYMOUS_DISPLAY_NAME;
        }
        return author.getName();
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateSingleFilter(PostListQuery query) {
        int filterCount = 0;
        if (StringUtils.hasText(query.keyword())) {
            filterCount++;
        }
        if (StringUtils.hasText(query.author())) {
            filterCount++;
        }
        if (StringUtils.hasText(query.mainCategory()) || StringUtils.hasText(query.subCategory())) {
            filterCount++;
        }
        if (filterCount > 1) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateCreateRequest(PostCreateRequest request) {
        validateRequiredText(request.title(), MAX_TITLE_LENGTH);
        validateRequiredText(request.mainCategory(), MAX_CATEGORY_LENGTH);
        validateRequiredText(request.subCategory(), MAX_CATEGORY_LENGTH);
        if (request.isAnonymous() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateUpdateRequest(PostUpdateRequest request) {
        if (!request.hasAnyField()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.titleProvided()) {
            validateRequiredText(request.title(), MAX_TITLE_LENGTH);
        }
        if (request.mainCategoryProvided()) {
            validateRequiredText(request.mainCategory(), MAX_CATEGORY_LENGTH);
        }
        if (request.subCategoryProvided()) {
            validateRequiredText(request.subCategory(), MAX_CATEGORY_LENGTH);
        }
        if (request.isAnonymousProvided() && request.isAnonymous() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateRequiredText(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() > maxLength) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
