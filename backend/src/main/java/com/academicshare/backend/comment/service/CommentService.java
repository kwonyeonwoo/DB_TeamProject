package com.academicshare.backend.comment.service;

import com.academicshare.backend.auth.session.CurrentUserProvider;
import com.academicshare.backend.comment.domain.Comment;
import com.academicshare.backend.comment.dto.CommentCreateRequest;
import com.academicshare.backend.comment.dto.CommentResponse;
import com.academicshare.backend.comment.dto.CommentUpdateRequest;
import com.academicshare.backend.comment.repository.CommentRepository;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.notification.service.NotificationService;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserStatus;
import com.academicshare.backend.user.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CommentService {

    private static final String ANONYMOUS_PREFIX = "\uC775\uBA85_";
    private static final String DELETED_USER_DISPLAY_NAME = "\uD0C8\uD1F4\uD55C \uC720\uC800";

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationService notificationService;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            UserRepository userRepository,
            CurrentUserProvider currentUserProvider,
            NotificationService notificationService
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Integer postId) {
        Post post = findPost(postId);
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAscIdAsc(postId);
        Map<Integer, User> usersById = usersById(comments);
        Map<Integer, Integer> anonymousNumbers = anonymousNumbers(post, comments, usersById);

        return comments.stream()
                .map(comment -> CommentResponse.from(
                        comment,
                        authorDisplayName(comment, usersById.get(comment.getUserId()), anonymousNumbers)
                ))
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Integer postId, CommentCreateRequest request) {
        validateCreateRequest(request);

        Post post = findPost(postId);
        Integer currentUserId = currentUserProvider.getCurrentUserId();
        Comment comment = commentRepository.saveAndFlush(new Comment(
                currentUserId,
                post.getId(),
                null,
                request.content(),
                isAnonymousOrFalse(request.isAnonymous())
        ));
        notificationService.createCommentNotificationIfNeeded(
                post.getUserId(),
                currentUserId,
                post.getId(),
                comment.getContent()
        );

        return toResponse(comment);
    }

    @Transactional
    public CommentResponse createReply(Integer parentCommentId, CommentCreateRequest request) {
        validateCreateRequest(request);

        Comment parentComment = findComment(parentCommentId);
        if (parentComment.isReply()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        Integer currentUserId = currentUserProvider.getCurrentUserId();
        Comment reply = commentRepository.saveAndFlush(new Comment(
                currentUserId,
                parentComment.getPostId(),
                parentComment.getId(),
                request.content(),
                isAnonymousOrFalse(request.isAnonymous())
        ));
        notificationService.createReplyNotificationIfNeeded(
                parentComment.getUserId(),
                currentUserId,
                parentComment.getPostId(),
                parentComment.getId(),
                reply.getContent()
        );

        return toResponse(reply);
    }

    @Transactional
    public CommentResponse updateComment(Integer commentId, CommentUpdateRequest request) {
        Comment comment = findComment(commentId);
        requireAuthor(comment);
        validateUpdateRequest(request);

        if (request.contentProvided()) {
            comment.changeContent(request.content());
        }
        if (request.isAnonymousProvided()) {
            comment.changeIsAnonymous(request.isAnonymous());
        }
        comment.markUpdated();

        return toResponse(comment);
    }

    @Transactional
    public void deleteComment(Integer commentId) {
        Comment comment = findComment(commentId);
        requireAuthor(comment);
        commentRepository.delete(comment);
    }

    private Post findPost(Integer postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private Comment findComment(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void requireAuthor(Comment comment) {
        if (!comment.isOwnedBy(currentUserProvider.getCurrentUserId())) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
    }

    private CommentResponse toResponse(Comment comment) {
        User user = userRepository.findById(comment.getUserId()).orElse(null);
        return CommentResponse.from(comment, authorDisplayName(comment, user, Map.of(comment.getUserId(), 1)));
    }

    private Map<Integer, User> usersById(List<Comment> comments) {
        if (comments.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(comments.stream().map(Comment::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Integer, Integer> anonymousNumbers(Post post, List<Comment> comments, Map<Integer, User> usersById) {
        Map<Integer, Integer> numbers = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(post.getIsAnonymous())) {
            User postAuthor = userRepository.findById(post.getUserId()).orElse(null);
            if (isActiveUser(postAuthor)) {
                numbers.put(post.getUserId(), numbers.size() + 1);
            }
        }

        for (Comment comment : comments) {
            if (Boolean.TRUE.equals(comment.getIsAnonymous()) && isActiveUser(usersById.get(comment.getUserId()))) {
                numbers.computeIfAbsent(comment.getUserId(), ignored -> numbers.size() + 1);
            }
        }
        return numbers;
    }

    private String authorDisplayName(Comment comment, User user, Map<Integer, Integer> anonymousNumbers) {
        if (!isActiveUser(user)) {
            return DELETED_USER_DISPLAY_NAME;
        }
        if (Boolean.TRUE.equals(comment.getIsAnonymous())) {
            return ANONYMOUS_PREFIX + anonymousNumbers.getOrDefault(comment.getUserId(), 1);
        }
        return user.getName();
    }

    private boolean isActiveUser(User user) {
        return user != null && user.getStatus() == UserStatus.ACTIVE;
    }

    private void validateCreateRequest(CommentCreateRequest request) {
        if (!StringUtils.hasText(request.content())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validateUpdateRequest(CommentUpdateRequest request) {
        if (!request.hasAnyField()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.contentProvided() && !StringUtils.hasText(request.content())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
        if (request.isAnonymousProvided() && request.isAnonymous() == null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private Boolean isAnonymousOrFalse(Boolean isAnonymous) {
        return isAnonymous != null && isAnonymous;
    }
}
