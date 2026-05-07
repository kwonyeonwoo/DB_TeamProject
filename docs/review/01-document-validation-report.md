# Document Validation Report

검토일: 2026-05-07  
검토 범위: `docs/source` 전체 문서

## 1. 문서 인벤토리

| 문서 | 상태 | 검토 결과 |
|---|---|---|
| `docs/source/requirements.md` | 작성됨 | 주요 기능 요구사항은 있으나 일부 화면/API가 요구사항보다 넓고, 검증 규칙과 권한 정책 일부가 미정이다. |
| `docs/source/user-flow.md` | 작성됨 | 주요 흐름은 정리되었으나 계정 찾기, 알림, 추천, 대댓글, 일정 수정/삭제 흐름이 부족하다. |
| `docs/source/screen-design.md` | 작성됨 | PDF 기준 `SC-01`부터 `SC-13`까지 작성되었고, 미확정 기능은 Open Questions로 분리되어 있다. |
| `docs/source/api-spec.md` | 작성됨 | 현재 문서 기준 API 초안이 작성되었고, 인증 방식/공통 에러/일부 정책은 `TBD` 또는 Open Questions로 남아 있다. |
| `docs/source/erd.md` | 작성됨 | 논리/물리 스키마와 주요 DB 구조가 일치한다. |
| `docs/source/logical-schema.md` | 작성됨 | ERD/물리/DBML과 주요 DB 구조가 일치한다. |
| `docs/source/physical-schema.md` | 작성됨 | ERD/논리/DBML과 주요 DB 구조가 일치한다. |
| `docs/source/dbml.md` | 작성됨 | ERD/논리/물리 스키마와 주요 DB 구조가 일치한다. |

## 2. 요약

| 심각도 | 건수 | 내용 |
|---|---:|---|
| BLOCKER | 0 | 현재 문서 검증 기준 즉시 중단해야 하는 미작성 핵심 문서는 없다. |
| MAJOR | 13 | 구현 전 요구사항, 유저 플로우, 화면, API 사이에 맞춰야 할 범위/정책 이슈가 남아 있다. |
| MINOR | 2 | 명명과 표시 정책 수준의 보완 항목이 있다. |
| QUESTION | 0 | 결정 필요 사항은 `03-missing-decisions.md`에 MAJOR/MINOR 결정 항목으로 통합 기록했다. |

## 3. BLOCKER

현재 미해결 BLOCKER는 없다.

이전의 `screen-design.md` 미작성 BLOCKER는 `SC-01`부터 `SC-13`까지 화면 설계가 작성되어 해소되었다.

## 4. MAJOR

### M-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 아이디 찾기와 비밀번호 찾기의 “본인확인 정보”가 정의되어 있지 않고, 유저 플로우에도 상세 흐름이 없다. |
| Why it matters | 계정 찾기 기능의 입력값, 검증 기준, API 요청/응답, 실패 처리를 확정할 수 없다. |
| Suggested fix | 본인확인에 사용할 정보와 절차를 요구사항, 유저 플로우, API 명세, 화면 설계에 동일하게 반영한다. |
| Required user decision | 본인확인 정보로 이메일만 사용할지, 이름+이메일 등 복합 정보를 사용할지 결정 |

### M-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항은 비밀번호 수정만 명시하지만, 유저 플로우와 화면 설계는 “회원 정보 수정”으로 더 넓게 표현한다. API는 비밀번호 수정만 제공한다. |
| Why it matters | 수정 가능한 회원 정보 범위가 불명확하면 API 필드와 권한 검증이 흔들린다. |
| Suggested fix | 회원 정보 수정 가능 범위를 하나로 확정하고 요구사항, 화면, API를 맞춘다. |
| Required user decision | 비밀번호만 수정 가능한지, 이름/이메일/닉네임 등도 수정 가능한지 결정 |

### M-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md` |
| Problem | 유저 플로우, 화면 설계, API에는 회원 탈퇴가 있으나 요구사항에는 회원 탈퇴 기능과 정책이 명시되어 있지 않다. |
| Why it matters | `users.status`, `deleted_at`의 사용 조건과 탈퇴 후 로그인/조회 가능 여부를 구현할 근거가 부족하다. |
| Suggested fix | 회원 탈퇴 요구사항과 탈퇴 후 상태 전이를 추가한다. |
| Required user decision | 탈퇴를 soft delete로 처리할지, 탈퇴 후 자동 로그아웃할지 결정 |

### M-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 알림 조회/읽음 API와 DB는 있으나 유저 플로우와 화면 설계에는 별도 알림 화면, 진입 경로, 읽음 처리 흐름이 부족하다. |
| Why it matters | 알림 API와 화면 연결, `notification.is_read` 변경 시점이 확정되지 않는다. |
| Suggested fix | 알림 목록 진입, 알림 클릭 또는 수동 읽음 처리, 관련 게시글 이동 흐름을 유저 플로우와 화면 설계에 추가한다. |
| Required user decision | 알림 클릭 시 읽음 처리 여부와 이동 대상 결정 |

### M-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 게시글 추천 요구사항/API/화면 버튼은 있으나 유저 플로우에는 추천 액션이 없다. |
| Why it matters | 추천 버튼 위치, 중복 추천 실패 처리, 추천 취소 가능 여부를 흐름 관점에서 검증하기 어렵다. |
| Suggested fix | 게시글 상세 페이지 흐름에 추천 버튼과 성공/실패 처리를 추가한다. |
| Required user decision | 추천 취소 기능을 허용할지, 중복 추천 시 메시지를 어떻게 처리할지 결정 |

### M-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/erd.md` |
| Problem | 대댓글 작성/수정/삭제 요구사항, 화면, API, DB 구조는 있으나 유저 플로우에는 대댓글 흐름이 없다. |
| Why it matters | `comments.parent_comment`를 사용하는 화면/API 동작과 대댓글 깊이 정책이 명확하지 않다. |
| Suggested fix | 댓글 하위에 대댓글 작성, 수정, 삭제, 공개 여부 확인 흐름을 추가한다. |
| Required user decision | 대댓글 깊이를 1단계로 제한할지, 대댓글의 공개/비밀 정책을 댓글과 동일하게 적용할지 결정 |

### M-07

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 개인 일정 조회/수정/삭제 요구사항, 화면, API는 있으나 유저 플로우는 일정 추가만 명확히 표현한다. |
| Why it matters | 개인 일정 API와 화면 동작 중 상세 조회, 수정, 삭제 범위가 흐름에서 누락된다. |
| Suggested fix | 개인 캘린더에서 일정 선택, 상세 조회, 수정, 삭제 흐름을 추가한다. |
| Required user decision | 일정 선택 시 상세 화면을 사용할지, 모달/인라인 편집을 사용할지 결정 |

### M-08

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 그룹 일정 삭제 요구사항, 화면, API는 있으나 유저 플로우에는 그룹 일정 삭제 흐름이 명확하지 않다. |
| Why it matters | 그룹 일정 삭제 권한과 화면/API 동작을 검증하기 어렵다. |
| Suggested fix | 그룹 캘린더 또는 그룹 관리 페이지에 그룹 일정 삭제 흐름을 추가한다. |
| Required user decision | 그룹원 누구나 삭제 가능한지, 작성자/리더만 삭제 가능한지 결정 |

### M-09

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | API 초안은 작성되었으나 인증 방식, 공통 에러 응답 형식, 일부 성공 응답 정책이 `TBD`로 남아 있다. |
| Why it matters | 백엔드 구현과 테스트에서 인증 처리와 에러 응답 형식이 일관되지 않을 수 있다. |
| Suggested fix | 인증 방식과 공통 에러 응답 body를 확정하고 API 명세 및 화면 실패 메시지 정책에 반영한다. |
| Required user decision | 세션/JWT 등 인증 방식, 공통 에러 응답 body 형식 결정 |

### M-10

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/screen-design.md`, `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md` |
| Problem | `SC-13. 그룹 채팅`은 PDF/화면 설계에 있으나 요구사항, API, DB 스키마에는 채팅 기능이 없다. |
| Why it matters | 화면이 백엔드 기능을 암시하지만 구현 계약과 데이터 모델이 없어 구현 범위가 흔들린다. |
| Suggested fix | 채팅을 범위에 포함한다면 요구사항/API/DB를 추가하고, 제외한다면 화면 설계에서 명시적으로 구현 제외 상태를 유지한다. |
| Required user decision | 그룹 채팅 기능을 이번 구현 범위에 포함할지 여부 |

### M-11

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/screen-design.md`, `docs/source/requirements.md`, `docs/source/api-spec.md` |
| Problem | `SC-04`는 검색어, 작성자 필터, 페이지네이션을 암시하지만 요구사항은 게시글 조회만 명시하고 API는 `main_category`, `sub_category`와 페이지네이션 Open Question만 둔다. |
| Why it matters | 목록 조회 API의 query parameter와 검색 인덱스, 화면 검증을 확정할 수 없다. |
| Suggested fix | 검색어/작성자 필터와 페이지네이션 방식을 요구사항과 API에 명시한다. |
| Required user decision | 지원할 검색 조건과 페이지네이션 방식 결정 |

### M-12

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md` |
| Problem | 그룹 생성자가 `group_members`에 자동 포함되는지와 `LEADER` 역할을 갖는지가 확정되지 않았다. |
| Why it matters | 그룹 생성 직후 생성자의 그룹 상세/그룹 일정 접근 권한과 중복 가입 검증이 달라진다. |
| Suggested fix | 그룹 생성 시 `groups.creator_id`와 `group_members` 생성 정책을 요구사항/API에 명시한다. |
| Required user decision | 생성자를 그룹 멤버로 자동 등록할지, 역할을 `LEADER`로 둘지 결정 |

### M-13

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/user-flow.md`, `docs/source/api-spec.md`, `docs/source/requirements.md` |
| Problem | 로그아웃은 유저 플로우와 API에 있으나 요구사항에는 명시적인 로그아웃 요구사항이 없다. |
| Why it matters | 인증 세션/토큰 무효화 정책과 API 테스트의 요구사항 추적성이 부족하다. |
| Suggested fix | 로그아웃 요구사항과 성공 후 이동/세션 무효화 정책을 요구사항과 API에 반영한다. |
| Required user decision | 로그아웃 시 세션 또는 토큰을 어떻게 무효화할지 결정 |

## 5. MINOR

### m-01

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/dbml.md` |
| Problem | 회원 이름 필드가 요구사항에서는 이름, 화면/API에서는 이름 또는 닉네임, DBML note에서는 닉네임으로 표현된다. |
| Why it matters | 필드명은 같지만 화면 라벨과 검증 메시지가 달라질 수 있다. |
| Suggested fix | `users.name`의 의미를 이름, 닉네임, 표시명 중 하나로 통일한다. |
| Required user decision | 사용자에게 표시할 회원명 필드의 명칭 결정 |

### m-02

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md` |
| Problem | `post.view_count`는 DB/API/화면에 있으나 조회 시 증가 조건은 명시되지 않았다. |
| Why it matters | 조회수 증가가 상세 조회마다 발생하는지, 중복 조회를 제한하는지 구현 방식이 달라질 수 있다. |
| Suggested fix | 조회수 증가 정책을 요구사항 또는 API에 보완한다. |
| Required user decision | 게시글 상세 조회 시 조회수를 언제 증가시킬지 결정 |

## 6. DB 스키마 검토 결과

`erd.md`, `logical-schema.md`, `physical-schema.md`, `dbml.md` 간의 이전 DB 구조 충돌은 현재 기준으로 해소되었다.

- `users.status`: `ACTIVE`, `DELETED` 상태값으로 통일
- `file`: `id`, `file_url` 복합 PK와 `id → post.id` 참조로 통일
- `notification`: `commented_post_id → post.id`, `commented_user_id → users.id`, `commented_id → comments.id`로 통일
- `notification.comment_content`: FK가 아닌 알림 표시용 댓글 내용으로 통일
- `likes`: `UNIQUE(user_id, post_id)`로 회원당 게시글 1회 추천 요구사항과 일치
- `schedules`: `group_id = NULL`이면 개인 일정, 값이 있으면 그룹 일정으로 통일

## 7. 다음 단계 판단

문서 작성 자체의 BLOCKER는 해소되었지만, MAJOR 이슈가 남아 있으므로 백엔드 구현으로 바로 넘어가면 안 된다. 먼저 `03-missing-decisions.md`의 결정 항목을 확정하거나, 미확정 항목을 명시적으로 구현 범위에서 제외한 뒤 normalized specification을 갱신해야 한다.
