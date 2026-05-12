package com.academicshare.backend.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.academicshare.backend.comment.domain.Comment;
import com.academicshare.backend.comment.repository.CommentRepository;
import com.academicshare.backend.notification.domain.Notification;
import com.academicshare.backend.notification.repository.NotificationRepository;
import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void createCommentNotificationCreatesNotificationForDifferentPostAuthor() {
        User postAuthor = saveUser("comment-noti-post-author", "comment-noti-post-author@example.com");
        User commentAuthor = saveUser("comment-noti-comment-author", "comment-noti-comment-author@example.com");
        Post post = savePost(postAuthor);

        Optional<Notification> notification = notificationService.createCommentNotificationIfNeeded(
                postAuthor.getId(),
                commentAuthor.getId(),
                post.getId(),
                "댓글 내용"
        );

        assertThat(notification).isPresent();
        Notification savedNotification = notification.orElseThrow();
        assertThat(savedNotification.getCommentContent()).isEqualTo("댓글 내용");
        assertThat(savedNotification.getCommentedPostId()).isEqualTo(post.getId());
        assertThat(savedNotification.getCommentedUserId()).isEqualTo(postAuthor.getId());
        assertThat(savedNotification.getCommentedId()).isNull();
        assertThat(savedNotification.getIsRead()).isFalse();
        assertThat(notificationRepository.count()).isEqualTo(1);
    }

    @Test
    void createCommentNotificationSkipsSelfNotification() {
        User postAuthor = saveUser("self-comment-author", "self-comment-author@example.com");
        Post post = savePost(postAuthor);

        Optional<Notification> notification = notificationService.createCommentNotificationIfNeeded(
                postAuthor.getId(),
                postAuthor.getId(),
                post.getId(),
                "자기 댓글"
        );

        assertThat(notification).isEmpty();
        assertThat(notificationRepository.count()).isZero();
    }

    @Test
    void createReplyNotificationCreatesNotificationForDifferentParentCommentAuthor() {
        User postAuthor = saveUser("reply-post-author", "reply-post-author@example.com");
        User parentCommentAuthor = saveUser("reply-parent-author", "reply-parent-author@example.com");
        User replyAuthor = saveUser("reply-author", "reply-author@example.com");
        Post post = savePost(postAuthor);
        Comment parentComment = commentRepository.saveAndFlush(new Comment(
                parentCommentAuthor.getId(),
                post.getId(),
                null,
                "부모 댓글",
                false
        ));

        Optional<Notification> notification = notificationService.createReplyNotificationIfNeeded(
                parentCommentAuthor.getId(),
                replyAuthor.getId(),
                post.getId(),
                parentComment.getId(),
                "대댓글 내용"
        );

        assertThat(notification).isPresent();
        Notification savedNotification = notification.orElseThrow();
        assertThat(savedNotification.getCommentContent()).isEqualTo("대댓글 내용");
        assertThat(savedNotification.getCommentedPostId()).isEqualTo(post.getId());
        assertThat(savedNotification.getCommentedUserId()).isEqualTo(parentCommentAuthor.getId());
        assertThat(savedNotification.getCommentedId()).isEqualTo(parentComment.getId());
        assertThat(savedNotification.getIsRead()).isFalse();
        assertThat(notificationRepository.count()).isEqualTo(1);
    }

    @Test
    void createReplyNotificationSkipsSelfNotification() {
        User postAuthor = saveUser("self-reply-post-author", "self-reply-post-author@example.com");
        User parentCommentAuthor = saveUser("self-reply-parent-author", "self-reply-parent-author@example.com");
        Post post = savePost(postAuthor);
        Comment parentComment = commentRepository.saveAndFlush(new Comment(
                parentCommentAuthor.getId(),
                post.getId(),
                null,
                "부모 댓글",
                false
        ));

        Optional<Notification> notification = notificationService.createReplyNotificationIfNeeded(
                parentCommentAuthor.getId(),
                parentCommentAuthor.getId(),
                post.getId(),
                parentComment.getId(),
                "자기 대댓글"
        );

        assertThat(notification).isEmpty();
        assertThat(notificationRepository.count()).isZero();
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
                "알림 생성 테스트 게시글",
                "본문",
                "전공",
                "데이터베이스",
                false
        ));
    }
}
