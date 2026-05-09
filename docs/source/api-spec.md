# API Spec

## 1. 작성 기준

- 요구사항: `docs/source/requirements.md`
- 유저 플로우: `docs/source/user-flow.md`
- 논리 스키마: `docs/source/logical-schema.md`
- ERD: `docs/source/erd.md`
- 물리 스키마: `docs/source/physical-schema.md`

이 문서는 현재 작성된 문서만 기준으로 한 API 초안이다. 아직 결정되지 않은 정책은 `TBD` 또는 `Open Questions`로 남긴다.

## 2. 공통 규칙

| 항목 | 값 |
|---|---|
| Base path | `/api` |
| Body format | JSON |
| 인증 방식 | TBD |
| 인증 필요 표기 | `인증 필요` |
| ID 타입 | integer |
| 일시 타입 | ISO-8601 문자열 |
| 에러 응답 형식 | TBD |

## 3. 공통 상태 코드

| Status | 의미 |
|---:|---|
| 200 | 조회, 수정, 처리 성공 |
| 201 | 생성 성공 |
| 204 | 응답 본문 없는 성공 |
| 400 | 요청 형식 또는 검증 실패 |
| 401 | 인증되지 않음 |
| 403 | 권한 없음 |
| 404 | 대상 리소스 없음 |
| 409 | 중복 또는 상태 충돌 |

## 4. 리소스 필드

### 4-1. User

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 회원 식별자 |
| `login_id` | string | 로그인 아이디 |
| `name` | string | 이름 또는 닉네임 |
| `email_address` | string | 이메일 |
| `created_at` | string | 가입 일시 |
| `deleted_at` | string, nullable | 회원 탈퇴 일시 |
| `status` | string | `ACTIVE`, `DELETED` |
| `role` | string, nullable | 사용자 역할 |

`password`는 응답에 포함하지 않는다.

### 4-2. Post

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 게시글 식별자 |
| `user_id` | integer | 작성자 id |
| `title` | string | 제목 |
| `content` | string, nullable | 본문 |
| `created_at` | string | 작성 일시 |
| `is_updated` | boolean | 수정 여부 |
| `view_count` | integer | 조회수 |
| `is_reported` | boolean | 신고 여부 |
| `main_category` | string | 대주제, 학과 |
| `sub_category` | string | 소주제, 과목 |
| `files` | File[] | 첨부파일 목록 |

### 4-3. File

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 게시글 id |
| `file_url` | string | 첨부파일 URL |

### 4-4. Comment

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 댓글 식별자 |
| `user_id` | integer | 작성자 id |
| `post_id` | integer | 게시글 id |
| `parent_comment` | integer, nullable | 부모 댓글 id. NULL이면 일반 댓글 |
| `content` | string | 댓글 또는 대댓글 내용 |
| `is_public` | boolean | 공개 여부 |
| `created_at` | string | 작성 일시 |
| `is_updated` | boolean | 수정 여부 |

### 4-5. Like

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 추천 식별자 |
| `user_id` | integer | 추천한 회원 id |
| `post_id` | integer | 추천받은 게시글 id |
| `created_at` | string | 추천 일시 |

### 4-6. Notification

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 알림 식별자 |
| `is_read` | boolean | 읽음 여부 |
| `comment_content` | string | 알림에 표시할 댓글 내용 |
| `commented_post_id` | integer | 댓글이 달린 게시글 id |
| `commented_user_id` | integer | 알림 수신자 id |
| `commented_id` | integer | 알림 대상 댓글 또는 대댓글 id |
| `created_at` | string | 알림 생성 일시 |

### 4-7. Group

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 그룹 식별자 |
| `group_link` | string | 그룹 초대 링크 |
| `name` | string | 그룹명 |
| `creator_id` | integer | 생성자 id |
| `created_at` | string | 생성 일시 |

### 4-8. GroupMember

| 필드 | 타입 | 설명 |
|---|---|---|
| `group_id` | integer | 그룹 id |
| `user_id` | integer | 회원 id |
| `role` | string | `LEADER`, `MEMBER` |

### 4-9. Schedule

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 일정 식별자 |
| `user_id` | integer | 일정 작성자 또는 소유자 id |
| `group_id` | integer, nullable | 그룹 id. NULL이면 개인 일정 |
| `title` | string | 일정 제목 |
| `start_at` | string | 시작 일시 |
| `end_at` | string | 종료 일시 |
| `description` | string, nullable | 메모 또는 설명 |
| `type` | integer | 일정 유형 |

## 5. 인증 및 회원 API

### A-01. 회원 가입

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/signup` |
| 인증 | 불필요 |
| Trace | 요구사항 1-1, UF-02, `users` |

Request body:

```json
{
  "login_id": "student01",
  "password": "password",
  "name": "홍길동",
  "email_address": "student@example.com"
}
```

Response `201`:

```json
{
  "id": 1,
  "login_id": "student01",
  "name": "홍길동",
  "email_address": "student@example.com",
  "created_at": "2026-05-07T10:00:00Z",
  "status": "ACTIVE"
}
```

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |
| 409 | `login_id` 중복 |

### A-02. 로그인

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/login` |
| 인증 | 불필요 |
| Trace | 요구사항 1-2, UF-01 |

Request body:

```json
{
  "login_id": "student01",
  "password": "password"
}
```

Response `200`:

```json
{
  "user": {
    "id": 1,
    "login_id": "student01",
    "name": "홍길동",
    "status": "ACTIVE"
  },
  "auth": "TBD"
}
```

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |
| 401 | 로그인 실패 |

### A-03. 로그아웃

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/logout` |
| 인증 | 인증 필요 |
| Trace | UF-03 |

Response:

| Status | Body |
|---:|---|
| 204 | 없음 |

### A-04. 아이디 찾기

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/find-login-id` |
| 인증 | 불필요 |
| Trace | 요구사항 1-2 |
| 상태 | 본인확인 정보 미정으로 초안 |

Request body:

```json
{
  "identity_verification": "TBD"
}
```

Response `200`:

```json
{
  "login_id": "student01"
}
```

Open Questions:

- 본인확인 정보가 이메일 단독인지, 이름+이메일인지 결정 필요
- 아이디 전체를 노출할지 일부 마스킹할지 결정 필요

### A-05. 비밀번호 찾기

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/find-password` |
| 인증 | 불필요 |
| Trace | 요구사항 1-2 |
| 상태 | 본인확인 정보와 비밀번호 재설정 방식 미정으로 초안 |

Request body:

```json
{
  "login_id": "student01",
  "identity_verification": "TBD"
}
```

Response `200`:

```json
{
  "result": "TBD"
}
```

Open Questions:

- 임시 비밀번호 발급인지, 비밀번호 재설정 화면으로 연결하는지 결정 필요
- 본인확인 정보와 성공 응답 형식 결정 필요

### A-06. 내 정보 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | UF-04, `users` |

Response `200`: User

### A-07. 비밀번호 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/users/me/password` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-3 |

Request body:

```json
{
  "new_password": "new-password"
}
```

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 400 | 기존 비밀번호와 동일한 비밀번호로 수정 시도 |
| 401 | 인증되지 않음 |

Open Questions:

- 현재 비밀번호 입력을 필수로 받을지 결정 필요

### A-08. 회원 탈퇴

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | UF-04, `users.status`, `users.deleted_at` |
| 상태 | 요구사항에는 직접 명시되지 않았으므로 초안 |

Response `204`: 없음

Open Questions:

- 탈퇴 후 자동 로그아웃 여부
- 탈퇴 상태를 `status = DELETED`, `deleted_at = now()`로 기록하는 정책 확정 필요

## 6. 게시글 API

### P-01. 게시글 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-3, UF-05, `post` |

Query parameters:

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `main_category` | string | no | 대주제 필터 |
| `sub_category` | string | no | 소주제 필터 |

Response `200`:

```json
{
  "items": [
    {
      "id": 1,
      "user_id": 1,
      "title": "게시글 제목",
      "created_at": "2026-05-07T10:00:00Z",
      "is_updated": false,
      "view_count": 0,
      "is_reported": false,
      "main_category": "컴퓨터공학과",
      "sub_category": "데이터베이스"
    }
  ]
}
```

Open Questions:

- 페이지네이션 방식 결정 필요

### P-02. 게시글 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts/{post_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-3, UF-05, `post`, `file`, `comments` |

Response `200`: Post

Errors:

| Status | 조건 |
|---:|---|
| 404 | 게시글 없음 |

### P-03. 게시글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-1, UF-06, `post`, `file` |

Request body:

```json
{
  "title": "게시글 제목",
  "content": "게시글 본문",
  "main_category": "컴퓨터공학과",
  "sub_category": "데이터베이스",
  "file_urls": ["https://example.com/file.pdf"]
}
```

Response `201`: Post

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |

Open Questions:

- 파일 업로드를 URL 목록으로 받을지, multipart 업로드 API를 별도로 둘지 결정 필요
- 작성 완료 후 게시글 상세로 이동할지 게시판 목록으로 이동할지 결정 필요

### P-04. 게시글 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/posts/{post_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-2, UF-07, `post.is_updated` |

Request body:

```json
{
  "title": "수정 제목",
  "content": "수정 본문",
  "main_category": "컴퓨터공학과",
  "sub_category": "데이터베이스"
}
```

Response `200`: Post

Errors:

| Status | 조건 |
|---:|---|
| 403 | 작성자가 아닌 회원의 수정 시도 |
| 404 | 게시글 없음 |

### P-05. 게시글 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/posts/{post_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-2, UF-07 |

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 403 | 작성자가 아닌 회원의 삭제 시도 |
| 404 | 게시글 없음 |

Open Questions:

- 삭제 후 이동 위치 결정 필요

### P-06. 게시글 추천

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts/{post_id}/likes` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-4, `likes` |

Response `201`: Like

Errors:

| Status | 조건 |
|---:|---|
| 404 | 게시글 없음 |
| 409 | 이미 추천한 게시글 |

Open Questions:

- 추천 취소 API를 제공할지 결정 필요

## 7. 댓글 API

### C-01. 게시글 댓글 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts/{post_id}/comments` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4, `comments` |

Response `200`:

```json
{
  "items": []
}
```

Visibility rule:

- 공개 댓글/대댓글은 조회 가능하다.
- 비공개 댓글/대댓글은 작성자, 부모 댓글 작성자, 원 게시글 작성자만 조회 가능하다.

### C-02. 댓글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts/{post_id}/comments` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-1, UF-08, `comments` |

Request body:

```json
{
  "content": "댓글 내용",
  "is_public": true
}
```

Response `201`: Comment

### C-03. 대댓글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/comments/{comment_id}/replies` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-3, `comments.parent_comment` |

Request body:

```json
{
  "content": "대댓글 내용",
  "is_public": true
}
```

Response `201`: Comment

Open Questions:

- 대댓글 깊이를 1단계로 제한할지 결정 필요

### C-04. 댓글 또는 대댓글 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/comments/{comment_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4, `comments.is_updated` |

Request body:

```json
{
  "content": "수정된 댓글 내용",
  "is_public": true
}
```

Response `200`: Comment

Errors:

| Status | 조건 |
|---:|---|
| 403 | 작성자가 아닌 회원의 수정 시도 |
| 404 | 댓글 없음 |

### C-05. 댓글 또는 대댓글 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/comments/{comment_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4 |

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 403 | 작성자가 아닌 회원의 삭제 시도 |
| 404 | 댓글 없음 |

Open Questions:

- 댓글 수정/삭제 후 화면 갱신 방식 결정 필요

## 8. 알림 API

### N-01. 내 알림 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/notifications` |
| 인증 | 인증 필요 |
| Trace | 요구사항 2-2, `notification` |

Response `200`:

```json
{
  "items": []
}
```

Authorization:

- 인증된 회원은 `commented_user_id`가 본인 id인 알림만 조회할 수 있다.

### N-02. 알림 읽음 처리

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/notifications/{notification_id}/read` |
| 인증 | 인증 필요 |
| Trace | `notification.is_read` |

Response `200`: Notification

Errors:

| Status | 조건 |
|---:|---|
| 403 | 다른 회원의 알림 접근 |
| 404 | 알림 없음 |

Open Questions:

- 알림 클릭 시 자동으로 읽음 처리할지 결정 필요
- 알림 클릭 시 게시글 상세로 이동할지 결정 필요

## 9. 개인 일정 API

### S-01. 개인 일정 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/me/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, UF-09, `schedules` |

Query parameters:

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `start_at` | string | no | 조회 시작 일시 |
| `end_at` | string | no | 조회 종료 일시 |

Response `200`:

```json
{
  "items": []
}
```

Authorization:

- 인증된 회원은 자신의 개인 일정만 조회할 수 있다.
- 개인 일정은 `group_id = null`인 일정이다.

### S-02. 개인 일정 등록

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/me/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-2, UF-09, `schedules` |

Request body:

```json
{
  "title": "일정 제목",
  "start_at": "2026-05-07T09:00:00Z",
  "end_at": "2026-05-07T10:00:00Z",
  "description": "메모",
  "type": 1
}
```

Response `201`: Schedule

Errors:

| Status | 조건 |
|---:|---|
| 400 | `end_at`이 `start_at`보다 빠른 경우 |

### S-03. 개인 일정 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/me/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, `schedules` |

Request body:

```json
{
  "title": "수정 일정",
  "start_at": "2026-05-07T09:00:00Z",
  "end_at": "2026-05-07T10:00:00Z",
  "description": "수정 메모",
  "type": 1
}
```

Response `200`: Schedule

Errors:

| Status | 조건 |
|---:|---|
| 403 | 다른 회원의 개인 일정 접근 |
| 404 | 일정 없음 |

### S-04. 개인 일정 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/me/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3 |

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 403 | 다른 회원의 개인 일정 접근 |
| 404 | 일정 없음 |

## 10. 그룹 API

### G-01. 내 그룹 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/groups` |
| 인증 | 인증 필요 |
| Trace | 요구사항 6-1, UF-10, `groups`, `group_members` |

Response `200`:

```json
{
  "items": []
}
```

### G-02. 그룹 생성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups` |
| 인증 | 인증 필요 |
| Trace | 요구사항 6-2, UF-10, `groups`, `group_members` |

Request body:

```json
{
  "name": "스터디 그룹"
}
```

Response `201`: Group

Notes:

- 그룹 생성자는 그룹의 `creator_id`가 된다.
- 생성된 그룹의 `group_link`를 유지한다.
- 생성자가 `group_members`에 포함되는지와 역할은 Open Questions에서 결정한다.

### G-03. 그룹 가입

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups/join` |
| 인증 | 인증 필요 |
| Trace | 요구사항 6-1, UF-10, `group_members` |

Request body:

```json
{
  "group_link": "invite-link"
}
```

Response `201`: GroupMember

Errors:

| Status | 조건 |
|---:|---|
| 404 | 유효하지 않은 그룹 링크 |
| 409 | 이미 가입한 그룹 |

### G-04. 그룹 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/groups/{group_id}` |
| 인증 | 인증 필요 |
| Trace | UF-10, `groups`, `group_members` |

Response `200`:

```json
{
  "group": {},
  "members": []
}
```

Errors:

| Status | 조건 |
|---:|---|
| 403 | 그룹원이 아닌 회원의 접근 |
| 404 | 그룹 없음 |

## 11. 그룹 일정 API

### GS-01. 그룹 일정 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/groups/{group_id}/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-10, `schedules` |

Response `200`:

```json
{
  "items": []
}
```

Authorization:

- 해당 그룹의 그룹원만 조회할 수 있다.

### GS-02. 그룹 일정 등록

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups/{group_id}/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, `schedules` |

Request body:

```json
{
  "title": "그룹 일정",
  "start_at": "2026-05-07T09:00:00Z",
  "end_at": "2026-05-07T10:00:00Z",
  "description": "메모",
  "type": 1
}
```

Response `201`: Schedule

Authorization:

- 해당 그룹의 그룹원만 등록할 수 있다.

### GS-03. 그룹 일정 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/groups/{group_id}/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-10, `schedules` |

Request body:

```json
{
  "title": "수정 그룹 일정",
  "start_at": "2026-05-07T09:00:00Z",
  "end_at": "2026-05-07T10:00:00Z",
  "description": "수정 메모",
  "type": 1
}
```

Response `200`: Schedule

Authorization:

- 해당 그룹의 그룹원만 수정할 수 있다.

Open Questions:

- 그룹원 전체가 수정 가능한지, 작성자 또는 리더만 가능한지 결정 필요

### GS-04. 그룹 일정 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/groups/{group_id}/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, `schedules` |

Response `204`: 없음

Authorization:

- 해당 그룹의 그룹원만 삭제할 수 있다.

Open Questions:

- 그룹원 전체가 삭제 가능한지, 작성자 또는 리더만 가능한지 결정 필요

## 12. Open Questions

| ID | 관련 API | 결정 필요 사항 |
|---|---|---|
| OQ-01 | A-04, A-05 | 아이디/비밀번호 찾기의 본인확인 정보 |
| OQ-02 | A-05 | 비밀번호 찾기 성공 후 재설정 방식 |
| OQ-03 | A-07 | 비밀번호 변경 시 현재 비밀번호 입력 필요 여부 |
| OQ-04 | A-08 | 회원 탈퇴 후 자동 로그아웃 여부 |
| OQ-05 | P-03 | 파일 업로드 방식 |
| OQ-06 | P-03 | 글쓰기 완료 후 이동 위치 |
| OQ-07 | P-05 | 게시글 삭제 후 이동 위치 |
| OQ-08 | P-06 | 게시글 추천 취소 허용 여부 |
| OQ-09 | C-03 | 대댓글 깊이 제한 |
| OQ-10 | C-05 | 댓글 수정/삭제 후 화면 갱신 방식 |
| OQ-11 | N-02 | 알림 클릭 시 읽음 처리와 이동 대상 |
| OQ-12 | GS-03, GS-04 | 그룹 일정 수정/삭제 권한 |
