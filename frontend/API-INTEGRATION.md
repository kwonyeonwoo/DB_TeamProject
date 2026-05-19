# Frontend API Integration

이 문서는 프론트엔드에서 백엔드 API를 연결할 때 지켜야 할 기준과 현재 연동 상태를 기록한다.

## 공통 기준

- API 기본 경로는 `/api`다.
- 환경 변수 `VITE_API_BASE_URL`이 있으면 해당 값을 우선 사용한다.
- 서버 세션 기반 인증을 사용한다.
- 모든 인증 요청은 쿠키를 포함해야 하므로 `withCredentials: true`를 사용한다.
- 프론트엔드는 로그인 토큰을 `localStorage` 또는 `sessionStorage`에 저장하지 않는다.
- API 요청/응답 필드명은 명세 기준에 따라 `snake_case`를 유지한다.
- 실패 응답은 `code`, `message`, 선택적 `details` 형식을 기대한다.

## Mock 전환 기준

- `VITE_USE_MOCK_API=true`일 때만 프론트엔드 내부 mock 응답을 사용한다.
- 실제 백엔드 연결 시에는 `VITE_USE_MOCK_API`를 비우거나 `false`로 둔다.
- mock은 화면 흐름 확인용이며, 실제 API 명세를 대체하지 않는다.

## 현재 구현된 API 모듈

### 인증

파일: `frontend/src/api/auth.ts`

- `GET /api/users/me`
- `POST /api/auth/login`
- `POST /api/auth/signup`
- `POST /api/auth/logout`
- `PATCH /api/users/me`
- `DELETE /api/users/me`

### 게시글

파일: `frontend/src/api/posts.ts`

- `GET /api/posts`
- `GET /api/posts/{post_id}`
- `POST /api/posts`
- `PATCH /api/posts/{post_id}`
- `DELETE /api/posts/{post_id}`
- `POST /api/posts/{post_id}/likes`
- `DELETE /api/posts/{post_id}/likes`

### 댓글

파일: `frontend/src/api/comments.ts`

- `GET /api/posts/{post_id}/comments`
- `POST /api/posts/{post_id}/comments`
- `POST /api/comments/{comment_id}/replies`
- `PATCH /api/comments/{comment_id}`
- `DELETE /api/comments/{comment_id}`

### 알림

파일: `frontend/src/api/notifications.ts`

- `GET /api/notifications`

### 개인 일정

파일: `frontend/src/api/schedules.ts`

- `GET /api/me/schedules`
- `POST /api/me/schedules`
- `PATCH /api/me/schedules/{schedule_id}`
- `DELETE /api/me/schedules/{schedule_id}`

### 그룹

파일: `frontend/src/api/groups.ts`

- `GET /api/groups`
- `POST /api/groups`
- `POST /api/groups/join`
- `GET /api/groups/{group_id}`
- `GET /api/groups/{group_id}/schedules`
- `POST /api/groups/{group_id}/schedules`
- `PATCH /api/groups/{group_id}/schedules/{schedule_id}`
- `DELETE /api/groups/{group_id}/schedules/{schedule_id}`

### 신고/관리자

파일: `frontend/src/api/reports.ts`

- `POST /api/reports`
- `GET /api/admin/reports`
- `PATCH /api/admin/reports/{report_id}`

## 이후 API 모듈 분리 계획

현재 계획된 1차 API 모듈 분리는 완료되었다.

## 백엔드 연동 시 확인할 항목

- 실제 `User` 응답에 `login_id`, `email_address`, `role`, `status`가 모두 포함되는지 확인한다.
- 실제 `Post` 응답에 `author_display_name`, `files`, `liked_by_me`, `like_count`가 모두 포함되는지 확인한다.
- 게시글 목록 응답이 `{ items, page, size, total_count, total_pages }` 형태인지 확인한다.
- 댓글 목록 응답이 `{ items: Comment[] }` 형태인지 확인한다.
- 알림 조회 시 반환된 알림이 읽음 처리되는지 확인한다.
- 개인 일정 목록 응답이 `{ items: Schedule[] }` 형태인지 확인한다.
- 개인 일정 필터에서 `start_at`, `end_at`의 겹침 조회 규칙이 백엔드와 일치하는지 확인한다.
- 개인 일정 `type` 값이 1부터 5 사이 숫자로 유지되는지 확인한다.
- 그룹 목록 응답이 `{ items: Group[] }` 형태인지 확인한다.
- 그룹 생성 응답이 `{ group, membership }` 형태인지 확인한다.
- 그룹 참여 응답이 `GroupMember` 형태인지 확인한다.
- 그룹 상세 응답이 `{ group, members }` 형태인지 확인한다.
- 그룹 일정 목록 응답이 `{ items: Schedule[] }` 형태인지 확인한다.
- 신고 생성 응답이 `Report` 형태인지 확인한다.
- 관리자 신고 목록 응답이 `{ items: Report[] }` 형태인지 확인한다.
- 신고 처리 응답에서 `status`, `processed_by`, `processed_at`만 변경되는지 확인한다.
- ADMIN 계정에는 일반 신고 버튼이 노출되지 않는지 확인한다.
- API 명세와 실제 응답이 다를 경우 이 문서에 충돌 항목으로 기록한다.

## 명세 충돌 기록

현재까지 프론트엔드 구현 중 새로 확인된 명세 충돌은 없다.
