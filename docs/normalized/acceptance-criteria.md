# Acceptance Criteria

기준 문서: `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`

검증 상태: PASS - source 문서 기준 acceptance criteria 충돌 없음

주의:

- 회원 탈퇴 시 개인정보성 컬럼은 즉시 변경하지 않고, `deleted_at`으로부터 6개월 후 NULL 또는 식별 불가 값으로 변경하는 기준으로 검증한다.
- 신고 생성은 원본 API 기준 USER 권한으로 검증한다. ADMIN이 `POST /api/reports`를 호출하면 권한 실패로 본다.
- 자기 게시글/댓글에 댓글 또는 대댓글을 작성한 경우 알림을 생성하지 않는 기준으로 검증한다.
- 알림의 `comment_content`는 생성 시점의 댓글/대댓글 내용 스냅샷이며 원본 댓글/대댓글 수정 또는 삭제 후에도 변경하지 않는 기준으로 검증한다.
- `AUTH-001` 회원 가입과 `AUTH-002` 로그인을 제외한 `POST`, `PATCH`, `DELETE` API는 CSRF 토큰을 요구하는 기준으로 검증한다.

## 1. 공통 기준

### AC-COMMON-001. 공통 에러 응답

- Given API 요청이 실패한다
- When 서버가 실패 응답 body를 반환한다
- Then 응답은 `code`, `message`를 포함한다
- And 입력값별 상세 오류가 필요한 경우에만 `details`를 선택적으로 포함한다

### AC-COMMON-002. 인증 필요 API

- Given 인증되지 않은 사용자가 인증 필요 API를 호출한다
- When 서버가 요청을 처리한다
- Then `401`을 반환한다

### AC-COMMON-003. 권한 실패

- Given 인증된 사용자가 권한이 없는 리소스 또는 관리자 기능에 접근한다
- When 서버가 요청을 처리한다
- Then `403`을 반환한다

### AC-COMMON-004. CSRF 토큰

- Given 인증된 세션이 있는 사용자가 `AUTH-001`과 `AUTH-002`를 제외한 `POST`, `PATCH`, `DELETE` API를 호출한다
- When CSRF 토큰이 누락되거나 일치하지 않는다
- Then `403`을 반환한다
- And 오류 응답의 `code`는 `ACCESS_DENIED`다

### AC-COMMON-005. 기능별 API 오류 조건 추적

- Given API 계약의 기능별 오류 조건을 acceptance criteria에 반영한다
- When 기능 테스트 범위를 검토한다
- Then 아래 표의 오류 조건은 해당 기능의 실패 케이스로 포함한다

| 기능 ID | 반영할 API 오류 조건 |
|---|---|
| AUTH-001 | `400` 필수/형식 오류, `409` `login_id` 중복, `409` `email_address` 중복 |
| AUTH-002 | `400` 필수 누락, `401` 로그인 실패, `403` 탈퇴 계정 |
| AUTH-003 | `401` 인증되지 않음, `403` CSRF 토큰 누락/불일치 |
| USER-001 | `401` 인증되지 않음 |
| USER-002 | `400` 수정 필드 없음/동일값/`current_password` 누락, `401` 인증되지 않음, `403` `current_password` 불일치, `409` 이메일 중복 |
| USER-003 | `401` 인증되지 않음 |
| POST-001 | `400` 둘 이상의 필터 종류, `400` 페이지 파라미터 오류 |
| POST-002 | `404` 게시글 없음 |
| POST-003 | `400` 필수 누락, `400` 파일 업로드 실패 |
| POST-004 | `400` 수정 필드 없음, `403` 작성자 아님, `404` 게시글 없음 |
| POST-005 | `403` 작성자 아님, `404` 게시글 없음 |
| POST-006 | `404` 게시글 없음, `409` 이미 추천 |
| POST-007 | `404` 게시글 없음, `404` 추천 없음 |
| COMMENT-001 | `404` 게시글 없음 |
| COMMENT-002 | `400` 내용 누락, `404` 게시글 없음 |
| COMMENT-003 | `400` 내용 누락, `400` 대댓글에 대댓글 작성, `404` 부모 댓글 없음 |
| COMMENT-004 | `400` 수정 필드 없음, `403` 작성자 아님, `404` 댓글 없음 |
| COMMENT-005 | `403` 작성자 아님, `404` 댓글 없음 |
| REPORT-001 | `400` 필수 누락, `400` 유효하지 않은 `target_type` 또는 `reason_type`, `403` 신고 권한 없음, `404` 신고 대상 없음, `409` 이미 신고 |
| REPORT-002 | `403` 관리자 권한 없음 |
| REPORT-003 | `400` 유효하지 않은 `status`, `403` 관리자 권한 없음, `404` 신고 없음 |
| NOTI-001 | `403` 다른 회원 알림 접근 |
| CAL-001 | `400` 날짜 형식 오류, `400` `end_at < start_at`, `401` 인증 없음, `403` 다른 회원 일정 접근 |
| CAL-002 | `400` 필수 누락, `400` `type`이 1..5 밖, `400` 종료가 시작보다 빠름 |
| CAL-003 | `400` 수정 필드 없음, `400` `type`이 1..5 밖, `400` 종료가 시작보다 빠름, `403` 다른 회원 일정 접근, `404` 일정 없음 |
| CAL-004 | `403` 다른 회원 일정 접근, `404` 일정 없음 |
| GROUP-001 | `401` 인증 없음 |
| GROUP-002 | `400` 그룹명 누락 |
| GROUP-003 | `404` 존재하지 않거나 유효하지 않은 그룹 코드, `409` 이미 가입 |
| GROUP-004 | `403` 그룹원이 아님, `404` 그룹 없음 |
| GCAL-001 | `400` 날짜 형식 오류, `400` `end_at < start_at`, `403` 그룹원이 아님, `404` 그룹 없음 |
| GCAL-002 | `400` 필수 누락, `400` `type`이 1..5 밖, `400` 종료가 시작보다 빠름, `403` 그룹원이 아님, `404` 그룹 없음 |
| GCAL-003 | `400` `type`이 1..5 밖, `400` 종료가 시작보다 빠름, `403` 그룹원이 아님, `404` 그룹 또는 일정 없음 |
| GCAL-004 | `403` 그룹원이 아님, `404` 그룹 또는 일정 없음 |

위 표에 별도 표기되지 않은 상태 변경 API도 `AC-COMMON-004`의 CSRF 실패 조건을 공통 실패 케이스로 포함한다.

### AC-COMMON-006. 기능 변경 범위 분리

- Given 검증 대상 Feature ID가 정해져 있다
- When backend 변경사항과 관련 테스트를 검토한다
- Then 변경 파일은 해당 Feature ID의 구현/테스트 또는 명시된 공통 의존 변경에만 속해야 한다
- And 타 Feature ID 구현/테스트가 포함되면 범위 초과 이슈로 기록한다
- And 분리 patch 또는 별도 리뷰 문서가 없으면 승인하지 않는다

## 2. 인증 및 회원

### AUTH-001. 회원 가입

Normal:

- Given 비회원이 이름, 아이디, 비밀번호, 이메일을 입력한다
- When `POST /api/auth/signup`을 호출한다
- Then `201`과 User 응답을 반환한다
- And 생성된 회원의 `role`은 `USER`다

Validation failures:

- Given 필수값이 누락된다
- When 회원 가입을 요청한다
- Then `400`을 반환한다

- Given 이미 사용 중인 `login_id` 또는 `email_address`가 있다
- When 같은 값으로 회원 가입을 요청한다
- Then `409`를 반환한다

Security:

- Given 요청 body에 `role = ADMIN`이 포함된다
- When 회원 가입을 요청한다
- Then ADMIN 권한은 부여되지 않는다

### AUTH-002. 로그인

Normal:

- Given ACTIVE 회원이 올바른 아이디와 비밀번호를 입력한다
- When `POST /api/auth/login`을 호출한다
- Then `200`과 사용자 정보를 반환한다
- And 서버 세션이 생성된다

Failures:

- Given `login_id` 또는 `password`가 누락된다
- When 로그인을 요청한다
- Then `400`을 반환한다

- Given 아이디 또는 비밀번호가 올바르지 않다
- When 로그인을 요청한다
- Then `401`을 반환한다

- Given 회원 상태가 `DELETED`다
- When 로그인을 요청한다
- Then `403`을 반환한다

### AUTH-003. 로그아웃

- Given 인증된 회원이 있다
- When `POST /api/auth/logout`을 호출한다
- Then `204`를 반환한다
- And 현재 세션은 무효화된다

- Given 인증되지 않은 사용자가 있다
- When `POST /api/auth/logout`을 호출한다
- Then `401`을 반환한다

- Given 인증된 회원 세션이 있다
- And CSRF 토큰이 누락되거나 일치하지 않는다
- When `POST /api/auth/logout`을 호출한다
- Then `403`을 반환한다

### USER-001/USER-002. 내 정보 조회 및 수정

Normal:

- Given 인증된 회원이 있다
- When `GET /api/users/me`를 호출한다
- Then 현재 회원의 User 정보를 반환한다

- Given 인증되지 않은 사용자가 있다
- When `GET /api/users/me`를 호출한다
- Then `401`을 반환한다

- Given 인증된 회원이 기존 정보와 다른 이름 또는 이메일을 입력한다
- When `PATCH /api/users/me`를 호출한다
- Then `200`과 수정된 User를 반환한다

Password change:

- Given 인증된 회원이 현재 비밀번호와 새 비밀번호를 입력한다
- When 현재 비밀번호가 기존 비밀번호와 일치한다
- Then 새 비밀번호로 변경한다

Failures:

- Given 수정 필드가 없다
- When 내 정보 수정을 요청한다
- Then `400`을 반환한다

- Given 기존 정보와 동일한 값으로 수정한다
- When 내 정보 수정을 요청한다
- Then `400`을 반환한다

- Given 비밀번호 변경 요청에서 `current_password`가 누락된다
- When 내 정보 수정을 요청한다
- Then `400`을 반환한다

- Given `current_password`가 기존 비밀번호와 다르다
- When 비밀번호 변경을 요청한다
- Then `403`을 반환한다

- Given 변경 이메일이 이미 사용 중이다
- When 내 정보 수정을 요청한다
- Then `409`를 반환한다

- Given 인증되지 않은 사용자가 있다
- When `PATCH /api/users/me`를 호출한다
- Then `401`을 반환한다

### USER-003. 회원 탈퇴

Normal:

- Given 인증된 회원이 있다
- When `DELETE /api/users/me`를 호출한다
- Then `204`를 반환한다
- And 회원 `status`는 `DELETED`, `deleted_at`은 현재 시각으로 저장된다
- And 현재 세션은 무효화된다

- Given 인증되지 않은 사용자가 있다
- When `DELETE /api/users/me`를 호출한다
- Then `401`을 반환한다

Retention:

- Given 탈퇴 처리가 방금 완료된 회원이 있다
- When 회원 row를 확인한다
- Then `login_id`, `password`, `name`, `email_address`는 탈퇴 처리 시점에 즉시 변경되지 않는다

- Given 탈퇴 회원의 `deleted_at`으로부터 6개월이 지났다
- When 개인정보 삭제 또는 비식별화 처리가 실행된다
- Then `login_id`, `password`, `name`, `email_address`는 NULL 값으로 변경된다
- And NULL 값이 들어갈 수 없는 속성은 식별할 수 없는 값으로 변경된다

Related data:

- Given 탈퇴 회원이 작성한 게시글, 댓글, 대댓글이 있다
- When 해당 작성물을 조회한다
- Then 작성물은 유지되고 작성자명은 `탈퇴한 유저`로 표시된다

- Given 탈퇴 회원의 개인 일정이 있다
- When 탈퇴 처리가 실행된다
- Then 해당 개인 일정은 즉시 삭제된다
- And 일반 개인 일정 조회에 노출되지 않는다

- Given 탈퇴 회원이 그룹에 가입되어 있다
- When 탈퇴 처리가 실행된다
- Then 탈퇴 회원은 가입되어 있던 모든 그룹에서 탈퇴 처리된다

- Given 탈퇴 회원이 그룹장이고 다른 그룹원이 있다
- When 탈퇴 처리가 실행된다
- Then 가장 먼저 가입한 다른 그룹원에게 `LEADER`가 위임된다
- And 위임 후 탈퇴 회원은 해당 그룹에서 탈퇴 처리된다

- Given 탈퇴 회원이 유일한 그룹원이다
- When 탈퇴 처리가 실행된다
- Then 해당 그룹은 즉시 삭제된다

## 3. 알림

### NOTI-001. 내 알림 목록 조회

Normal:

- Given 인증된 회원에게 알림이 있다
- When `GET /api/notifications`를 호출한다
- Then 본인 알림 목록을 반환한다
- And 반환 대상 알림은 읽음 처리된다

Navigation data:

- Given 댓글 알림이다
- When 알림을 조회한다
- Then `commented_post_id`가 대상 게시글이고 `commented_id`는 NULL이다
- And `comment_content`는 알림 생성 시점의 댓글 내용이다

- Given 대댓글 알림이다
- When 알림을 조회한다
- Then `commented_post_id`와 부모 댓글 id인 `commented_id`가 반환된다
- And `comment_content`는 알림 생성 시점의 대댓글 내용이다

Authorization failure:

- Given 다른 회원의 알림에 접근한다
- When 알림 조회를 요청한다
- Then `403`을 반환한다

## 4. 게시글

### POST-001. 게시글 목록 조회

Normal:

- Given 존재하는 게시글이 있다
- When `GET /api/posts`를 호출한다
- Then 작성일 최신순으로 페이지 응답을 반환한다

- Given `page` 파라미터가 전달되지 않는다
- When `GET /api/posts`를 호출한다
- Then `page = 1` 기준으로 목록을 반환한다

Filtering:

- Given 검색어 필터만 전달된다
- When 목록을 조회한다
- Then 제목/내용 기준으로 필터링된다

- Given 작성자 필터만 전달된다
- When 목록을 조회한다
- Then 탈퇴 작성자와 익명 게시글은 작성자 필터 결과에서 제외된다

- Given 주제 필터가 전달된다
- When 목록을 조회한다
- Then `main_category`, `sub_category` 묶음으로 필터링된다

Validation failure:

- Given 검색어, 작성자, 주제 필터 중 둘 이상의 필터 종류가 동시에 전달된다
- When 목록을 조회한다
- Then `400`을 반환한다

- Given 페이지 번호 또는 페이지 크기 파라미터가 유효하지 않다
- When 목록을 조회한다
- Then `400`을 반환한다

### POST-002. 게시글 상세 조회

- Given 존재하는 게시글이 있다
- When `GET /api/posts/{post_id}`를 호출한다
- Then Post를 반환한다
- And 조회수가 증가한다

Anonymous display:

- Given 동일 게시글 상세 화면 안에서 같은 회원이 익명 게시글/댓글/대댓글을 작성했다
- When 게시글 상세를 조회한다
- Then 해당 작성자의 익명 표시명은 항상 같은 `익명_숫자`다

- Given 서로 다른 게시글 상세 화면에 익명 작성물이 있다
- When 각각의 게시글 상세를 조회한다
- Then `익명_숫자`는 게시글별로 독립적으로 부여된다

- Given 탈퇴 회원이 익명 작성물을 작성했다
- When 해당 작성물을 조회한다
- Then 작성자명은 `익명_숫자`가 아니라 `탈퇴한 유저`로 표시된다

- Given 게시글이 없다
- When 상세 조회를 요청한다
- Then `404`를 반환한다

### POST-003. 게시글 작성 및 파일 업로드

Normal:

- Given 인증된 회원이 대주제, 소주제, 제목, 익명 여부를 입력한다
- When `POST /api/posts`를 multipart로 호출한다
- Then `201`과 Post를 반환한다
- And 업로드 파일은 `/uploads/posts/{post_id}/{UUID}` 로컬 경로에 저장된다
- And DB에는 `file_url`만 저장된다

Validation failures:

- Given 필수값이 누락된다
- When 게시글 작성을 요청한다
- Then `400`을 반환한다

- Given 파일 업로드에 실패한다
- When 게시글 작성을 요청한다
- Then `400`을 반환한다

### POST-004/POST-005. 게시글 수정 및 삭제

Normal:

- Given 게시글 작성자가 수정 필드를 입력한다
- When `PATCH /api/posts/{post_id}`를 호출한다
- Then `200`과 수정된 Post를 반환한다
- And `updated_at`이 기록된다

- Given 수정 요청에 새 파일이 포함된다
- When 수정이 성공한다
- Then 기존 파일 목록은 전체 교체된다

- Given 수정 요청에 새 파일이 없다
- When 수정이 성공한다
- Then 기존 파일 목록은 유지된다

- Given 게시글 작성자가 삭제를 요청한다
- When `DELETE /api/posts/{post_id}`를 호출한다
- Then `204`를 반환한다
- And 해당 게시글의 추천, 댓글/대댓글, 첨부파일, 알림은 FK cascade 정책에 따라 함께 삭제된다
- And 해당 게시글 또는 함께 삭제된 댓글/대댓글을 대상으로 생성된 신고 이력은 유지된다

Failures:

- Given 작성자가 아닌 회원이 수정 또는 삭제한다
- When 요청을 처리한다
- Then `403`을 반환한다

- Given 수정 필드가 없다
- When 수정을 요청한다
- Then `400`을 반환한다

- Given 수정 또는 삭제 대상 게시글이 없다
- When 게시글 수정 또는 삭제를 요청한다
- Then `404`를 반환한다

### POST-006/POST-007. 추천 등록 및 취소

- Given 회원이 추천하지 않은 게시글이 있다
- When `POST /api/posts/{post_id}/likes`를 호출한다
- Then `201`과 Like를 반환한다

- Given 회원이 이미 추천한 게시글이 있다
- When 추천 등록을 요청한다
- Then `409`를 반환한다

- Given 회원이 추천한 게시글이 있다
- When `DELETE /api/posts/{post_id}/likes`를 호출한다
- Then `204`를 반환한다

- Given 회원이 추천하지 않은 게시글이다
- When 추천 취소를 요청한다
- Then `404`를 반환한다

- Given 추천 대상 게시글이 없다
- When 추천 등록 또는 취소를 요청한다
- Then `404`를 반환한다

## 5. 댓글/대댓글

### COMMENT-001. 댓글 목록 조회

- Given 게시글에 댓글과 대댓글이 있다
- When `GET /api/posts/{post_id}/comments`를 호출한다
- Then 댓글과 대댓글을 반환한다
- And 익명 작성자는 `익명_숫자`, 탈퇴 작성자는 `탈퇴한 유저`로 표시된다

- Given 게시글이 없다
- When `GET /api/posts/{post_id}/comments`를 호출한다
- Then `404`를 반환한다

### COMMENT-002/COMMENT-003. 댓글 및 대댓글 작성

- Given 인증된 회원이 댓글 내용을 입력한다
- When `POST /api/posts/{post_id}/comments`를 호출한다
- Then `201`과 Comment를 반환한다

- Given 인증된 회원이 다른 회원의 게시글에 댓글을 작성한다
- When 댓글 작성이 성공한다
- Then 게시글 작성자에게 댓글 알림이 생성된다
- And 알림의 `commented_post_id`는 댓글이 달린 게시글 id다
- And 알림의 `commented_id`는 NULL이다

- Given 인증된 회원이 자기 게시글에 댓글을 작성한다
- When 댓글 작성이 성공한다
- Then 댓글은 생성된다
- And 댓글 알림은 생성되지 않는다

- Given 인증된 회원이 일반 댓글에 대댓글 내용을 입력한다
- When `POST /api/comments/{comment_id}/replies`를 호출한다
- Then `201`과 대댓글 Comment를 반환한다

- Given 인증된 회원이 다른 회원의 일반 댓글에 대댓글을 작성한다
- When 대댓글 작성이 성공한다
- Then 부모 댓글 작성자에게 대댓글 알림이 생성된다
- And 알림의 `commented_post_id`는 부모 댓글이 속한 게시글 id다
- And 알림의 `commented_id`는 부모 댓글 id다

- Given 인증된 회원이 자기 일반 댓글에 대댓글을 작성한다
- When 대댓글 작성이 성공한다
- Then 대댓글은 생성된다
- And 대댓글 알림은 생성되지 않는다

- Given 대댓글을 부모로 다시 대댓글 작성을 요청한다
- When 서버가 요청을 검증한다
- Then `400`을 반환한다

- Given 내용이 누락된다
- When 댓글 또는 대댓글 작성을 요청한다
- Then `400`을 반환한다

- Given 댓글 작성 대상 게시글이 없다
- When 댓글 작성을 요청한다
- Then `404`를 반환한다

- Given 대댓글 작성 대상 부모 댓글이 없다
- When 대댓글 작성을 요청한다
- Then `404`를 반환한다

### COMMENT-004/COMMENT-005. 댓글 및 대댓글 수정/삭제

- Given 댓글 작성자가 수정한다
- When `PATCH /api/comments/{comment_id}`를 호출한다
- Then `200`과 수정된 Comment를 반환한다
- And `updated_at`이 기록된다

- Given 댓글 작성자가 삭제한다
- When `DELETE /api/comments/{comment_id}`를 호출한다
- Then `204`를 반환한다
- And 일반 댓글 삭제 시 해당 댓글의 대댓글은 FK cascade 정책에 따라 함께 삭제된다
- And 삭제 전 생성된 알림은 유지되고 `comment_content`는 변경되지 않는다
- And 해당 댓글 또는 함께 삭제된 대댓글을 대상으로 생성된 신고 이력은 유지된다

- Given 작성자가 아닌 회원이 수정 또는 삭제한다
- When 요청을 처리한다
- Then `403`을 반환한다

- Given 댓글 수정 요청에 수정 필드가 없다
- When `PATCH /api/comments/{comment_id}`를 호출한다
- Then `400`을 반환한다

- Given 수정 또는 삭제 대상 댓글이 없다
- When 댓글 또는 대댓글 수정/삭제를 요청한다
- Then `404`를 반환한다

## 6. 신고

### REPORT-001. 신고 생성

- Given USER 회원이 신고 대상과 신고 사유를 선택한다
- When `POST /api/reports`를 호출한다
- Then `201`과 Report를 반환한다
- And `status = PENDING`, `processed_by = null`, `processed_at = null`이다

- Given `target_type`, `target_id`, `reason_type` 중 하나가 누락된다
- When `POST /api/reports`를 호출한다
- Then `400`을 반환한다

- Given `target_type`이 `POST`, `COMMENT`가 아니거나 `reason_type`이 1..4 범위 밖이다
- When `POST /api/reports`를 호출한다
- Then `400`을 반환한다

- Given ADMIN 회원이 신고 생성을 요청한다
- When `POST /api/reports`를 호출한다
- Then `403`을 반환한다

- Given 같은 회원이 같은 대상을 다시 신고한다
- When 신고를 요청한다
- Then `409`를 반환한다

- Given 신고 대상이 없다
- When 신고를 요청한다
- Then `404`를 반환한다

### REPORT-002/REPORT-003. 관리자 신고 관리

- Given ADMIN 회원이 있다
- When `GET /api/admin/reports`를 호출한다
- Then 신고 목록을 반환한다
- And 신고 처리 상태, 처리자, 처리 시각을 포함한다

- Given 신고 대상 게시글 또는 댓글이 삭제되어 대상 row를 조회할 수 없다
- When ADMIN 회원이 `GET /api/admin/reports`를 호출한다
- Then 해당 신고의 `target_display_name`은 `삭제된 대상`으로 표시된다

- Given ADMIN 회원이 신고 처리를 요청한다
- When `PATCH /api/admin/reports/{report_id}`에 `status = PROCESSED`를 전달한다
- Then `200`과 처리된 Report를 반환한다
- And `processed_by`는 현재 관리자 id, `processed_at`은 현재 시각이다
- And 신고 대상 게시글 또는 댓글은 자동 삭제되지 않는다

- Given ADMIN 회원이 `status = PROCESSED`가 아닌 값 또는 누락된 `status`를 전달한다
- When 신고 처리를 요청한다
- Then `400`을 반환한다

- Given 존재하지 않는 신고 id다
- When 신고 처리를 요청한다
- Then `404`를 반환한다

- Given USER 회원이 관리자 신고 API를 호출한다
- When 서버가 권한을 검증한다
- Then `403`을 반환한다

## 7. 개인 일정

### CAL-001..CAL-004. 개인 일정

- Given 인증된 회원이 있다
- When `GET /api/me/schedules`를 호출한다
- Then 본인의 개인 일정만 반환한다

- Given `start_at`과 `end_at`이 모두 없다
- When `GET /api/me/schedules`를 호출한다
- Then 본인의 전체 개인 일정을 반환한다

- Given `start_at`과 `end_at`이 모두 있고 개인 일정 기간과 조회 기간이 겹친다
- When `GET /api/me/schedules?start_at=...&end_at=...`를 호출한다
- Then `schedules.start_at <= end_at AND schedules.end_at >= start_at`인 본인 개인 일정을 반환한다

- Given `start_at`만 전달된다
- When 개인 일정 목록을 조회한다
- Then `schedules.end_at >= start_at`인 본인 개인 일정을 반환한다

- Given `end_at`만 전달된다
- When 개인 일정 목록을 조회한다
- Then `schedules.start_at <= end_at`인 본인 개인 일정을 반환한다

- Given `start_at` 또는 `end_at`이 ISO-8601 문자열이 아니다
- When 개인 일정 목록을 조회한다
- Then `400`을 반환한다

- Given `start_at`과 `end_at`이 모두 있고 `end_at`이 `start_at`보다 빠르다
- When 개인 일정 목록을 조회한다
- Then `400`을 반환한다

- Given 일정 제목, 시작/종료 일시, `type`이 1..5 중 하나로 유효하다
- When `POST /api/me/schedules`를 호출한다
- Then `201`과 Schedule을 반환한다

- Given 일정 필수값인 `title`, `start_at`, `end_at`, `type` 중 하나가 누락된다
- When 개인 일정 등록을 요청한다
- Then `400`을 반환한다

- Given `type`이 1, 2, 3, 4, 5 중 하나가 아니다
- When 개인 일정 등록 또는 수정을 요청한다
- Then `400`을 반환한다

- Given 본인 개인 일정이 있다
- When `PATCH /api/me/schedules/{schedule_id}`를 호출한다
- Then `200`과 수정된 Schedule을 반환한다
- And `updated_at`이 기록된다

- Given 개인 일정 수정 요청에 수정 필드가 없다
- When `PATCH /api/me/schedules/{schedule_id}`를 호출한다
- Then `400`을 반환한다

- Given 본인 개인 일정이 있다
- When `DELETE /api/me/schedules/{schedule_id}`를 호출한다
- Then `204`를 반환한다
- And 해당 개인 일정은 삭제된다

- Given 종료 일시가 시작 일시보다 빠르다
- When 일정 저장을 요청한다
- Then `400`을 반환한다

- Given 타인의 개인 일정에 접근한다
- When 조회/수정/삭제를 요청한다
- Then `403`을 반환한다

- Given 개인 일정이 없다
- When 수정 또는 삭제를 요청한다
- Then `404`를 반환한다

## 8. 그룹 및 그룹 일정

### GROUP-001..GROUP-004. 그룹

- Given 인증된 회원이 있다
- When `GET /api/groups`를 호출한다
- Then 본인이 속한 그룹만 반환한다

- Given 그룹명이 있다
- When `POST /api/groups`를 호출한다
- Then `201`을 반환한다
- And 그룹 생성자는 `groups.leader_id`와 `group_members.role = LEADER`로 자동 등록된다
- And 생성된 `group_code`가 반환된다

- Given 유효한 그룹 가입 코드가 있다
- When `POST /api/groups/join`을 호출한다
- Then `201`과 GroupMember를 반환한다

- Given 이미 가입한 그룹 코드다
- When 그룹 가입을 요청한다
- Then `409`를 반환한다

- Given 존재하지 않거나 유효하지 않은 그룹 가입 코드다
- When `POST /api/groups/join`을 호출한다
- Then `404`를 반환한다

- Given 그룹명이 누락된다
- When 그룹 생성을 요청한다
- Then `400`을 반환한다

- Given 그룹원이 아닌 회원이다
- When 그룹 상세 조회를 요청한다
- Then `403`을 반환한다

- Given 그룹이 없다
- When 그룹 상세 조회를 요청한다
- Then `404`를 반환한다

### GCAL-001..GCAL-004. 그룹 일정

- Given 그룹원이 있다
- When `GET /api/groups/{group_id}/schedules`를 호출한다
- Then 해당 그룹의 그룹 일정을 반환한다

- Given `start_at`과 `end_at`이 모두 없다
- When `GET /api/groups/{group_id}/schedules`를 호출한다
- Then 해당 그룹의 전체 그룹 일정을 반환한다

- Given `start_at`과 `end_at`이 모두 있고 그룹 일정 기간과 조회 기간이 겹친다
- When `GET /api/groups/{group_id}/schedules?start_at=...&end_at=...`를 호출한다
- Then `schedules.start_at <= end_at AND schedules.end_at >= start_at`인 해당 그룹 일정을 반환한다

- Given `start_at`만 전달된다
- When 그룹 일정 목록을 조회한다
- Then `schedules.end_at >= start_at`인 해당 그룹 일정을 반환한다

- Given `end_at`만 전달된다
- When 그룹 일정 목록을 조회한다
- Then `schedules.start_at <= end_at`인 해당 그룹 일정을 반환한다

- Given `start_at` 또는 `end_at`이 ISO-8601 문자열이 아니다
- When 그룹 일정 목록을 조회한다
- Then `400`을 반환한다

- Given `start_at`과 `end_at`이 모두 있고 `end_at`이 `start_at`보다 빠르다
- When 그룹 일정 목록을 조회한다
- Then `400`을 반환한다

- Given 그룹원이 `title`, `start_at`, `end_at`, `type`이 1..5 중 하나인 유효한 일정 정보를 입력한다
- When `POST /api/groups/{group_id}/schedules`를 호출한다
- Then `201`과 Schedule을 반환한다

- Given 그룹 일정 필수값인 `title`, `start_at`, `end_at`, `type` 중 하나가 누락된다
- When 그룹 일정 등록을 요청한다
- Then `400`을 반환한다

- Given 그룹 일정의 `type`이 1, 2, 3, 4, 5 중 하나가 아니다
- When 그룹 일정 등록 또는 수정을 요청한다
- Then `400`을 반환한다

- Given 그룹 일정의 종료 일시가 시작 일시보다 빠르다
- When 그룹 일정 저장을 요청한다
- Then `400`을 반환한다

- Given 그룹원이 그룹 일정을 수정한다
- When `PATCH /api/groups/{group_id}/schedules/{schedule_id}`를 호출한다
- Then `200`과 수정된 Schedule을 반환한다

- Given 그룹원이 그룹 일정을 삭제한다
- When `DELETE /api/groups/{group_id}/schedules/{schedule_id}`를 호출한다
- Then `204`를 반환한다
- And 해당 그룹 일정은 삭제된다

- Given 그룹원이 아닌 회원이다
- When 그룹 일정 API를 호출한다
- Then `403`을 반환한다

- Given 그룹이 없다
- When 그룹 일정 조회, 등록, 수정 또는 삭제를 요청한다
- Then `404`를 반환한다

- Given 그룹 일정이 없다
- When 그룹 일정 수정 또는 삭제를 요청한다
- Then `404`를 반환한다

- Given 타 그룹 일정이다
- When 수정 또는 삭제를 요청한다
- Then `403` 또는 `404`를 반환한다

## 9. Excluded Feature Criteria

- Given 사용자가 아이디 찾기 또는 비밀번호 찾기/재설정 기능을 찾는다
- When 서비스 기능 범위를 확인한다
- Then 해당 기능/API는 제공하지 않는다

- Given 사용자가 그룹 채팅 기능을 찾는다
- When 서비스 기능 범위를 확인한다
- Then 그룹 채팅 화면/API는 제공하지 않는다

- Given 관리자가 신고 처리를 한다
- When 처리가 성공한다
- Then 게시글/댓글 자동 삭제는 발생하지 않는다

## 10. Open Questions

현재 source 문서 기준 미해결 Open Question은 없다.
