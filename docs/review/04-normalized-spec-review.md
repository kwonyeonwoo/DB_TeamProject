# Normalized Spec Review

검토일: 2026-05-11

검토 범위:

- 원본 문서: `docs/source/user-flow.md`, `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`
- 정규화 명세: `docs/normalized/product-spec.md`, `docs/normalized/feature-list.md`, `docs/normalized/domain-model.md`, `docs/normalized/api-contract.md`, `docs/normalized/db-schema-contract.md`, `docs/normalized/auth-policy.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`, `docs/normalized/naming-convention.md`

최종 판정: **NEEDS_REVISION**

판정 이유: 요청된 정규화 수정 사항은 반영되었다. `REPORT-001`은 USER 전용 신고 생성으로 통일되어 있고, 관리자 신고 생성 관련 보류 ID는 정규화 문서에 남아 있지 않다. `acceptance-criteria.md`에는 기능별 API 오류 조건 추적 표가 추가되었고, 개인/그룹 일정 `type` 1..5, 일정 필수값, 그룹 코드 404, 신고 처리 `status` 검증을 포함한 주요 실패 케이스가 Given/When/Then 형태로 보강되었다. 다만 현재 source 문서는 회원 탈퇴 후 개인정보 처리와 self-notification 정책을 이미 확정하고 있는데, 일부 normalized 문서는 이를 아직 Open Question으로 유지한다. 이 정규화-원본 불일치가 해소되기 전까지 전체 명세는 APPROVED가 아니다.

## 1. 검토 기준별 결과

| 기준 | 결과 | 요약 |
|---|---|---|
| 1. 원본에 없는 기능 추가 여부 | PASS | 그룹 채팅, 초대 링크, 파일 메타데이터, ADMIN 권한 부여 API 등 제외 범위를 유지한다. 신고 생성도 USER 전용으로 원본 API와 맞다. |
| 2. 원본 요구사항 누락 여부 | NEEDS_REVISION | source 문서의 6개월 후 개인정보 삭제/비식별화 정책과 self-notification 미생성 정책이 normalized 일부 문서에 아직 확정 정책으로 반영되지 않았다. |
| 3. API와 DB 스키마 정합성 | PASS with revision | 신고, 일정, 그룹 코드 오류 조건은 AC까지 trace된다. 회원 탈퇴 개인정보 처리 시점은 DB/API source와 normalized 문서가 아직 맞지 않는다. |
| 4. 권한 정책 정합성 | PASS | 신고 생성은 USER 전용, 관리자 신고 목록/처리는 ADMIN 전용으로 정리되었다. |
| 5. Acceptance criteria 충분성 | PASS | API 오류 조건이 기능별 표와 세부 GWT 케이스로 보강되었다. 일정 `type` 1..5, 필수값, 그룹 코드 404, 신고 처리 `status` 검증이 명시되었다. |
| 6. 구현 순서 안전성 | NEEDS_REVISION | 구현 계획은 여전히 정책 미확정을 이유로 USER-003을 차단한다. 현재 source 정책 기준으로 normalized 계획을 갱신해야 한다. |

## 2. 남은 이슈

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| NSR-B01 | BLOCKER | `docs/source/requirements.md` 1-4, `docs/source/api-spec.md` A-06, `docs/source/user-flow.md` UF-06, `docs/source/screen-design.md` SC-11, `docs/normalized/product-spec.md`, `docs/normalized/api-contract.md`, `docs/normalized/db-schema-contract.md`, `docs/normalized/implementation-plan.md` | 현재 source 문서는 탈퇴 처리 시 `status = DELETED`, `deleted_at = now()`를 기록하고, 개인정보성 컬럼은 즉시 변경하지 않으며, `deleted_at`으로부터 6개월 후 NULL 또는 식별 불가 값으로 변경한다고 통일되어 있다. 그러나 normalized 일부 문서는 여전히 개인정보 처리 시점을 Open Question으로 둔다. | 탈퇴 API 처리, 배치 필요 여부, 중복 가입 판정, 테스트 기준과 구현 차단 여부가 달라진다. | normalized 문서의 탈퇴 관련 Open Question을 source의 6개월 후 처리 정책으로 대체한다. | source 문서가 현재 확정본이라면 추가 결정은 필요 없다. |
| NSR-M02 | MAJOR | `docs/source/requirements.md` 2-1, `docs/source/user-flow.md` UF-12/UF-13, `docs/source/screen-design.md` SC-09, `docs/source/api-spec.md` C-02/C-03, `docs/normalized/api-contract.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md` | 현재 source 문서는 자기 게시글/댓글에 댓글 또는 대댓글을 작성한 경우 알림을 생성하지 않는다고 확정한다. 그러나 normalized 일부 문서는 self-notification을 Open Question으로 둔다. | 댓글/대댓글 작성 부작용과 알림 테스트 기대값이 달라진다. | normalized 문서에서 self-notification 보류 문구를 제거하고, 작성자와 알림 수신자가 같으면 알림을 생성하지 않는 정책으로 대체한다. | source 문서가 현재 확정본이라면 추가 결정은 필요 없다. |

## 3. 해결 확인 항목

| 이전 이슈 | 현재 상태 | 확인 결과 |
|---|---|---|
| 관리자 신고 생성 권한 미확정 | 해결 | `api-contract.md`, `auth-policy.md`, `feature-list.md`, `acceptance-criteria.md`, `implementation-plan.md`에서 신고 생성은 USER 전용으로 통일되었다. |
| 관리자 신고 생성 보류 ID 잔존 | 해결 | 정규화 문서와 현재 리뷰 문서에 해당 보류 ID를 남기지 않았다. |
| Acceptance criteria 누락 실패 케이스 | 해결 | 기능별 API 오류 조건 추적 표가 추가되었고, 누락됐던 게시글/댓글 404, 로그인 필수값 400, 일정 404, 그룹 일정 404 등이 보강되었다. |
| 일정 API 검증 조건 | 해결 | 개인/그룹 일정 등록·수정에서 필수값, `type` 1..5, `end_at >= start_at` 실패 조건이 명시되었다. |
| 그룹 코드 오류 조건 | 해결 | 그룹 가입 코드가 존재하지 않거나 유효하지 않으면 `404`를 반환하는 기준이 명시되었다. |
| 신고 처리 상태 검증 | 해결 | 관리자 신고 처리에서 `status = PROCESSED`가 아닌 값 또는 누락된 `status`는 `400`으로 검증한다. |

## 4. 일치 확인 항목

- 공통 에러 응답은 `code`, `message` 필수와 선택 `details` 정책으로 원본과 정규화 명세가 일치한다.
- 신고 생성은 USER 전용이고, 관리자 신고 목록 조회와 신고 처리는 ADMIN 전용이다.
- 신고 DB 계약은 `target_type`, `target_id`, `reason_type`, `status`, `processed_by`, `processed_at`을 원본과 맞게 포함한다.
- 신고 처리 시 게시글/댓글 자동 삭제를 수행하지 않는 정책은 원본과 정규화 명세가 일치한다.
- 개인 일정은 `schedules.group_id = NULL`, 그룹 일정은 `schedules.group_id != NULL` 구조로 API와 DB가 맞다.
- 그룹 가입은 별도 초대 URL이 아니라 `group_code` 입력 방식이며, 유효하지 않은 코드는 `404`로 처리한다.
- 그룹 채팅, 아이디 찾기, 비밀번호 찾기/재설정, ADMIN 권한 부여/회수 API, 파일 메타데이터 저장은 제외 범위로 유지된다.

## 5. API-DB 정합성 점검

| 영역 | 결과 | 근거 |
|---|---|---|
| User/Auth | NEEDS_REVISION | source는 6개월 후 개인정보성 컬럼 처리로 통일되어 있으나 normalized 일부 문서는 아직 Open Question으로 둔다. |
| Post/File | PASS | `post`, `file(id, file_url)` 구조가 게시글 작성/수정 API의 직접 업로드 및 `file_url` 저장 정책과 맞다. |
| Comment/Notification | NEEDS_REVISION | DB 구조는 맞지만 source의 self-notification 미생성 정책이 normalized 일부 문서에 확정 정책으로 반영되지 않았다. |
| Like | PASS | `likes(user_id, post_id)` unique가 회원당 게시글 1회 추천 정책을 지원한다. |
| Report | PASS | `report` 컬럼과 unique(`reporter_id`, `target_type`, `target_id`)가 신고 생성/처리 API와 맞다. 신고 생성 권한도 USER 전용이다. |
| Schedule | PASS | `schedules.group_id = NULL` 개인 일정, non-NULL 그룹 일정 구조가 API와 맞다. |
| Group | PASS | `groups.group_code`, `group_members(role, joined_at)`가 그룹 생성/가입 및 그룹장 위임 규칙을 지원한다. |

## 6. 결론

요청된 REPORT 권한 정리와 acceptance criteria 오류 조건 보강은 완료되었다. 다음 갱신 대상은 source에서 이미 확정된 회원 탈퇴 6개월 후 개인정보 처리 정책과 self-notification 미생성 정책을 normalized 문서 전반에 반영하는 것이다. 그 작업이 끝나면 현재 리뷰는 APPROVED로 전환할 수 있다.
