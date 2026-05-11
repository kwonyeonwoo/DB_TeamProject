# API Spec

## 1. 작성 기준

- 요구사항: `docs/source/requirements.md`
- 화면 설계: `docs/source/screen-design.md`
- 유저 플로우: `docs/source/user-flow.md`
- 논리 스키마: `docs/source/logical-schema.md`
- ERD: `docs/source/erd.md`
- 물리 스키마: `docs/source/physical-schema.md`
- DBML: `docs/source/dbml.md`

이 문서는 최신 요구사항 명세서를 우선 기준으로 한 API 계약 문서다. 요구사항에서 결정된 정책은 API 계약에 반영하고, 요구사항만으로 세부 API 형태를 확정할 수 없는 항목은 `Open Questions`로 남긴다.

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
| 에러 응답 형식 | `code`, `message` 필수. `details` 선택 |

Authentication:

- 로그인 성공 시 서버는 인증 세션을 생성한다.
- 인증 필요 API는 유효한 서버 세션이 있어야 호출할 수 있다.
- 로그아웃 시 서버에 저장된 사용자 세션을 무효화한다.
- 세션 무효화 이후 해당 세션으로는 인증이 필요한 기능을 사용할 수 없다.

Authorization:

- 회원 가입으로 생성되는 회원의 `role`은 `USER`다.
- 회원 가입 요청 또는 일반 API 호출로 `ADMIN` 권한을 부여할 수 없다.
- `ADMIN` 권한은 DB seed 데이터 또는 운영자의 직접 DB 변경으로만 부여한다.
- 관리자 권한 부여 또는 회수 API는 제공하지 않는다.

Lifecycle:

- 탈퇴 회원은 `users.status = DELETED`, `users.deleted_at = now()`로 처리한다.
- 탈퇴 처리 시점에는 회원 row와 개인정보성 컬럼을 유지하고, `deleted_at`으로부터 6개월이 지나지 않은 탈퇴 회원은 정보 삭제 대기 중인 회원으로 본다.
- `deleted_at`으로부터 6개월이 지난 탈퇴 회원은 정책에 따라 개인정보 삭제 대상으로 처리하며, `login_id`, `password`, `name`, `email_address` 등 개인정보성 컬럼은 NULL 값으로 변경한다. NULL 값이 들어갈 수 없는 속성의 경우 식별할 수 없는 값으로 변경한다.
- 탈퇴 회원이 작성한 게시글, 댓글, 대댓글은 삭제하지 않고 유지한다.
- 탈퇴 회원 작성물은 일반 조회, 검색, 페이지네이션 대상에 포함한다.
- 탈퇴 회원 작성물의 작성자명은 `탈퇴한 유저`로 표시하며, 익명 표시보다 우선한다.
- 탈퇴 회원의 개인 일정은 `schedules.status = DELETED`, `schedules.deleted_at = now()`로 비활성화 및 삭제 대기 상태를 표현하며 일반 조회에 노출하지 않는다.
- 탈퇴 회원은 가입되어 있던 모든 그룹에서 탈퇴 처리한다.
- 탈퇴 회원이 그룹장인 그룹은 탈퇴 전에 가장 먼저 가입한 다른 그룹원에게 `LEADER` 권한을 자동 위임한다.
- 탈퇴 회원이 유일한 그룹원인 그룹은 `groups.status = INACTIVE`, `groups.deleted_at = now()`로 비활성화 및 삭제 대기 상태를 표현하며 일반 조회에 노출하지 않는다.
- 삭제 예정일 컬럼은 사용하지 않는다.

Display:

- 익명 게시글, 댓글, 대댓글은 작성자 이름 대신 `익명_숫자`로 표시한다.
- `익명_숫자`는 하나의 게시글 상세 화면 기준으로 부여하며, 동일 게시글 내 동일 작성자는 항상 같은 번호로 표시한다.
- 작성자 표시명은 API 응답에서 `author_display_name`으로 내려줄 수 있다.

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

공통 에러 응답:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "필수 입력값이 누락되었습니다."
}
```

Rules:

- 실패 응답 body는 `code`, `message`를 필수 필드로 사용한다.
- `details`는 기본 응답 필드로 사용하지 않으며, 입력값별 상세 오류가 필요한 경우에만 선택값으로 포함할 수 있다.
- `details`를 포함하는 경우에도 `code`, `message`는 항상 포함한다.

## 4. 리소스 필드

### 4-1. User

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 회원 식별자 |
| `login_id` | string, nullable | 로그인 아이디. 탈퇴 후 NULL 가능 |
| `name` | string, nullable | 이름. 탈퇴 후 NULL 또는 식별 불가 값 가능 |
| `email_address` | string, nullable | 이메일. 탈퇴 후 NULL 가능 |
| `created_at` | string | 가입 일시 |
| `deleted_at` | string, nullable | 회원 탈퇴 일시 |
| `status` | string | `ACTIVE`, `DELETED` |
| `role` | string | `USER`, `ADMIN` |

`password`는 응답에 포함하지 않는다.

### 4-2. Post

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 게시글 식별자 |
| `user_id` | integer | 작성자 id |
| `author_display_name` | string | 화면 표시용 작성자명 |
| `title` | string | 제목 |
| `content` | string, nullable | 본문 |
| `created_at` | string | 작성 일시 |
| `updated_at` | string, nullable | 수정 일시. NULL이 아니면 수정된 게시글 |
| `deleted_at` | string, nullable | 삭제 일시 |
| `status` | string | `ACTIVE`, `DELETED` |
| `view_count` | integer | 조회수 |
| `main_category` | string | 대주제, 학과 |
| `sub_category` | string | 소주제, 과목 |
| `is_anonymous` | boolean | 익명 작성 여부 |
| `files` | File[] | 첨부파일 목록 |
| `liked_by_me` | boolean | 현재 로그인 회원의 추천 여부 |
| `like_count` | integer | 추천 수 |

### 4-3. File

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 게시글 id |
| `file_url` | string | 업로드된 첨부파일의 저장 위치 |

### 4-4. Comment

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 댓글 식별자 |
| `user_id` | integer | 작성자 id |
| `author_display_name` | string | 화면 표시용 작성자명 |
| `post_id` | integer | 게시글 id |
| `parent_comment` | integer, nullable | 부모 댓글 id. NULL이면 일반 댓글 |
| `content` | string | 댓글 또는 대댓글 내용 |
| `is_anonymous` | boolean | 익명 작성 여부 |
| `created_at` | string | 작성 일시 |
| `updated_at` | string, nullable | 수정 일시. NULL이 아니면 수정된 댓글 |
| `deleted_at` | string, nullable | 삭제 일시 |
| `status` | string | `ACTIVE`, `DELETED` |

대댓글에는 다시 대댓글을 작성할 수 없다. 대댓글 작성 API의 부모 댓글은 `parent_comment = null`인 일반 댓글이어야 한다.

### 4-5. Like

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 추천 식별자 |
| `user_id` | integer | 추천한 회원 id |
| `post_id` | integer | 추천받은 게시글 id |
| `created_at` | string | 추천 일시 |

### 4-6. Report

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 신고 식별자 |
| `reporter_id` | integer | 신고한 회원 id |
| `target_type` | string | `POST`, `COMMENT` |
| `target_id` | integer | 신고 대상 id. `target_type`에 따라 게시글 id 또는 댓글/대댓글 id |
| `reason_type` | integer | 신고 사유. `1`: 부적절한 내용, `2`: 광고/도배, `3`: 저작권 침해, `4`: 기타 |
| `created_at` | string | 신고 시각 |
| `status` | string | 신고 처리 상태. `PENDING`, `PROCESSED` |
| `processed_by` | integer, nullable | 신고를 처리한 관리자 회원 id |
| `processed_at` | string, nullable | 신고 처리 시각 |

### 4-7. Notification

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 알림 식별자 |
| `is_read` | boolean | 읽음 여부 |
| `comment_content` | string | 알림에 표시할 댓글 내용 |
| `commented_post_id` | integer | 댓글이 달린 게시글 id |
| `commented_user_id` | integer | 알림 수신자 id |
| `commented_id` | integer, nullable | 댓글 알림이면 NULL, 대댓글 알림이면 부모 댓글 id |
| `created_at` | string | 알림 생성 일시 |

### 4-8. Group

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 그룹 식별자 |
| `group_code` | string | 그룹 가입 코드 |
| `name` | string | 그룹명 |
| `creator_id` | integer | 생성자 id |
| `created_at` | string | 생성 일시 |
| `deleted_at` | string, nullable | 그룹 삭제 또는 비활성화 일시 |
| `status` | string | `ACTIVE`, `INACTIVE`, `DELETED` |

그룹 채팅은 요구사항상 구현하지 않으므로 채팅 리소스와 API를 제공하지 않는다.

### 4-9. GroupMember

| 필드 | 타입 | 설명 |
|---|---|---|
| `group_id` | integer | 그룹 id |
| `user_id` | integer | 회원 id |
| `role` | string | `LEADER`, `MEMBER` |
| `joined_at` | string | 그룹 가입 일시. 그룹장 자동 위임 기준 |

### 4-10. Schedule

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | integer | 일정 식별자 |
| `user_id` | integer | 일정 작성자 또는 소유자 id |
| `group_id` | integer, nullable | 그룹 id. NULL이면 개인 일정 |
| `title` | string | 일정 제목 |
| `start_at` | string | 시작 일시 |
| `end_at` | string | 종료 일시 |
| `description` | string, nullable | 메모 또는 설명 |
| `type` | integer | 일정 종류. `1`: 수업, `2`: 과제, `3`: 시험, `4`: 스터디, `5`: 기타 |
| `created_at` | string | 생성 일시 |
| `updated_at` | string, nullable | 수정 일시 |
| `deleted_at` | string, nullable | 삭제 일시 |
| `status` | string | `ACTIVE`, `DELETED` |

## 5. 인증 및 회원 API

### A-01. 회원 가입

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/auth/signup` |
| 인증 | 불필요 |
| Trace | 요구사항 1-1, UF-02, SC-02, `users` |

Request body:

```json
{
  "login_id": "student01",
  "password": "password",
  "name": "홍길동",
  "email_address": "student@example.com"
}
```

Response `201`: User

Rules:

- `login_id`, `password`, `name`, `email_address`는 필수다.
- `login_id`, `email_address`는 중복될 수 없다.
- 탈퇴 회원의 NULL 처리된 `login_id`, `email_address`는 중복 판단 대상에서 제외한다.
- 회원 가입으로 생성되는 회원의 `role`은 `USER`다.
- 회원 가입 request body는 `role`을 받지 않는다.
- `ADMIN` 권한은 회원 가입/API로 부여하지 않고 DB seed 데이터 또는 운영자의 직접 DB 변경으로만 부여한다.

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
| Trace | 요구사항 1-2, UF-03, SC-01 |

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
    "status": "ACTIVE",
    "role": "USER"
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
| Trace | 요구사항 1-2, UF-03, SC-06 |

Response `204`: 없음

Processing:

- 서버에 저장된 현재 사용자 세션을 무효화한다.
- 세션 무효화 후 인증 필요 API는 사용할 수 없다.

### A-04. 내 정보 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | UF-05, SC-11, `users` |

Response `200`: User

### A-05. 내 정보 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-3, UF-05, SC-11 |

Request body:

```json
{
  "name": "홍길동",
  "email_address": "new-email@example.com",
  "current_password": "current-password",
  "new_password": "new-password"
}
```

Rules:

- 이름, 이메일, 비밀번호 중 하나 이상을 수정한다.
- 기존 정보와 동일한 내용으로 수정할 수 없다.
- `email_address`는 중복될 수 없다.
- 비밀번호를 변경하는 경우 `current_password`를 필수로 입력한다.
- 서버는 `current_password`가 기존 비밀번호와 일치하는 경우에만 새 비밀번호로 변경한다.

Response `200`: User

Errors:

| Status | 조건 |
|---:|---|
| 400 | 수정 필드 없음 |
| 400 | 기존 정보와 동일한 내용으로 수정 시도 |
| 400 | 비밀번호 변경 시 `current_password` 누락 |
| 401 | 인증되지 않음 |
| 403 | `current_password` 불일치 |
| 409 | `email_address` 중복 |

### A-06. 회원 탈퇴

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/users/me` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-4, UF-06, SC-11, `users.status`, `users.deleted_at` |

Processing:

- 현재 회원의 `deleted_at`을 현재 시각으로 기록한다.
- 현재 회원의 `status`를 `DELETED`로 변경한다.
- 현재 회원의 개인정보성 컬럼은 탈퇴 처리 시점에 즉시 변경하지 않는다.
- `deleted_at`으로부터 6개월이 지난 탈퇴 회원은 정책에 따라 개인정보 삭제 대상으로 처리하며, `login_id`, `password`, `name`, `email_address` 등 개인정보성 컬럼은 NULL 값으로 변경한다.
- 현재 회원의 서버 세션을 무효화한다.
- 탈퇴 회원이 작성한 게시글, 댓글, 대댓글은 삭제하지 않는다.
- 탈퇴 회원의 개인 일정은 `schedules.status = DELETED`, `schedules.deleted_at = now()`로 전환하고 일반 조회에 노출하지 않는다.
- 탈퇴 회원은 가입되어 있던 모든 그룹에서 탈퇴 처리한다.
- 탈퇴 회원이 그룹장인 그룹은 탈퇴 전에 다른 그룹원에게 `LEADER` 권한을 자동 위임한다.
- 위임 대상은 탈퇴 회원을 제외하고 해당 그룹에 가장 먼저 가입한 그룹원이다.
- 탈퇴 회원이 유일한 그룹원인 그룹은 `groups.status = INACTIVE`, `groups.deleted_at = now()`로 전환하고 일반 조회에 노출하지 않는다.

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 401 | 인증되지 않음 |

## 6. 게시글 API

### P-01. 게시글 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts` |
| 인증 | 인증 필요 |
| Trace | 요구사항 3-3, UF-08, SC-08, `post` |

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
- `post.status = ACTIVE`인 게시글만 일반 목록에 노출한다.
- 탈퇴 회원의 게시글은 유지되며 목록, 검색, 페이지네이션 대상에 포함한다.
- 작성자 필터 결과에서는 탈퇴한 유저의 게시글과 익명 게시글을 제외한다.

Response `200`:

```json
{
  "items": [
    {
      "id": 1,
      "user_id": 1,
      "author_display_name": "익명_1",
      "title": "게시글 제목",
      "created_at": "2026-05-09T10:00:00Z",
      "updated_at": null,
      "status": "ACTIVE",
      "view_count": 0,
      "main_category": "컴퓨터공학과",
      "sub_category": "데이터베이스",
      "is_anonymous": true,
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
| Trace | 요구사항 3-3, UF-10, SC-09, `post`, `file`, `comments` |

Processing:

- 게시글 상세 화면 접근 시 조회수를 증가시킨다.

Response `200`: Post

Errors:

| Status | 조건 |
|---:|---|
| 404 | 게시글 없음 또는 삭제된 게시글 |

### P-03. 게시글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts` |
| 인증 | 인증 필요 |
| Content-Type | `multipart/form-data` |
| Trace | 요구사항 3-1, UF-09, SC-10, `post`, `file` |

Multipart fields:

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `title` | string | yes | 게시글 제목 |
| `content` | string | no | 게시글 본문 |
| `main_category` | string | yes | 대주제 |
| `sub_category` | string | yes | 소주제 |
| `is_anonymous` | boolean | yes | 익명 작성 여부 |
| `files` | file[] | no | 직접 업로드할 첨부파일 |

Response `201`: Post

Processing:

- 업로드된 실제 파일은 서버 로컬 경로에 저장한다.
- 파일 저장 경로와 DB에 저장되는 `file_url`은 `/uploads/posts/{post_id}/...` 형식을 기본으로 한다.
- DB에는 `file_url`만 저장하며, 파일명, 원본 파일명, 파일 크기, MIME 타입, 확장자 등 별도 파일 메타데이터는 저장하지 않는다.
- 파일 크기와 확장자 제한 정책은 최소 구현 범위에서 별도 요구사항으로 두지 않는다.

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
| Trace | 요구사항 3-2, UF-10, SC-10, `post.updated_at` |

Request fields:

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `title` | string | no | 수정 제목 |
| `content` | string | no | 수정 본문 |
| `main_category` | string | no | 수정 대주제 |
| `sub_category` | string | no | 수정 소주제 |
| `is_anonymous` | boolean | no | 익명 작성 여부 |
| `files` | file[] | no | 새로 업로드할 첨부파일 목록 |

Processing:

- 수정 성공 시 `updated_at`을 현재 시각으로 기록한다.
- 새 파일이 업로드되면 기존 첨부파일 목록을 전체 교체한다.
- 새 파일이 업로드되지 않으면 기존 첨부파일 목록을 유지한다.
- DB에는 업로드된 파일의 `file_url`만 저장하며 별도 파일 메타데이터는 저장하지 않는다.

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
| Trace | 요구사항 3-2, UF-10, SC-09, `post.status`, `post.deleted_at` |

Processing:

- 삭제 성공 시 `post.status = DELETED`, `post.deleted_at = now()`로 처리한다.

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
| Trace | 요구사항 3-4, UF-11, SC-09, `likes` |

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
| Trace | 요구사항 3-4, UF-11, SC-09, `likes` |

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 404 | 게시글 없음 |
| 404 | 추천하지 않은 게시글 |

## 7. 신고 API

### R-01. 신고 생성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/reports` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-5, 3-5, UF-11A, SC-09, `report` |

Request body:

```json
{
  "target_type": "POST",
  "target_id": 1,
  "reason_type": 1
}
```

Rules:

- 일반 사용자(`role = USER`)는 게시글 또는 댓글을 신고할 수 있다.
- `target_type`은 `POST`, `COMMENT` 중 하나다. `COMMENT`는 댓글과 대댓글이 저장된 `comments` 대상을 의미한다.
- `target_id`는 `target_type`에 따라 게시글 id 또는 댓글 id를 의미한다.
- `reason_type`은 `1`, `2`, `3`, `4` 중 하나다.
- 동일 회원은 동일 신고 대상에 대해 한 번만 신고할 수 있다.
- 신고 생성 시 `status = PENDING`, `processed_by = null`, `processed_at = null`로 저장한다.

Response `201`: Report

Errors:

| Status | 조건 |
|---:|---|
| 400 | 필수 입력값 누락 |
| 400 | 유효하지 않은 `target_type` 또는 `reason_type` |
| 403 | 신고 권한 없음 |
| 404 | 신고 대상 없음 |
| 409 | 이미 신고한 대상 |

### R-02. 관리자 신고 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/admin/reports` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-5, 3-5, UF-18, SC-18, `report` |

Authorization:

- 관리자(`role = ADMIN`)만 신고 목록을 조회할 수 있다.

Response `200`:

```json
{
  "items": [
    {
      "id": 1,
      "reporter_id": 2,
      "target_type": "POST",
      "target_id": 10,
      "reason_type": 1,
      "created_at": "2026-05-09T10:00:00Z",
      "status": "PENDING",
      "processed_by": null,
      "processed_at": null
    }
  ]
}
```

Rules:

- 관리자 신고 목록에는 신고 대상, 신고자, 신고 사유, 신고 시각, 신고 처리 상태, 처리자, 처리 시각을 표시할 수 있도록 `target_type`, `target_id`, `reporter_id`, `reason_type`, `created_at`, `status`, `processed_by`, `processed_at`을 반환한다.

Errors:

| Status | 조건 |
|---:|---|
| 403 | 관리자 권한 없음 |

### R-03. 관리자 신고 처리

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/admin/reports/{report_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 1-5, 3-5, UF-18, SC-18, `report.status`, `report.processed_by`, `report.processed_at` |

Authorization:

- 관리자(`role = ADMIN`)만 신고 처리 API를 호출할 수 있다.

Request body:

```json
{
  "status": "PROCESSED"
}
```

Processing:

- 신고 처리 API는 신고 대상 게시글 또는 댓글 삭제를 자동 수행하지 않는다.
- 신고 처리 API는 신고 상태만 변경하며, 처리 이력으로 `processed_by`와 `processed_at`을 함께 기록한다.
- 처리 성공 시 `report.status = PROCESSED`, `report.processed_by = 현재 관리자 id`, `report.processed_at = now()`로 저장한다.

Response `200`: Report

Errors:

| Status | 조건 |
|---:|---|
| 400 | 유효하지 않은 `status` |
| 403 | 관리자 권한 없음 |
| 404 | 신고 없음 |

## 8. 댓글 API

### C-01. 게시글 댓글 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/posts/{post_id}/comments` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4, UF-12, UF-13, SC-09, `comments` |

Response `200`:

```json
{
  "items": []
}
```

Rules:

- `comments.status = ACTIVE`인 댓글과 대댓글만 일반 목록에 노출한다.
- 탈퇴 회원이 작성한 댓글과 대댓글은 삭제하지 않고 유지하며, 작성자명은 `탈퇴한 유저`로 표시한다.
- 익명 댓글과 대댓글은 작성자명 대신 `익명_숫자`로 표시한다.

### C-02. 댓글 작성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/posts/{post_id}/comments` |
| 인증 | 인증 필요 |
| Trace | 요구사항 2-1, 4-1, UF-12, SC-09, `comments`, `notification` |

Request body:

```json
{
  "content": "댓글 내용",
  "is_anonymous": false
}
```

Response `201`: Comment

Processing:

- 댓글 작성자가 게시글 작성자와 다른 회원이면 게시글 작성자에게 댓글 알림을 생성한다.
- 댓글 작성자가 게시글 작성자와 같은 회원이면 자기 자신의 게시글에 댓글을 작성한 경우이므로 댓글 알림을 생성하지 않는다.
- 댓글 알림의 `commented_post_id`는 댓글이 작성된 게시글 id이고, `commented_id`는 NULL이다.
- 댓글 알림의 `commented_user_id`는 게시글 작성자 id다.

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
| Trace | 요구사항 2-1, 4-3, UF-13, SC-09, `comments.parent_comment`, `notification` |

Request body:

```json
{
  "content": "대댓글 내용",
  "is_anonymous": false
}
```

Response `201`: Comment

Rules:

- 부모 댓글은 일반 댓글이어야 한다.
- 부모 댓글의 `parent_comment`가 NULL이 아니면 대댓글을 작성할 수 없다.

Processing:

- 대댓글 작성자가 부모 댓글 작성자와 다른 회원이면 부모 댓글 작성자에게 대댓글 알림을 생성한다.
- 대댓글 작성자가 부모 댓글 작성자와 같은 회원이면 자기 자신의 댓글에 대댓글을 작성한 경우이므로 댓글 알림을 생성하지 않는다.
- 대댓글 알림의 `commented_post_id`는 부모 댓글이 속한 게시글 id다.
- 대댓글 알림의 `commented_id`는 대댓글이 달린 부모 댓글 id다.
- 대댓글 알림의 `commented_user_id`는 부모 댓글 작성자 id다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 내용 누락 |
| 400 | 대댓글에 다시 대댓글 작성 시도 |
| 404 | 부모 댓글 없음 |

### C-04. 댓글 또는 대댓글 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/comments/{comment_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 4-2, 4-4, UF-12, UF-13, SC-09, `comments.updated_at` |

Request body:

```json
{
  "content": "수정된 댓글 내용",
  "is_anonymous": false
}
```

Processing:

- 수정 성공 시 `updated_at`을 현재 시각으로 기록한다.

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
| Trace | 요구사항 4-2, 4-4, UF-12, UF-13, SC-09, `comments.status`, `comments.deleted_at` |

Processing:

- 삭제 성공 시 `comments.status = DELETED`, `comments.deleted_at = now()`로 처리한다.

Response `204`: 없음

Client behavior:

- 삭제 완료 후 화면은 해당 게시글 상세 페이지 전체를 갱신한다.

Errors:

| Status | 조건 |
|---:|---|
| 403 | 작성자가 아닌 회원의 삭제 시도 |
| 404 | 댓글 없음 |

## 9. 알림 API

### N-01. 내 알림 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/notifications` |
| 인증 | 인증 필요 |
| Trace | 요구사항 2-2, 2-3, UF-07, SC-07, `notification` |

Response `200`:

```json
{
  "items": []
}
```

Authorization:

- 인증된 회원은 `commented_user_id`가 본인 id인 알림만 조회할 수 있다.

Processing:

- 본인 알림 목록을 팝업 형태로 조회했을 때, 반환 대상 알림을 읽음 처리한다.

Navigation:

- 댓글 알림은 `commented_post_id`가 가리키는 게시글 상세 화면으로 이동한다.
- 댓글 알림의 `commented_id`는 NULL이다.
- 대댓글 알림은 `commented_post_id`가 가리키는 게시글 안의 부모 댓글 위치로 이동한다.
- 대댓글 알림의 `commented_id`는 대댓글이 달린 부모 댓글 id다.

Errors:

| Status | 조건 |
|---:|---|
| 403 | 다른 회원의 알림 접근 |

## 10. 개인 일정 API

### S-01. 개인 일정 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/me/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, UF-15, SC-12, `schedules` |

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
- `schedules.status = ACTIVE`인 개인 일정만 일반 조회에 노출한다.

### S-02. 개인 일정 등록

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/me/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-2, UF-15, SC-13, `schedules` |

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
| 400 | 유효하지 않은 `type` |
| 400 | `end_at`이 `start_at`보다 빠른 경우 |

### S-03. 개인 일정 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/me/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, UF-15, SC-13, `schedules` |

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

Processing:

- 수정 성공 시 `updated_at`을 현재 시각으로 기록한다.

Response `200`: Schedule

Errors:

| Status | 조건 |
|---:|---|
| 400 | 유효하지 않은 `type` |
| 400 | `end_at`이 `start_at`보다 빠른 경우 |
| 403 | 다른 회원의 개인 일정 접근 |
| 404 | 일정 없음 |

### S-04. 개인 일정 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/me/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 5-3, UF-15, SC-13, `schedules.status`, `schedules.deleted_at` |

Processing:

- 삭제 성공 시 `schedules.status = DELETED`, `schedules.deleted_at = now()`로 처리한다.

Response `204`: 없음

Errors:

| Status | 조건 |
|---:|---|
| 403 | 다른 회원의 개인 일정 접근 |
| 404 | 일정 없음 |

## 11. 그룹 API

### G-01. 내 그룹 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/groups` |
| 인증 | 인증 필요 |
| Trace | 요구사항 6-1, UF-16, SC-14, `groups`, `group_members` |

Response `200`:

```json
{
  "items": []
}
```

Rules:

- 현재 회원이 속한 그룹만 조회한다.
- `groups.status = ACTIVE`인 그룹만 일반 목록에 노출한다.

### G-02. 그룹 생성

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups` |
| 인증 | 인증 필요 |
| Trace | 요구사항 6-2, UF-16, SC-14, `groups`, `group_members` |

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
    "group_code": "ABC123",
    "name": "스터디 그룹",
    "creator_id": 1,
    "created_at": "2026-05-09T10:00:00Z",
    "status": "ACTIVE"
  },
  "membership": {
    "group_id": 1,
    "user_id": 1,
    "role": "LEADER",
    "joined_at": "2026-05-09T10:00:00Z"
  }
}
```

Processing:

- 그룹 생성자는 그룹의 `creator_id`가 된다.
- `creator_id`는 최초 생성자를 기록하며, 현재 그룹장은 `group_members.role = LEADER`로 판단한다.
- 그룹 생성자는 그룹 생성과 동시에 `group_members`에 자동 등록된다.
- 그룹 생성자의 역할은 `LEADER`로 설정한다.
- 생성된 그룹의 `group_code`를 유지한다.
- 생성된 `group_code`는 화면에 보여준다.
- 화면에서 `group_link`라고 표현하는 값은 가입자가 입력하는 그룹 가입 코드와 같은 값이다.
- 복잡한 초대 링크, 외부 공유 기능, 만료 시간, 재발급 기능은 제공하지 않는다.

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
| Trace | 요구사항 6-1, 6-2, UF-16, SC-14, `groups.group_code`, `group_members` |

Request body:

```json
{
  "group_code": "ABC123"
}
```

Response `201`: GroupMember

Rules:

- 회원은 그룹 가입 코드를 입력하여 그룹에 가입한다.
- 가입은 단순 코드 입력 방식으로 처리한다.
- 같은 그룹에 이미 가입한 회원은 중복 가입할 수 없다.

Errors:

| Status | 조건 |
|---:|---|
| 404 | 유효하지 않은 그룹 가입 코드 |
| 409 | 이미 가입한 그룹 |

### G-04. 그룹 상세 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/groups/{group_id}` |
| 인증 | 인증 필요 |
| Trace | UF-16, SC-15, `groups`, `group_members` |

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
| 404 | 그룹 없음 또는 비활성화/삭제된 그룹 |

## 12. 그룹 일정 API

### GS-01. 그룹 일정 목록 조회

| 항목 | 내용 |
|---|---|
| Method | `GET` |
| Path | `/api/groups/{group_id}/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-17, SC-16, `schedules` |

Response `200`:

```json
{
  "items": []
}
```

Authorization:

- 해당 그룹의 그룹원만 조회할 수 있다.
- 타 그룹 캘린더의 그룹 일정은 조회할 수 없다.
- `schedules.status = ACTIVE`인 그룹 일정만 일반 조회에 노출한다.

### GS-02. 그룹 일정 등록

| 항목 | 내용 |
|---|---|
| Method | `POST` |
| Path | `/api/groups/{group_id}/schedules` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-17, SC-16, `schedules` |

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
| 400 | 유효하지 않은 `type` |
| 400 | `end_at`이 `start_at`보다 빠른 경우 |
| 403 | 그룹원이 아닌 회원의 접근 |
| 404 | 그룹 없음 |

### GS-03. 그룹 일정 수정

| 항목 | 내용 |
|---|---|
| Method | `PATCH` |
| Path | `/api/groups/{group_id}/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-17, SC-16, `schedules` |

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

Processing:

- 수정 성공 시 `updated_at`을 현재 시각으로 기록한다.

Response `200`: Schedule

Authorization:

- 모든 그룹원은 자신이 속한 그룹 캘린더의 그룹 일정을 수정할 수 있다.
- 타 그룹 캘린더의 그룹 일정은 수정할 수 없다.

Errors:

| Status | 조건 |
|---:|---|
| 400 | 유효하지 않은 `type` |
| 400 | `end_at`이 `start_at`보다 빠른 경우 |
| 403 | 그룹원이 아닌 회원의 접근 |
| 404 | 그룹 또는 일정 없음 |

### GS-04. 그룹 일정 삭제

| 항목 | 내용 |
|---|---|
| Method | `DELETE` |
| Path | `/api/groups/{group_id}/schedules/{schedule_id}` |
| 인증 | 인증 필요 |
| Trace | 요구사항 7-2, UF-17, SC-16, `schedules.status`, `schedules.deleted_at` |

Processing:

- 삭제 성공 시 `schedules.status = DELETED`, `schedules.deleted_at = now()`로 처리한다.

Response `204`: 없음

Authorization:

- 모든 그룹원은 자신이 속한 그룹 캘린더의 그룹 일정을 삭제할 수 있다.
- 타 그룹 캘린더의 그룹 일정은 삭제할 수 없다.

Errors:

| Status | 조건 |
|---:|---|
| 403 | 그룹원이 아닌 회원의 접근 |
| 404 | 그룹 또는 일정 없음 |

## 13. 제외 API

### X-01. 그룹 채팅

| 항목 | 내용 |
|---|---|
| 상태 | 구현 제외 |
| Trace | 요구사항 6-3, SC-17 |

그룹 채팅은 최신 요구사항에서 구현하지 않는다고 확정되었으므로 채팅 메시지 조회, 전송, 수정, 삭제 API를 제공하지 않는다.

## 14. Open Questions

현재 요구사항 기준으로 API 명세에 남은 Open Questions는 없다.
