# feature-POST Code Review

판정: RESOLVED

재검증일: 2026-05-17

## 검증 범위

- 대상 기능: POST-001..POST-007
- 기준 문서: `docs/normalized/feature-list.md`, `docs/normalized/api-contract.md`, `docs/normalized/db-schema-contract.md`, `docs/normalized/auth-policy.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`
- 분리 묶음: `docs/review/change-bundles/post.patch`
- POST 묶음 포함 파일:
  - `backend/src/main/java/com/academicshare/backend/post/dto/PostResponse.java`
  - `backend/src/test/java/com/academicshare/backend/post/controller/PostControllerTest.java`
  - `docs/review/feature-POST-code-review.md`
- POST 묶음 제외 파일: REPORT 구현/테스트, COMMENT 알림 snapshot/익명 표시명 변경, Auth-test 변경

## 테스트 결과

- `backend`: `.\gradlew.bat test --tests "*PostControllerTest"` PASS
- `backend`: `.\gradlew.bat test` PASS

## Summary

- `POST-CR-001`은 해결되었습니다. REPORT 변경은 `report.patch`로 분리되고 POST 묶음에는 POST DTO/test/review 문서만 남도록 분리 산출물을 만들었습니다.
- `POST-CR-002`는 해결되었습니다. `PostResponse`에 `@JsonInclude(JsonInclude.Include.ALWAYS)`가 적용되어 수정되지 않은 게시글도 `updated_at: null`을 응답에 포함합니다.
- `POST-CR-003`은 해결되었습니다. POST acceptance criteria 기준의 주요 정상/실패 케이스와 DB 상태 검증 테스트가 보강되었습니다.

## Issues

### POST-CR-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/feature-list.md`, `docs/normalized/implementation-plan.md`
- Related artifact:
  - `docs/review/change-bundles/post.patch`
  - `docs/review/change-bundles/report.patch`
- Original problem: 리뷰 대상은 POST-001..POST-007이지만 backend 변경사항에 REPORT-001..REPORT-003 구현과 테스트가 포함되어 있었습니다.
- Current finding: POST 변경 묶음은 `post.patch`로 분리되었습니다. REPORT 관련 controller/service/dto/domain/repository/test는 `report.patch`에 별도 귀속됩니다.
- Evidence:
  - POST patch는 `PostResponse`와 `PostControllerTest`만 포함한다.
  - REPORT patch는 REPORT 구현과 `ReportControllerTest`를 별도 포함한다.
- Required user decision: 없음.

### POST-CR-002

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/api-contract.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/post/dto/PostResponse.java`
  - `backend/src/test/java/com/academicshare/backend/post/controller/PostControllerTest.java`
- Original problem: API contract의 Post 응답 필드에는 `updated_at`이 포함되어 있지만, 전역 Jackson 설정이 `non_null`이라 아직 수정되지 않은 게시글의 `updatedAt = null`은 응답에서 필드 자체가 누락될 수 있었습니다.
- Current finding: `PostResponse`에 `@JsonInclude(JsonInclude.Include.ALWAYS)`가 적용되었습니다. 생성/목록/상세 응답에서 수정되지 않은 게시글의 `updated_at: null`을 테스트로 고정했습니다.
- Required user decision: 없음.

### POST-CR-003

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/api-contract.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`
- Related code:
  - `backend/src/test/java/com/academicshare/backend/post/controller/PostControllerTest.java`
- Original problem: POST acceptance criteria의 일부 정상/실패 케이스 테스트가 부족했습니다.
- Current finding: 누락됐던 주요 테스트가 추가되어 POST-001..POST-007 범위의 acceptance criteria를 고정합니다.
- Required user decision: 없음.

## Checklist Result

1. POST-001..POST-007 범위 분리: PASS - REPORT 변경은 `report.patch`로 분리
2. API contract 일치: PASS
3. DB schema contract 일치: PASS
4. validation rule 구현: PASS
5. 권한/인증 정책: PASS
6. 정상 케이스 테스트: PASS
7. 실패 케이스 테스트: PASS
8. 에러 응답 형식: PASS
9. 문서에 없는 기능 추가: PASS - POST 묶음에는 POST 소유 변경만 포함
