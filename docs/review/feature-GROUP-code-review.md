# feature-GROUP Code Review

판정: APPROVED

검증일: 2026-05-17

## 검증 범위

- 대상 기능: GROUP-001..GROUP-004
- 참고 명세:
  - `docs/normalized/feature-list.md`
  - `docs/normalized/api-contract.md`
  - `docs/normalized/db-schema-contract.md`
  - `docs/normalized/auth-policy.md`
  - `docs/normalized/acceptance-criteria.md`
  - `docs/normalized/implementation-plan.md`
  - `docs/normalized/naming-convention.md`
  - `docs/normalized/product-spec.md`
- 검증 대상: `backend/` 전체 변경사항 및 관련 테스트 코드
- 분리 묶음: `docs/review/change-bundles/group.patch`

## 테스트 결과

- `backend`: `.\gradlew.bat test --tests com.academicshare.backend.group.controller.GroupControllerTest` PASS
  - 6 tests, 0 failures, 0 errors
- `backend`: `.\gradlew.bat test` PASS
  - 90 tests, 0 failures, 0 errors
- 최초 sandbox 실행은 Gradle distribution 다운로드 네트워크 제한으로 실패했고, 승인된 실행에서 검증을 완료했다.

## Issues

### GROUP-CR-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document:
  - `docs/normalized/implementation-plan.md`
  - `docs/normalized/acceptance-criteria.md`의 `AC-COMMON-005. 기능 변경 범위 분리`
- Related artifact:
  - `backend/src/main/java/com/academicshare/backend/comment/service/CommentService.java`
  - `backend/src/main/java/com/academicshare/backend/post/dto/PostResponse.java`
  - `backend/src/main/java/com/academicshare/backend/report/**`
  - `backend/src/main/java/com/academicshare/backend/schedule/**`
  - `backend/src/test/java/com/academicshare/backend/auth/session/**`
  - `backend/src/test/java/com/academicshare/backend/comment/controller/CommentControllerTest.java`
  - `backend/src/test/java/com/academicshare/backend/post/controller/PostControllerTest.java`
  - `backend/src/test/java/com/academicshare/backend/report/**`
  - `backend/src/test/java/com/academicshare/backend/schedule/**`
  - `backend/src/main/resources/db/migration/V1__create_base_schema.sql`
- Problem: 이번 검증 대상은 GROUP-001..GROUP-004이지만, 원래 `backend/` 변경사항에는 COMMENT, POST, REPORT, CAL/GCAL 후보, Auth-test, notification FK 정합성 변경이 함께 포함되어 있었다. 특히 `V1__create_base_schema.sql`의 현재 diff는 `notification` FK 제거로 GROUP-001..GROUP-004와 직접 관련이 없다.
- Why it matters: 구현 계획은 하나의 구현/검증 단위가 지정된 Feature ID 범위의 코드와 테스트만 포함되어야 한다고 정한다. 범위 분리 없이 전체 변경사항을 하나의 GROUP 묶음으로 보면 GROUP 기능 자체의 적합성과 무관하게 승인할 수 없다.
- Resolution:
  - GROUP-001..GROUP-004 전용 변경 묶음을 `docs/review/change-bundles/group.patch`로 분리했다.
  - GROUP 묶음에는 `backend/src/main/java/com/academicshare/backend/group/**`, GROUP에 필요한 기존 `Group`/`GroupMember` getter 및 repository 메서드, `backend/src/test/java/com/academicshare/backend/group/**`만 포함했다.
  - REPORT, CAL/GCAL, POST, COMMENT, Auth-test, notification FK 변경은 GROUP 묶음에서 제외했다.
  - `docs/review/06-feature-change-bundles.md`에 GROUP bundle과 `GROUP-CR-001` 해소 내역을 추가했다.
- Required user decision: 없음.

## GROUP 구현 검토 결과

GROUP-001..GROUP-004 구현 자체에서는 API contract, DB schema contract, validation, 인증/권한, 에러 응답 형식, 정상/실패 테스트 관점의 BLOCKER 또는 MAJOR 결함을 발견하지 못했다.

- API contract: PASS
  - `GET /api/groups`는 context-path `/api`와 컨트롤러 `/groups` 매핑으로 제공된다.
  - `POST /api/groups`, `POST /api/groups/join`, `GET /api/groups/{group_id}`의 method, request, response, status code가 명세와 일치한다.
- DB schema contract: PASS
  - `groups`, `group_members` 컬럼, PK/FK, unique/check/index 계약과 Entity/Repository 매핑이 일치한다.
- Validation rule: PASS
  - 그룹명 누락/blank/길이 초과는 `400 VALIDATION_ERROR`로 처리된다.
  - 그룹 코드 누락/blank/길이 초과/미존재는 `404 RESOURCE_NOT_FOUND`로 처리된다.
  - 중복 가입은 `409 CONFLICT`로 처리된다.
- Auth/Authz: PASS
  - 네 API 모두 인증 필요 조건을 만족한다.
  - 그룹 상세 조회는 그룹원만 허용하며 비그룹원은 `403 ACCESS_DENIED`를 반환한다.
- Tests: PASS
  - 정상 케이스: 내 그룹 목록, 그룹 생성, 코드 가입, 그룹 상세 조회.
  - 실패 케이스: 인증 없음, 그룹명 오류, 잘못된 코드, 중복 가입, 비그룹원 상세 조회, 그룹 없음.
- Error response format: PASS
  - 실패 응답은 공통 `code`, `message`, optional `details` 형식을 따른다.
- Security: PASS
  - GROUP 구현 범위에서 명백한 권한 우회, 임의 사용자 지정, 민감 정보 노출, SQL injection 위험은 발견하지 못했다.
- Undocumented feature: PASS
  - GROUP 구현 자체에서 명세에 없는 별도 API나 비즈니스 기능 추가는 발견하지 못했다.

## Checklist Result

1. GROUP-001..GROUP-004 범위 준수: PASS - GROUP 전용 변경 묶음 `docs/review/change-bundles/group.patch`로 분리됨.
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

GROUP 코드 자체는 승인 가능한 수준이며, 기존 범위 혼입 이슈도 `group.patch` 분리로 해소되었다. GROUP-001..GROUP-004는 승인한다.
