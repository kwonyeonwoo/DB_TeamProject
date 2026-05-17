package com.academicshare.backend.user.repository;

import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByLoginId(String loginId);

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByEmailAddressAndIdNot(String emailAddress, Integer id);

    Optional<User> findByLoginId(String loginId);

    List<User> findByStatusAndDeletedAtLessThanEqual(UserStatus status, LocalDateTime deletedAt);
}
