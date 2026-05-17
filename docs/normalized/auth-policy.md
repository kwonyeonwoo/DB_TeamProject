# Auth Policy

기준 문서: `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md`

검증 상태: PASS - source 문서 기준 권한 정책 충돌 없음

주의: `REPORT-001` 신고 생성 권한은 원본 API의 `role = USER` 표현을 기준으로 USER 전용으로 확정한다. ADMIN은 관리자 신고 목록 조회와 신고 처리 권한만 가진다.

## 1. 인증 방식

- 인증 방식은 서버 세션 기반이다.
- 로그인 성공 시 서버는 인증 세션을 생성한다.
- 로그아웃 시 서버에 저장된 현재 사용자 세션을 무효화한다.
- 세션 무효화 이후 해당 세션으로는 인증 필요 기능을 사용할 수 없다.
- 비인증 상태에서 인증 필요 화면 또는 API에 접근하면 로그인 화면으로 이동하거나 `401`을 반환한다.
- `AUTH-001` 회원 가입과 `AUTH-002` 로그인을 제외한 `POST`, `PATCH`, `DELETE` API는 CSRF 토큰을 요구한다.
- 인증 세션은 있지만 CSRF 토큰이 누락되었거나 일치하지 않으면 `403`을 반환한다.

## 2. 사용자 역할

| 역할 | 설명 | 부여 방식 |
|---|---|---|
| `USER` | 일반 사용자. 회원 가입으로 생성되는 기본 역할이다. | 회원 가입 시 자동 부여 |
| `ADMIN` | 신고 목록 조회와 신고 처리 권한을 가진 관리자다. | DB seed 데이터 또는 운영자 DB 변경으로만 부여 |

정책:

- 회원 가입 request body는 `role`을 받지 않는다.
- 일반 API 호출로 `ADMIN` 권한을 부여할 수 없다.
- 관리자 권한 부여 또는 회수 API는 구현 범위에서 제외한다.

## 3. 인증 필요 여부

| 기능 ID | 기능 | 인증 필요 | 역할 |
|---|---|---:|---|
| AUTH-001 | 회원 가입 | no | 비회원 |
| AUTH-002 | 로그인 | no | 비회원 |
| AUTH-003 | 로그아웃 | yes | USER, ADMIN |
| USER-001 | 내 정보 조회 | yes | USER, ADMIN |
| USER-002 | 내 정보 수정 | yes | USER, ADMIN |
| USER-003 | 회원 탈퇴 | yes | USER, ADMIN |
| NOTI-001 | 내 알림 조회 | yes | USER, ADMIN |
| POST-001 | 게시글 목록 조회 | yes | USER, ADMIN |
| POST-002 | 게시글 상세 조회 | yes | USER, ADMIN |
| POST-003 | 게시글 작성 | yes | USER, ADMIN |
| POST-004 | 게시글 수정 | yes | 작성자 |
| POST-005 | 게시글 삭제 | yes | 작성자 |
| POST-006 | 추천 등록 | yes | USER, ADMIN |
| POST-007 | 추천 취소 | yes | USER, ADMIN |
| COMMENT-001 | 댓글 목록 조회 | yes | USER, ADMIN |
| COMMENT-002 | 댓글 작성 | yes | USER, ADMIN |
| COMMENT-003 | 대댓글 작성 | yes | USER, ADMIN |
| COMMENT-004 | 댓글/대댓글 수정 | yes | 작성자 |
| COMMENT-005 | 댓글/대댓글 삭제 | yes | 작성자 |
| REPORT-001 | 신고 생성 | yes | USER |
| REPORT-002 | 관리자 신고 목록 조회 | yes | ADMIN |
| REPORT-003 | 관리자 신고 처리 | yes | ADMIN |
| CAL-001 | 개인 일정 조회 | yes | 일정 소유자 |
| CAL-002 | 개인 일정 등록 | yes | USER, ADMIN |
| CAL-003 | 개인 일정 수정 | yes | 일정 소유자 |
| CAL-004 | 개인 일정 삭제 | yes | 일정 소유자 |
| GROUP-001 | 내 그룹 목록 조회 | yes | USER, ADMIN |
| GROUP-002 | 그룹 생성 | yes | USER, ADMIN |
| GROUP-003 | 그룹 가입 | yes | USER, ADMIN |
| GROUP-004 | 그룹 상세 조회 | yes | 그룹원 |
| GCAL-001 | 그룹 일정 조회 | yes | 그룹원 |
| GCAL-002 | 그룹 일정 등록 | yes | 그룹원 |
| GCAL-003 | 그룹 일정 수정 | yes | 그룹원 |
| GCAL-004 | 그룹 일정 삭제 | yes | 그룹원 |

## 4. 본인 리소스 접근 규칙

| 리소스 | 허용 규칙 |
|---|---|
| 내 정보 | 현재 세션의 사용자만 조회/수정/탈퇴할 수 있다. |
| 게시글 수정/삭제 | `post.user_id`가 현재 사용자 id와 같아야 한다. |
| 댓글/대댓글 수정/삭제 | `comments.user_id`가 현재 사용자 id와 같아야 한다. |
| 알림 조회 | `notification.commented_user_id`가 현재 사용자 id인 알림만 조회할 수 있다. |
| 개인 일정 | `schedules.group_id = NULL`이고 `schedules.user_id`가 현재 사용자 id인 일정만 조회/수정/삭제할 수 있다. |
| 그룹 상세 | 현재 사용자가 `group_members`에 포함된 그룹만 조회할 수 있다. |
| 그룹 일정 | 현재 사용자가 해당 그룹의 `group_members`에 포함되어야 조회/등록/수정/삭제할 수 있다. |

## 5. 관리자 권한 규칙

- 관리자 신고 목록 조회는 `users.role = ADMIN`만 가능하다.
- 관리자 신고 처리는 `users.role = ADMIN`만 가능하다.
- 신고 처리 API는 신고 대상 게시글 또는 댓글을 자동 삭제하지 않는다.
- 신고 처리 성공 시 `report.status = PROCESSED`, `report.processed_by = 현재 관리자 id`, `report.processed_at = now()`를 기록한다.
- ADMIN은 일반 사용자 신고 생성 API인 `POST /api/reports`의 권한 범위에 포함하지 않는다.

## 6. 상태 기반 접근 제한

| 상태 | 접근 규칙 |
|---|---|
| `users.status = DELETED` | 로그인할 수 없다. 작성물은 유지되고 화면에는 `탈퇴한 유저`로 표시된다. 개인정보성 컬럼은 탈퇴 시점에 즉시 변경하지 않고 `deleted_at`으로부터 6개월 후 NULL 또는 식별 불가 값으로 변경한다. |

원본 물리 스키마 기준 `post`, `comments`, `groups`, `schedules`에는 별도 삭제 상태 컬럼이 없다. 해당 리소스의 삭제와 cascade 동작은 API 계약 및 DB FK 정책을 따른다.

## 7. 권한 실패 상태 코드

| 상황 | Status |
|---|---:|
| 인증되지 않음 | 401 |
| 로그인 실패 | 401 |
| 권한 없음 | 403 |
| CSRF 토큰 누락 또는 불일치 | 403 |
| 작성자가 아닌 수정/삭제 시도 | 403 |
| 관리자 권한 없음 | 403 |
| 그룹원이 아닌 접근 | 403 |
| 다른 회원의 개인 일정 접근 | 403 |

## 8. Open Questions

현재 source 문서 기준 미해결 Open Question은 없다.
