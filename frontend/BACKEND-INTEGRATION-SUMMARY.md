# Backend Integration Summary

이 문서는 `origin/main`의 백엔드 구현을 `frontend/init` 브랜치로 가져오면서 확인한 연동 차이점, 사용한 백엔드 로직, 남은 누락 사항을 정리한다.

## 기존과 달라진 점

### 1. Mock API 중심에서 실제 백엔드 연결 준비로 변경

기존 프론트엔드는 `VITE_USE_MOCK_API=true`일 때 브라우저 저장소 기반 mock 데이터를 사용하도록 구성되어 있었다.

이번 연동 후 기본 예시 환경은 실제 백엔드 연결을 우선한다.

- `VITE_USE_MOCK_API=false`
- `VITE_API_BASE_URL=/api`
- `VITE_API_PROXY_TARGET=http://localhost:8080`

즉, 프론트 개발 서버는 `/api`로 시작하는 요청을 Spring Boot 백엔드 서버로 넘긴다.

### 2. Vite 개발 서버 프록시 추가

`frontend/vite.config.ts`에 개발 서버 프록시를 추가했다.

- 프론트 주소: `http://127.0.0.1:5173`
- 프론트 요청: `/api/...`
- 프록시 대상: `http://localhost:8080`
- 백엔드 context path: `/api`

브라우저에서는 같은 프론트 서버로 요청하는 것처럼 보이고, Vite가 내부적으로 백엔드로 전달한다.

### 3. 백엔드 API 응답 규칙 확인

백엔드는 Jackson 설정으로 `SNAKE_CASE`를 사용한다.

따라서 Java DTO의 `userId`, `createdAt`, `totalCount` 같은 필드는 프론트에서 `user_id`, `created_at`, `total_count`로 받는 현재 구조와 맞는다.

## 연동에 사용한 백엔드 로직

### 인증/사용자

사용 파일:

- `backend/src/main/java/com/academicshare/backend/auth/controller/AuthController.java`
- `backend/src/main/java/com/academicshare/backend/user/controller/UserController.java`

연동 API:

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/users/me`
- `PATCH /api/users/me`
- `DELETE /api/users/me`

특징:

- 세션 기반 인증을 사용한다.
- 프론트 `apiClient`는 `withCredentials: true`로 쿠키를 포함한다.
- 로그인 성공 시 백엔드는 세션에 현재 사용자 ID를 저장한다.

### 게시글

사용 파일:

- `backend/src/main/java/com/academicshare/backend/post/controller/PostController.java`

연동 API:

- `GET /api/posts`
- `GET /api/posts/{postId}`
- `POST /api/posts`
- `PATCH /api/posts/{postId}`
- `DELETE /api/posts/{postId}`
- `POST /api/posts/{postId}/likes`
- `DELETE /api/posts/{postId}/likes`

특징:

- 게시글 생성은 `multipart/form-data`를 사용한다.
- 파일 필드는 `files`와 `files[]`를 모두 받는다.
- 파일이 없는 수정은 JSON `PATCH`도 받을 수 있다.

### 댓글/답글

사용 파일:

- `backend/src/main/java/com/academicshare/backend/comment/controller/CommentController.java`

연동 API:

- `GET /api/posts/{postId}/comments`
- `POST /api/posts/{postId}/comments`
- `POST /api/comments/{commentId}/replies`
- `PATCH /api/comments/{commentId}`
- `DELETE /api/comments/{commentId}`

특징:

- 댓글과 답글은 같은 응답 타입을 사용한다.
- `parent_comment` 값으로 일반 댓글과 답글을 구분한다.

### 알림

사용 파일:

- `backend/src/main/java/com/academicshare/backend/notification/controller/NotificationController.java`

연동 API:

- `GET /api/notifications`

특징:

- 현재 백엔드는 알림 목록 조회만 제공한다.
- 프론트 mock은 조회 후 읽음 처리처럼 보이게 만들었지만, 실제 백엔드에는 읽음 처리 API가 아직 없다.

### 개인 일정

사용 파일:

- `backend/src/main/java/com/academicshare/backend/schedule/controller/ScheduleController.java`

연동 API:

- `GET /api/me/schedules`
- `POST /api/me/schedules`
- `PATCH /api/me/schedules/{scheduleId}`
- `DELETE /api/me/schedules/{scheduleId}`

특징:

- `start_at`, `end_at` 쿼리로 기간 겹침 조회를 지원한다.
- 프론트 캘린더 화면은 현재 월의 시작/끝 범위를 이 API에 전달한다.
- 일정 유형 `type`은 1부터 5까지의 숫자다.

### 그룹/그룹 일정

사용 파일:

- `backend/src/main/java/com/academicshare/backend/group/controller/GroupController.java`
- `backend/src/main/java/com/academicshare/backend/schedule/controller/ScheduleController.java`

연동 API:

- `GET /api/groups`
- `POST /api/groups`
- `POST /api/groups/join`
- `GET /api/groups/{groupId}`
- `GET /api/groups/{groupId}/schedules`
- `POST /api/groups/{groupId}/schedules`
- `PATCH /api/groups/{groupId}/schedules/{scheduleId}`
- `DELETE /api/groups/{groupId}/schedules/{scheduleId}`

특징:

- 그룹 생성 응답은 `{ group, membership }` 구조다.
- 그룹 상세 응답은 `{ group, members }` 구조다.
- 그룹 멤버 응답에는 현재 사용자 이름이 없어서, 프론트는 `사용자 {id}` 형태의 fallback을 사용한다.

### 신고/관리자

사용 파일:

- `backend/src/main/java/com/academicshare/backend/report/controller/ReportController.java`

연동 API:

- `POST /api/reports`
- `GET /api/admin/reports`
- `PATCH /api/admin/reports/{reportId}`

특징:

- 신고 대상은 `POST`, `COMMENT`를 사용한다.
- 신고 처리 상태는 `PENDING`, `PROCESSED`를 사용한다.
- 관리자 신고 처리 요청은 `{ status: "PROCESSED" }` 형태다.

## 구현되어 있지만 프론트에서 아직 충분히 활용하지 않는 부분

- 사용자 탈퇴 API: `DELETE /api/users/me`
  - 프론트에는 기본 계정 삭제 흐름이 있지만, 실제 백엔드 연결 상태에서 세션 종료와 후속 화면 이동 검증이 필요하다.
- 사용자 비밀번호 변경 필드: `current_password`, `new_password`
  - 프론트 마이페이지에서 필드는 준비되어 있으나, 실제 실패 메시지와 유효성 UX는 더 다듬을 수 있다.
- 그룹 멤버 역할 정보: `LEADER`, `MEMBER`
  - 화면 표시는 하지만, 리더 전용 제어 기능은 아직 제한적으로만 사용한다.
- 그룹 일정 수정/삭제 권한
  - 백엔드는 권한을 검사하므로, 프론트에서는 권한 실패 시 안내 문구를 더 구체화할 필요가 있다.
- 신고 관리자 API
  - 관리자 계정으로 로그인했을 때의 전체 흐름은 API가 준비되어 있으나, 실제 ADMIN 세션으로 화면 검증이 필요하다.

## 백엔드에는 아직 없거나 프론트 mock과 다른 부분

- 알림 읽음 처리 API
  - 현재 백엔드는 `GET /api/notifications`만 제공한다.
  - 프론트 mock은 조회 후 `is_read`를 true로 바꾸지만, 실제 API에서는 이 동작이 보장되지 않는다.
  - 필요하면 `PATCH /api/notifications/{notificationId}` 또는 `POST /api/notifications/read` 같은 API가 추가되어야 한다.
- 그룹 수정/삭제 API
  - 현재 백엔드는 그룹 생성, 참여, 목록, 상세만 제공한다.
  - 그룹명 수정, 그룹 삭제, 멤버 추방, 그룹 탈퇴 같은 기능은 별도 API가 없다.
- 게시글 첨부파일 개별 삭제 API
  - 게시글 수정 시 파일을 새로 올릴 수는 있지만, 파일 하나만 선택 삭제하는 API는 확인되지 않았다.
- 댓글 좋아요/신고 상세 처리
  - 댓글 신고 생성은 `POST /api/reports`로 가능하지만, 댓글 자체의 좋아요 같은 추가 상호작용은 없다.
- 알림 실시간 갱신
  - 현재는 목록 조회 방식이다.
  - SSE, WebSocket, polling 주기 같은 실시간 방식은 아직 없다.

## 실제 연동 실행 조건

프론트 개발 서버만 켜면 실제 API가 동작하지 않는다. 아래 조건이 같이 필요하다.

- MySQL 또는 MariaDB가 실행 중이어야 한다.
- 백엔드가 `http://localhost:8080`에서 실행 중이어야 한다.
- 백엔드 context path는 `/api`다.
- 프론트는 `http://127.0.0.1:5173`에서 실행한다.
- 프론트 `.env` 또는 실행 환경에서 `VITE_USE_MOCK_API=false`여야 한다.

백엔드가 아직 켜져 있지 않으면 프론트 화면에서 로그인 등 API 요청은 실패한다. 이 경우 임시 화면 테스트는 `VITE_USE_MOCK_API=true`로 다시 켜서 진행할 수 있다.

## 개발용 임시 로그인 fallback

백엔드 서버가 아직 켜져 있지 않은 상태에서도 프론트 화면을 테스트할 수 있도록 개발 모드에서만 임시 로그인 fallback을 둔다.

동작 조건:

- `import.meta.env.DEV`가 true인 개발 서버에서만 동작한다.
- 실제 백엔드 로그인 요청을 먼저 시도한다.
- 백엔드가 꺼져 있거나 5xx 응답처럼 서버 사용이 불가능한 경우에만 fallback이 동작한다.
- 백엔드가 정상 응답하는 경우에는 임시 계정을 우선하지 않는다.
- production build에서는 동작하지 않는다.

임시 계정:

- ADMIN: `admin` / `admin123`
- USER: `user1` / `user123`
- USER: `user2` / `user123`
- USER: `user3` / `user123`

환경 변수:

- `VITE_ENABLE_DEMO_AUTH_FALLBACK=false`로 두면 개발 서버에서도 fallback을 끌 수 있다.

주의:

- 이 fallback은 실제 백엔드 세션이나 DB 사용자를 만들지 않는다.
- 실제 백엔드/DB 연동 검증 시에는 백엔드 서버를 실행하고 실제 DB 계정으로 로그인해야 한다.
- 제거하지 않고 남아 있어도 production build에서는 비활성화되도록 `import.meta.env.DEV` 조건을 걸어두었다.

## 이번 연동 검증

- `origin/main`의 백엔드 구현을 `frontend/init`에 병합했다.
- 프론트 `/api` 프록시를 추가했다.
- 프론트 lint를 통과했다.
- 프론트 production build를 통과했다.
- 백엔드 Gradle 테스트를 통과했다.

실제 브라우저에서 백엔드 DB까지 연결한 E2E 검증은 백엔드 서버와 DB가 실행된 뒤 추가로 진행해야 한다.
