package com.academicshare.backend.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.academicshare.backend.post.domain.Post;
import com.academicshare.backend.post.repository.PostRepository;
import com.academicshare.backend.schedule.domain.Schedule;
import com.academicshare.backend.schedule.repository.ScheduleRepository;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Test
    void flywayCreatesContractTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT LOWER(table_name) FROM information_schema.tables WHERE table_schema = 'public'",
                String.class
        );

        assertThat(tables).contains(
                "users",
                "post",
                "likes",
                "comments",
                "report",
                "groups",
                "group_members",
                "schedules",
                "file",
                "notification"
        );
    }

    @Test
    void flywayCreatesFileMetadataColumns() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT LOWER(column_name) FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'file'",
                String.class
        );

        assertThat(columns).contains("file_name", "content_type");
    }

    @Test
    void repositoriesPersistUserAndPost() {
        User user = userRepository.saveAndFlush(new User(
                "user01",
                "encoded-password",
                "사용자",
                "user01@example.com"
        ));

        Post post = postRepository.saveAndFlush(new Post(
                user.getId(),
                "자료 공유",
                "본문",
                "전공",
                "데이터베이스",
                false
        ));

        assertThat(user.getId()).isNotNull();
        assertThat(post.getId()).isNotNull();
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(postRepository.count()).isEqualTo(1);
    }

    @Test
    void duplicateUserLoginIdFails() {
        userRepository.saveAndFlush(new User(
                "duplicate",
                "encoded-password",
                "사용자1",
                "duplicate1@example.com"
        ));

        User duplicate = new User(
                "duplicate",
                "encoded-password",
                "사용자2",
                "duplicate2@example.com"
        );

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void activeUserWithoutLoginIdFailsByDatabaseCheck() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO users (password, name, email_address, status, role)
                VALUES (?, ?, ?, ?, ?)
                """, "encoded-password", "사용자", "no-login@example.com", "ACTIVE", "USER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void scheduleEndBeforeStartFailsValidation() {
        User user = userRepository.saveAndFlush(new User(
                "schedule-user",
                "encoded-password",
                "일정 사용자",
                "schedule@example.com"
        ));

        Schedule schedule = new Schedule(
                user.getId(),
                null,
                "잘못된 일정",
                LocalDateTime.parse("2026-05-12T12:00:00"),
                LocalDateTime.parse("2026-05-12T11:00:00"),
                null,
                1
        );

        assertThatThrownBy(() -> scheduleRepository.saveAndFlush(schedule))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
