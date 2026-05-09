# Spec Conflict List

검토일: 2026-05-09
검토 범위: `docs/source` 문서 간 충돌
검토 기준: 최신 `requirements.md`, `screen-design.md`, `api-spec.md`

## 1. 미해결 BLOCKER 충돌

### C-B01

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 요구사항과 API는 탈퇴 회원의 게시글/댓글/개인 캘린더/개인 일정과 유일 그룹의 비활성화 및 6개월 삭제 대기를 요구하지만 DB에는 이를 표현할 상태, 삭제대기, 삭제예정일 계약이 없다. |
| Why it matters | 탈퇴 기능 구현 시 데이터 보존/비활성화/삭제 기준을 지킬 수 없다. |
| Suggested fix | 관련 엔티티의 상태 컬럼, 삭제 대기 일시, 삭제 예정 일시, 조회 제외 정책을 DB 문서에 추가한다. |
| Required user decision | 비활성화/삭제대기 상태값과 6개월 후 실제 삭제 방식 |

### C-B02

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 요구사항과 API는 그룹장 탈퇴 시 가장 먼저 가입한 그룹원에게 권한을 위임한다고 하지만 `group_members`에 가입 일시가 없다. |
| Why it matters | "가장 먼저 가입한 그룹원"을 판정할 데이터가 없다. |
| Suggested fix | `group_members`에 가입 일시를 추가하고 위임 후 `groups.creator_id` 처리 정책을 확정한다. |
| Required user decision | 위임 후 `groups.creator_id`를 갱신할지, 최초 생성자 기록으로 유지할지 결정 |

### C-B03

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 요구사항과 API는 이메일 중복 불가를 명시하지만 DB 스키마는 `email_address` 유니크 제약을 두지 않는다. |
| Why it matters | 회원 가입 요구사항과 데이터 무결성이 DB에서 보장되지 않는다. |
| Suggested fix | `users.email_address` 유니크 제약을 DB 문서에 추가한다. |
| Required user decision | 없음 |

## 2. 미해결 MAJOR 충돌

### C-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 계정 찾기가 이메일 기반으로 정의되었지만 유저 플로우에는 계정 찾기 상세 흐름이 없다. |
| Why it matters | 로그인 전 계정 복구 동작의 화면 전환을 검증할 수 없다. |
| Suggested fix | 아이디 찾기, 비밀번호 찾기, 비밀번호 재설정 화면 이동 흐름을 유저 플로우에 추가한다. |
| Required user decision | 아이디 표시 마스킹 여부 결정 |

### C-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API는 이름/이메일/비밀번호 수정과 탈퇴 후 로그인 페이지 이동을 정의하지만 유저 플로우는 탈퇴 후 메인 페이지 이동으로 남아 있다. |
| Why it matters | 회원 정보 수정 범위와 탈퇴 후 인증 상태가 유저 플로우와 충돌한다. |
| Suggested fix | 유저 플로우를 최신 회원 정보 수정/탈퇴 정책에 맞춘다. |
| Required user decision | 현재 비밀번호 확인 필요 여부 |

### C-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항과 API에는 알림 조회/읽음 처리가 있으나 유저 플로우에는 알림 진입 경로가 없다. |
| Why it matters | 알림 조회 API가 어느 화면에서 사용되는지 확정할 수 없다. |
| Suggested fix | 메인 페이지 또는 마이 페이지 등에 알림 목록 진입 흐름을 추가한다. |
| Required user decision | 알림 목록 진입 위치 결정 |

### C-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 게시글 추천/추천 취소와 대댓글 작성이 있으나 유저 플로우에는 추천 액션과 대댓글 흐름이 없다. |
| Why it matters | 게시글 상세 화면의 주요 상호작용이 유저 플로우에서 누락된다. |
| Suggested fix | 게시글 상세 페이지 흐름에 추천/추천 취소와 대댓글 흐름을 추가한다. |
| Required user decision | 없음 |

### C-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 개인 일정 조회/수정/삭제와 그룹 일정 삭제가 있으나 유저 플로우에는 일부 흐름이 명확하지 않다. |
| Why it matters | 일정 수정/삭제 권한과 화면 이동을 검증하기 어렵다. |
| Suggested fix | 개인 일정 선택/수정/삭제와 그룹 일정 삭제 흐름을 유저 플로우에 추가한다. |
| Required user decision | 개인 일정 상세/수정 UI 방식 결정 |

## 3. MINOR 충돌

### c-06

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/dbml.md` |
| Problem | `users.name`의 사용자 표시 명칭이 대부분 이름으로 정리되었지만 DBML note에는 닉네임으로 남아 있다. |
| Why it matters | 구현 필드는 같지만 문서 라벨이 달라질 수 있다. |
| Suggested fix | 회원명 필드의 표시 이름을 이름으로 통일한다. |
| Required user decision | 없음 |

### c-07

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md` |
| Problem | 게시글 조회수 증가 조건이 요구사항에 없다. |
| Why it matters | 조회수 증가 정책이 구현 중 임의로 결정될 수 있다. |
| Suggested fix | 상세 조회 시 증가 여부와 중복 조회 처리 방식을 명시한다. |
| Required user decision | 조회수 증가 정책 결정 |

### c-08

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md` |
| Problem | 그룹 캘린더 유지 정보 목록 끝에 빈 항목이 남아 있다. |
| Why it matters | 누락된 요구사항 항목이 있는 것처럼 보일 수 있다. |
| Suggested fix | 빈 항목을 제거하거나 내용을 채운다. |
| Required user decision | 없음 |

## 4. 해소된 주요 충돌

| 항목 | 결과 |
|---|---|
| 계정 찾기 본인확인 | 요구사항, 화면, API 모두 이메일 기준으로 정리됨 |
| 비밀번호 찾기 후 처리 | 요구사항, 화면, API 모두 재설정 화면 이동 기준으로 정리됨 |
| 로그아웃 방식 | 요구사항과 API 모두 서버 세션 무효화 기준으로 정리됨 |
| 회원 탈퇴 API | API에 `DELETED`, `deleted_at`, 세션 무효화, 데이터 비활성화/삭제대기, 그룹장 위임 부수효과가 반영됨 |
| 파일 업로드 | 화면과 API 모두 `multipart/form-data` 직접 업로드 기준으로 정리됨 |
| 글쓰기/삭제 후 이동 | 화면과 API client behavior가 게시판 첫 번째 페이지 이동 기준으로 정리됨 |
| 게시글 목록 정책 | 화면과 API 모두 페이지 번호 기반, 최신순, 단일 필터 기준으로 정리됨 |
| 추천 취소 | API에 `DELETE /api/posts/{post_id}/likes` 추가됨 |
| 대댓글 깊이 | API와 화면 모두 대댓글에 다시 대댓글 작성 불가로 정리됨 |
| 그룹 생성자 역할 | API와 화면 모두 `group_members`에 `LEADER` 자동 등록으로 정리됨 |
| 그룹 채팅 | 요구사항, 화면, API 모두 구현 제외로 정리됨 |
| 그룹 일정 권한 | API와 화면 모두 모든 그룹원 수정/삭제 가능으로 정리됨 |

## 5. 기존 DB 문서 간 해소 상태

| 항목 | 결과 |
|---|---|
| `users.status` | ERD, 논리 스키마, 물리 스키마, DBML 모두 `ACTIVE`, `DELETED` 상태값 기준으로 통일됨 |
| `file` | ERD, 논리 스키마, 물리 스키마, DBML 모두 `id`, `file_url` 구조로 통일됨 |
| `notification` FK | ERD, 논리 스키마, 물리 스키마, DBML 모두 `post.id`, `users.id`, `comments.id` 참조로 통일됨 |
| `notification.comment_content` | FK가 아닌 알림 표시용 댓글 내용으로 통일됨 |
| `likes` 추천 제약 | DB의 `UNIQUE(user_id, post_id)`는 추천 1회 등록 요구사항과 일치하며, API에는 추천 취소가 추가됨 |
