package com.academicshare.backend.user.repository;

import com.academicshare.backend.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByLoginId(String loginId);

    boolean existsByEmailAddress(String emailAddress);

    Optional<User> findByLoginId(String loginId);
}
