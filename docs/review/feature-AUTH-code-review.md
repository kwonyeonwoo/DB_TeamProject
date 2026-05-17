# feature-AUTH-001 Code Review

판정: RESOLVED

재검증일: 2026-05-16

## 검증 범위

- `backend/src/main/java/com/academicshare/backend/auth`
- `backend/src/main/resources/application.yml`
- `backend/build.gradle`
- 관련 테스트: `backend/src/test/java/com/academicshare/backend/auth`

## 테스트 결과

- `backend`: `.\gradlew.bat test` PASS

## Summary

- 기존 리뷰의 `AUTH-001-CR-001`, `AUTH-001-CR-002`는 현재 코드 기준으로 해결된 상태입니다.
- Spring Security CSRF가 활성화되어 있고, 상태 변경 요청인 `POST /api/auth/logout`은 CSRF 토큰이 없으면 거부됩니다.
- User 응답 DTO는 nullable 필드인 `deleted_at`을 명시적으로 직렬화하도록 변경되어 있습니다.

## Issues

### AUTH-001-CR-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/auth-policy.md`, `docs/normalized/api-contract.md`
- Related code:
  - `backend/build.gradle:20`
  - `backend/src/main/java/com/academicshare/backend/auth/config/SecurityConfig.java:29`
  - `backend/src/test/java/com/academicshare/backend/auth/controller/AuthControllerTest.java:223`
  - `backend/src/test/java/com/academicshare/backend/auth/controller/AuthControllerTest.java:249`
- Original problem: 세션 기반 인증을 사용하지만 CSRF 방어가 없어 `POST /api/auth/logout` 같은 상태 변경 요청이 세션 쿠키만으로 처리될 수 있었습니다.
- Current finding: Spring Security Web이 추가되었고, `SecurityConfig`에서 CSRF 토큰 저장소가 설정되어 있습니다. `/auth/signup`, `/auth/login`만 CSRF 예외로 두고 있으며, `/auth/logout`은 CSRF 검증 대상입니다.
- Evidence:
  - `implementation 'org.springframework.boot:spring-boot-starter-security'`
  - `CookieCsrfTokenRepository.withHttpOnlyFalse()`
  - `logoutInvalidatesCurrentSession`은 유효한 CSRF 토큰이 있을 때 `204`를 검증합니다.
  - `logoutWithSessionWithoutCsrfReturns403`은 CSRF 토큰이 없을 때 `403 ACCESS_DENIED`를 검증합니다.
- Remaining risk: 세션 쿠키의 `Secure`, `SameSite` 값을 애플리케이션 설정에서 명시한 흔적은 확인되지 않았습니다. 다만 본 리뷰 이슈의 핵심인 CSRF 방어 부재는 해결된 것으로 판정합니다.
- Required user decision: 없음.

### AUTH-001-CR-002

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/api-contract.md`
- Related code:
  - `backend/src/main/resources/application.yml:10`
  - `backend/src/main/java/com/academicshare/backend/auth/dto/UserResponse.java:9`
  - `backend/src/test/java/com/academicshare/backend/auth/controller/AuthControllerTest.java:64`
  - `backend/src/test/java/com/academicshare/backend/auth/controller/AuthControllerTest.java:143`
- Original problem: 전역 Jackson 설정이 `non_null`이라 API contract의 User 응답 필드인 `deleted_at`이 ACTIVE 사용자 응답에서 누락될 수 있었습니다.
- Current finding: `UserResponse`에 `@JsonInclude(JsonInclude.Include.ALWAYS)`가 적용되어 nullable 필드도 응답에 포함됩니다.
- Evidence:
  - 회원가입 응답에서 `$.deleted_at == null` 검증
  - 로그인 응답에서 `$.user.deleted_at == null` 검증
- Required user decision: 없음.

## Checklist Result

1. CSRF 방어: PASS
2. `POST /api/auth/logout` CSRF 실패 케이스 테스트: PASS
3. User nullable 응답 필드 직렬화: PASS
4. User nullable 응답 필드 테스트: PASS
