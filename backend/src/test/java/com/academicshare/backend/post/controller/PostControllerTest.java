package com.academicshare.backend.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.comment.domain.Comment;
import com.academicshare.backend.comment.repository.CommentRepository;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.notification.domain.Notification;
import com.academicshare.backend.notification.repository.NotificationRepository;
import com.academicshare.backend.post.domain.Like;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.domain.PostFile;
import com.academicshare.backend.post.repository.LikeRepository;
import com.academicshare.backend.post.repository.PostFileRepository;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.report.domain.Report;
import com.academicshare.backend.report.domain.ReportTargetType;
import com.academicshare.backend.report.repository.ReportRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.upload.root=build/test-uploads")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostControllerTest {

    private static final String ANONYMOUS_DISPLAY_NAME = "\uC775\uBA85_1";
    private static final String DELETED_USER_DISPLAY_NAME = "\uD0C8\uD1F4\uD55C \uC720\uC800";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostFileRepository postFileRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getPostListReturnsNewestPostsWithLikeFields() throws Exception {
        User currentUser = saveUser("list-current", "List Current", "list-current@example.com");
        User author = saveUser("list-author", "List Author", "list-author@example.com");
        Post olderPost = savePost(author, "Older title", "older content", "Major", "Subject", false);
        Post newerPost = savePost(author, "Newer title", "newer content", "Major", "Subject", false);
        likeRepository.saveAndFlush(new Like(currentUser.getId(), newerPost.getId()));

        mockMvc.perform(get("/posts")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(newerPost.getId()))
                .andExpect(jsonPath("$.items[0].author_display_name").value("List Author"))
                .andExpect(jsonPath("$.items[0].updated_at").value(nullValue()))
                .andExpect(jsonPath("$.items[0].liked_by_me").value(true))
                .andExpect(jsonPath("$.items[0].like_count").value(1))
                .andExpect(jsonPath("$.items[1].id").value(olderPost.getId()))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total_count").value(2));
    }

    @Test
    void getPostListRejectsMultipleFilterKindsAndInvalidPage() throws Exception {
        User currentUser = saveUser("list-invalid", "List Invalid", "list-invalid@example.com");

        mockMvc.perform(get("/posts")
                        .param("keyword", "title")
                        .param("author", "author")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(get("/posts")
                        .param("page", "0")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void getPostListSupportsSingleFiltersAndExcludesAnonymousOrDeletedAuthors() throws Exception {
        User currentUser = saveUser("filter-current", "Filter Current", "filter-current@example.com");
        User visibleAuthor = saveUser("filter-visible", "Filter Visible", "filter-visible@example.com");
        User otherAuthor = saveUser("filter-other", "Filter Other", "filter-other@example.com");
        Integer deletedAuthorId = insertDeletedUser("filter-deleted", "Filter Visible", "filter-deleted@example.com");
        Post keywordPost = savePost(otherAuthor, "Alpha search title", "plain content", "Major", "Subject", false);
        Post authorPost = savePost(visibleAuthor, "Author result", "plain content", "Major", "Subject", false);
        savePost(visibleAuthor, "Anonymous author result", "plain content", "Major", "Subject", true);
        savePost(deletedAuthorId, "Deleted author result", "plain content", "Major", "Subject", false);
        Post categoryPost = savePost(otherAuthor, "Category result", "plain content", "Engineering", "Backend", false);
        savePost(otherAuthor, "Other category result", "plain content", "Engineering", "Frontend", false);

        mockMvc.perform(get("/posts")
                        .param("keyword", "Alpha")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(keywordPost.getId()));

        mockMvc.perform(get("/posts")
                        .param("author", "Filter Visible")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(authorPost.getId()));

        mockMvc.perform(get("/posts")
                        .param("main_category", "Engineering")
                        .param("sub_category", "Backend")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(categoryPost.getId()));
    }

    @Test
    void getPostsUseAnonymousAndDeletedAuthorDisplayNames() throws Exception {
        User currentUser = saveUser("display-current", "Display Current", "display-current@example.com");
        User activeAuthor = saveUser("display-active", "Display Active", "display-active@example.com");
        Integer deletedAuthorId = insertDeletedUser("display-deleted", "Display Deleted", "display-deleted@example.com");
        Post anonymousPost = savePost(activeAuthor, "Anonymous post", "content", "Major", "Subject", true);
        Post deletedAuthorPost = savePost(deletedAuthorId, "Deleted author post", "content", "Major", "Subject", false);

        mockMvc.perform(get("/posts")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == %d)].author_display_name".formatted(anonymousPost.getId())).value(contains(ANONYMOUS_DISPLAY_NAME)))
                .andExpect(jsonPath("$.items[?(@.id == %d)].author_display_name".formatted(deletedAuthorPost.getId())).value(contains(DELETED_USER_DISPLAY_NAME)));

        mockMvc.perform(get("/posts/{postId}", anonymousPost.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author_display_name").value(ANONYMOUS_DISPLAY_NAME))
                .andExpect(jsonPath("$.updated_at").value(nullValue()));

        mockMvc.perform(get("/posts/{postId}", deletedAuthorPost.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author_display_name").value(DELETED_USER_DISPLAY_NAME));
    }

    @Test
    void getPostDetailIncrementsViewCountAndReturns404WhenMissing() throws Exception {
        User currentUser = saveUser("detail-current", "Detail Current", "detail-current@example.com");
        Post post = savePost(currentUser, "Detail title", "detail content", "Major", "Subject", false);

        mockMvc.perform(get("/posts/{postId}", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.updated_at").value(nullValue()))
                .andExpect(jsonPath("$.view_count").value(1));

        assertThat(postRepository.findById(post.getId()).orElseThrow().getViewCount()).isEqualTo(1);

        mockMvc.perform(get("/posts/{postId}", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void createPostStoresUploadedFileAndRejectsMissingRequiredField() throws Exception {
        User currentUser = saveUser("create-current", "Create Current", "create-current@example.com");
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "notes.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "file-content".getBytes(StandardCharsets.UTF_8)
        );

        MvcResult result = mockMvc.perform(multipart("/posts")
                        .file(file)
                        .param("title", "Created title")
                        .param("content", "Created content")
                        .param("main_category", "Major")
                        .param("sub_category", "Subject")
                        .param("is_anonymous", "false")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Created title"))
                .andExpect(jsonPath("$.updated_at").value(nullValue()))
                .andExpect(jsonPath("$.files.length()").value(1))
                .andExpect(jsonPath("$.files[0].file_url", startsWith("/uploads/posts/")))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        Integer postId = response.get("id").asInt();
        String fileUrl = response.get("files").get(0).get("file_url").asText();
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        assertThat(postFileRepository.findByIdOrderByFileUrlAsc(postId))
                .extracting(PostFile::getFileUrl)
                .allMatch(storedFileUrl -> storedFileUrl.startsWith("/uploads/posts/" + postId + "/"));

        mockMvc.perform(get(fileUrl)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("file-content".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/posts/{postId}/files/{fileName}", postId, fileName)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("file-content".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(get("/posts/{postId}/files/{fileName}", postId, "missing-file")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(multipart("/posts")
                        .param("content", "Missing title")
                        .param("main_category", "Major")
                        .param("sub_category", "Subject")
                        .param("is_anonymous", "false")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(multipart("/posts")
                        .file(new FailingMockMultipartFile("files", "broken.txt", MediaType.TEXT_PLAIN_VALUE))
                        .param("title", "Upload failure")
                        .param("content", "Created content")
                        .param("main_category", "Major")
                        .param("sub_category", "Subject")
                        .param("is_anonymous", "false")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void updatePostSupportsJsonAndRejectsInvalidAuthorOrEmptyPatch() throws Exception {
        User owner = saveUser("update-owner", "Update Owner", "update-owner@example.com");
        User other = saveUser("update-other", "Update Other", "update-other@example.com");
        Post post = savePost(owner, "Before", "before content", "Major", "Subject", false);

        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "After",
                                  "is_anonymous": true
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After"))
                .andExpect(jsonPath("$.is_anonymous").value(true))
                .andExpect(jsonPath("$.updated_at").isNotEmpty());

        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Blocked\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, other.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(patch("/posts/{postId}", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Missing\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void updatePostByMultipartReplacesFiles() throws Exception {
        User owner = saveUser("file-owner", "File Owner", "file-owner@example.com");
        Post post = savePost(owner, "File post", "content", "Major", "Subject", false);
        postFileRepository.saveAndFlush(new PostFile(post.getId(), "/uploads/posts/" + post.getId() + "/old-file"));
        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "new.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "new-file".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/posts/{postId}", post.getId())
                        .file(newFile)
                        .param("title", "File post updated")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("File post updated"))
                .andExpect(jsonPath("$.files.length()").value(1))
                .andExpect(jsonPath("$.files[0].file_url", startsWith("/uploads/posts/" + post.getId() + "/")));

        assertThat(postFileRepository.findByIdOrderByFileUrlAsc(post.getId()))
                .extracting(PostFile::getFileUrl)
                .doesNotContain("/uploads/posts/" + post.getId() + "/old-file")
                .hasSize(1);
    }

    @Test
    void updatePostWithoutNewFilesKeepsExistingFiles() throws Exception {
        User owner = saveUser("keep-file-owner", "Keep File Owner", "keep-file-owner@example.com");
        Post post = savePost(owner, "Keep file post", "content", "Major", "Subject", false);
        String existingFileUrl = "/uploads/posts/" + post.getId() + "/existing-file";
        postFileRepository.saveAndFlush(new PostFile(post.getId(), existingFileUrl));

        mockMvc.perform(patch("/posts/{postId}", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Keep file post updated\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Keep file post updated"))
                .andExpect(jsonPath("$.files.length()").value(1))
                .andExpect(jsonPath("$.files[0].file_url").value(existingFileUrl));

        assertThat(postFileRepository.findByIdOrderByFileUrlAsc(post.getId()))
                .extracting(PostFile::getFileUrl)
                .containsExactly(existingFileUrl);
    }

    @Test
    void deletePostRequiresAuthorAndReturns204() throws Exception {
        User owner = saveUser("delete-owner", "Delete Owner", "delete-owner@example.com");
        User other = saveUser("delete-other", "Delete Other", "delete-other@example.com");
        Post post = savePost(owner, "Delete post", "content", "Major", "Subject", false);

        mockMvc.perform(delete("/posts/{postId}", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, other.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(delete("/posts/{postId}", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(post.getId())).isFalse();

        mockMvc.perform(delete("/posts/{postId}", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void deletePostCascadesPostChildrenAndKeepsReportHistory() throws Exception {
        User owner = saveUser("cascade-owner", "Cascade Owner", "cascade-owner@example.com");
        User reporter = saveUser("cascade-reporter", "Cascade Reporter", "cascade-reporter@example.com");
        Post post = savePost(owner, "Cascade post", "content", "Major", "Subject", false);
        likeRepository.saveAndFlush(new Like(reporter.getId(), post.getId()));
        Comment comment = commentRepository.saveAndFlush(new Comment(
                reporter.getId(),
                post.getId(),
                null,
                "Cascade comment",
                false
        ));
        postFileRepository.saveAndFlush(new PostFile(post.getId(), "/uploads/posts/" + post.getId() + "/cascade-file"));
        Notification notification = notificationRepository.saveAndFlush(new Notification(
                "Cascade notification",
                post.getId(),
                owner.getId(),
                comment.getId()
        ));
        Report report = reportRepository.saveAndFlush(new Report(
                reporter.getId(),
                ReportTargetType.POST,
                post.getId(),
                1
        ));

        mockMvc.perform(delete("/posts/{postId}", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(postRepository.existsById(post.getId())).isFalse();
        assertThat(likeRepository.countByPostId(post.getId())).isZero();
        assertThat(commentRepository.findByPostIdOrderByCreatedAtAscIdAsc(post.getId())).isEmpty();
        assertThat(postFileRepository.findByIdOrderByFileUrlAsc(post.getId())).isEmpty();
        assertThat(notificationRepository.existsById(notification.getId())).isFalse();
        assertThat(reportRepository.existsById(report.getId())).isTrue();
    }

    @Test
    void createAndDeleteLikeHandleDuplicateAndMissingLike() throws Exception {
        User currentUser = saveUser("like-current", "Like Current", "like-current@example.com");
        Post post = savePost(currentUser, "Like post", "content", "Major", "Subject", false);

        mockMvc.perform(post("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(currentUser.getId()))
                .andExpect(jsonPath("$.post_id").value(post.getId()));

        mockMvc.perform(post("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));

        mockMvc.perform(delete("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/posts/{postId}/likes", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(post("/posts/{postId}/likes", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));

        mockMvc.perform(delete("/posts/{postId}/likes", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    private User saveUser(String loginId, String name, String emailAddress) {
        return userRepository.saveAndFlush(new User(
                loginId,
                "encoded-password",
                name,
                emailAddress
        ));
    }

    private Post savePost(User user, String title, String content, String mainCategory, String subCategory, boolean anonymous) {
        return savePost(user.getId(), title, content, mainCategory, subCategory, anonymous);
    }

    private Post savePost(Integer userId, String title, String content, String mainCategory, String subCategory, boolean anonymous) {
        return postRepository.saveAndFlush(new Post(
                userId,
                title,
                content,
                mainCategory,
                subCategory,
                anonymous
        ));
    }

    private Integer insertDeletedUser(String loginId, String name, String emailAddress) {
        jdbcTemplate.update("""
                INSERT INTO users (login_id, password, name, email_address, deleted_at, status, role)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'DELETED', 'USER')
                """, loginId, "encoded-password", name, emailAddress);

        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Integer.class, loginId);
    }

    private static class FailingMockMultipartFile extends MockMultipartFile {

        FailingMockMultipartFile(String name, String originalFilename, String contentType) {
            super(name, originalFilename, contentType, new byte[] {1});
        }

        @Override
        public InputStream getInputStream() throws IOException {
            throw new IOException("forced upload failure");
        }
    }
}
