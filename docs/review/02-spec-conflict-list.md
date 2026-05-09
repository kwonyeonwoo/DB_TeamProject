# Spec Conflict List

검토일: 2026-05-07  
검토 범위: `docs/source` 문서 간 충돌

## 1. 미해결 BLOCKER

현재 문서 간 DB 구조 충돌이나 핵심 문서 미작성으로 인한 BLOCKER는 없다.

이전의 `screen-design.md` 미작성 BLOCKER는 화면 설계서 작성으로 해소되었다.

## 2. 미해결 MAJOR 충돌

### C-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 아이디/비밀번호 찾기가 있으나 유저 플로우에는 계정 찾기 화면과 상세 흐름이 없다. |
| Why it matters | 로그인 전 계정 복구 동작의 시작 상태, 사용자 액션, 시스템 응답, 종료 상태를 검증할 수 없다. |
| Suggested fix | 로그인 페이지에서 아이디 찾기, 비밀번호 찾기 흐름을 `user-flow.md`에 추가한다. |
| Required user decision | 계정 찾기 입력 정보와 결과 표시 방식을 결정해야 한다. |

### C-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항과 API는 비밀번호 수정 중심인데 유저 플로우와 화면 설계는 회원 정보 수정을 더 넓게 표현한다. |
| Why it matters | 회원 정보 수정 API와 화면 필드 범위가 충돌할 수 있다. |
| Suggested fix | 수정 가능한 회원 정보 범위를 하나로 맞춘다. |
| Required user decision | 비밀번호 외 정보 수정 허용 여부 결정 |

### C-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md` |
| Problem | 유저 플로우, 화면 설계, API에는 회원 탈퇴가 있으나 요구사항에는 회원 탈퇴 기능이 없다. |
| Why it matters | 탈퇴 기능을 구현할 근거와 상태 전이 규칙이 부족하다. |
| Suggested fix | 회원 탈퇴 요구사항을 추가하거나 유저 플로우/화면/API에서 구현 범위 제외로 표시한다. |
| Required user decision | 회원 탈퇴 기능 포함 여부와 탈퇴 후 상태 결정 |

### C-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항과 API에는 알림 조회/읽음 처리가 있으나 유저 플로우와 화면 설계에는 독립적인 알림 진입 경로가 없다. |
| Why it matters | 알림 조회 API가 어느 화면에서 사용되는지와 읽음 처리가 언제 발생하는지 확정할 수 없다. |
| Suggested fix | 메인 페이지, 마이 페이지, 게시글 상세 등 명확한 위치에 알림 목록 진입 흐름을 추가한다. |
| Required user decision | 알림 진입 위치와 읽음 처리 정책 결정 |

### C-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 게시글 추천이 있으나 유저 플로우에는 추천 액션이 없다. |
| Why it matters | 게시글 상세 화면의 추천 성공/실패 흐름과 중복 추천 처리를 검증하기 어렵다. |
| Suggested fix | 게시글 상세 페이지 흐름에 추천 버튼과 처리 결과를 추가한다. |
| Required user decision | 추천 취소 허용 여부와 중복 추천 처리 방식 결정 |

### C-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 대댓글 작성/수정/삭제가 있으나 유저 플로우에는 대댓글 흐름이 없다. |
| Why it matters | 댓글 화면과 API에서 `parent_comment` 사용 방식이 빠진다. |
| Suggested fix | 게시글 상세 페이지의 댓글 흐름 아래 대댓글 흐름을 추가한다. |
| Required user decision | 대댓글 깊이와 비밀 대댓글 권한 정책 결정 |

### C-07

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 개인 일정 조회/수정/삭제가 있으나 유저 플로우에는 개인 일정 추가만 있다. |
| Why it matters | 개인 캘린더 기능이 등록 중심으로만 해석될 수 있다. |
| Suggested fix | 개인 일정 선택, 조회, 수정, 삭제 흐름을 추가한다. |
| Required user decision | 상세 화면 또는 모달 등 일정 편집 UI 방식 결정 |

### C-08

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 그룹 일정 삭제가 있으나 유저 플로우에는 그룹 일정 삭제 흐름이 명확하지 않다. |
| Why it matters | 그룹 일정 삭제 권한과 화면/API 동작을 확정할 수 없다. |
| Suggested fix | 그룹 일정 조회 후 수정/삭제 흐름을 명확히 추가한다. |
| Required user decision | 그룹 일정 삭제 권한 결정 |

### C-09

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/screen-design.md`, `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md` |
| Problem | `SC-13. 그룹 채팅`은 화면 설계에 있으나 요구사항, API, DB 스키마에는 채팅 기능이 없다. |
| Why it matters | 화면 설계가 백엔드 동작을 암시하지만 구현 계약이 없어서 범위를 확정할 수 없다. |
| Suggested fix | 채팅을 포함하려면 요구사항/API/DB를 추가하고, 제외하려면 화면 설계에 구현 제외로 계속 표시한다. |
| Required user decision | 채팅 기능 포함 여부 결정 |

### C-10

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/screen-design.md`, `docs/source/requirements.md`, `docs/source/api-spec.md` |
| Problem | `SC-04`는 검색어, 작성자 필터, 페이지네이션 입력을 암시하지만 요구사항과 API는 해당 조건을 완전히 정의하지 않는다. |
| Why it matters | 게시글 목록 조회 API의 query parameter와 화면 필터 동작이 달라질 수 있다. |
| Suggested fix | 지원 검색 조건과 페이지네이션 방식을 요구사항/API/화면에 맞춘다. |
| Required user decision | 검색어, 작성자 필터, page/size 또는 cursor 방식 결정 |

### C-11

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/api-spec.md`, `docs/source/requirements.md` |
| Problem | 로그아웃은 유저 플로우와 API에 있으나 요구사항에는 명시되어 있지 않다. |
| Why it matters | API 구현의 요구사항 추적성이 부족하고 인증 무효화 방식이 확정되지 않는다. |
| Suggested fix | 로그아웃 요구사항과 성공 후 상태를 요구사항에 추가한다. |
| Required user decision | 로그아웃 시 세션/토큰 무효화 방식 결정 |

### C-12

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md`, `docs/source/screen-design.md` |
| Problem | 그룹 생성자는 `groups.creator_id`가 되지만, 동시에 `group_members`에 포함되는지와 역할이 `LEADER`인지 확정되지 않았다. |
| Why it matters | 그룹 생성 직후 권한, 그룹 목록 조회, 그룹 일정 접근 조건이 문서마다 다르게 해석될 수 있다. |
| Suggested fix | 생성자를 그룹 멤버로 자동 등록하는지와 역할을 요구사항/API에 명시한다. |
| Required user decision | 생성자의 멤버십 및 역할 정책 결정 |

## 3. MINOR 충돌

### c-13

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/dbml.md` |
| Problem | `users.name`의 사용자 표시 명칭이 이름, 이름 또는 닉네임, 닉네임으로 혼재되어 있다. |
| Why it matters | 구현 필드는 같지만 UI 라벨과 검증 메시지가 달라질 수 있다. |
| Suggested fix | 회원명 필드의 표시 이름을 통일한다. |
| Required user decision | 이름/닉네임/표시명 중 하나로 결정 |

## 4. 해소된 주요 충돌

| 항목 | 결과 |
|---|---|
| 화면 설계 문서 | `screen-design.md`가 작성되어 이전 미작성 BLOCKER 해소 |
| `users.status` | ERD, 논리 스키마, 물리 스키마, DBML 모두 `ACTIVE`, `DELETED` 상태값 기준으로 통일됨 |
| `file` | ERD, 논리 스키마, 물리 스키마, DBML 모두 `id`, `file_url` 구조로 통일됨 |
| `notification` FK | ERD, 논리 스키마, 물리 스키마, DBML 모두 `post.id`, `users.id`, `comments.id` 참조로 통일됨 |
| `notification.comment_content` | FK가 아닌 알림 표시용 댓글 내용으로 통일됨 |
| `likes` 추천 제약 | 요구사항의 “게시글당 한 번만 추천”과 DB의 `UNIQUE(user_id, post_id)`가 일치함 |
