# Feature List

기준 문서: `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md`

검증 상태: PASS - source 문서 기준 구현 전 보류 기능 없음

주의:

- `USER-003`은 회원 상태, 세션, 작성물 유지, 개인 일정, 그룹 멤버십, 그룹장 위임을 포함하는 교차 도메인 기능이다. 개인정보성 컬럼은 탈퇴 시점에 즉시 변경하지 않고 `deleted_at`으로부터 6개월 후 NULL 또는 식별 불가 값으로 변경한다.
- `REPORT-001`은 원본 API 기준 USER 신고 생성으로 확정한다. ADMIN은 `REPORT-002`, `REPORT-003`만 수행한다.
- `NOTI-001`은 조회/읽음 처리뿐 아니라 댓글/대댓글 작성 시 생성되는 알림 데이터와 연결된다. 자기 게시글/댓글에 댓글 또는 대댓글을 작성한 경우 알림을 생성하지 않는다.

우선순위:

- P0: 구현 기반 또는 주요 사용자 흐름
- P1: 핵심 기능 보조 흐름
- P2: 관리자/운영 또는 후속 검증 흐름

| 기능 ID | 기능명 | 관련 요구사항 | 관련 화면/플로우 | 관련 API | 관련 DB 테이블 | 우선순위 |
|---|---|---|---|---|---|---|
| AUTH-001 | 회원 가입 | 1-1, 1-5 | SC-02, UF-02 | `POST /api/auth/signup` | `users` | P0 |
| AUTH-002 | 로그인 | 1-2 | SC-01, UF-03 | `POST /api/auth/login` | `users` | P0 |
| AUTH-003 | 로그아웃 | 1-2 | SC-06, UF-03 | `POST /api/auth/logout` | 세션 저장소 | P0 |
| USER-001 | 내 정보 조회 | 1-3 | SC-11, UF-05 | `GET /api/users/me` | `users` | P0 |
| USER-002 | 내 정보 수정 | 1-3 | SC-11, UF-05 | `PATCH /api/users/me` | `users` | P0 |
| USER-003 | 회원 탈퇴 및 생명주기 정리 | 1-4 | SC-11, UF-06 | `DELETE /api/users/me` | `users`, `schedules`, `groups`, `group_members` | P0 |
| NOTI-001 | 내 알림 목록 조회 및 읽음 처리 | 2-1, 2-2, 2-3 | SC-07, UF-07 | `GET /api/notifications` | `notification`, `post`, `comments`, `users` | P1 |
| POST-001 | 게시글 목록 조회/검색 | 3-3, 4-5, 4-6 | SC-08, UF-08, UF-14 | `GET /api/posts` | `post`, `users`, `likes` | P0 |
| POST-002 | 게시글 상세 조회 | 3-3, 4-5, 4-6 | SC-09, UF-10, UF-14 | `GET /api/posts/{post_id}` | `post`, `file`, `comments`, `users`, `likes` | P0 |
| POST-003 | 게시글 작성 및 파일 업로드 | 3-1 | SC-10, UF-09 | `POST /api/posts` | `post`, `file` | P0 |
| POST-004 | 게시글 수정 | 3-2 | SC-10, UF-10 | `PATCH /api/posts/{post_id}` | `post`, `file` | P0 |
| POST-005 | 게시글 삭제 | 3-2 | SC-09, UF-10 | `DELETE /api/posts/{post_id}` | `post`, `file`, `comments`, `likes`, `notification` | P0 |
| POST-006 | 게시글 추천 등록 | 3-4 | SC-09, UF-11 | `POST /api/posts/{post_id}/likes` | `likes`, `post` | P1 |
| POST-007 | 게시글 추천 취소 | 3-4 | SC-09, UF-11 | `DELETE /api/posts/{post_id}/likes` | `likes`, `post` | P1 |
| COMMENT-001 | 댓글 목록 조회 | 4-2, 4-4, 4-5, 4-6 | SC-09, UF-12, UF-13, UF-14 | `GET /api/posts/{post_id}/comments` | `comments`, `users` | P0 |
| COMMENT-002 | 댓글 작성 | 4-1, 2-1 | SC-09, UF-12 | `POST /api/posts/{post_id}/comments` | `comments`, `notification`, `post` | P0 |
| COMMENT-003 | 대댓글 작성 | 4-3, 2-1 | SC-09, UF-13 | `POST /api/comments/{comment_id}/replies` | `comments`, `notification`, `post` | P0 |
| COMMENT-004 | 댓글/대댓글 수정 | 4-2, 4-4 | SC-09, UF-12, UF-13 | `PATCH /api/comments/{comment_id}` | `comments` | P0 |
| COMMENT-005 | 댓글/대댓글 삭제 | 4-2, 4-4 | SC-09, UF-12, UF-13 | `DELETE /api/comments/{comment_id}` | `comments`, `notification` | P0 |
| REPORT-001 | 게시글/댓글 신고(USER) | 1-5, 3-5 | SC-09, UF-11A | `POST /api/reports` | `report`, `post`, `comments`, `users` | P1 |
| REPORT-002 | 관리자 신고 목록 조회 | 1-5, 3-5 | SC-18, UF-18 | `GET /api/admin/reports` | `report`, `users` | P2 |
| REPORT-003 | 관리자 신고 처리 | 1-5, 3-5 | SC-18, UF-18 | `PATCH /api/admin/reports/{report_id}` | `report`, `users` | P2 |
| CAL-001 | 개인 일정 목록 조회 | 5-1, 5-3 | SC-12, UF-15 | `GET /api/me/schedules` | `schedules` | P1 |
| CAL-002 | 개인 일정 등록 | 5-2 | SC-13, UF-15 | `POST /api/me/schedules` | `schedules` | P1 |
| CAL-003 | 개인 일정 수정 | 5-3 | SC-13, UF-15 | `PATCH /api/me/schedules/{schedule_id}` | `schedules` | P1 |
| CAL-004 | 개인 일정 삭제 | 5-3 | SC-13, UF-15 | `DELETE /api/me/schedules/{schedule_id}` | `schedules` | P1 |
| GROUP-001 | 내 그룹 목록 조회 | 6-1 | SC-14, UF-16 | `GET /api/groups` | `groups`, `group_members` | P1 |
| GROUP-002 | 그룹 생성 | 6-2 | SC-14, UF-16 | `POST /api/groups` | `groups`, `group_members` | P1 |
| GROUP-003 | 그룹 가입 코드로 가입 | 6-1, 6-2 | SC-14, UF-16 | `POST /api/groups/join` | `groups`, `group_members` | P1 |
| GROUP-004 | 그룹 상세 조회 | 6-1, 6-2, 7-1 | SC-15, UF-16, UF-17 | `GET /api/groups/{group_id}` | `groups`, `group_members`, `users` | P1 |
| GCAL-001 | 그룹 일정 목록 조회 | 7-1, 7-2 | SC-16, UF-17 | `GET /api/groups/{group_id}/schedules` | `schedules`, `groups`, `group_members` | P1 |
| GCAL-002 | 그룹 일정 등록 | 7-2 | SC-16, UF-17 | `POST /api/groups/{group_id}/schedules` | `schedules`, `groups`, `group_members` | P1 |
| GCAL-003 | 그룹 일정 수정 | 7-2 | SC-16, UF-17 | `PATCH /api/groups/{group_id}/schedules/{schedule_id}` | `schedules`, `groups`, `group_members` | P1 |
| GCAL-004 | 그룹 일정 삭제 | 7-2 | SC-16, UF-17 | `DELETE /api/groups/{group_id}/schedules/{schedule_id}` | `schedules`, `groups`, `group_members` | P1 |

## Excluded Feature IDs

| 제외 ID | 제외 기능 | 근거 |
|---|---|---|
| X-001 | 아이디 찾기 | 요구사항 1-2에서 구현 제외 |
| X-002 | 비밀번호 찾기/재설정 | 요구사항 1-2에서 구현 제외 |
| X-003 | ADMIN 권한 부여/회수 API | 요구사항 1-5에서 구현 제외 |
| X-004 | 그룹 채팅 | 요구사항 6-3에서 구현 제외 |
| X-005 | 복잡한 초대 링크/외부 공유/만료/재발급 | 요구사항 6-2에서 구현 제외 |
| X-006 | 파일 메타데이터 저장 | 요구사항 3-1에서 구현 제외 |
| X-007 | 신고 처리 시 게시글/댓글 자동 삭제 | 요구사항 3-5에서 구현 제외 |

## Pending Decision Feature Notes

현재 source 문서 기준 기능 구현을 막는 보류 결정은 없다.
