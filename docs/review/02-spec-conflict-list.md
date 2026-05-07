# Spec Conflict List

검토일: 2026-05-07  
검토 범위: `docs/source` 문서 간 충돌

## 1. 미해결 BLOCKER

현재 문서 간 DB 구조 충돌 중 BLOCKER는 없다. 다만 API 명세와 화면 설계가 미작성 상태라 `01-document-validation-report.md`에 BLOCKER로 기록했다.

## 2. 미해결 MAJOR 충돌

### C-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항에는 아이디/비밀번호 찾기가 있으나 유저 플로우에는 계정 찾기 화면과 흐름이 없다. |
| Why it matters | 로그인 전 계정 복구 동작을 화면/API로 연결할 수 없다. |
| Suggested fix | 로그인 페이지에서 아이디 찾기, 비밀번호 찾기 흐름을 추가한다. |
| Required user decision | 계정 찾기 입력 정보와 결과 표시 방식을 결정해야 한다. |

### C-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항은 비밀번호 수정만 명시하지만 유저 플로우는 회원 정보 수정 페이지를 둔다. |
| Why it matters | 회원 정보 수정 API와 화면 필드 범위가 충돌할 수 있다. |
| Suggested fix | 수정 가능한 회원 정보 범위를 하나로 맞춘다. |
| Required user decision | 비밀번호 외 정보 수정 허용 여부 결정 |

### C-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 유저 플로우에는 회원 탈퇴가 있으나 요구사항에는 회원 탈퇴 기능이 없다. |
| Why it matters | 탈퇴 기능을 구현할 근거와 상태 전이 규칙이 부족하다. |
| Suggested fix | 회원 탈퇴 요구사항을 추가하거나 유저 플로우에서 제거한다. |
| Required user decision | 회원 탈퇴 기능 포함 여부와 탈퇴 후 상태 결정 |

### C-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항에는 알림 조회가 있으나 유저 플로우에는 알림 진입 경로가 없다. |
| Why it matters | 알림 조회 API가 어느 화면에서 사용되는지 확정할 수 없다. |
| Suggested fix | 메인 페이지 또는 마이 페이지 등에 알림 목록 진입 흐름을 추가한다. |
| Required user decision | 알림 진입 위치와 읽음 처리 정책 결정 |

### C-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항에는 게시글 추천이 있으나 유저 플로우에는 추천 액션이 없다. |
| Why it matters | 게시글 상세 화면과 API에 추천 기능을 반영하기 어렵다. |
| Suggested fix | 게시글 상세 페이지 흐름에 추천 버튼과 처리 결과를 추가한다. |
| Required user decision | 추천 취소 허용 여부와 중복 추천 처리 방식 결정 |

### C-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항에는 대댓글 작성/수정/삭제가 있으나 유저 플로우에는 대댓글 흐름이 없다. |
| Why it matters | 댓글 화면과 API에서 `parent_comment` 사용 방식이 빠진다. |
| Suggested fix | 게시글 상세 페이지의 댓글 흐름 아래 대댓글 흐름을 추가한다. |
| Required user decision | 대댓글 깊이와 비밀 대댓글 권한 정책 결정 |

### C-07

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항에는 개인 일정 조회/수정/삭제가 있으나 유저 플로우에는 개인 일정 추가만 있다. |
| Why it matters | 개인 캘린더 기능이 등록 중심으로만 해석될 수 있다. |
| Suggested fix | 개인 일정 선택, 조회, 수정, 삭제 흐름을 추가한다. |
| Required user decision | 상세 화면 또는 모달 등 일정 편집 UI 방식 결정 |

### C-08

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항에는 그룹 일정 삭제가 있으나 유저 플로우에는 그룹 일정 삭제 흐름이 명확하지 않다. |
| Why it matters | 그룹 일정 삭제 권한과 화면/API 동작을 확정할 수 없다. |
| Suggested fix | 그룹 일정 조회 후 수정/삭제 흐름을 명확히 추가한다. |
| Required user decision | 그룹 일정 삭제 권한 결정 |

## 3. 해소된 주요 충돌

| 항목 | 결과 |
|---|---|
| `users.status` | ERD, 논리 스키마, 물리 스키마, DBML 모두 `ACTIVE`, `DELETED` 상태값 기준으로 통일됨 |
| `file` | ERD, 논리 스키마, 물리 스키마, DBML 모두 `id`, `file_url` 구조로 통일됨 |
| `notification` FK | ERD, 논리 스키마, 물리 스키마, DBML 모두 `post.id`, `users.id`, `comments.id` 참조로 통일됨 |
| `notification.comment_content` | FK가 아닌 알림 표시용 댓글 내용으로 통일됨 |
