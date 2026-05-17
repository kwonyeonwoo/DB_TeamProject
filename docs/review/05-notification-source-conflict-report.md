# Notification Source Conflict Report

판정: RESOLVED_BY_DOC_UPDATE

검증일: 2026-05-16

## 검증 범위

- 원본 문서:
  - `docs/source/requirements.md`
  - `docs/source/api-spec.md`
  - `docs/source/physical-schema.md`
  - `docs/source/logical-schema.md`
  - `docs/source/erd.md`
  - `docs/source/dbml.md`
- normalized / review 문서:
  - `docs/normalized/api-contract.md`
  - `docs/normalized/acceptance-criteria.md`
  - `docs/normalized/db-schema-contract.md`
  - `docs/normalized/domain-model.md`
  - `docs/normalized/implementation-plan.md`
  - `docs/review/feature-COMMENT-code-review.md`

## 확정 정책

- `notification.comment_content`는 알림 생성 시점의 댓글/대댓글 내용 스냅샷이다.
- 원본 댓글 또는 대댓글이 수정되거나 삭제되어도 기존 알림의 `comment_content`는 변경하지 않는다.
- 댓글 또는 대댓글 삭제만으로 기존 알림 row를 삭제하지 않는다.
- 일반 댓글 삭제 시 해당 댓글의 대댓글은 FK cascade로 삭제될 수 있지만, 이미 생성된 알림은 유지한다.
- `notification.commented_id`는 댓글 알림이면 NULL, 대댓글 알림이면 부모 댓글 id를 저장하는 nullable navigation hint다.
- `notification.commented_id`는 댓글 삭제 생명주기를 제어하는 FK cascade 대상으로 사용하지 않는다.
- 게시글 삭제 시 `notification.commented_post_id -> post.id` FK cascade로 해당 게시글 알림을 삭제하는 정책은 유지한다.

## Resolved Conflicts

### NOTI-DOC-CONFLICT-001

- Previous severity: BLOCKER
- Status: RESOLVED
- Related document:
  - `docs/source/requirements.md`
  - `docs/source/api-spec.md`
  - `docs/normalized/api-contract.md`
  - `docs/normalized/acceptance-criteria.md`
  - `docs/normalized/domain-model.md`
  - `docs/normalized/implementation-plan.md`
- Previous problem: 요구사항은 알림 `comment_content`를 스냅샷으로 정의했지만, API/normalized 문서는 댓글 삭제 시 알림도 FK cascade로 삭제한다고 서술했다.
- Resolution: `COMMENT-005` 문구를 "일반 댓글 삭제 시 대댓글은 cascade, 기존 알림은 유지, `comment_content`는 불변"으로 통일했다.

### NOTI-DOC-CONFLICT-002

- Previous severity: BLOCKER
- Status: RESOLVED
- Related document:
  - `docs/source/physical-schema.md`
  - `docs/source/logical-schema.md`
  - `docs/source/erd.md`
  - `docs/source/dbml.md`
  - `docs/normalized/db-schema-contract.md`
- Previous problem: `notification.commented_id`가 이동용 부모 댓글 id이면서 동시에 `comments.id ON DELETE CASCADE` FK로 정의되어 스냅샷 유지 정책과 충돌했다.
- Resolution: `commented_id`를 FK cascade 대상에서 제외하고, nullable navigation hint 및 index-only 컬럼으로 정리했다.

### NOTI-DOC-CONFLICT-003

- Previous severity: MAJOR
- Status: RESOLVED
- Related document:
  - `docs/normalized/api-contract.md`
  - `docs/normalized/acceptance-criteria.md`
  - `docs/normalized/domain-model.md`
  - `docs/normalized/implementation-plan.md`
- Previous problem: 원본 요구사항의 `comment_content` 스냅샷/불변 규칙이 normalized 문서에 명시적으로 반영되어 있지 않았다.
- Resolution: normalized API 계약, acceptance criteria, domain model, implementation plan에 스냅샷/불변 규칙과 삭제 후 알림 유지 기준을 추가했다.

### NOTI-DOC-CONFLICT-004

- Previous severity: MAJOR
- Status: RESOLVED
- Related document:
  - `docs/review/feature-COMMENT-code-review.md`
- Previous problem: COMMENT 리뷰 문서가 댓글/대댓글 삭제 시 알림 삭제를 요구해 원본 요구사항과 반대 방향의 수정안을 제시했다.
- Resolution: COMMENT 리뷰 기준을 "알림 삭제 필요"가 아니라 "알림 유지 및 `comment_content` 불변 검증 필요"로 재정의했다.

## Remaining Implementation Review Risk

문서 충돌은 해소되었지만, 구현 검증에서는 별도 확인이 필요하다.

- backend migration에 `notification.commented_id -> comments.id ON DELETE CASCADE`가 남아 있다면 원본 명세 기준 DB 계약과 충돌한다.
- COMMENT 테스트는 댓글/대댓글 삭제 후 기존 알림 row와 `comment_content`가 유지되는지 검증해야 한다.
- 알림 클릭 시 원본 댓글 또는 부모 댓글이 삭제된 경우의 화면 fallback은 프론트/사용자 흐름에서 별도 UX 결정이 필요할 수 있다.
