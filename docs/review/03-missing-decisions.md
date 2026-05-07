# Missing Decisions

검토일: 2026-05-07  
범위: 구현 전 결정이 필요한 항목

## 1. BLOCKER 결정 사항

현재 미해결 BLOCKER 결정 사항은 없다.

이전의 화면 설계 미작성 결정 항목은 `docs/source/screen-design.md` 작성으로 해소되었다.

## 2. MAJOR 결정 사항

### D-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | API 초안은 작성되었으나 인증 방식과 공통 에러 응답 형식이 확정되지 않았다. |
| Why it matters | 인증 처리와 에러 응답 테스트를 일관되게 작성할 수 없다. |
| Suggested fix | 인증 방식과 공통 에러 응답 body를 확정하고 API 명세에 반영한다. |
| Required user decision | 세션/JWT 등 인증 방식, 공통 에러 응답 형식 |

### D-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 아이디/비밀번호 찾기의 본인확인 정보가 정의되어 있지 않다. |
| Why it matters | 계정 찾기 API와 DB 검증 조건을 만들 수 없다. |
| Suggested fix | 본인확인 정보를 요구사항과 API 명세에 추가한다. |
| Required user decision | 이메일 단독 검증인지, 이름+이메일 등 복합 검증인지 결정 |

### D-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 비밀번호 찾기 성공 후 처리 방식이 임시 비밀번호 발급인지, 재설정 화면 연결인지 결정되지 않았다. |
| Why it matters | 성공 응답, 보안 정책, 후속 화면이 달라진다. |
| Suggested fix | 비밀번호 찾기 후 재설정 방식을 요구사항/API/화면에 명시한다. |
| Required user decision | 임시 비밀번호 발급, 재설정 링크/화면, 다른 방식 중 선택 |

### D-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 회원 정보 수정 범위가 비밀번호 수정인지, 일반 회원 정보 수정인지 불명확하다. |
| Why it matters | 회원 수정 API의 request body와 권한 검증 범위가 달라진다. |
| Suggested fix | 수정 가능 필드 목록을 확정한다. |
| Required user decision | 수정 가능 필드: 비밀번호, 이름, 이메일, 닉네임, 기타 |

### D-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md` |
| Problem | 회원 탈퇴 후 메인 페이지로 이동한다고 되어 있으나 로그인 상태 유지 여부가 불명확하고 요구사항에 탈퇴 정책이 없다. |
| Why it matters | `users.status`, `deleted_at`, 세션 무효화 처리가 달라진다. |
| Suggested fix | 회원 탈퇴 요구사항과 상태 전이를 명시한다. |
| Required user decision | 탈퇴 후 자동 로그아웃 여부, 탈퇴 계정 재로그인 가능 여부 |

### D-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 알림을 언제 읽음 처리하는지, 알림 클릭 시 어디로 이동하는지 결정되어 있지 않다. |
| Why it matters | `notification.is_read` 변경 API와 화면 흐름을 확정할 수 없다. |
| Suggested fix | 알림 조회/읽음/이동 정책을 유저 플로우와 API 명세에 추가한다. |
| Required user decision | 알림 클릭 시 게시글 상세로 이동할지, 알림 목록에서 수동 읽음 처리할지 결정 |

### D-07

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 게시글 추천 취소 가능 여부가 정의되어 있지 않다. |
| Why it matters | likes API가 생성 전용인지, 토글/삭제를 포함하는지 달라진다. |
| Suggested fix | 추천 취소 허용 여부와 중복 추천 시 응답을 정의한다. |
| Required user decision | 추천 취소 허용 여부, 중복 추천 시 에러 또는 기존 상태 반환 여부 |

### D-08

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/screen-design.md` |
| Problem | 대댓글 깊이 제한이 명시되어 있지 않다. |
| Why it matters | `comments.parent_comment`를 무한 계층으로 허용할지 1단계 대댓글만 허용할지에 따라 API 검증과 조회 방식이 달라진다. |
| Suggested fix | 대댓글 깊이와 부모 댓글 검증 규칙을 명시한다. |
| Required user decision | 대댓글은 1단계만 허용할지, 다단계 nesting을 허용할지 결정 |

### D-09

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 글쓰기 완료 후 이동 위치가 명확하지 않다. |
| Why it matters | 게시글 작성 API 성공 응답과 화면 redirect 정책이 달라진다. |
| Suggested fix | 글쓰기 완료 후 이동 위치를 유저 플로우와 화면 설계에 명시한다. |
| Required user decision | 게시글 상세 페이지로 이동할지, 게시판 목록으로 이동할지 결정 |

### D-10

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 게시글 삭제 후 이동 위치가 명확하지 않다. |
| Why it matters | 삭제 API 성공 후 화면 이동과 응답 형식이 달라진다. |
| Suggested fix | 게시글 삭제 후 이동 위치를 유저 플로우와 화면 설계에 명시한다. |
| Required user decision | 삭제 후 게시판 목록으로 이동할지, 메인 페이지로 이동할지 결정 |

### D-11

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 댓글 수정/삭제 후 화면 갱신 방식이 명확하지 않다. |
| Why it matters | 댓글 API 성공 응답과 프론트 갱신 방식이 달라진다. |
| Suggested fix | 댓글 수정/삭제 후 목록 재조회, 부분 갱신, redirect 여부를 명시한다. |
| Required user decision | 댓글 변경 후 현재 화면에서 부분 갱신할지 전체 재조회할지 결정 |

### D-12

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 게시글 목록의 검색어, 작성자 필터, 페이지네이션 방식이 확정되지 않았다. |
| Why it matters | 목록 조회 API의 query parameter와 응답 메타데이터, 검색 성능 설계가 달라진다. |
| Suggested fix | 지원할 필터와 페이지네이션 방식을 요구사항/API/화면 설계에 명시한다. |
| Required user decision | `keyword`, 작성자 필터, `page/size` 또는 cursor 기반 페이지네이션 여부 |

### D-13

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 파일 업로드 방식이 URL 목록 입력인지, multipart 업로드 API인지 확정되지 않았다. |
| Why it matters | 게시글 작성/수정 request body와 파일 저장 책임이 달라진다. |
| Suggested fix | 파일 업로드 방식을 확정하고 게시글 API 또는 별도 파일 API에 반영한다. |
| Required user decision | `file_urls`만 받을지, multipart 업로드 엔드포인트를 둘지 결정 |

### D-14

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 그룹 일정 수정/삭제 권한이 명확하지 않다. |
| Why it matters | 그룹 일정 수정/삭제 API의 authorization 조건을 확정할 수 없다. |
| Suggested fix | 그룹 일정 수정/삭제 권한을 역할 또는 작성자 기준으로 명시한다. |
| Required user decision | 그룹원 전체, 일정 작성자, 그룹 리더 중 누가 수정/삭제 가능한지 결정 |

### D-15

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md`, `docs/source/logical-schema.md` |
| Problem | 그룹 생성자가 `group_members`에 자동 등록되는지와 역할이 `LEADER`인지 결정되지 않았다. |
| Why it matters | 그룹 생성 직후 생성자의 그룹 접근, 일정 관리, 목록 조회 권한이 달라진다. |
| Suggested fix | 그룹 생성 시 멤버십과 역할 생성 규칙을 요구사항/API에 반영한다. |
| Required user decision | 생성자 자동 멤버 등록 여부와 기본 역할 결정 |

### D-16

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/screen-design.md`, `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md` |
| Problem | `SC-13. 그룹 채팅`을 구현 범위에 포함할지 여부가 결정되지 않았다. |
| Why it matters | 포함한다면 메시지 저장소, 전송 API, 권한 정책, 화면 흐름이 새로 필요하다. 제외한다면 구현 계약에서 명확히 제외해야 한다. |
| Suggested fix | 채팅 기능의 포함/제외를 결정하고 관련 문서에 반영한다. |
| Required user decision | 이번 백엔드 구현 범위에 그룹 채팅을 포함할지 여부 |

### D-17

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/api-spec.md`, `docs/source/requirements.md` |
| Problem | 로그아웃 시 세션 또는 토큰 무효화 방식이 결정되지 않았다. |
| Why it matters | 인증 구현, 보안 테스트, 로그아웃 API 응답이 달라진다. |
| Suggested fix | 로그아웃 요구사항과 인증 무효화 방식을 명시한다. |
| Required user decision | 서버 세션 제거, 토큰 블랙리스트, 클라이언트 토큰 폐기 등 방식 결정 |

## 3. MINOR 결정 사항

### d-18

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/dbml.md` |
| Problem | `users.name`의 표시 의미가 이름, 닉네임, 이름 또는 닉네임으로 혼재되어 있다. |
| Why it matters | UI 라벨과 검증 메시지가 달라질 수 있다. |
| Suggested fix | 회원명 필드의 표시 명칭을 통일한다. |
| Required user decision | 이름, 닉네임, 표시명 중 어떤 용어를 사용할지 결정 |

### d-19

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md` |
| Problem | 게시글 조회수 증가 정책이 명시되어 있지 않다. |
| Why it matters | 상세 조회마다 증가할지, 같은 사용자의 반복 조회를 제한할지 구현이 달라진다. |
| Suggested fix | 조회수 증가 조건을 요구사항 또는 API에 추가한다. |
| Required user decision | 조회수 증가 시점과 중복 조회 처리 방식 결정 |
