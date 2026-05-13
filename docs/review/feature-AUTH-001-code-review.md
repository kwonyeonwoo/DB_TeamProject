# feature-AUTH-001 Code Review

판정: NEEDS_FIX

검증 범위:
- `backend/` 공통 응답/예외, DB 연결/마이그레이션, Entity/Repository, 세션 인증 인프라, 인증 필터, 현재 사용자 helper
- 관련 테스트: `backend/src/test/java`

테스트 결과:
- `backend`: `.\gradlew.bat test --rerun-tasks` PASS

## Issues

### AUTH-001-CR-001

- Severity: MAJOR
- Related document: `docs/normalized/auth-policy.md`, `docs/normalized/api-contract.md`
- Related code:
  - `backend/build.gradle:19`
  - `backend/src/main/java/com/academicshare/backend/auth/config/AuthFilterConfig.java:30`
  - `backend/src/main/java/com/academicshare/backend/auth/controller/AuthController.java:47`
- Problem: 세션 기반 인증을 사용하지만 CSRF 방어가 구현되어 있지 않습니다. 현재는 `spring-security-crypto`만 사용하고, 커스텀 필터가 세션 존재 여부만 확인합니다. `POST /api/auth/logout` 같은 상태 변경 요청도 CSRF 토큰 검증 없이 세션 쿠키만으로 처리됩니다.
- Why it matters: 브라우저 기반 세션 인증은 쿠키가 자동 전송되므로, 별도 CSRF 방어가 없으면 사용자가 로그인된 상태에서 외부 사이트가 상태 변경 요청을 유도할 수 있습니다. 이번 기능은 세션 인증 인프라이므로 보안 기본값을 이 단계에서 고정해야 합니다.
- Suggested fix: Spring Security Web의 CSRF 기능을 도입하거나, 명시적인 CSRF 토큰 발급/검증 필터를 추가하세요. 최소한 상태 변경 메서드(`POST`, `PATCH`, `DELETE`)에 대해 토큰 검증을 적용하고, 세션 쿠키의 `HttpOnly`, `Secure`, `SameSite` 정책도 명시하세요.
- Required user decision: 프론트엔드가 사용할 CSRF 전달 방식 결정 필요. 예: 쿠키 기반 `XSRF-TOKEN` + `X-XSRF-TOKEN` 헤더, 또는 세션 생성 후 별도 API로 토큰 조회.
- Required tests:
  - CSRF 토큰 없는 인증된 `POST /api/auth/logout` 요청이 거부되는 실패 케이스
  - 유효한 CSRF 토큰을 포함한 상태 변경 요청이 통과하는 정상 케이스

### AUTH-001-CR-002

- Severity: MAJOR
- Related document: `docs/normalized/api-contract.md`
- Related code:
  - `backend/src/main/resources/application.yml:9`
  - `backend/src/main/resources/application.yml:10`
  - `backend/src/main/java/com/academicshare/backend/auth/dto/UserResponse.java:8`
  - `backend/src/main/java/com/academicshare/backend/auth/dto/UserResponse.java:14`
- Problem: API contract의 User 응답 필드에는 `deleted_at`이 포함되어 있지만, 전역 Jackson 설정이 `non_null`이라 ACTIVE 사용자 응답에서 `deleted_at`이 누락됩니다. `UserResponse`는 `deletedAt` 필드를 가지고 있어도 null이면 직렬화되지 않습니다.
- Why it matters: `POST /api/auth/signup`, `POST /api/auth/login`의 User 응답이 문서화된 User 리소스 필드 목록과 달라집니다. 클라이언트가 `deleted_at: null` 존재를 기준으로 계약을 구현하면 호환성 문제가 생깁니다.
- Suggested fix: User 응답 계약에서 nullable 필드도 항상 포함할지 결정하세요. 포함해야 한다면 `UserResponse`에 `@JsonInclude(JsonInclude.Include.ALWAYS)` 또는 필드 단위 include 설정을 적용해 `deleted_at: null`을 반환하게 하세요. 반대로 null 필드 생략이 의도라면 `api-contract.md`에 nullable 필드 생략 규칙을 명시한 뒤 테스트도 그 규칙에 맞추세요.
- Required user decision: nullable 응답 필드의 API 표현 방식 결정 필요. `null` 포함 vs 필드 생략.
- Required tests:
  - 회원가입 응답에 `deleted_at` 필드가 계약대로 포함 또는 생략되는지 검증
  - 로그인 응답의 `user.deleted_at`도 동일 규칙을 따르는지 검증

