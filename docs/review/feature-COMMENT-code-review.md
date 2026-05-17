# feature-COMMENT Code Review

판정: RESOLVED

재검증일: 2026-05-17

## 검증 범위

- 대상 기능: COMMENT-001..COMMENT-005
- 기준 문서: `docs/normalized/feature-list.md`, `docs/normalized/api-contract.md`, `docs/normalized/db-schema-contract.md`, `docs/normalized/auth-policy.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`, `docs/normalized/naming-convention.md`, `docs/normalized/product-spec.md`
- 분리 묶음: `docs/review/change-bundles/comment.patch`
- COMMENT 묶음 포함:
  - `CommentService` 익명 표시명 단건 응답 수정
  - `CommentControllerTest` COMMENT acceptance 테스트 보강
  - `notification.commented_id` FK cascade 제거 및 알림 snapshot 정책 문서
  - `docs/review/feature-COMMENT-code-review.md`
- COMMENT 묶음 제외 파일: POST 응답/test, REPORT 구현/test, Auth-test 변경

## 테스트 결과

- `backend`: `.\gradlew.bat test --tests "*CommentControllerTest" --tests "*NotificationServiceTest"` PASS
- `backend`: `.\gradlew.bat test` PASS

## Summary

- `COMMENT-CR-001`은 해결되었습니다. POST, REPORT, Auth-test 변경은 각각 별도 patch 묶음으로 분리되었습니다.
- `COMMENT-CR-002`는 해결되었습니다. `notification.commented_id` FK cascade가 제거되어 댓글/대댓글 삭제 후에도 기존 알림 스냅샷을 유지합니다.
- `COMMENT-CR-003`은 해결되었습니다. 댓글/대댓글 생성 및 수정 단건 응답도 목록 조회와 같은 게시글 단위 익명 번호 계산을 사용합니다.
- `COMMENT-CR-004`는 해결되었습니다. 알림 스냅샷 유지, 신고 이력 유지, validation 실패, 인증 실패 테스트가 보강되었습니다.

## Issues

### COMMENT-CR-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/feature-list.md`, `docs/normalized/implementation-plan.md`
- Related artifact:
  - `docs/review/change-bundles/comment.patch`
  - `docs/review/change-bundles/post.patch`
  - `docs/review/change-bundles/report.patch`
  - `docs/review/change-bundles/auth-test.patch`
- Original problem: 검증 대상은 COMMENT-001..COMMENT-005였지만 backend 변경사항에 POST 응답/테스트, REPORT 구현/테스트, 인증 인프라 테스트 변경이 함께 포함되어 있었습니다.
- Current finding: COMMENT 소유 변경은 `comment.patch`로 분리되었습니다. POST, REPORT, Auth-test 변경은 각각 별도 patch 묶음으로 분리되어 COMMENT 검증 범위에 포함하지 않습니다.
- Required user decision: 없음.

### COMMENT-CR-002

- Severity: BLOCKER
- Current status: RESOLVED
- Related document: `docs/source/requirements.md`, `docs/normalized/api-contract.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/db-schema-contract.md`, `docs/normalized/domain-model.md`
- Related code:
  - `backend/src/main/resources/db/migration/V1__create_base_schema.sql`
  - `backend/src/test/java/com/academicshare/backend/comment/controller/CommentControllerTest.java`
- Original problem: 원본 명세는 `notification.comment_content`를 알림 발생 당시의 스냅샷으로 정의하지만, migration의 `notification.commented_id -> comments.id ON DELETE CASCADE` FK와 기존 테스트 기대값은 부모 댓글 삭제 시 대댓글 알림을 삭제하는 방향이었습니다.
- Current finding: `notification.commented_id` FK cascade가 제거되었고, `idx_notification_comment_id` 인덱스만 유지됩니다. 테스트는 기존 알림 row와 `comment_content` 유지 여부를 검증합니다.
- Required user decision: 없음.

### COMMENT-CR-003

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/api-contract.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/comment/service/CommentService.java`
  - `backend/src/test/java/com/academicshare/backend/comment/controller/CommentControllerTest.java`
- Original problem: 생성/수정 응답의 `author_display_name`은 목록 조회 결과와 달라질 수 있었습니다.
- Current finding: 단건 응답 생성 시에도 해당 `post_id`의 댓글 목록과 게시글 작성자를 기준으로 익명 번호를 다시 계산합니다.
- Required user decision: 없음.

### COMMENT-CR-004

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`
- Related code:
  - `backend/src/test/java/com/academicshare/backend/comment/controller/CommentControllerTest.java`
- Original problem: COMMENT acceptance criteria의 일부 실패/보존 케이스 테스트가 부족했습니다.
- Current finding: 대댓글 내용 누락, 수정 content 공백, 알림 스냅샷 유지, 삭제된 댓글/대댓글 신고 이력 유지, 인증 없는 COMMENT API 요청 테스트가 추가되었습니다.
- Required user decision: 없음.

## Checklist Result

1. COMMENT-001..COMMENT-005 범위 분리: PASS - 타 기능 변경은 별도 patch 묶음으로 분리
2. API contract 일치: PASS
3. DB schema contract 일치: PASS
4. validation rule 구현: PASS
5. 권한/인증 정책: PASS
6. 정상 케이스 테스트: PASS
7. 실패 케이스 테스트: PASS
8. 에러 응답 형식: PASS
9. 보안상 위험 코드: PASS
10. 문서에 없는 기능 임의 추가: PASS - COMMENT 묶음에는 COMMENT 소유 변경만 포함
