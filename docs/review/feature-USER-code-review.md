# feature-USER Code Review

판정: APPROVED

검증일: 2026-05-17

## 검증 범위

- 대상 기능: USER-001..USER-003
- 검증 대상: `backend/` 전체 변경사항 및 관련 테스트 코드
- 분리 묶음: `docs/review/change-bundles/user.patch`
- 기준 문서:
  - `docs/normalized/feature-list.md`
  - `docs/normalized/api-contract.md`
  - `docs/normalized/db-schema-contract.md`
  - `docs/normalized/auth-policy.md`
  - `docs/normalized/acceptance-criteria.md`
  - `docs/normalized/implementation-plan.md`
  - `docs/normalized/naming-convention.md`
  - `docs/normalized/product-spec.md`

## 테스트 결과

- `backend`: `.\gradlew.bat test --tests com.academicshare.backend.user.controller.UserControllerTest` PASS
  - 6 tests, 0 failures, 0 errors
- `backend`: `.\gradlew.bat test` PASS
  - 102 tests, 0 failures, 0 errors
- 최초 sandbox 실행은 Gradle distribution 다운로드 네트워크 제한으로 실패했고, 승인된 실행에서 검증을 완료했다.

## Issues

### USER-CR-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document:
  - `docs/normalized/feature-list.md`
  - `docs/normalized/implementation-plan.md`
  - `docs/normalized/acceptance-criteria.md`의 `AC-COMMON-005. 기능 변경 범위 분리`
- Related artifact:
  - `backend/src/main/java/com/academicshare/backend/comment/service/CommentService.java`
  - `backend/src/main/java/com/academicshare/backend/post/dto/PostResponse.java`
  - `backend/src/main/java/com/academicshare/backend/report/**`
  - `backend/src/main/java/com/academicshare/backend/schedule/controller/**`
  - `backend/src/main/java/com/academicshare/backend/schedule/dto/**`
  - `backend/src/main/java/com/academicshare/backend/schedule/service/**`
  - `backend/src/test/java/com/academicshare/backend/comment/controller/CommentControllerTest.java`
  - `backend/src/test/java/com/academicshare/backend/post/controller/PostControllerTest.java`
  - `backend/src/test/java/com/academicshare/backend/report/**`
  - `backend/src/test/java/com/academicshare/backend/schedule/**`
  - `backend/src/main/resources/db/migration/V1__create_base_schema.sql`
- Problem: 이번 검증 대상은 USER-001..USER-003이지만, 원래 `backend/` 전체 변경사항에는 COMMENT, POST, REPORT, CAL/GCAL, notification FK 정합성 변경 등 USER 범위를 벗어난 코드와 테스트가 함께 포함되어 있었다.
- Why it matters: 구현 계획은 하나의 구현/검증 단위가 지정된 Feature ID 범위의 코드와 테스트만 포함되어야 한다고 정한다. 범위 분리 없이 전체 변경사항을 하나의 USER 묶음으로 보면 USER 구현 자체의 적합성과 무관하게 승인할 수 없다.
- Resolution:
  - USER-001..USER-003 전용 변경 묶음을 `docs/review/change-bundles/user.patch`로 분리했다.
  - USER 묶음에는 `backend/src/main/java/com/academicshare/backend/user/**`, `backend/src/test/java/com/academicshare/backend/user/**`, 그리고 USER-003 생명주기에 필요한 공통 의존 변경을 포함했다.
  - USER-003 공통 의존으로 `User` 상태/개인정보 정리 메서드, `UserRepository` 만료 탈퇴 회원 조회/이메일 중복 제외 조회, `ScheduleRepository.deleteByUserIdAndGroupIdIsNull`, `Group.changeLeaderId`, `GroupMember.changeRole`, `GroupMemberRepository.findByUserIdOrderByJoinedAtAscGroupIdAsc`를 USER bundle 소유 범위로 기록했다.
  - REPORT, CAL/GCAL endpoint 구현, POST/COMMENT 표시명/테스트, notification FK 변경은 USER 묶음에서 제외했다.
  - `docs/review/06-feature-change-bundles.md`에 USER bundle과 `USER-CR-001` 해소 내역을 추가했다.
- Required user decision: 없음.

## USER 구현 검토 결과

USER-001..USER-003 구현 자체에서는 API contract, DB schema contract, validation, 인증/권한, 에러 응답 형식, 정상/실패 테스트 관점의 BLOCKER 또는 MAJOR 결함을 발견하지 못했다.

- API contract: PASS
  - `GET /api/users/me`, `PATCH /api/users/me`, `DELETE /api/users/me`는 `server.servlet.context-path=/api`와 `UserController`의 `/users/me` 매핑으로 제공된다.
  - `GET`은 현재 세션 사용자 `User`, `PATCH`는 수정된 `User`, `DELETE`는 `204 No Content`를 반환한다.
- DB schema contract: PASS
  - `User` 엔티티는 `users`의 nullable 개인정보 컬럼, `deleted_at`, `status`, `role`을 계약과 일치하게 매핑한다.
  - USER-003 생명주기에 필요한 일정/그룹 repository 보조 메서드는 기존 DB 계약을 바꾸지 않고 사용한다.
- Validation rule: PASS
  - 수정 필드 없음, 동일 이름/이메일/비밀번호, `current_password` 누락, `current_password` 불일치, 이메일 중복을 각각 `400`, `403`, `409`로 처리한다.
  - 이름 길이, 이메일 형식/길이, 비밀번호 blank/길이 제한을 검증한다.
- Auth/Authz: PASS
  - 세 API 모두 인증이 필요하며 인증 없이는 `401 AUTHENTICATION_REQUIRED`를 반환한다.
  - 현재 세션 사용자 id만 기준으로 조회/수정/탈퇴한다.
- USER-003 lifecycle: PASS
  - 탈퇴 시 `status = DELETED`, `deleted_at = now()`로 변경하고 세션을 무효화한다.
  - 탈퇴 시점의 `login_id`, `password`, `name`, `email_address`는 유지한다.
  - 개인 일정은 즉시 삭제한다.
  - 모든 그룹 멤버십에서 제거하고, 그룹장 탈퇴 시 가장 먼저 가입한 남은 멤버에게 `LEADER`와 `groups.leader_id`를 위임한다.
  - 유일 그룹원이 탈퇴하면 그룹을 삭제한다.
  - `deleted_at` 기준 6개월 경과 탈퇴 회원 개인정보 NULL 처리 서비스 메서드가 있다.
- Tests: PASS
  - 정상 케이스: 내 정보 조회, 이름/이메일 수정, 비밀번호 수정, 탈퇴 생명주기, 6개월 경과 개인정보 정리.
  - 실패 케이스: 인증 없음, 수정 필드 없음, 동일값, 비밀번호 현재값 누락/불일치, 이메일 중복.
- Error response format: PASS
  - 실패 응답은 공통 `code`, `message`, optional `details` 형식을 따른다.
- Security: PASS
  - 응답에 `password`가 포함되지 않는다.
  - 비밀번호 변경은 현재 비밀번호 검증 후 암호화 저장한다.
  - 세션 탈취 완화 범위에서 탈퇴 후 세션 무효화가 수행된다.
- Undocumented feature: PASS
  - USER 구현 자체에서 명세에 없는 별도 API나 권한 부여 기능 추가는 발견하지 못했다.

## Checklist Result

1. USER-001..USER-003 범위 준수: PASS - USER 전용 변경 묶음 `docs/review/change-bundles/user.patch`로 분리됨.
2. API contract 일치: PASS
3. DB schema contract 일치: PASS
4. validation rule 구현: PASS
5. 권한/인증 정책 일치: PASS
6. 정상 케이스 테스트: PASS
7. 실패 케이스 테스트: PASS
8. 에러 응답 형식 일관성: PASS
9. 보안상 위험 코드: PASS
10. 문서에 없는 기능 임의 추가: PASS

## Final Decision

APPROVED

USER-001..USER-003 코드 자체는 승인 가능한 수준이며, 기존 범위 혼입 이슈도 `user.patch` 분리로 해소되었다. USER-001..USER-003는 승인한다.
