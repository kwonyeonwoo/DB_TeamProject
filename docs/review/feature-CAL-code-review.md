# feature-CAL Code Review

판정: APPROVED

검증일: 2026-05-17

## 검증 범위

- 대상 기능: CAL-001..CAL-004
- 분리 묶음: `docs/review/change-bundles/cal.patch`
- CAL 묶음 포함 파일:
  - `backend/src/main/java/com/academicshare/backend/schedule/domain/Schedule.java`
  - `backend/src/main/java/com/academicshare/backend/schedule/repository/ScheduleRepository.java`
  - `backend/src/main/java/com/academicshare/backend/schedule/controller/**`
  - `backend/src/main/java/com/academicshare/backend/schedule/dto/**`
  - `backend/src/main/java/com/academicshare/backend/schedule/service/**`
  - `backend/src/test/java/com/academicshare/backend/schedule/**`
- CAL 묶음 제외 파일: REPORT, POST, COMMENT, Auth-test, notification FK 정책 변경
- 기준 문서:
  - `docs/normalized/feature-list.md`
  - `docs/normalized/api-contract.md`
  - `docs/normalized/db-schema-contract.md`
  - `docs/normalized/auth-policy.md`
  - `docs/normalized/acceptance-criteria.md`
  - `docs/normalized/implementation-plan.md`
  - `docs/normalized/naming-convention.md`
  - `docs/normalized/product-spec.md`
- 검증 대상: `backend/` 전체 변경사항 및 관련 테스트 코드

## 테스트 결과

- `backend`: `.\gradlew.bat test --rerun-tasks` PASS
- `backend`: `.\gradlew.bat test --tests "*ScheduleControllerTest" --rerun-tasks` PASS
- 실행 결과: 84 tests, 0 failures, 0 errors
- 비고: 최초 샌드박스 실행은 Gradle distribution 다운로드 네트워크 차단으로 실패했고, 승인된 실행으로 재시도하여 전체 테스트를 실제 재실행했다.

## Summary

- CAL 소유 구현인 `schedule` controller/service/dto/repository/domain 변경은 API 경로, 메서드, 주요 request/response, 상태 코드, DB 컬럼 매핑, 인증/소유자 제한, 주요 validation 규칙을 대체로 충족한다.
- `CAL-CR-001`은 해결되었다. CAL 소유 변경은 `docs/review/change-bundles/cal.patch`로 분리했고, REPORT/POST/COMMENT/Auth-test/notification 정책 변경은 CAL 묶음에서 제외했다.
- `CAL-CR-002`는 해결되었다. CAL-003 빈 PATCH body는 `400 수정 필드 없음`으로 명세화했고 테스트를 추가했다.
- `CAL-CR-003`은 해결되었다. 현재 사용자가 소유한 그룹 일정도 개인 일정 수정/삭제 endpoint에서는 `403`으로 거부되고 row가 유지되는지 테스트를 추가했다.

## Issues

### CAL-CR-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/feature-list.md`, `docs/normalized/implementation-plan.md`
- Related artifact:
  - `docs/review/change-bundles/cal.patch`
  - `docs/review/change-bundles/post.patch`
  - `docs/review/change-bundles/comment.patch`
  - `docs/review/change-bundles/report.patch`
  - `docs/review/change-bundles/auth-test.patch`
- Original problem: 이번 검증 대상은 CAL-001..CAL-004지만 raw `backend/` 변경사항에는 REPORT 구현/테스트, POST 응답/테스트, COMMENT 서비스/테스트, Auth-test 변경, notification FK 정책 변경이 함께 포함되어 있었다.
- Current finding: CAL 소유 변경은 `cal.patch`로 분리되었다. CAL 묶음에는 개인 일정 구현/테스트에 필요한 `schedule` 패키지 변경만 포함하고, REPORT/POST/COMMENT/Auth-test/notification 정책 변경은 별도 묶음 또는 별도 검증 범위로 제외한다.
- Evidence:
  - `cal.patch`는 `Schedule`, `ScheduleRepository`, `ScheduleController`, `ScheduleService`, schedule DTO, `ScheduleControllerTest` 변경만 포함한다.
  - `docs/review/06-feature-change-bundles.md`에 CAL bundle 소유권을 추가했다.
- Required user decision: 없음.

### CAL-CR-002

- Severity: QUESTION
- Current status: RESOLVED
- Related document: `docs/normalized/api-contract.md`, `docs/normalized/acceptance-criteria.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/schedule/service/ScheduleService.java:113`
  - `backend/src/main/java/com/academicshare/backend/schedule/service/ScheduleService.java:114`
  - `backend/src/test/java/com/academicshare/backend/schedule/controller/ScheduleControllerTest.java`
- Problem: `PATCH /api/me/schedules/{schedule_id}`에서 수정 필드가 하나도 없으면 `400 VALIDATION_ERROR`를 반환하도록 구현되어 있다. 그러나 CAL-003 API contract와 acceptance criteria에는 `400 수정 필드 없음` 규칙이 명시되어 있지 않다.
- Why it matters: POST/COMMENT 수정 API는 명세에 "수정 필드 없음" 오류가 명시되어 있지만 CAL-003에는 없다. 이 상태에서는 구현이 문서에 없는 validation rule을 추가한 것인지, 문서가 누락된 것인지 판단할 수 없다.
- Resolution: 현재 구현을 유지하고 빈 PATCH body를 `400 수정 필드 없음`으로 명세화했다. `docs/normalized/api-contract.md`의 CAL-003 Errors와 `docs/normalized/acceptance-criteria.md`의 CAL-003 오류 조건에 `400 수정 필드 없음`을 추가했고, `ScheduleControllerTest`에 `{}` 요청이 `400 VALIDATION_ERROR`를 반환하는 실패 케이스를 추가했다.
- Evidence:
  - CAL-003 API contract에 `수정 필드 중 하나 이상 필요`와 `400 수정 필드 없음`이 반영되었다.
  - CAL acceptance criteria의 오류 조건 표와 개인 일정 시나리오에 수정 필드 없음 `400`이 반영되었다.
  - `.\gradlew.bat test --tests "*ScheduleControllerTest" --rerun-tasks` PASS.
- Required user decision: 없음.

### CAL-CR-003

- Severity: MINOR
- Current status: RESOLVED
- Related document: `docs/normalized/api-contract.md`, `docs/normalized/auth-policy.md`, `docs/normalized/acceptance-criteria.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/schedule/service/ScheduleService.java:98`
  - `backend/src/test/java/com/academicshare/backend/schedule/controller/ScheduleControllerTest.java`
- Problem: 구현은 `group_id != null`인 그룹 일정을 개인 일정 수정/삭제 endpoint에서 `403`으로 막지만, 테스트는 다른 사용자의 개인 일정 접근 실패만 검증하고 현재 사용자가 소유한 그룹 일정을 개인 endpoint로 수정/삭제하는 실패 케이스를 검증하지 않는다.
- Why it matters: CAL-003/CAL-004는 "본인 개인 일정만" 수정/삭제해야 한다. 현재 코드가 맞더라도 이 분기 테스트가 없으면 추후 GROUP/GCAL 구현 중 개인 endpoint가 그룹 일정을 잘못 수정/삭제하는 회귀를 잡기 어렵다.
- Resolution: `ScheduleControllerTest`에 현재 사용자 `user_id`와 일치하지만 `group_id != null`인 일정에 대해 `PATCH /me/schedules/{scheduleId}`와 `DELETE /me/schedules/{scheduleId}`가 `403 ACCESS_DENIED`를 반환하고 row가 유지되는 테스트를 추가했다.
- Evidence:
  - 그룹 일정 수정 시도 후 `title`과 `updated_at`이 변경되지 않는지 검증한다.
  - 그룹 일정 삭제 시도 후 row가 유지되는지 검증한다.
  - `.\gradlew.bat test --tests "*ScheduleControllerTest" --rerun-tasks` PASS.
  - `.\gradlew.bat test --rerun-tasks` PASS, 84 tests, 0 failures, 0 errors.
- Required user decision: 없음.

## Checklist Result

1. CAL-001..CAL-004 범위 준수: PASS - CAL 소유 변경은 `cal.patch`로 분리
2. API contract 일치: PASS
3. DB schema contract 일치: PASS - `schedules` 컬럼, 제약, 인덱스와 Entity/Repository 매핑은 CAL 기준 일치
4. validation rule 구현: PASS
5. 권한/인증 정책: PASS - 세션 인증, 개인 일정 소유자 제한 구현
6. 정상 케이스 테스트: PASS
7. 실패 케이스 테스트: PASS
8. 에러 응답 형식: PASS - `code`, `message` 형식 유지
9. 보안상 위험 코드: PASS - CAL 구현에서 명백한 위험 코드 발견 없음
10. 문서에 없는 기능 임의 추가: PASS

## Verification Notes

- CAL endpoint 매핑: `ScheduleController`의 `GET/POST/PATCH/DELETE /me/schedules`.
- 목록 조회 조건: `ScheduleRepository.findPersonalSchedules`가 `user_id`, `group_id is null`, 열린 기간/겹침 기간 조건을 사용한다.
- 권한 제한: `ScheduleService.requirePersonalOwner`가 개인 일정 여부와 현재 사용자 소유 여부를 함께 검증한다.
- DB 계약: migration의 `schedules` 테이블은 `end_at >= start_at`, `type IN (1..5)`, user/group FK, 기간 인덱스를 포함한다.
