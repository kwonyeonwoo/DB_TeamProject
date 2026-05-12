package com.academicshare.backend.auth.service;

import com.academicshare.backend.auth.dto.LoginRequest;
import com.academicshare.backend.auth.dto.SignupRequest;
import com.academicshare.backend.common.error.ErrorCode;
import com.academicshare.backend.common.exception.ApiException;
import com.academicshare.backend.user.domain.User;
import com.academicshare.backend.user.domain.UserStatus;
import com.academicshare.backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signup(SignupRequest request) {
        validateDuplicateSignupValues(request);

        User user = new User(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.emailAddress()
        );

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new ApiException(ErrorCode.AUTHENTICATION_REQUIRED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "탈퇴 계정은 로그인할 수 없습니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(ErrorCode.AUTHENTICATION_REQUIRED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return user;
    }

    private void validateDuplicateSignupValues(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmailAddress(request.emailAddress())) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
    }
}
