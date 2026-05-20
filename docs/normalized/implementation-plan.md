# Implementation Plan

기준 문서:

- `docs/normalized/product-spec.md`
- `docs/normalized/feature-list.md`
- `docs/normalized/domain-model.md`
- `docs/normalized/api-contract.md`
- `docs/normalized/db-schema-contract.md`
- `docs/normalized/auth-policy.md`
- `docs/normalized/acceptance-criteria.md`

구현 시작 판단: PASS

주의: 이 문서는 구현 계획이다. 현재 단계에서는 backend 코드를 작성하지 않는다.

확정 정책:

- 회원 탈퇴 후 개인정보성 컬럼은 탈퇴 시점에 즉시 변경하지 않고, `deleted_at`으로부터 6개월 후 NULL 또는 식별 불가 값으로 변경한다.
- 자기 게시글/댓글에 댓글 또는 대댓글을 작성한 경우 알림을 생성하지 않는다.

## 1. 구현 원칙

- 기능은 작은 그룹 단위로 구현한다.
- 하나의 구현/검증 단위는 지정된 Feature ID 범위의 코드와 테스트만 포함한다.
- 타 기능 파일 변경이 필요하면 같은 변경에 섞지 않고 별도 feature bundle, patch, PR, 또는 리뷰 문서로 분리한다.
- 공통 인프라 변경은 예외적으로 허용하되, 영향을 받는 Feature ID와 이유를 구현 계획 또는 리뷰에 명시한다.
- 범위 혼입이 발견되면 기능 구현 적합성과 별개로 `NEEDS_FIX`로 판정한다.
- 모든 API는 요구사항, API 계약, DB 계약, acceptance criteria에 trace되어야 한다.
- 모든 기능 그룹에는 테스트를 포함한다.
- 구현 중 문서와 코드가 충돌하면 구현을 멈추고 문서를 우선 확인한다.
- 구현 제외 항목은 API, 화면 진입점, 테스트 기대값으로 만들지 않는다.

## 2. 구현 순서 개요

| 단계 | 기능 그룹 | 기능 ID | 목적 |
|---:|---|---|---|
| 1 | 프로젝트 기반/DB | 공통 | DB migration, 공통 에러 응답, 세션 기반 인증 기반 |
| 2 | 인증/회원 기본 | AUTH-001..AUTH-003, USER-001..USER-002 | 계정, 세션, 내 정보 조회/수정 구현 |
| 3 | 게시글/파일/추천 조회 | POST-001..POST-005, POST 응답 추천 필드 | 게시글 CRUD, 파일 업로드/교체, `liked_by_me`/`like_count` 읽기 계약 구현 |
| 4 | 댓글/알림 | COMMENT-001..COMMENT-005, NOTI-001 | 댓글/대댓글과 알림 생성/조회 구현 |
| 5 | 추천/신고 | POST-006..POST-007, REPORT-001..REPORT-003 | 추천 토글, USER 신고 생성, 관리자 신고 처리 구현 |
| 6 | 일정 | CAL-001..CAL-004 | 개인 일정 CRUD 구현 |
| 7 | 그룹/그룹 일정 | GROUP-001..GROUP-004, GCAL-001..GCAL-004 | 그룹 가입 코드, 멤버십, 그룹 일정 구현 |
| 8 | 회원 탈퇴 통합 생명주기 | USER-003 | 일정/그룹 의존 로직이 준비된 뒤 탈퇴 생명주기 구현 |
| 9 | 통합 검증 | 전체 | 권한, 상태, 회귀 테스트 및 문서 trace 점검 |

## 3. DB Migration 순서

1. `users`
2. `post`
3. `likes`
4. `comments`
5. `report`
6. `groups`
7. `group_members`
8. `schedules`
9. `file`
10. `notification`
11. indexes
12. seed data for ADMIN if required by local development

Migration requirements:

- `users.role` default는 `USER`다.
- `report.status` default는 `PENDING`이다.
- `processed_by`는 nullable FK to `users.id`다.
- `file`은 `(id, file_url)` 복합 PK다.
- `group_members`는 `(group_id, user_id)` 복합 PK다.
- 다형 신고 대상인 `report.target_id`는 단일 FK로 만들지 않는다.

## 4. 기능 그룹별 구현 계획

### 4-1. 기반

Tasks:

- 공통 에러 응답 serializer를 만든다.
- 인증 필요 middleware 또는 guard를 만든다.
- 현재 사용자 조회 helper를 만든다.
- `author_display_name` 계산 helper를 만든다.
- `users.status`, `users.deleted_at`, `updated_at` 처리 패턴을 정한다.

Tests:

- 인증 없는 요청은 `401`을 반환한다.
- 권한 없는 요청은 `403`을 반환한다.
- 에러 응답은 `code`, `message`를 포함한다.

### 4-2. 인증/회원

API order:

1. `POST /api/auth/signup`
2. `POST /api/auth/login`
3. `POST /api/auth/logout`
4. `GET /api/users/me`
5. `PATCH /api/users/me`
6. `DELETE /api/users/me`는 일정/그룹 기능 구현 이후 통합 생명주기 단계에서 구현한다.

Implementation notes:

- 회원 가입 request body는 `role`을 받지 않는다.
- 로그인은 `DELETED` 회원을 거부한다.
- 비밀번호 변경 시 `current_password`를 검증한다.
- 탈퇴는 회원 row를 삭제하지 않고 `status`, `deleted_at`으로 표현한다.
- 탈퇴 처리 시점에는 개인정보성 컬럼을 즉시 변경하지 않는다.
- `deleted_at`으로부터 6개월이 지난 탈퇴 회원은 개인정보 삭제 대상으로 처리하며, `login_id`, `password`, `name`, `email_address`를 NULL 값으로 변경한다. NULL 값이 들어갈 수 없는 속성은 식별할 수 없는 값으로 변경한다.
- 개인 일정 즉시 삭제, 전체 그룹 탈퇴, `groups.leader_id` 기반 그룹장 위임, 유일 그룹 즉시 삭제는 일정/그룹 기능 구현 이후 통합 단계에서 구현한다.

Tests:

- 회원 가입 성공/필수값 누락/중복.
- 로그인 성공/실패/탈퇴 회원 거부.
- 내 정보 수정 성공/동일값/현재 비밀번호 누락/불일치/이메일 중복.
- 탈퇴 후 로그인 불가와 작성물 유지.
- 개인 일정 즉시 삭제, 모든 그룹 탈퇴, 그룹장 위임, 유일 그룹 즉시 삭제는 통합 생명주기 테스트에서 검증.

### 4-3. 게시글/파일

API order:

1. `GET /api/posts`
2. `POST /api/posts`
3. `GET /api/posts/{post_id}`
4. `PATCH /api/posts/{post_id}`
5. `DELETE /api/posts/{post_id}`

Implementation notes:

- 필터 종류는 `keyword`, `author`, `category` 중 하나만 허용한다.
- 상세 조회 시 조회수를 증가시킨다.
- Post 응답 계약의 `liked_by_me`, `like_count`는 추천 등록/취소 API보다 먼저 게시글 조회에서 읽기 전용으로 제공할 수 있어야 한다.
- 파일은 로컬 `/uploads/posts/{post_id}/{UUID[.extension]}`에 저장하고 DB에는 `file_url`, `file_name`, `content_type`을 저장한다.
- 첨부파일은 `/api/posts/{post_id}/files/{file_name}` 다운로드 API와 `/api/uploads/posts/{post_id}/{file_name}` 정적 리소스 경로로 제공한다.
- 수정 시 새 파일이 있으면 기존 파일 목록을 전체 교체한다.
- 게시글 삭제 시 추천, 댓글/대댓글, 첨부파일, 알림은 FK cascade 정책에 따라 함께 삭제한다.
- 삭제된 게시글 또는 함께 삭제된 댓글/대댓글 대상 신고 이력은 유지하고 관리자 신고 목록에서 `삭제된 대상`으로 표시한다.

Tests:

- 목록 최신순/페이지/단일 필터/복수 필터 실패.
- 상세 조회 조회수 증가.
- 목록/상세 응답의 `liked_by_me`, `like_count`.
- 작성 성공과 파일 URL 저장.
- 수정 시 파일 교체/유지.
- 작성자 아닌 수정/삭제 `403`.

### 4-4. 댓글/알림

API order:

1. `GET /api/posts/{post_id}/comments`
2. `POST /api/posts/{post_id}/comments`
3. `POST /api/comments/{comment_id}/replies`
4. `PATCH /api/comments/{comment_id}`
5. `DELETE /api/comments/{comment_id}`
6. `GET /api/notifications`

Implementation notes:

- 댓글과 대댓글은 `comments`에 함께 저장한다.
- 대댓글의 부모는 일반 댓글이어야 한다.
- 댓글/대댓글 생성 시 대상 작성자에게 알림을 생성한다.
- 작성자와 대상 작성자가 같은 경우 알림을 생성하지 않는다.
- 알림 조회 시 반환 대상 알림을 읽음 처리한다.
- 작성자 표시명은 탈퇴 작성자가 익명보다 우선한다.
- 일반 댓글 삭제 시 대댓글은 FK cascade 정책에 따라 함께 삭제한다.
- 댓글/대댓글 삭제 후에도 기존 알림은 유지하고 `notification.comment_content`는 변경하지 않는다.
- 삭제된 댓글 또는 함께 삭제된 대댓글 대상 신고 이력은 유지하고 관리자 신고 목록에서 `삭제된 대상`으로 표시한다.

Tests:

- 댓글/대댓글 생성.
- 대댓글에 대댓글 작성 실패.
- 작성자 아닌 수정/삭제 실패.
- 다른 사용자에게 발생하는 알림 생성, 댓글 알림 `commented_id = NULL`, 대댓글 알림 `commented_id = parent_comment`.
- `commented_id`는 이동용 hint로만 사용하고 댓글 삭제 cascade FK로 사용하지 않는다.
- `comment_content`는 알림 생성 시점의 스냅샷으로 저장하고 원본 댓글/대댓글 수정 또는 삭제 후에도 갱신하지 않는다.
- 자기 게시글/댓글에 대한 댓글/대댓글 작성 시 알림 미생성.
- 본인 알림 조회와 읽음 처리.

### 4-5. 추천/신고

API order:

1. `POST /api/posts/{post_id}/likes`
2. `DELETE /api/posts/{post_id}/likes`
3. `POST /api/reports`
4. `GET /api/admin/reports`
5. `PATCH /api/admin/reports/{report_id}`

Implementation notes:

- `likes(user_id, post_id)` unique로 중복 추천을 방지한다.
- 신고 생성은 USER 권한 기준으로 구현한다. ADMIN은 `POST /api/reports` 권한 범위에 포함하지 않는다.
- 신고 대상 존재 여부는 `target_type`에 따라 서비스 로직에서 검증한다.
- 신고 생성 시 `PENDING`, 처리자/처리시각 NULL로 저장한다.
- 신고 처리는 `PROCESSED`, `processed_by`, `processed_at`만 변경한다.
- 신고 처리로 게시글/댓글을 자동 삭제하지 않는다.
- 관리자 신고 목록은 신고 대상 row를 조회할 수 없으면 `target_display_name = 삭제된 대상`을 반환한다.

Tests:

- 추천 등록/중복 실패/취소/추천 없는 취소 실패.
- USER 신고 생성/중복 실패/대상 없음 실패.
- ADMIN의 `POST /api/reports` 호출은 `403`을 반환한다.
- USER의 관리자 API 접근 실패.
- ADMIN 신고 목록 조회/처리 성공.
- 신고 처리 후 대상 게시글/댓글 유지.

### 4-6. 개인 일정

API order:

1. `GET /api/me/schedules`
2. `POST /api/me/schedules`
3. `PATCH /api/me/schedules/{schedule_id}`
4. `DELETE /api/me/schedules/{schedule_id}`

Implementation notes:

- 개인 일정은 `group_id = NULL`이다.
- 본인 일정만 접근 가능하다.
- 목록 조회의 `start_at`, `end_at`은 ISO-8601 optional query다.
- 목록 조회에서 `start_at`과 `end_at`이 모두 없으면 본인의 전체 개인 일정을 반환한다.
- 목록 조회에서 `start_at`과 `end_at`이 모두 있으면 `schedules.start_at <= end_at AND schedules.end_at >= start_at`인 기간 겹침 일정을 반환한다.
- 목록 조회에서 `start_at` 또는 `end_at` 한쪽만 있으면 열린 기간 조건으로 조회한다.
- 목록 조회의 날짜 형식 오류와 `end_at < start_at`은 `400`으로 처리한다.
- `type`은 1..5만 허용한다.
- `end_at >= start_at`을 검증한다.
- 삭제는 일정 row 삭제로 처리한다.

Tests:

- 본인 일정 전체 목록 조회와 기간 겹침 조회.
- 개인 일정 목록 조회의 날짜 형식 오류와 `end_at < start_at` 실패.
- 등록/수정/삭제 성공.
- 종료가 시작보다 빠른 경우 실패.
- 타인 일정 접근 실패.

### 4-7. 그룹/그룹 일정

API order:

1. `GET /api/groups`
2. `POST /api/groups`
3. `POST /api/groups/join`
4. `GET /api/groups/{group_id}`
5. `GET /api/groups/{group_id}/schedules`
6. `POST /api/groups/{group_id}/schedules`
7. `PATCH /api/groups/{group_id}/schedules/{schedule_id}`
8. `DELETE /api/groups/{group_id}/schedules/{schedule_id}`

Implementation notes:

- 그룹 생성 시 `group_code`를 생성한다.
- 생성자는 `groups.leader_id`와 `group_members.role = LEADER`로 자동 등록한다.
- 가입은 `group_code` 입력 방식이다.
- 이미 가입한 그룹은 중복 가입할 수 없다.
- 그룹 상세와 그룹 일정은 그룹원만 접근한다.
- 그룹 일정 목록 조회의 `start_at`, `end_at`은 ISO-8601 optional query다.
- 그룹 일정 목록 조회에서 `start_at`과 `end_at`이 모두 없으면 해당 그룹의 전체 그룹 일정을 반환한다.
- 그룹 일정 목록 조회에서 `start_at`과 `end_at`이 모두 있으면 `schedules.start_at <= end_at AND schedules.end_at >= start_at`인 기간 겹침 일정을 반환한다.
- 그룹 일정 목록 조회에서 `start_at` 또는 `end_at` 한쪽만 있으면 열린 기간 조건으로 조회한다.
- 그룹 일정 목록 조회의 날짜 형식 오류와 `end_at < start_at`은 `400`으로 처리한다.
- 모든 그룹원은 그룹 일정을 등록/수정/삭제할 수 있다.

Tests:

- 내 그룹 목록은 가입한 그룹만 반환.
- 그룹 생성 시 코드 반환과 LEADER 등록.
- 유효 코드 가입/잘못된 코드/중복 가입.
- 그룹원이 아닌 상세/일정 접근 실패.
- 그룹 일정 전체 목록 조회와 기간 겹침 조회.
- 그룹 일정 목록 조회의 날짜 형식 오류와 `end_at < start_at` 실패.
- 그룹 일정 CRUD와 저장 시간 검증.

## 5. API 구현 순서 상세

| 순서 | API | 선행 조건 |
|---:|---|---|
| 1 | `POST /api/auth/signup` | `users` migration |
| 2 | `POST /api/auth/login` | password/session 기반 |
| 3 | `POST /api/auth/logout` | session 기반 |
| 4 | `GET/PATCH /api/users/me` | auth guard |
| 5 | `GET/POST /api/posts` | `post`, `file` |
| 6 | `GET/PATCH/DELETE /api/posts/{post_id}` | post ownership |
| 7 | `POST/DELETE /api/posts/{post_id}/likes` | `likes` |
| 8 | comments/replies APIs | `comments`, notification helper |
| 9 | `GET /api/notifications` | `notification` |
| 10 | reports/admin reports APIs | `report`, user/admin guard, comments target support |
| 11 | personal schedules APIs | `schedules` |
| 12 | groups APIs | `groups`, `group_members` |
| 13 | group schedules APIs | group membership guard |
| 14 | `DELETE /api/users/me` | schedules/groups implemented |

## 6. 테스트 작성 순서

1. 공통 에러 응답, 인증 guard, 권한 guard 테스트
2. DB migration/schema smoke test
3. AUTH/USER 기본 API 단위 및 통합 테스트
4. POST/File/추천 조회 필드 API 통합 테스트
5. COMMENT/Notification API 통합 테스트
6. Like/Report/Admin API 통합 테스트
7. Personal Schedule API 통합 테스트
8. Group/Group Schedule API 통합 테스트
9. 탈퇴 생명주기와 6개월 후 개인정보 처리 회귀 테스트
10. 문서 trace 확인 테스트 목록 정리
11. 기능별 변경 범위 확인

## 7. 위험도 높은 부분

| 위험 | 이유 | 완화 |
|---|---|---|
| 회원 탈퇴 처리 | 사용자, 일정, 그룹 멤버십, 그룹장 위임, 6개월 후 개인정보 처리가 함께 연결된다. | 일정/그룹 구현 뒤 트랜잭션 경계를 명확히 하고 케이스별 테스트를 분리한다. |
| 파일 업로드/교체 | 게시글 생성 후 post_id 기반 경로가 필요하고 수정 시 전체 교체 정책이 있다. | DB 저장과 파일 저장 순서를 명확히 하고 실패 시 롤백 또는 보상 처리를 설계한다. |
| 익명 표시명 | 게시글 상세 화면 기준 동일 작성자에 같은 번호를 부여해야 한다. | 응답 조립 helper를 만들고 목록/상세 테스트를 분리한다. |
| 신고 다형 참조 | `target_type`, `target_id`는 DB 단일 FK가 아니다. | 서비스 로직에서 target 존재 여부를 강제 검증한다. |
| 알림 이동 데이터 | 댓글 알림과 대댓글 알림의 `commented_id` nullable 규칙이 다르며 삭제 cascade FK가 아니다. | 댓글/대댓글 생성 테스트에서 알림 필드를 검증하고, 댓글/대댓글 삭제 후 기존 알림과 `comment_content`가 유지되는지 검증한다. |
| 자기 대상 알림 미생성 정책 | 자기 게시글/댓글에는 알림을 만들지 않아야 하므로 생성 조건 분기가 필요하다. | 다른 사용자 대상 알림 생성과 자기 대상 알림 미생성 테스트를 함께 둔다. |
| 그룹장 위임 | 탈퇴 시 joined_at 기준으로 LEADER를 재지정해야 한다. | 정렬 기준 테스트와 유일 그룹원 테스트를 작성한다. |

## 8. Assumptions

- 구현 프레임워크, DBMS, ORM, 테스트 러너는 아직 이 문서에서 특정하지 않는다.
- 비밀번호 해시와 세션 저장소는 보안 기본값을 사용하되 API 계약을 변경하지 않는다.
- 6개월 경과 탈퇴 회원의 개인정보성 컬럼 처리 실행 방식은 구현 세부사항이며, source의 6개월 후 처리 정책과 API 계약을 바꾸지 않는다.

## 9. Open Questions

현재 source 문서 기준 미해결 Open Question은 없다.
