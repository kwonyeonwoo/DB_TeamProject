# Missing Decisions

검토일: 2026-05-07  
범위: 구현 전 결정이 필요한 항목

## 1. BLOCKER 결정 사항

### D-01

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/api-spec.md` |
| Problem | API 명세가 작성되지 않아 백엔드 구현 계약이 없다. |
| Why it matters | 엔드포인트, 요청/응답, 상태 코드, 인증/인가, 에러 형식을 확정하지 않으면 구현과 테스트를 시작할 수 없다. |
| Suggested fix | 요구사항과 유저 플로우를 기준으로 API 명세를 먼저 작성한다. |
| Required user decision | API URL 체계, 인증 방식, 공통 에러 응답 형식, 파일 업로드 방식 |

### D-02

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/screen-design.md` |
| Problem | 화면 설계가 작성되지 않아 화면 액션과 API를 매핑할 수 없다. |
| Why it matters | 화면별 입력 필드와 검증 규칙이 없으면 API 필드와 유효성 검사를 확정할 수 없다. |
| Suggested fix | 유저 플로우 기반 화면 설계를 작성하고 화면별 API 연결을 명시한다. |
| Required user decision | 각 화면의 입력 필드, 버튼, 성공/실패 메시지, 화면 이동 방식 |

## 2. MAJOR 결정 사항

### D-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md` |
| Problem | 아이디/비밀번호 찾기의 본인확인 정보가 정의되어 있지 않다. |
| Why it matters | 계정 찾기 API와 DB 검증 조건을 만들 수 없다. |
| Suggested fix | 본인확인 정보를 요구사항과 API 명세에 추가한다. |
| Required user decision | 이메일 단독 검증인지, 이름+이메일 등 복합 검증인지 결정 |

### D-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 회원 정보 수정 범위가 비밀번호 수정인지, 일반 회원 정보 수정인지 불명확하다. |
| Why it matters | 회원 수정 API의 request body와 권한 검증 범위가 달라진다. |
| Suggested fix | 수정 가능 필드 목록을 확정한다. |
| Required user decision | 수정 가능 필드: 비밀번호, 이름, 이메일, 닉네임, 기타 |

### D-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/requirements.md` |
| Problem | 회원 탈퇴 후 메인 페이지로 이동한다고 되어 있으나 로그인 상태 유지 여부가 불명확하고 요구사항에 탈퇴 정책이 없다. |
| Why it matters | `users.status`, `deleted_at`, 세션 무효화 처리가 달라진다. |
| Suggested fix | 회원 탈퇴 요구사항과 상태 전이를 명시한다. |
| Required user decision | 탈퇴 후 자동 로그아웃 여부, 탈퇴 계정 재로그인 가능 여부 |

### D-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 알림을 언제 읽음 처리하는지, 알림 클릭 시 어디로 이동하는지 결정되어 있지 않다. |
| Why it matters | `notification.is_read` 변경 API와 화면 흐름을 확정할 수 없다. |
| Suggested fix | 알림 조회/읽음/이동 정책을 유저 플로우와 API 명세에 추가한다. |
| Required user decision | 알림 클릭 시 게시글 상세로 이동할지, 알림 목록에서 수동 읽음 처리할지 결정 |

### D-07

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md` |
| Problem | 게시글 추천 취소 가능 여부가 정의되어 있지 않다. |
| Why it matters | likes API가 생성 전용인지, 토글/삭제를 포함하는지 달라진다. |
| Suggested fix | 추천 취소 허용 여부와 중복 추천 시 응답을 정의한다. |
| Required user decision | 추천 취소 허용 여부, 중복 추천 시 에러 또는 기존 상태 반환 여부 |

### D-08

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/erd.md` |
| Problem | 대댓글 깊이 제한이 명시되어 있지 않다. |
| Why it matters | `comments.parent_comment`를 무한 계층으로 허용할지 1단계 대댓글만 허용할지에 따라 API 검증과 조회 방식이 달라진다. |
| Suggested fix | 대댓글 깊이와 부모 댓글 검증 규칙을 명시한다. |
| Required user decision | 대댓글은 1단계만 허용할지, 다단계 nesting을 허용할지 결정 |

### D-09

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md` |
| Problem | 글쓰기 완료 후 이동 위치가 명확하지 않다. |
| Why it matters | 게시글 작성 API 성공 응답과 화면 redirect 정책이 달라진다. |
| Suggested fix | 글쓰기 완료 후 이동 위치를 유저 플로우와 화면 설계에 명시한다. |
| Required user decision | 게시글 상세 페이지로 이동할지, 게시판 목록으로 이동할지 결정 |

### D-10

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md` |
| Problem | 게시글 삭제 후 이동 위치가 명확하지 않다. |
| Why it matters | 삭제 API 성공 후 화면 이동과 응답 형식이 달라진다. |
| Suggested fix | 게시글 삭제 후 이동 위치를 유저 플로우와 화면 설계에 명시한다. |
| Required user decision | 삭제 후 게시판 목록으로 이동할지, 메인 페이지로 이동할지 결정 |

### D-11

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md` |
| Problem | 댓글 수정/삭제 후 화면 갱신 방식이 명확하지 않다. |
| Why it matters | 댓글 API 성공 응답과 프론트 갱신 방식이 달라진다. |
| Suggested fix | 댓글 수정/삭제 후 목록 재조회, 부분 갱신, redirect 여부를 명시한다. |
| Required user decision | 댓글 변경 후 현재 화면에서 부분 갱신할지 전체 재조회할지 결정 |

### D-12

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 그룹 일정 삭제 권한이 명확하지 않다. |
| Why it matters | 그룹 일정 삭제 API의 authorization 조건을 확정할 수 없다. |
| Suggested fix | 그룹 일정 수정/삭제 권한을 역할 또는 작성자 기준으로 명시한다. |
| Required user decision | 그룹원 전체, 일정 작성자, 그룹 리더 중 누가 삭제 가능한지 결정 |
