# feature-통합 Review

판정: RESOLVED

재검증일: 2026-05-17

## 검증 범위

- 검증 대상 기능: 통합 테스트와 권한/상태 회귀 테스트
- 검증 대상 코드: `backend/` 전체 변경사항 및 관련 테스트 코드
- 기준 문서:
  - `docs/normalized/feature-list.md`
  - `docs/normalized/api-contract.md`
  - `docs/normalized/db-schema-contract.md`
  - `docs/normalized/auth-policy.md`
  - `docs/normalized/acceptance-criteria.md`
  - `docs/normalized/implementation-plan.md`
  - `docs/normalized/naming-convention.md`
  - `docs/normalized/product-spec.md`

## 검증 결과

- 테스트 실행: `backend`에서 `.\gradlew.bat test --rerun-tasks`
- 결과: PASS, 102 tests, 0 failures, 0 errors, 0 skipped
- 결론: 테스트 기대값과 보안 요구를 정규화 명세에 맞춰 갱신했으므로 승인 가능하다.

## Issues

### INT-REV-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document:
  - `docs/normalized/api-contract.md`
  - `docs/normalized/auth-policy.md`
  - `docs/normalized/acceptance-criteria.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/post/service/PostService.java:42`
  - `backend/src/test/java/com/academicshare/backend/post/controller/PostControllerTest.java:57`
- Original problem: 탈퇴 회원의 게시글 작성자 표시명이 명세의 `탈퇴한 유저`가 아니라 `탈퇴한 사용자`로 구현되어 있었다. `PostControllerTest`도 같은 잘못된 문자열을 기대하므로 회귀 테스트가 명세 위반을 통과시켰다.
- Resolution:
  - `PostService`의 탈퇴 작성자 표시 상수를 `탈퇴한 유저`로 수정했다.
  - `PostControllerTest`의 기대값도 `탈퇴한 유저`로 수정했다.
  - 기존 게시글 목록/상세 조회 회귀 테스트가 탈퇴 작성자 `author_display_name`을 검증한다.
- Required user decision: 없음.

### INT-REV-002

- Severity: MAJOR
- Current status: RESOLVED
- Related document:
  - `docs/normalized/api-contract.md`
  - `docs/normalized/auth-policy.md`
  - `docs/normalized/acceptance-criteria.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/auth/config/SecurityConfig.java:28`
  - `backend/src/main/java/com/academicshare/backend/auth/config/SecurityConfig.java:30`
  - `backend/src/test/java/com/academicshare/backend/auth/controller/AuthControllerTest.java:249`
- Original problem: 명세에는 서버 세션 기반 인증만 정의되어 있고 API request/status code에 CSRF 토큰 요구가 없었지만, 구현은 `POST /auth/signup`, `POST /auth/login` 외의 변경 요청에 CSRF를 요구했다. 테스트도 `logoutWithSessionWithoutCsrfReturns403`처럼 문서에 없는 403 동작을 고정했다.
- Decision: CSRF 토큰을 세션 기반 상태 변경 API의 공식 인증/권한 계약으로 채택한다.
- Resolution:
  - `api-contract.md`에 `AUTH-001`, `AUTH-002`를 제외한 `POST`, `PATCH`, `DELETE` API의 CSRF 토큰 요구와 누락/불일치 시 `403 ACCESS_DENIED`를 명시했다.
  - `auth-policy.md`에 동일한 CSRF 권한 실패 정책을 추가했다.
  - `acceptance-criteria.md`에 공통 CSRF 실패 기준과 `AUTH-003` 로그아웃 CSRF 실패 케이스를 추가했다.
- Required user decision: 해결됨. CSRF를 공식 계약으로 채택.

## Checklist Result

1. API contract와 endpoint/method/request/response/status code 일치: PASS
2. DB schema contract와 Entity/Repository 일치: PASS
3. validation rule 구현: PASS
4. 권한/인증 정책 일치: PASS
5. 정상 케이스 테스트: PASS
6. 실패 케이스 테스트: PASS
7. 에러 응답 형식 일관성: PASS
8. 보안상 위험한 코드: PASS - CSRF 요구가 문서화된 API 계약으로 정리됨
9. 문서에 없는 기능 임의 추가 여부: PASS

## Final Judgment

RESOLVED
