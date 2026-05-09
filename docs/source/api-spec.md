# API Spec

## 1. 작성 기준

- 요구사항: `docs/source/requirements.md`
- 화면 설계: `docs/source/screen-design.md`
- 유저 플로우: `docs/source/user-flow.md`
- 논리 스키마: `docs/source/logical-schema.md`
- ERD: `docs/source/erd.md`
- 물리 스키마: `docs/source/physical-schema.md`

이 문서는 최신 요구사항 명세서를 우선 기준으로 한 API 계약 초안이다. 요구사항에서 결정된 정책은 API 계약에 반영하고, 요구사항만으로 세부 API 형태를 확정할 수 없는 항목은 `Open Questions`로 남긴다.

## 2. 공통 규칙

| 항목 | 값 |
|---|---|
| Base path | `/api` |
| 기본 Body format | JSON |
| 파일 업로드 Body format | `multipart/form-data` |
| 인증 방식 | 서버 세션 기반 |
| 인증 필요 표기 | `인증 필요` |
| ID 타입 | integer |
| 일시 타입 | ISO-8601 문자열 |
| 기본 정렬 | 명시가 없으면 최신순 |
| 에러 응답 형식 | Open Questions에서 결정 필요 |

Authentication:

- 로그인 성공 시 서버는 인증 세션을 생성한다.
- 인증 필요 API는 유효한 서버 세션이 있어야 호출할 수 있다.
- 로그아웃 시 서버에 저장된 사용자 세션을 무효화한다.
- 세션 무효화 이후 해당 세션으로는 인증이 필요한 기능을 사용할 수 없다.

Lifecycle:

- 탈퇴 회원은 `users.status = DELETED`, `users.deleted_at = now()`로 처리한다.
- 탈퇴 회원이 작성한 게시글, 댓글, 개인 캘린더, 개인 일정은 비활성화 및 삭제 대기 상태로 전환되어 일반 조회 API에 노출하지 않는다.
- 탈퇴 회원이 유일한 그룹원인 그룹은 비활성화 및 삭제 대기 상태로 전환되어 일반 조회 API에 노출하지 않는다.
- 탈퇴 데이터의 6개월 삭제 대기 상태를 저장하기 위한 DB 스키마 보완이 필요하다.

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
| `name` | string | 이름 |
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
| `liked_by_me` | boolean | 현재 로그인 회원의 추천 여부 |
| `like_count` | integer | 추천 수 |

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

대댓글에는 다시 대댓글을 작성할 수 없다. 따라서 대댓글 작성 API의 부모 댓글은 `parent_comment = null`인 일반 댓글이어야 한다.

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

그룹 채팅은 요구사항상 구현하지 않으므로 채팅 리소스와 API를 제공하지 않는다.

### 4-8. GroupMember

| 필드 | 타입 | 설명 |
|---|---|---|
| `group_id` | integer | 그룹 id |
| `user_id` | integer | 회원 id |
| `role` | string | `LEADER`, `MEMBER` |

그룹장 자동 위임은 "가장 먼저 가입한 그룹원" 기준이다. 이 기준을 구현하려면 `group_members`에 가입 순서를 판단할 스키마 보완이 필요하다.

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
  "created_at": "2026-05-09T10:00:00Z",
  "status": "ACTIVE"
}
```

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 또는 형식 오류 |
| 409 | `login_id` 중복 |
| 409 | `email_address` 중복 |

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
    "email_address": "student@example.com",
    "status": "ACTIVE"
  }
}
```

Processing:

- 로그인 성공 시 서버 세션을 생성한다.
- 탈퇴 상태(`DELETED`) 회원은 로그인할 수 없다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |
| 401 | 로그인 실패 |
| 403 | 탈퇴 계정 접근 |

### A-03. 로그아웃

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/logout` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-2, UF-03 |

Processing:

- 서버에 저장된 현재 사용자 세션을 무효화한다.

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
| Trace | 요구사항 1-2, SC-03 |

Request body:

```json
{
  "email_address": "student@example.com"
}
```

Response `200`:

```json
{
  "login_id": "student01"
}
```

Errors:

| Status | 조건 |
|---:|---|
| 400 | 이메일 누락 또는 형식 오류 |
| 404 | 해당 이메일의 회원 없음 |

Open Questions:

- 아이디 전체를 노출할지 일부 마스킹할지 결정 필요

### A-05. 비밀번호 찾기

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/find-password` |
| 인증 | 불필요 |
| Trace | 요구사항 1-2, SC-03 |

Request body:

```json
{
  "login_id": "student01",
  "email_address": "student@example.com"
}
```

Response `200`:

```json
{
  "next_action": "PASSWORD_RESET_SCREEN"
}
```

Processing:

- `login_id`와 `email_address`가 일치하면 비밀번호 재설정 화면으로 이동할 수 있음을 응답한다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 또는 형식 오류 |
| 404 | 일치하는 회원 없음 |

Open Questions:

- 비밀번호 재설정 화면에서 사용할 검증 토큰 또는 임시 세션 방식 결정 필요

### A-06. 내 정보 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | UF-04, `users` |

Response `200`: User

### A-07. 내 정보 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-3, SC-07 |

Request body:

```json
{
  "name": "홍길동",
  "email_address": "new-email@example.com",
  "new_password": "new-password"
}
```

Rules:

- 이름, 이메일, 비밀번호 중 하나 이상을 수정한다.
- 기존 정보와 동일한 내용으로 수정할 수 없다.
- `email_address`는 중복될 수 없다.

Response `200`: User

Errors:

| Status | 조건 |
|---:|---|
| 400 | 수정 필드 없음 |
| 400 | 기존 정보와 동일한 내용으로 수정 시도 |
| 401 | 인증되지 않음 |
| 409 | `email_address` 중복 |

Open Questions:

- 비밀번호 변경 시 현재 비밀번호 입력을 필수로 받을지 결정 필요

### A-08. 회원 탈퇴

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-4, UF-04, `users.status`, `users.deleted_at` |

Processing:

- 현재 회원의 `deleted_at`을 현재 시각으로 기록한다.
- 현재 회원의 `status`를 `DELETED`로 변경한다.
- 현재 회원의 서버 세션을 무효화한다.
- 탈퇴 회원이 작성한 게시글, 댓글, 개인 캘린더, 개인 일정은 비활성화 및 삭제 대기 상태로 전환한다.
- 탈퇴 회원은 가입되어 있던 모든 그룹에서 탈퇴 처리한다.
- 탈퇴 회원이 그룹장인 그룹은 탈퇴 전에 다른 그룹원에게 `LEADER` 권한을 자동 위임한다.
- 위임 대상은 탈퇴 회원을 제외하고 해당 그룹에 가장 먼저 가입한 그룹원이다.
- 탈퇴 회원이 유일한 그룹원인 그룹은 비활성화 및 삭제 대기 상태로 전환한다.
- 탈퇴 회원 데이터와 비활성화된 관련 데이터는 탈퇴 시점부터 6개월 동안 삭제 대기 상태로 보관한 후 삭제 대상이 된다.

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 401 | 인증되지 않음 |

Schema notes:

- 관련 데이터의 비활성화/삭제 대기 상태를 표현할 DB 스키마 보완이 필요하다.
- 그룹장 위임을 위해 그룹 가입 순서를 판단할 DB 스키마 보완이 필요하다.

## 6. 게시글 API

### P-01. 게시글 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-3, UF-05, SC-04, `post` |

Query parameters:

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `page` | integer | no | 페이지 번호. 기본값은 1 |
| `size` | integer | no | 페이지 크기 |
| `keyword` | string | no | 제목/내용 검색어 |
| `author` | string | no | 작성자 필터 |
| `main_category` | string | no | 대주제 필터 |
| `sub_category` | string | no | 소주제 필터 |

Rules:

- 기본 정렬은 작성일 기준 최신순이다.
- `keyword`, `author`, 주제 필터(`main_category`, `sub_category`)는 한 번에 하나만 사용할 수 있다.
- 주제 필터에서 `main_category`와 `sub_category`는 하나의 주제 필터 묶음으로 본다.
- 비활성화 또는 삭제 대기 상태의 게시글은 목록에 노출하지 않는다.

Response `200`:

```json
{
  "items": [
    {
      "id": 1,
      "user_id": 1,
      "title": "게시글 제목",
      "created_at": "2026-05-09T10:00:00Z",
      "is_updated": false,
      "view_count": 0,
      "is_reported": false,
      "main_category": "컴퓨터공학과",
      "sub_category": "데이터베이스",
      "like_count": 3,
      "liked_by_me": false
    }
  ],
  "page": 1,
  "size": 20,
  "total_count": 1,
  "total_pages": 1
}
```

Errors:

| Status | 조건 |
|---:|---|
| 400 | 둘 이상의 필터 종류를 동시에 사용 |
| 400 | 페이지 파라미터 오류 |

### P-02. 게시글 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts/{post_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-3, UF-05, SC-05, `post`, `file`, `comments` |

Response `200`: Post

Errors:

| Status | 조건 |
|---:|---|
| 404 | 게시글 없음 또는 비활성화/삭제 대기 게시글 |

Open Questions:

- 게시글 상세 조회 시 조회수 증가 조건 결정 필요

### P-03. 게시글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts` |
| 인증 | 인증 필요 |
| Content-Type | `multipart/form-data` |
| Trace | 요구사항 3-1, UF-06, SC-06, `post`, `file` |

Multipart fields:

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `title` | string | yes | 게시글 제목 |
| `content` | string | no | 게시글 본문 |
| `main_category` | string | yes | 대주제 |
| `sub_category` | string | yes | 소주제 |
| `files` | file[] | no | 직접 업로드할 첨부파일 |

Response `201`: Post

Client behavior:

- 작성 완료 후 화면은 게시판 첫 번째 페이지로 이동한다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |
| 400 | 파일 업로드 실패 |

### P-04. 게시글 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/posts/{post_id}` |
| 인증 | 인증 필요 |
| Content-Type | `multipart/form-data` 또는 JSON |
| Trace | 요구사항 3-2, UF-07, SC-06, `post.is_updated` |

Request fields:

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `title` | string | no | 수정 제목 |
| `content` | string | no | 수정 본문 |
| `main_category` | string | no | 수정 대주제 |
| `sub_category` | string | no | 수정 소주제 |
| `files` | file[] | no | 교체 또는 추가할 첨부파일 |

Response `200`: Post

Errors:

| Status | 조건 |
|---:|---|
| 400 | 수정 필드 없음 |
| 403 | 작성자가 아닌 회원의 수정 시도 |
| 404 | 게시글 없음 |

### P-05. 게시글 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/posts/{post_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-2, UF-07, SC-05 |

Response `204`: 없음

Client behavior:

- 삭제 완료 후 화면은 게시판 첫 번째 페이지로 이동한다.

Errors:

| Status | 조건 |
|---:|---|
| 403 | 작성자가 아닌 회원의 삭제 시도 |
| 404 | 게시글 없음 |

### P-06. 게시글 추천

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts/{post_id}/likes` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-4, SC-05, `likes` |

Response `201`: Like

Errors:

| Status | 조건 |
|---:|---|
| 404 | 게시글 없음 |
| 409 | 이미 추천한 게시글 |

### P-07. 게시글 추천 취소

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/posts/{post_id}/likes` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-4, SC-05, `likes` |

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 404 | 게시글 없음 |
| 404 | 추천하지 않은 게시글 |

## 7. 댓글 API

### C-01. 게시글 댓글 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts/{post_id}/comments` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4, SC-05, `comments` |

Response `200`:

```json
{
  "items": []
}
```

Visibility rule:

- 공개 댓글/대댓글은 조회 가능하다.
- 비공개 댓글/대댓글은 작성자, 부모 댓글 작성자, 원 게시글 작성자만 조회 가능하다.
- 비활성화 또는 삭제 대기 상태의 댓글/대댓글은 목록에 노출하지 않는다.

### C-02. 댓글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts/{post_id}/comments` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-1, UF-08, SC-05, `comments` |

Request body:

```json
{
  "content": "댓글 내용",
  "is_public": true
}
```

Response `201`: Comment

Errors:

| Status | 조건 |
|---:|---|
| 400 | 내용 누락 |
| 404 | 게시글 없음 |

### C-03. 대댓글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/comments/{comment_id}/replies` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-3, SC-05, `comments.parent_comment` |

Request body:

```json
{
  "content": "대댓글 내용",
  "is_public": true
}
```

Response `201`: Comment

Errors:

| Status | 조건 |
|---:|---|
| 400 | 내용 누락 |
| 400 | 대댓글에 다시 대댓글 작성 시도 |
| 404 | 부모 댓글 없음 |

Rules:

- 부모 댓글은 일반 댓글이어야 한다.
- 부모 댓글의 `parent_comment`가 NULL이 아니면 대댓글을 작성할 수 없다.

### C-04. 댓글 또는 대댓글 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/comments/{comment_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4, SC-05, `comments.is_updated` |

Request body:

```json
{
  "content": "수정된 댓글 내용",
  "is_public": true
}
```

Response `200`: Comment

Client behavior:

- 수정 완료 후 화면은 해당 게시글 상세 페이지 전체를 갱신한다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 수정 필드 없음 |
| 403 | 작성자가 아닌 회원의 수정 시도 |
| 404 | 댓글 없음 |

### C-05. 댓글 또는 대댓글 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/comments/{comment_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4, SC-05 |

Response `204`: 없음

Client behavior:

- 삭제 완료 후 화면은 해당 게시글 상세 페이지 전체를 갱신한다.

Errors:

| Status | 조건 |
|---:|---|
| 403 | 작성자가 아닌 회원의 삭제 시도 |
| 404 | 댓글 없음 |

## 8. 알림 API

### N-01. 내 알림 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/notifications` |
| 인증 | 인증 필요 |
| Trace | 요구사항 2-2, SC-05, `notification` |

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
| Trace | 요구사항 2-3, `notification.is_read` |

Processing:

- 클라이언트는 알림 클릭 후 해당 알림이 발생한 게시글 또는 댓글 위치에 정상적으로 도달했을 때 이 API를 호출한다.

Response `200`: Notification

Errors:

| Status | 조건 |
|---:|---|
| 403 | 다른 회원의 알림 접근 |
| 404 | 알림 없음 |

## 9. 개인 일정 API

### S-01. 개인 일정 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/me/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, UF-09, SC-08, `schedules` |

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
- 비활성화 또는 삭제 대기 상태의 개인 일정은 조회하지 않는다.

### S-02. 개인 일정 등록

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/me/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-2, UF-09, SC-09, `schedules` |

Request body:

```json
{
  "title": "일정 제목",
  "start_at": "2026-05-09T09:00:00Z",
  "end_at": "2026-05-09T10:00:00Z",
  "description": "메모",
  "type": 1
}
```

Response `201`: Schedule

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |
| 400 | `end_at`이 `start_at`보다 빠른 경우 |

### S-03. 개인 일정 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/me/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, SC-09, `schedules` |

Request body:

```json
{
  "title": "수정 일정",
  "start_at": "2026-05-09T09:00:00Z",
  "end_at": "2026-05-09T10:00:00Z",
  "description": "수정 메모",
  "type": 1
}
```

Response `200`: Schedule

Errors:

| Status | 조건 |
|---:|---|
| 400 | `end_at`이 `start_at`보다 빠른 경우 |
| 403 | 다른 회원의 개인 일정 접근 |
| 404 | 일정 없음 |

### S-04. 개인 일정 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/me/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, SC-09 |

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
| Trace | 요구사항 6-1, UF-10, SC-10, `groups`, `group_members` |

Response `200`:

```json
{
  "items": []
}
```

Rules:

- 현재 회원이 속한 그룹만 조회한다.
- 비활성화 또는 삭제 대기 상태의 그룹은 조회하지 않는다.

### G-02. 그룹 생성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups` |
| 인증 | 인증 필요 |
| Trace | 요구사항 6-2, UF-10, SC-10, `groups`, `group_members` |

Request body:

```json
{
  "name": "스터디 그룹"
}
```

Response `201`:

```json
{
  "group": {
    "id": 1,
    "group_link": "invite-link",
    "name": "스터디 그룹",
    "creator_id": 1,
    "created_at": "2026-05-09T10:00:00Z"
  },
  "membership": {
    "group_id": 1,
    "user_id": 1,
    "role": "LEADER"
  }
}
```

Processing:

- 그룹 생성자는 그룹의 `creator_id`가 된다.
- 그룹 생성자는 그룹 생성과 동시에 `group_members`에 자동 등록된다.
- 그룹 생성자의 역할은 `LEADER`로 설정한다.
- 생성된 그룹의 `group_link`를 유지한다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 그룹명 누락 |

### G-03. 그룹 가입

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups/join` |
| 인증 | 인증 필요 |
| Trace | 요구사항 6-1, UF-10, SC-10, `group_members` |

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
| Trace | UF-10, SC-11, `groups`, `group_members` |

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
| 404 | 그룹 없음 또는 비활성화/삭제 대기 그룹 |

## 11. 그룹 일정 API

### GS-01. 그룹 일정 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/groups/{group_id}/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-10, SC-12, `schedules` |

Response `200`:

```json
{
  "items": []
}
```

Authorization:

- 해당 그룹의 그룹원만 조회할 수 있다.
- 타 그룹 캘린더의 그룹 일정은 조회할 수 없다.

### GS-02. 그룹 일정 등록

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups/{group_id}/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, SC-12, `schedules` |

Request body:

```json
{
  "title": "그룹 일정",
  "start_at": "2026-05-09T09:00:00Z",
  "end_at": "2026-05-09T10:00:00Z",
  "description": "메모",
  "type": 1
}
```

Response `201`: Schedule

Authorization:

- 모든 그룹원은 자신이 속한 그룹 캘린더에 그룹 일정을 등록할 수 있다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |
| 400 | `end_at`이 `start_at`보다 빠른 경우 |
| 403 | 그룹원이 아닌 회원의 접근 |
| 404 | 그룹 없음 |

### GS-03. 그룹 일정 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/groups/{group_id}/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-10, SC-12, `schedules` |

Request body:

```json
{
  "title": "수정 그룹 일정",
  "start_at": "2026-05-09T09:00:00Z",
  "end_at": "2026-05-09T10:00:00Z",
  "description": "수정 메모",
  "type": 1
}
```

Response `200`: Schedule

Authorization:

- 모든 그룹원은 자신이 속한 그룹 캘린더의 그룹 일정을 수정할 수 있다.
- 타 그룹 캘린더의 그룹 일정은 수정할 수 없다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | `end_at`이 `start_at`보다 빠른 경우 |
| 403 | 그룹원이 아닌 회원의 접근 |
| 404 | 그룹 또는 일정 없음 |

### GS-04. 그룹 일정 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/groups/{group_id}/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, SC-12, `schedules` |

Response `204`: 없음

Authorization:

- 모든 그룹원은 자신이 속한 그룹 캘린더의 그룹 일정을 삭제할 수 있다.
- 타 그룹 캘린더의 그룹 일정은 삭제할 수 없다.

Errors:

| Status | 조건 |
|---:|---|
| 403 | 그룹원이 아닌 회원의 접근 |
| 404 | 그룹 또는 일정 없음 |

## 12. 제외 API

### X-01. 그룹 채팅

| 항목 | 내용 |
|---|---|
| 상태 | 구현 제외 |
| Trace | 요구사항 6-3, SC-13 |

그룹 채팅은 최신 요구사항에서 구현하지 않는다고 확정되었으므로 채팅 메시지 조회, 전송, 수정, 삭제 API를 제공하지 않는다.

## 13. Open Questions

| ID | 관련 API | 결정 필요 사항 |
|---|---|---|
| OQ-01 | 공통 | 공통 에러 응답 body 형식 |
| OQ-02 | A-04 | 아이디 찾기 성공 시 아이디 전체 노출 또는 마스킹 여부 |
| OQ-03 | A-05 | 비밀번호 재설정 화면에서 사용할 검증 토큰 또는 임시 세션 방식 |
| OQ-04 | A-07 | 비밀번호 변경 시 현재 비밀번호 입력 필요 여부 |
| OQ-05 | A-08 | 탈퇴 관련 데이터의 비활성화/삭제 대기 상태를 저장할 DB 컬럼과 상태값 |
| OQ-06 | A-08 | 그룹장 자동 위임 기준을 구현하기 위한 그룹 가입 순서 저장 방식 |
| OQ-07 | P-02 | 게시글 상세 조회 시 조회수 증가 조건 |
