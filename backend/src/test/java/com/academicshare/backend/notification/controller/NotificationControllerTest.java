package com.academicshare.backend.notification.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationControllerTest {

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

    @Test
    void getNotificationsReturnsOnlyCurrentUserNotificationsAndMarksThemRead() throws Exception {
        User receiver = saveUser("noti-receiver", "noti-receiver@example.com");
        User otherReceiver = saveUser("noti-other", "noti-other@example.com");
        Post post = savePost(receiver);
        Comment parentComment = commentRepository.saveAndFlush(new Comment(
                otherReceiver.getId(),
                post.getId(),
                null,
                "부모 댓글",
                false
        ));

        Notification commentNotification = notificationRepository.saveAndFlush(new Notification(
                "댓글 알림",
                post.getId(),
                receiver.getId(),
                null
        ));
        Notification replyNotification = notificationRepository.saveAndFlush(new Notification(
                "대댓글 알림",
                post.getId(),
                receiver.getId(),
                parentComment.getId()
        ));
        Notification otherNotification = notificationRepository.saveAndFlush(new Notification(
                "다른 회원 알림",
                post.getId(),
                otherReceiver.getId(),
                null
        ));

        mockMvc.perform(get("/notifications")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, receiver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[?(@.id == %d)].is_read".formatted(commentNotification.getId())).value(contains(true)))
                .andExpect(jsonPath("$.items[?(@.id == %d)].comment_content".formatted(commentNotification.getId())).value(contains("댓글 알림")))
                .andExpect(jsonPath("$.items[?(@.id == %d)].commented_post_id".formatted(commentNotification.getId())).value(contains(post.getId())))
                .andExpect(jsonPath("$.items[?(@.id == %d)].commented_user_id".formatted(commentNotification.getId())).value(contains(receiver.getId())))
                .andExpect(jsonPath("$.items[?(@.id == %d)].commented_id".formatted(commentNotification.getId())).value(contains(nullValue())))
                .andExpect(jsonPath("$.items[?(@.id == %d)].is_read".formatted(replyNotification.getId())).value(contains(true)))
                .andExpect(jsonPath("$.items[?(@.id == %d)].commented_id".formatted(replyNotification.getId())).value(contains(parentComment.getId())))
                .andExpect(jsonPath("$.items[?(@.id == %d)]".formatted(otherNotification.getId())).isEmpty());

        assertThat(notificationRepository.findById(commentNotification.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(replyNotification.getId()).orElseThrow().getIsRead()).isTrue();
        assertThat(notificationRepository.findById(otherNotification.getId()).orElseThrow().getIsRead()).isFalse();
    }

    @Test
    void getNotificationsWithNoNotificationsReturnsEmptyItems() throws Exception {
        User receiver = saveUser("empty-noti-user", "empty-noti@example.com");

        mockMvc.perform(get("/notifications")
                        .sessionAttr(AuthSessionAttributes.CURRENT_USER_ID, receiver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void getNotificationsWithoutSessionReturns401() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_REQUIRED.name()))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private User saveUser(String loginId, String emailAddress) {
        return userRepository.saveAndFlush(new User(
                loginId,
                "encoded-password",
                "사용자",
                emailAddress
        ));
    }

    private Post savePost(User user) {
        return postRepository.saveAndFlush(new Post(
                user.getId(),
                "알림 테스트 게시글",
                "본문",
                "전공",
                "데이터베이스",
                false
        ));
    }
}
