# API Contract

기준 문서: `docs/source/api-spec.md`, `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/user-flow.md`

검증 상태: PASS - source 문서 기준 API 계약 충돌 없음

확정 정책:

- `USER-003`: 탈퇴 시 `status = DELETED`, `deleted_at = now()`를 기록하고 세션을 무효화한다. 개인정보성 컬럼은 탈퇴 시점에 즉시 변경하지 않고 `deleted_at`으로부터 6개월 후 NULL 또는 식별 불가 값으로 변경한다.
- `COMMENT-002`/`COMMENT-003`: 댓글/대댓글 작성자와 알림 수신자가 같으면 알림을 생성하지 않는다.
- `NOTI-001`: `notification.comment_content`는 알림 생성 시점의 댓글/대댓글 내용 스냅샷이며, 원본 댓글/대댓글 수정 또는 삭제 후에도 갱신하지 않는다.
- `COMMON-AUTH`: 세션 기반 인증에서 `AUTH-001` 회원 가입과 `AUTH-002` 로그인을 제외한 `POST`, `PATCH`, `DELETE` API는 CSRF 토큰을 요구한다.

## 1. 공통 계약

| 항목 | 값 |
|---|---|
| Base path | `/api` |
| 기본 Body format | JSON |
| 파일 업로드 Body format | `multipart/form-data` |
| 인증 방식 | 서버 세션 기반 + 상태 변경 API CSRF 토큰 |
| ID 타입 | integer |
| 일시 타입 | ISO-8601 string |

공통 성공 상태:

| Status | 의미 |
|---:|---|
| 200 | 조회, 수정, 처리 성공 |
| 201 | 생성 성공 |
| 204 | 응답 본문 없는 성공 |

공통 실패 상태:

| Status | 의미 |
|---:|---|
| 400 | 요청 형식 또는 검증 실패 |
| 401 | 인증되지 않음 |
| 403 | 권한 없음 또는 CSRF 토큰 누락/불일치 |
| 404 | 대상 리소스 없음 |
| 409 | 중복 또는 상태 충돌 |

공통 에러 응답:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "필수 입력값이 누락되었습니다."
}
```

Rules:

- 실패 응답 body는 `code`, `message`를 필수로 포함한다.
- `details`는 입력값별 상세 오류가 필요한 경우에만 선택값으로 포함한다.
- `details`가 포함되더라도 `code`, `message`는 항상 포함한다.
- 인증 필요 API에 인증 세션이 없으면 `401`을 반환한다.
- `AUTH-001`, `AUTH-002`를 제외한 `POST`, `PATCH`, `DELETE` API는 서버가 발급한 CSRF 토큰을 요청에 포함해야 한다.
- 인증 세션은 있지만 CSRF 토큰이 누락되었거나 일치하지 않으면 `403`과 `ACCESS_DENIED` 오류 응답을 반환한다.

## 2. 리소스 응답 타입

| 타입 | 필드 |
|---|---|
| User | `id`, `login_id`, `name`, `email_address`, `created_at`, `deleted_at`, `status`, `role` |
| Post | `id`, `user_id`, `author_display_name`, `title`, `content`, `created_at`, `updated_at`, `view_count`, `main_category`, `sub_category`, `is_anonymous`, `files`, `liked_by_me`, `like_count` |
| File | `id`, `file_url` |
| Comment | `id`, `user_id`, `author_display_name`, `post_id`, `parent_comment`, `content`, `is_anonymous`, `created_at`, `updated_at` |
| Like | `id`, `user_id`, `post_id`, `created_at` |
| Report | `id`, `reporter_id`, `target_type`, `target_id`, `target_display_name`, `reason_type`, `created_at`, `status`, `processed_by`, `processed_at` |
| Notification | `id`, `is_read`, `comment_content`, `commented_post_id`, `commented_user_id`, `commented_id`, `created_at` |
| Group | `id`, `group_code`, `name`, `leader_id`, `created_at` |
| GroupMember | `group_id`, `user_id`, `role`, `joined_at` |
| Schedule | `id`, `user_id`, `group_id`, `title`, `start_at`, `end_at`, `description`, `type`, `created_at`, `updated_at` |

`password`는 어떤 응답에도 포함하지 않는다.

## 3. 인증 및 회원 API

| 기능 ID | Method | Endpoint | Auth | Request | Response | Validation / Rules | Errors |
|---|---|---|---|---|---|---|---|
| AUTH-001 | POST | `/api/auth/signup` | no | JSON: `login_id`, `password`, `name`, `email_address` | 201 User | 모든 필드 필수. `login_id`, `email_address` 중복 불가. 생성 role은 `USER`. request body에 `role` 없음. 탈퇴 회원의 NULL 값은 중복 판단 제외. | 400 필수/형식 오류, 409 아이디 중복, 409 이메일 중복 |
| AUTH-002 | POST | `/api/auth/login` | no | JSON: `login_id`, `password` | 200 `{ "user": User }` | 로그인 성공 시 서버 세션 생성. `DELETED` 회원 로그인 불가. | 400 필수 누락, 401 로그인 실패, 403 탈퇴 계정 |
| AUTH-003 | POST | `/api/auth/logout` | yes | 없음 | 204 없음 | 현재 사용자 세션 무효화. | 401 인증되지 않음 |
| USER-001 | GET | `/api/users/me` | yes | 없음 | 200 User | 현재 세션 사용자만 조회. | 401 인증되지 않음 |
| USER-002 | PATCH | `/api/users/me` | yes | JSON: `name`, `email_address`, `current_password`, `new_password` 중 수정값 | 200 User | 이름/이메일/비밀번호 중 하나 이상 수정. 기존 정보와 동일한 값 불가. 이메일 중복 불가. 비밀번호 변경 시 `current_password` 필수 및 일치 필요. | 400 수정 필드 없음/동일값/current_password 누락, 401 인증되지 않음, 403 current_password 불일치, 409 이메일 중복 |
| USER-003 | DELETE | `/api/users/me` | yes | 없음 | 204 없음 | `users.status = DELETED`, `users.deleted_at = now()`. 세션 무효화. 탈퇴 처리 시점에는 개인정보성 컬럼을 즉시 변경하지 않음. `deleted_at`으로부터 6개월 후 개인정보성 컬럼을 NULL 또는 식별 불가 값으로 변경. 개인 일정은 즉시 삭제. 가입 그룹에서 탈퇴 처리. 그룹장인 경우 `groups.leader_id`와 `group_members.role`을 가장 먼저 가입한 다른 그룹원에게 위임. 유일 그룹원이면 해당 그룹 즉시 삭제. | 401 인증되지 않음 |

## 4. 게시글 API

| 기능 ID | Method | Endpoint | Auth | Request | Response | Validation / Rules | Errors |
|---|---|---|---|---|---|---|---|
| POST-001 | GET | `/api/posts` | yes | Query: `page`, `size`, `keyword`, `author`, `main_category`, `sub_category` | 200 page object with `items`, `page`, `size`, `total_count`, `total_pages` | 최신순. `page` 기본값은 1. `keyword`, `author`, 주제 필터는 한 번에 하나만 사용. 주제 필터는 `main_category`, `sub_category` 묶음. 작성자 필터에서 탈퇴/익명 작성자 제외. | 400 둘 이상의 필터 종류, 400 페이지 파라미터 오류 |
| POST-002 | GET | `/api/posts/{post_id}` | yes | Path: `post_id` | 200 Post | 상세 접근 시 조회수 증가. | 404 게시글 없음 |
| POST-003 | POST | `/api/posts` | yes | multipart: `title`, `content`, `main_category`, `sub_category`, `is_anonymous`, `files[]` | 201 Post | `title`, `main_category`, `sub_category`, `is_anonymous` 필수. 파일은 직접 업로드. 실제 파일은 `/uploads/posts/{post_id}/{UUID}` 저장, DB에는 `file_url`만 저장. | 400 필수 누락, 400 파일 업로드 실패 |
| POST-004 | PATCH | `/api/posts/{post_id}` | yes, author | multipart 또는 JSON: `title`, `content`, `main_category`, `sub_category`, `is_anonymous`, `files[]` | 200 Post | 작성자만 수정. 수정 성공 시 `updated_at = now()`. 새 파일이 있으면 기존 파일 목록 전체 교체, 없으면 유지. | 400 수정 필드 없음, 403 작성자 아님, 404 게시글 없음 |
| POST-005 | DELETE | `/api/posts/{post_id}` | yes, author | Path: `post_id` | 204 없음 | 작성자만 삭제. 게시글 삭제 시 추천, 댓글/대댓글, 첨부파일, 알림은 FK cascade 정책에 따라 함께 삭제된다. 해당 게시글 또는 함께 삭제된 댓글/대댓글 대상 신고 이력은 유지한다. | 403 작성자 아님, 404 게시글 없음 |
| POST-FILE-001 | GET | `/api/posts/{post_id}/files/{file_name}` | yes | Path: `post_id`, `file_name` | 200 binary file | `file.file_url = /uploads/posts/{post_id}/{file_name}` row가 존재해야 한다. 실제 파일은 `app.upload.root/posts/{post_id}/{file_name}`에서 조회한다. 정적 접근은 `/api/uploads/posts/{post_id}/{file_name}`로 제공한다. | 404 첨부파일 DB row 없음, 404 실제 파일 없음 |
| POST-006 | POST | `/api/posts/{post_id}/likes` | yes | Path: `post_id` | 201 Like | 한 회원은 한 게시글에 추천 1회만 가능. | 404 게시글 없음, 409 이미 추천 |
| POST-007 | DELETE | `/api/posts/{post_id}/likes` | yes | Path: `post_id` | 204 없음 | 추천한 게시글만 취소 가능. | 404 게시글 없음, 404 추천 없음 |

## 5. 댓글 API

| 기능 ID | Method | Endpoint | Auth | Request | Response | Validation / Rules | Errors |
|---|---|---|---|---|---|---|---|
| COMMENT-001 | GET | `/api/posts/{post_id}/comments` | yes | Path: `post_id` | 200 `{ "items": Comment[] }` | 댓글/대댓글을 반환한다. 탈퇴 작성자는 `탈퇴한 유저`, 익명 작성자는 `익명_숫자`. | 404 게시글 없음 |
| COMMENT-002 | POST | `/api/posts/{post_id}/comments` | yes | JSON: `content`, `is_anonymous` | 201 Comment | 내용 필수. 게시글에 일반 댓글 작성. 댓글 작성자가 게시글 작성자와 다른 회원이면 댓글 알림을 생성하고, 같은 회원이면 알림을 생성하지 않는다. | 400 내용 누락, 404 게시글 없음 |
| COMMENT-003 | POST | `/api/comments/{comment_id}/replies` | yes | JSON: `content`, `is_anonymous` | 201 Comment | 내용 필수. 부모 댓글은 일반 댓글이어야 한다. 대댓글에는 다시 대댓글 작성 불가. 대댓글 작성자가 부모 댓글 작성자와 다른 회원이면 대댓글 알림을 생성하고, 같은 회원이면 알림을 생성하지 않는다. | 400 내용 누락, 400 대댓글에 대댓글 작성, 404 부모 댓글 없음 |
| COMMENT-004 | PATCH | `/api/comments/{comment_id}` | yes, author | JSON: `content`, `is_anonymous` | 200 Comment | 작성자만 수정. 수정 성공 시 `updated_at = now()`. | 400 수정 필드 없음, 403 작성자 아님, 404 댓글 없음 |
| COMMENT-005 | DELETE | `/api/comments/{comment_id}` | yes, author | Path: `comment_id` | 204 없음 | 작성자만 삭제. 일반 댓글 삭제 시 해당 댓글의 대댓글은 FK cascade 정책에 따라 함께 삭제된다. 이미 생성된 알림은 유지하며 `notification.comment_content`는 변경하지 않는다. 해당 댓글 또는 함께 삭제된 대댓글 대상 신고 이력은 유지한다. | 403 작성자 아님, 404 댓글 없음 |

## 6. 추천, 신고, 알림 API

| 기능 ID | Method | Endpoint | Auth | Request | Response | Validation / Rules | Errors |
|---|---|---|---|---|---|---|---|
| REPORT-001 | POST | `/api/reports` | yes, USER | JSON: `target_type`, `target_id`, `reason_type` | 201 Report | `target_type`은 `POST` 또는 `COMMENT`. `reason_type`은 1..4. 동일 회원은 동일 대상 1회만 신고. 생성 시 `PENDING`, `processed_by = null`, `processed_at = null`. ADMIN은 이 API의 권한 범위에 포함하지 않는다. | 400 필수 누락/유효하지 않은 값, 403 신고 권한 없음, 404 신고 대상 없음, 409 이미 신고 |
| REPORT-002 | GET | `/api/admin/reports` | yes, ADMIN | 없음 | 200 `{ "items": Report[] }` | ADMIN만 조회. 신고 대상, 신고자, 사유, 시각, 처리 상태, 처리자, 처리 시각을 반환한다. 신고 대상 row를 조회할 수 없으면 `target_display_name = 삭제된 대상`으로 반환한다. | 403 관리자 권한 없음 |
| REPORT-003 | PATCH | `/api/admin/reports/{report_id}` | yes, ADMIN | JSON: `status = PROCESSED` | 200 Report | ADMIN만 처리. `status`, `processed_by`, `processed_at`만 변경. 게시글/댓글 삭제 자동 수행 안 함. | 400 유효하지 않은 status, 403 관리자 권한 없음, 404 신고 없음 |
| NOTI-001 | GET | `/api/notifications` | yes | 없음 | 200 `{ "items": Notification[] }` | 본인 알림만 조회. 팝업 조회 시 반환 대상 알림을 읽음 처리. 댓글 알림은 게시글 상세, 대댓글 알림은 부모 댓글 위치로 이동. | 403 다른 회원 알림 접근 |

## 7. 개인 일정 API

| 기능 ID | Method | Endpoint | Auth | Request | Response | Validation / Rules | Errors |
|---|---|---|---|---|---|---|---|
| CAL-001 | GET | `/api/me/schedules` | yes | Query: `start_at`, `end_at` ISO-8601 optional | 200 `{ "items": Schedule[] }` | 현재 사용자의 `group_id = null` 개인 일정만 조회. `start_at`/`end_at`이 모두 없으면 전체 개인 일정. `start_at`만 있으면 `schedules.end_at >= start_at`인 일정. `end_at`만 있으면 `schedules.start_at <= end_at`인 일정. 둘 다 있으면 `schedules.start_at <= end_at AND schedules.end_at >= start_at`인 기간 겹침 일정. | 400 날짜 형식 오류, 400 `end_at < start_at`, 401 인증 없음, 403 다른 회원 일정 접근 |
| CAL-002 | POST | `/api/me/schedules` | yes | JSON: `title`, `start_at`, `end_at`, `description`, `type` | 201 Schedule | `title`, `start_at`, `end_at`, `type` 필수. `type`은 1..5. `end_at >= start_at`. | 400 필수 누락, 400 유효하지 않은 type, 400 종료가 시작보다 빠름 |
| CAL-003 | PATCH | `/api/me/schedules/{schedule_id}` | yes, owner | JSON: `title`, `start_at`, `end_at`, `description`, `type` | 200 Schedule | 본인 개인 일정만 수정. 수정 필드 중 하나 이상 필요. 수정 성공 시 `updated_at = now()`. | 400 수정 필드 없음, 400 유효하지 않은 type/시간, 403 다른 회원 일정 접근, 404 일정 없음 |
| CAL-004 | DELETE | `/api/me/schedules/{schedule_id}` | yes, owner | Path: `schedule_id` | 204 없음 | 본인 개인 일정만 삭제. | 403 다른 회원 일정 접근, 404 일정 없음 |

## 8. 그룹 API

| 기능 ID | Method | Endpoint | Auth | Request | Response | Validation / Rules | Errors |
|---|---|---|---|---|---|---|---|
| GROUP-001 | GET | `/api/groups` | yes | 없음 | 200 `{ "items": Group[] }` | 현재 사용자가 속한 그룹만 조회. | 401 인증 없음 |
| GROUP-002 | POST | `/api/groups` | yes | JSON: `name` | 201 `{ "group": Group, "membership": GroupMember }` | 그룹명 필수. `group_code` 생성. 생성자는 `groups.leader_id`와 `group_members.role = LEADER`로 등록. `group_code`를 화면에 보여준다. | 400 그룹명 누락 |
| GROUP-003 | POST | `/api/groups/join` | yes | JSON: `group_code` | 201 GroupMember | 단순 코드 입력 방식. 이미 가입한 그룹 중복 가입 불가. | 404 유효하지 않은 그룹 코드, 409 이미 가입 |
| GROUP-004 | GET | `/api/groups/{group_id}` | yes, member | Path: `group_id` | 200 `{ "group": Group, "members": GroupMember[] }` | 그룹원만 조회. | 403 그룹원이 아님, 404 그룹 없음 |

## 9. 그룹 일정 API

| 기능 ID | Method | Endpoint | Auth | Request | Response | Validation / Rules | Errors |
|---|---|---|---|---|---|---|---|
| GCAL-001 | GET | `/api/groups/{group_id}/schedules` | yes, member | Path: `group_id`; Query: `start_at`, `end_at` ISO-8601 optional | 200 `{ "items": Schedule[] }` | `group_id`의 그룹 일정만 조회. `start_at`/`end_at`이 모두 없으면 해당 그룹의 전체 그룹 일정. `start_at`만 있으면 `schedules.end_at >= start_at`인 일정. `end_at`만 있으면 `schedules.start_at <= end_at`인 일정. 둘 다 있으면 `schedules.start_at <= end_at AND schedules.end_at >= start_at`인 기간 겹침 일정. | 400 날짜 형식 오류, 400 `end_at < start_at`, 403 그룹원이 아님, 404 그룹 없음 |
| GCAL-002 | POST | `/api/groups/{group_id}/schedules` | yes, member | JSON: `title`, `start_at`, `end_at`, `description`, `type` | 201 Schedule | 모든 그룹원 등록 가능. 필수값, `type` 1..5, `end_at >= start_at`. | 400 필수/유효성 오류, 403 그룹원이 아님, 404 그룹 없음 |
| GCAL-003 | PATCH | `/api/groups/{group_id}/schedules/{schedule_id}` | yes, member | JSON: `title`, `start_at`, `end_at`, `description`, `type` | 200 Schedule | 모든 그룹원 수정 가능. 타 그룹 일정 수정 불가. 수정 성공 시 `updated_at = now()`. | 400 유효성 오류, 403 그룹원이 아님, 404 그룹 또는 일정 없음 |
| GCAL-004 | DELETE | `/api/groups/{group_id}/schedules/{schedule_id}` | yes, member | Path: `group_id`, `schedule_id` | 204 없음 | 모든 그룹원 삭제 가능. 타 그룹 일정 삭제 불가. | 403 그룹원이 아님, 404 그룹 또는 일정 없음 |

## 10. 제외 API

| 제외 ID | API | 상태 |
|---|---|---|
| X-001 | 아이디 찾기 API | 제공하지 않음 |
| X-002 | 비밀번호 찾기/재설정 API | 제공하지 않음 |
| X-003 | ADMIN 권한 부여/회수 API | 제공하지 않음 |
| X-004 | 그룹 채팅 API | 제공하지 않음 |
| X-005 | 그룹 초대 링크/공유/만료/재발급 API | 제공하지 않음 |

## 11. Assumptions

- API 응답의 구체적인 pagination 기본 `size` 값은 source 문서에 고정되어 있지 않다. `page` 기본값은 원본 API 기준 1이다.
- `author_display_name` 계산은 API 응답 생성 단계에서 수행한다.
- Post 응답의 `liked_by_me`, `like_count`는 게시글 조회 API 구현 시 읽기 모델로 함께 제공해야 한다. 추천 등록/취소 API를 나중에 구현하더라도 응답 계약은 유지한다.

## 12. Open Questions

현재 source 문서 기준 미해결 Open Question은 없다.
