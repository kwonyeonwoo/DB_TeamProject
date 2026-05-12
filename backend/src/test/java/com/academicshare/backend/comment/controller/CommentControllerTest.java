package com.academicshare.backend.comment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.academicshare.backend.auth.session.AuthSessionAttributes;
import com.academicshare.backend.comment.domain.Comment;
import com.academicshare.backend.comment.repository.CommentRepository;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.notification.domain.Notification;
import com.academicshare.backend.notification.repository.NotificationRepository;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentControllerTest {

    private static final String ANONYMOUS_1 = "\uC775\uBA85_1";
    private static final String DELETED_USER = "\uD0C8\uD1F4\uD55C \uC720\uC800";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getCommentsReturnsCommentsAndRepliesWithDisplayNames() throws Exception {
        User currentUser = saveUser("comment-list-current", "Current User", "comment-list-current@example.com");
        User visibleAuthor = saveUser("comment-visible-author", "Visible Author", "comment-visible@example.com");
        User anonymousAuthor = saveUser("comment-anonymous-author", "Anonymous Author", "comment-anonymous@example.com");
        Integer deletedUserId = insertDeletedUser("comment-deleted-author", "comment-deleted@example.com");
        Post post = savePost(currentUser);
        Comment visibleComment = saveComment(visibleAuthor, post, null, "visible comment", false);
        Comment anonymousComment = saveComment(anonymousAuthor, post, null, "anonymous comment", true);
        Comment deletedComment = commentRepository.saveAndFlush(new Comment(
                deletedUserId,
                post.getId(),
                null,
                "deleted user comment",
                true
        ));
        Comment reply = saveComment(currentUser, post, visibleComment.getId(), "reply comment", false);

        mockMvc.perform(get("/posts/{postId}/comments", post.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(4))
                .andExpect(jsonPath("$.items[?(@.id == %d)].author_display_name".formatted(visibleComment.getId())).value(contains("Visible Author")))
                .andExpect(jsonPath("$.items[?(@.id == %d)].author_display_name".formatted(anonymousComment.getId())).value(contains(ANONYMOUS_1)))
                .andExpect(jsonPath("$.items[?(@.id == %d)].author_display_name".formatted(deletedComment.getId())).value(contains(DELETED_USER)))
                .andExpect(jsonPath("$.items[?(@.id == %d)].parent_comment".formatted(reply.getId())).value(contains(visibleComment.getId())))
                .andExpect(jsonPath("$.items[?(@.id == %d)].content".formatted(reply.getId())).value(contains("reply comment")));

        mockMvc.perform(get("/posts/{postId}/comments", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, currentUser.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void createCommentCreatesNotificationAndRejectsInvalidRequests() throws Exception {
        User postAuthor = saveUser("comment-post-author", "Post Author", "comment-post-author@example.com");
        User commentAuthor = saveUser("comment-author", "Comment Author", "comment-author@example.com");
        Post post = savePost(postAuthor);

        mockMvc.perform(post("/posts/{postId}/comments", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "new comment",
                                  "is_anonymous": false
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, commentAuthor.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(commentAuthor.getId()))
                .andExpect(jsonPath("$.post_id").value(post.getId()))
                .andExpect(jsonPath("$.parent_comment").value(nullValue()))
                .andExpect(jsonPath("$.content").value("new comment"));

        assertThat(notificationRepository.findByCommentedUserIdOrderByCreatedAtDesc(postAuthor.getId()))
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.getCommentedPostId()).isEqualTo(post.getId());
                    assertThat(notification.getCommentedId()).isNull();
                    assertThat(notification.getCommentContent()).isEqualTo("new comment");
                });

        mockMvc.perform(post("/posts/{postId}/comments", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, commentAuthor.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/posts/{postId}/comments", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"missing post\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, commentAuthor.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void createCommentByPostAuthorDoesNotCreateNotification() throws Exception {
        User postAuthor = saveUser("self-comment-author", "Self Author", "self-comment-author@example.com");
        Post post = savePost(postAuthor);

        mockMvc.perform(post("/posts/{postId}/comments", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"self comment\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, postAuthor.getId())
                        .with(csrf()))
                .andExpect(status().isCreated());

        assertThat(notificationRepository.count()).isZero();
    }

    @Test
    void createReplyCreatesNotificationAndRejectsReplyToReplyOrMissingParent() throws Exception {
        User postAuthor = saveUser("reply-post-author", "Reply Post Author", "reply-post-author@example.com");
        User parentAuthor = saveUser("reply-parent-author", "Reply Parent Author", "reply-parent-author@example.com");
        User replyAuthor = saveUser("reply-author", "Reply Author", "reply-author@example.com");
        Post post = savePost(postAuthor);
        Comment parentComment = saveComment(parentAuthor, post, null, "parent comment", false);

        mockMvc.perform(post("/comments/{commentId}/replies", parentComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "new reply",
                                  "is_anonymous": true
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, replyAuthor.getId())
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(replyAuthor.getId()))
                .andExpect(jsonPath("$.post_id").value(post.getId()))
                .andExpect(jsonPath("$.parent_comment").value(parentComment.getId()))
                .andExpect(jsonPath("$.is_anonymous").value(true));

        Notification replyNotification = notificationRepository
                .findByCommentedUserIdOrderByCreatedAtDesc(parentAuthor.getId())
                .get(0);
        assertThat(replyNotification.getCommentedPostId()).isEqualTo(post.getId());
        assertThat(replyNotification.getCommentedId()).isEqualTo(parentComment.getId());

        Integer replyId = commentRepository.findByPostIdOrderByCreatedAtAscIdAsc(post.getId())
                .stream()
                .filter(Comment::isReply)
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(post("/comments/{commentId}/replies", replyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"nested reply\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, replyAuthor.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(post("/comments/{commentId}/replies", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"missing parent\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, replyAuthor.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void updateCommentRequiresAuthorAndUpdateField() throws Exception {
        User owner = saveUser("comment-update-owner", "Update Owner", "comment-update-owner@example.com");
        User other = saveUser("comment-update-other", "Update Other", "comment-update-other@example.com");
        Post post = savePost(owner);
        Comment comment = saveComment(owner, post, null, "before update", false);

        mockMvc.perform(patch("/comments/{commentId}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "after update",
                                  "is_anonymous": true
                                }
                                """)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("after update"))
                .andExpect(jsonPath("$.is_anonymous").value(true))
                .andExpect(jsonPath("$.updated_at").isNotEmpty());

        mockMvc.perform(patch("/comments/{commentId}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));

        mockMvc.perform(patch("/comments/{commentId}", comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"blocked\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, other.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(patch("/comments/{commentId}", 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"missing\"}")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()));
    }

    @Test
    void deleteCommentRequiresAuthorAndDeletesRepliesAndReplyNotifications() throws Exception {
        User owner = saveUser("comment-delete-owner", "Delete Owner", "comment-delete-owner@example.com");
        User other = saveUser("comment-delete-other", "Delete Other", "comment-delete-other@example.com");
        Post post = savePost(owner);
        Comment parentComment = saveComment(owner, post, null, "parent to delete", false);
        Comment reply = saveComment(other, post, parentComment.getId(), "reply to delete", false);
        Notification replyNotification = notificationRepository.saveAndFlush(new Notification(
                "reply notification",
                post.getId(),
                owner.getId(),
                parentComment.getId()
        ));

        mockMvc.perform(delete("/comments/{commentId}", parentComment.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, other.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCESS_DENIED.name()));

        mockMvc.perform(delete("/comments/{commentId}", parentComment.getId())
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        commentRepository.flush();
        assertThat(commentRepository.existsById(parentComment.getId())).isFalse();
        assertThat(commentRepository.existsById(reply.getId())).isFalse();
        assertThat(notificationRepository.existsById(replyNotification.getId())).isFalse();

        mockMvc.perform(delete("/comments/{commentId}", 999_999)
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, owner.getId())
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

    private Integer insertDeletedUser(String loginId, String emailAddress) {
        jdbcTemplate.update("""
                INSERT INTO users (login_id, password, name, email_address, deleted_at, status, role)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'DELETED', 'USER')
                """, loginId, "encoded-password", "Deleted User", emailAddress);
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Integer.class, loginId);
    }

    private Post savePost(User user) {
        return postRepository.saveAndFlush(new Post(
                user.getId(),
                "post title",
                "post content",
                "Major",
                "Subject",
                false
        ));
    }

    private Comment saveComment(User user, Post post, Integer parentComment, String content, boolean anonymous) {
        return commentRepository.saveAndFlush(new Comment(
                user.getId(),
                post.getId(),
                parentComment,
                content,
                anonymous
        ));
    }
}
