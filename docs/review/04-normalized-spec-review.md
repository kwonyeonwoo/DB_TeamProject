# Normalized Spec Review

검토일: 2026-05-11

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

비고: `docs/normalized/naming-convention.md`은 이번 사용자 요청 범위에 포함되지 않아 판정 대상에서 제외했다.

## 2. 검토 기준별 결과

| 기준 | 결과 | 판단 |
|---|---|---|
| 1. 원본에 없는 기능 추가 여부 | PASS | 아이디 찾기, 비밀번호 재설정, ADMIN 권한 부여/회수 API, 그룹 채팅, 복잡한 초대 링크/공유/만료/재발급, 파일 메타데이터 저장, 신고 처리 시 자동 삭제가 제외 범위로 유지된다. |
| 2. 원본 요구사항 누락 여부 | PASS | 회원, 알림, 게시글/파일/추천, 신고, 댓글/대댓글, 개인 일정, 그룹, 그룹 일정, 탈퇴 생명주기 요구사항이 정규화 명세에 반영되어 있다. |
| 3. API와 DB 스키마 정합성 | PASS | API 리소스와 DB 테이블/컬럼/상태값/제약이 대응된다. 다형 신고 대상은 원본과 동일하게 서비스 로직 검증 대상으로 정리되어 있다. |
| 4. 권한 정책 정합성 | PASS | 세션 인증, USER/ADMIN 역할, 작성자 권한, 소유자 권한, 그룹원 권한, 관리자 신고 권한이 원본과 맞다. |
| 5. Acceptance criteria 충분성 | PASS | 정상 흐름과 주요 실패 케이스가 기능별로 반영되어 있으며, API 오류 조건 추적 표가 구현 테스트 범위를 보완한다. |
| 6. 구현 순서 안전성 | PASS | DB/공통 기반 이후 기능 그룹별로 진행하고, 회원 탈퇴 통합 생명주기는 일정/그룹 구현 이후로 배치되어 있다. |

## 3. 발견 이슈

현재 검토 범위에서 구현 전 수정이 필요한 BLOCKER, MAJOR, MINOR, QUESTION 이슈는 발견되지 않았다.

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 문제 없음 | - | - | 없음 |

## 4. 원본에 없는 기능 추가 여부

정규화 명세는 다음 제외 범위를 유지한다.

- `docs/normalized/product-spec.md` 4. 제외 범위: 아이디 찾기, 비밀번호 찾기/재설정, ADMIN 권한 부여/회수 API, 그룹 채팅, 복잡한 초대 링크/외부 공유, 그룹 코드 만료/재발급, 파일 메타데이터 저장, 파일 크기/확장자 정책, 신고 처리 시 자동 삭제를 제외로 명시한다.
- `docs/normalized/feature-list.md` Excluded Feature IDs: 원본 요구사항의 제외 항목과 일치한다.
- `docs/normalized/api-contract.md` 10. 제외 API: 제외 기능에 대한 API를 제공하지 않는다고 명시한다.
- `docs/normalized/implementation-plan.md` 1. 구현 원칙: 구현 제외 항목을 API, 화면 진입점, 테스트 기대값으로 만들지 않는다고 명시한다.

판정: **추가 기능 없음**.

## 5. 원본 요구사항 누락 여부

주요 요구사항 반영 상태:

| 영역 | 정규화 반영 위치 | 판단 |
|---|---|---|
| 공통 에러 응답 | `api-contract.md` 1, `acceptance-criteria.md` AC-COMMON-001 | PASS |
| 회원 가입/로그인/로그아웃/내 정보 | `feature-list.md` AUTH/USER, `api-contract.md` 3, `auth-policy.md` | PASS |
| 회원 탈퇴 생명주기 | `product-spec.md` 5, `domain-model.md` User/Group/Schedule 규칙, `api-contract.md` USER-003, `acceptance-criteria.md` USER-003 | PASS |
| 알림 생성/조회/읽음 처리 | `domain-model.md` Notification, `api-contract.md` NOTI-001, `acceptance-criteria.md` NOTI-001 및 COMMENT-002/003 | PASS |
| 게시글/파일/추천 | `api-contract.md` 4, `db-schema-contract.md` post/file/likes, `acceptance-criteria.md` 4 | PASS |
| 신고 | `api-contract.md` REPORT-001..003, `auth-policy.md` 5, `db-schema-contract.md` report, `acceptance-criteria.md` 6 | PASS |
| 댓글/대댓글 | `api-contract.md` 5, `domain-model.md` Comment and Reply, `acceptance-criteria.md` 5 | PASS |
| 개인 일정 | `api-contract.md` 7, `db-schema-contract.md` schedules, `acceptance-criteria.md` 7 | PASS |
| 그룹/그룹 일정 | `api-contract.md` 8-9, `domain-model.md` Group/Schedule, `acceptance-criteria.md` 8 | PASS |

판정: **누락 없음**.

## 6. API-DB 정합성

| 영역 | 정합성 | 근거 |
|---|---|---|
| User/Auth | PASS | `users.id`, `login_id`, `status`, `role`, `deleted_at`이 회원 API와 권한 정책을 지원한다. 탈퇴 후 개인정보성 컬럼은 즉시 변경하지 않고 6개월 후 NULL 또는 식별 불가 값으로 처리한다. |
| Post/File | PASS | 게시글 API의 `main_category`, `sub_category`, `is_anonymous`, `view_count`, `updated_at`, `status`가 `post`와 일치한다. 파일은 `file(id, file_url)`에 저장하고 별도 메타데이터를 저장하지 않는다. |
| Like | PASS | `likes(user_id, post_id)` unique가 회원당 게시글 1회 추천 정책을 지원한다. |
| Comment/Reply | PASS | 댓글과 대댓글은 `comments.parent_comment`로 구분하며, 대댓글의 재대댓글 금지는 서비스 로직 검증 대상으로 정리되어 있다. |
| Notification | PASS | `commented_post_id`, `commented_user_id`, nullable `commented_id`가 댓글/대댓글 알림 이동 규칙과 맞다. |
| Report | PASS | `report.target_type`, `target_id`, `reason_type`, `status`, `processed_by`, `processed_at`이 신고 생성/처리 API와 맞다. |
| Schedule | PASS | 개인 일정은 `group_id = NULL`, 그룹 일정은 `group_id != NULL`로 구분된다. |
| Group | PASS | `groups.group_code`, `group_members.role`, `group_members.joined_at`이 그룹 가입과 그룹장 위임 규칙을 지원한다. |

## 7. 권한 정책 정합성

- `docs/normalized/auth-policy.md`는 비인증 접근을 `401`, 권한 실패를 `403`으로 정리하며 원본 API의 상태 코드와 일치한다.
- 신고 생성 `REPORT-001`은 USER 전용이고, 관리자 신고 목록/처리 `REPORT-002`, `REPORT-003`은 ADMIN 전용이다.
- 게시글과 댓글/대댓글 수정·삭제는 작성자만 가능하다.
- 개인 일정은 소유자만 조회/수정/삭제할 수 있다.
- 그룹 상세와 그룹 일정은 그룹원만 접근할 수 있으며, 모든 그룹원은 그룹 일정을 등록/수정/삭제할 수 있다.
- ADMIN 권한은 회원 가입 또는 일반 API로 부여하지 않고 DB seed 또는 운영자 DB 변경으로만 부여한다.

판정: **권한 정책 일치**.

## 8. Acceptance Criteria 충분성

`docs/normalized/acceptance-criteria.md`는 다음을 포함한다.

- 공통 에러 응답과 인증/권한 실패 기준
- 기능별 API 오류 조건 추적 표
- 회원 가입, 로그인, 로그아웃, 내 정보 수정, 탈퇴 생명주기 정상/실패 기준
- 게시글 필터, 상세 조회수 증가, 파일 업로드/교체, 작성자 권한 실패 기준
- 댓글/대댓글 작성, self-notification 미생성, 대댓글 중첩 금지, 수정/삭제 권한 기준
- USER 신고 생성, ADMIN 신고 조회/처리, 신고 처리 시 자동 삭제 금지 기준
- 일정 `type` 1..5, 필수값, 시간 검증, 소유자/그룹원 권한 기준
- 그룹 생성/가입 코드, 중복 가입, 유효하지 않은 코드, 그룹원 접근 기준

판정: **구현 테스트 작성에 충분함**.

## 9. 구현 순서 안전성

`docs/normalized/implementation-plan.md`의 구현 순서는 안전하다.

- DB migration과 공통 에러/인증 기반을 먼저 구축한다.
- 인증/회원 기본 기능을 먼저 구현하되, `DELETE /api/users/me`는 일정/그룹 의존 로직 이후 통합 생명주기 단계로 분리한다.
- 게시글/파일 구현 이후 추천·신고, 댓글·알림, 일정, 그룹/그룹 일정으로 확장한다.
- 회원 탈퇴는 개인 일정 비활성화, 그룹 탈퇴, 그룹장 위임, 유일 그룹 비활성화가 모두 구현된 뒤 처리한다.
- 통합 검증 단계에서 권한, 상태, 회귀 테스트와 문서 trace를 확인한다.

판정: **작은 단계별 구현 계획으로 안전함**.

## 10. 결론

정규화 명세는 현재 원본 문서와 일치한다. 원본에 없는 기능 추가, 원본 요구사항 누락, API-DB 불일치, 권한 정책 불일치, acceptance criteria 부족, 구현 순서상 위험 요소는 발견되지 않았다.

**최종 판정: APPROVED - 구현 가능**
