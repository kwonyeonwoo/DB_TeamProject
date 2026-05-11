# Domain Model

기준 문서: `docs/source/requirements.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`

검증 상태: PASS - source 문서 기준 정책 충돌 없음

확정 정책:

- 회원 탈퇴 후 개인정보성 컬럼은 탈퇴 시점에 즉시 변경하지 않고, `deleted_at`으로부터 6개월 후 NULL 또는 식별 불가 값으로 변경한다.
- 댓글/대댓글 작성자와 알림 수신자가 같으면 알림을 생성하지 않는다.
- 신고 생성은 원본 API 기준 USER 기능으로 확정한다. ADMIN은 신고 목록 조회와 신고 처리만 수행한다.

## 1. 주요 도메인 객체

| 객체 | 테이블 | 설명 | 주요 기능 ID |
|---|---|---|---|
| User | `users` | 회원 계정. 로그인 아이디가 아니라 `id`가 회원 식별자다. | AUTH-001, AUTH-002, USER-001, USER-002, USER-003 |
| Post | `post` | 학업 자료 공유 게시글. 익명 여부, 주제, 조회수, 상태를 가진다. | POST-001..POST-005 |
| File | `file` | 게시글 첨부파일 저장 위치. DB에는 `file_url`만 저장한다. | POST-003, POST-004 |
| Like | `likes` | 회원의 게시글 추천 이력. 회원당 게시글 1회만 유지한다. | POST-006, POST-007 |
| Comment | `comments` | 댓글과 대댓글을 같은 테이블에 저장한다. `parent_comment`가 NULL이면 댓글, 값이 있으면 대댓글이다. | COMMENT-001..COMMENT-005 |
| Report | `report` | 게시글 또는 댓글/대댓글 신고 이력과 관리자 처리 상태를 저장한다. | REPORT-001..REPORT-003 |
| Notification | `notification` | 댓글/대댓글 발생 알림. 본인 알림만 조회한다. | NOTI-001 |
| Group | `groups` | 그룹 정보와 가입 코드를 저장한다. | GROUP-001..GROUP-004 |
| GroupMember | `group_members` | 회원과 그룹의 가입 관계 및 그룹 내 역할을 저장한다. | GROUP-002..GROUP-004, GCAL-001..GCAL-004 |
| Schedule | `schedules` | 개인 일정과 그룹 일정을 통합 저장한다. `group_id`가 NULL이면 개인 일정이다. | CAL-001..CAL-004, GCAL-001..GCAL-004 |

## 2. 객체 간 관계

| 관계 | 카디널리티 | 규칙 |
|---|---|---|
| User - Post | 1:N | 한 회원은 여러 게시글을 작성할 수 있고 게시글 하나는 한 작성자를 가진다. |
| Post - File | 1:N | 게시글 하나는 여러 첨부파일 경로를 가질 수 있다. |
| User - Like - Post | N:M | `likes`를 통해 연결된다. `UNIQUE(user_id, post_id)`로 중복 추천을 방지한다. |
| Post - Comment | 1:N | 게시글 하나는 여러 댓글/대댓글을 가진다. |
| User - Comment | 1:N | 회원 한 명은 여러 댓글/대댓글을 작성할 수 있다. |
| Comment - Comment | 1:N | 일반 댓글 하나는 여러 대댓글의 부모가 될 수 있다. 대댓글에는 다시 대댓글을 작성할 수 없다. |
| User - Report | 1:N | 회원 한 명은 여러 대상을 신고할 수 있다. |
| Report - Post/Comment | N:1 | `target_type`, `target_id`로 다형 참조한다. 실제 대상 검증은 서비스 로직에서 수행한다. |
| User - Group | 1:N | `groups.creator_id`는 최초 그룹 생성자를 기록한다. |
| User - GroupMember - Group | N:M | 회원은 여러 그룹에 가입할 수 있고 그룹은 여러 회원을 가진다. |
| User/Group - Schedule | 1:N | 개인 일정은 `group_id = NULL`, 그룹 일정은 `group_id != NULL`이다. |
| Notification - User | N:1 | `commented_user_id`가 알림 수신자다. |
| Notification - Post/Comment | N:1 | 댓글 알림은 게시글로 이동하고, 대댓글 알림은 부모 댓글 위치로 이동한다. |

## 3. 상태값과 enum

| 필드 | 값 | 설명 |
|---|---|---|
| `users.status` | `ACTIVE`, `DELETED` | 활성 또는 탈퇴 회원 |
| `users.role` | `USER`, `ADMIN` | 일반 사용자 또는 관리자 |
| `post.status` | `ACTIVE`, `DELETED` | 일반 노출 또는 삭제된 게시글 |
| `comments.status` | `ACTIVE`, `DELETED` | 일반 노출 또는 삭제된 댓글/대댓글 |
| `groups.status` | `ACTIVE`, `INACTIVE`, `DELETED` | 활성, 비활성, 삭제 그룹 |
| `group_members.role` | `LEADER`, `MEMBER` | 그룹장 또는 일반 그룹원 |
| `schedules.status` | `ACTIVE`, `DELETED` | 활성 또는 삭제된 일정 |
| `schedules.type` | `1`, `2`, `3`, `4`, `5` | 수업, 과제, 시험, 스터디, 기타 |
| `report.target_type` | `POST`, `COMMENT` | 신고 대상 유형 |
| `report.reason_type` | `1`, `2`, `3`, `4` | 부적절한 내용, 광고/도배, 저작권 침해, 기타 |
| `report.status` | `PENDING`, `PROCESSED` | 신고 미처리 또는 처리 완료 |
| `notification.is_read` | `false`, `true` | 미확인 또는 확인 알림 |

## 4. 비즈니스 규칙

### User

- 회원은 `users.id`로 식별한다.
- `login_id`, `email_address`는 중복될 수 없다. 탈퇴 후 NULL 처리된 값은 중복 판단 대상에서 제외된다.
- 회원 가입 시 `role = USER`로 생성한다.
- ADMIN은 회원 가입/API로 부여하지 않고 DB seed 또는 운영자 DB 변경으로만 부여한다.
- 탈퇴 시 회원 row는 삭제하지 않고 `status = DELETED`, `deleted_at = now()`로 변경한다.
- 탈퇴 처리 시점에는 `login_id`, `password`, `name`, `email_address`를 즉시 변경하지 않는다.
- `deleted_at`으로부터 6개월이 지난 탈퇴 회원은 개인정보 삭제 대상으로 처리하며, `login_id`, `password`, `name`, `email_address`는 NULL 값으로 변경한다. NULL 값이 들어갈 수 없는 속성은 식별할 수 없는 값으로 변경한다.

### Post and File

- 게시글은 작성자 본인만 수정/삭제할 수 있다.
- 게시글 상세 조회 시 조회수를 증가시킨다.
- 게시글 목록은 페이지 번호 기반이며 기본 정렬은 작성일 최신순이다.
- 제목/내용 검색어, 작성자, 주제 필터는 한 번에 하나만 사용한다.
- 파일은 직접 업로드 방식이다. DB에는 `/uploads/posts/{post_id}/...` 형식의 `file_url`만 저장한다.
- 게시글 수정 시 새 파일이 업로드되면 기존 파일 목록을 전체 교체하고, 새 파일이 없으면 유지한다.

### Anonymous and Deleted Author Display

- 익명 작성물은 화면에서 `익명_숫자`로 표시한다.
- 익명 번호는 하나의 게시글 상세 화면 기준으로 부여한다.
- 동일 게시글 내 동일 작성자는 항상 같은 익명 번호로 표시한다.
- 서로 다른 게시글에서는 익명 번호를 독립적으로 부여한다.
- 탈퇴 회원 작성물은 삭제하지 않고 유지한다.
- 탈퇴 회원 작성물은 익명 여부보다 `탈퇴한 유저` 표시가 우선한다.

### Comment and Reply

- 댓글과 대댓글은 `comments` 테이블에 함께 저장한다.
- `parent_comment = NULL`이면 일반 댓글이다.
- 대댓글은 일반 댓글에만 작성할 수 있고, 대댓글에는 다시 대댓글을 작성할 수 없다.
- 댓글/대댓글은 작성자 본인만 수정/삭제할 수 있다.
- 댓글/대댓글 수정 또는 삭제 후 게시글 상세 화면 전체를 갱신한다.

### Notification

- 댓글 알림의 `commented_id`는 NULL이다.
- 대댓글 알림의 `commented_id`는 부모 댓글 id다.
- 회원은 본인 알림만 조회할 수 있다.
- 본인 알림 목록 팝업을 조회하면 반환 대상 알림을 읽음 처리한다.
- 댓글 작성자가 게시글 작성자와 다른 회원이면 게시글 작성자에게 댓글 알림을 생성한다.
- 대댓글 작성자가 부모 댓글 작성자와 다른 회원이면 부모 댓글 작성자에게 대댓글 알림을 생성한다.
- 댓글/대댓글 작성자와 알림 수신자가 같은 경우 알림을 생성하지 않는다.

### Report

- USER는 게시글 또는 댓글/대댓글을 신고할 수 있다.
- ADMIN은 일반 사용자 신고 생성 API의 권한 범위에 포함하지 않는다.
- 동일 회원은 동일 신고 대상에 대해 한 번만 신고할 수 있다.
- 신고 생성 시 `status = PENDING`, `processed_by = NULL`, `processed_at = NULL`이다.
- 관리자만 신고 목록 조회와 신고 처리를 할 수 있다.
- 신고 처리는 `status`, `processed_by`, `processed_at`만 변경한다.
- 신고 처리 자체가 게시글/댓글 삭제를 자동 수행하지 않는다.

### Group

- 그룹 생성자는 `groups.creator_id`로 기록한다.
- 현재 그룹장은 `group_members.role = LEADER`로 판단한다.
- 그룹 생성자는 생성과 동시에 `group_members`에 `LEADER`로 등록된다.
- 그룹 가입은 `group_code` 입력 방식이다.
- `group_link`는 `group_code`와 같은 값이며 별도 초대 URL이 아니다.
- 그룹장 탈퇴 시 탈퇴 회원을 제외하고 가장 먼저 가입한 그룹원에게 `LEADER`를 위임한다.
- 유일한 그룹원이 탈퇴하면 그룹은 `INACTIVE`와 `deleted_at` 기준으로 비활성화된다.

### Schedule

- 개인 일정은 본인만 조회/등록/수정/삭제할 수 있다.
- 그룹 일정은 해당 그룹원만 조회/등록/수정/삭제할 수 있다.
- 모든 그룹원은 자신이 속한 그룹 캘린더의 그룹 일정을 자유롭게 변경할 수 있다.
- 종료 일시는 시작 일시보다 빠를 수 없다.

## 5. Assumptions

- 비밀번호 해시 알고리즘, 세션 저장소, 파일명 충돌 회피 방식은 구현 세부사항으로 둔다.
- 삭제된 게시글/댓글/일정의 일반 조회 제외는 `status` 기준으로 처리한다.

## 6. Open Questions

현재 source 문서 기준 미해결 Open Question은 없다.
