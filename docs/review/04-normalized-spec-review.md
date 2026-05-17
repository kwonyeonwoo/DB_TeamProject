# 04. Normalized Spec Review

검토일: 2026-05-12

최종 판정: **APPROVED: 구현 가능**

## 1. 검토 범위

원본 문서:

- `docs/source/user-flow.md`
- `docs/source/requirements.md`
- `docs/source/screen-design.md`
- `docs/source/api-spec.md`
- `docs/source/erd.md`
- `docs/source/logical-schema.md`
- `docs/source/physical-schema.md`

정규화 명세:

- `docs/normalized/product-spec.md`
- `docs/normalized/feature-list.md`
- `docs/normalized/domain-model.md`
- `docs/normalized/api-contract.md`
- `docs/normalized/db-schema-contract.md`
- `docs/normalized/auth-policy.md`
- `docs/normalized/acceptance-criteria.md`
- `docs/normalized/implementation-plan.md`

## 2. 검토 요약

| 검토 기준 | 판정 | 근거 |
|---|---|---|
| 1. 원본에 없는 기능 추가 여부 | PASS | 정규화 명세의 기능은 원본의 회원, 알림, 게시글, 댓글/대댓글, 추천, 신고, 개인 일정, 그룹, 그룹 일정 범위 안에 있다. 아이디 찾기, 비밀번호 재설정, ADMIN 권한 부여 API, 그룹 채팅, 복잡한 초대 링크, 파일 메타데이터 저장, 신고 처리 자동 삭제는 제외 범위로 유지된다. |
| 2. 원본 요구사항 누락 여부 | PASS | 회원 탈퇴 6개월 후 개인정보 처리, 자기 게시글/댓글 알림 미생성, 작성자/그룹원/ADMIN 권한, 파일 `/uploads/posts/{post_id}/{UUID}` 저장, 일정 기간 겹침 조회, 신고 이력 유지가 반영되어 있다. |
| 3. API와 DB 스키마 정합성 | PASS | API 리소스 필드는 `users`, `post`, `file`, `comments`, `likes`, `report`, `notification`, `groups`, `group_members`, `schedules` 계약과 맞는다. `report.target_id`는 원본처럼 다형 참조로 두고, 대상 존재 검증은 서비스 로직으로 처리한다. |
| 4. 권한 정책 정합성 | PASS | 서버 세션 인증, 작성자만 수정/삭제, 본인 알림/개인 일정 접근, 그룹원 그룹 일정 접근, USER 신고 생성, ADMIN 신고 조회/처리 정책이 원본 API와 요구사항에 맞는다. |
| 5. Acceptance criteria 충분성 | PASS | 정상 흐름뿐 아니라 공통 오류, 인증/권한 실패, 중복/존재하지 않는 대상, 일정 시간 검증, 파일 업로드 실패, 신고 대상 삭제 후 표시, 자기 대상 알림 미생성까지 검증한다. |
| 6. 구현 순서 안전성 | PASS | DB/공통 기반 후 인증, 게시글, 댓글/알림, 추천/신고, 일정, 그룹 순으로 진행하고, 의존성이 큰 회원 탈퇴 생명주기를 일정/그룹 구현 뒤에 배치해 안전하다. |

## 3. 세부 확인 결과

### API 계약

- 원본 API의 주요 엔드포인트가 정규화 API 계약에 대응된다: `/api/auth/*`, `/api/users/me`, `/api/posts`, `/api/posts/{post_id}`, `/api/posts/{post_id}/likes`, `/api/reports`, `/api/admin/reports`, 댓글/대댓글 API, `/api/notifications`, 개인/그룹 일정 API, 그룹 API.
- `POST /api/reports`는 원본 API의 `role = USER` 신고 생성 규칙을 따른다.
- 관리자 신고 조회/처리는 `GET /api/admin/reports`, `PATCH /api/admin/reports/{report_id}`로 분리되어 있다.

### DB 계약

- 논리/물리 스키마의 테이블, 주요 컬럼, PK/FK/Unique/Check 제약이 정규화 DB 계약에 반영되어 있다.
- `users` 개인정보성 컬럼 nullable 정책과 `status = ACTIVE/DELETED`, `role = USER/ADMIN` 제약이 반영되어 있다.
- `comments.parent_comment`, `schedules.group_id`, `report.target_type/target_id`의 nullable/다형 참조 정책과 `notification.commented_id`의 nullable navigation hint 정책이 원본과 일치한다.
- `post`, `comments`, `groups`, `schedules`에 원본에 없는 삭제 상태 컬럼을 추가하지 않았다.

### Acceptance Criteria

- 기능별 API 오류 조건 표가 정규화 API 계약의 오류 상태를 포괄한다.
- 회원 탈퇴 후 작성물 유지/표시, 개인 일정 삭제, 그룹 탈퇴, 그룹장 위임, 유일 그룹 삭제를 검증한다.
- 일정 조회는 `start_at`, `end_at` 없음/한쪽만 있음/둘 다 있음과 ISO-8601 형식 오류, `end_at < start_at` 오류를 검증한다.

### 구현 계획

- 모든 기능 그룹에 테스트 계획이 포함되어 있다.
- 회원 탈퇴는 개인 일정과 그룹 기능 구현 이후 통합 단계에서 처리하도록 되어 있어 교차 도메인 의존성이 안전하게 관리된다.
- 신고 다형 참조, 파일 저장/교체, 익명 표시명, 알림 이동 데이터 등 위험 요소가 별도 리스크로 기록되어 있다.

## 4. 이슈 목록

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 현재 검토 범위에서 구현 전 수정이 필요한 추가/누락/충돌 없음 | - | - | 없음 |

## 5. 결론

정규화 명세는 원본 문서와 구현 가능한 수준으로 일치한다. API와 DB 계약, 권한 정책, acceptance criteria, 구현 순서 모두 현재 단계에서 backend 구현을 시작해도 되는 상태다.

**최종 판정: APPROVED - 구현 가능**
