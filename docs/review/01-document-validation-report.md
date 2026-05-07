# Document Validation Report

검토일: 2026-05-07  
검토 범위: `docs/source` 전체 문서

## 1. 문서 인벤토리

| 문서 | 상태 | 검토 결과 |
|---|---|---|
| `docs/source/requirements.md` | 작성됨 | 주요 기능 요구사항은 있으나 일부 결정이 누락되어 있다. |
| `docs/source/user-flow.md` | 작성됨 | 주요 화면 흐름은 정리되었으나 요구사항 일부 흐름이 빠져 있다. |
| `docs/source/screen-design.md` | 미작성 | `TODO` 상태다. |
| `docs/source/api-spec.md` | 미작성 | `TODO` 상태다. |
| `docs/source/erd.md` | 작성됨 | 논리/물리 스키마와 주요 DB 구조가 일치한다. |
| `docs/source/logical-schema.md` | 작성됨 | ERD/물리/DBML과 주요 DB 구조가 일치한다. |
| `docs/source/physical-schema.md` | 작성됨 | ERD/논리/DBML과 주요 DB 구조가 일치한다. |
| `docs/source/dbml.md` | 작성됨 | ERD/논리/물리 스키마와 주요 DB 구조가 일치한다. |

## 2. 요약

| 심각도 | 건수 | 내용 |
|---|---:|---|
| BLOCKER | 2 | API 명세와 화면 설계가 아직 작성되지 않았다. |
| MAJOR | 8 | 요구사항과 유저 플로우 간 누락 또는 결정 필요 흐름이 있다. |
| MINOR | 0 | 현재 고위험 검토 범위에서는 별도 기록 없음 |
| QUESTION | 0 | `03-missing-decisions.md`에 MAJOR 결정 항목으로 통합 기록 |

## 3. BLOCKER

### B-01

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/api-spec.md` |
| Problem | API 명세가 `TODO` 상태다. 엔드포인트, 요청/응답 필드, 상태 코드, 인증/인가 규칙이 정의되어 있지 않다. |
| Why it matters | API 명세 없이 백엔드를 구현하면 요구사항, 화면, DB 스키마와의 추적성이 깨지고 구현 범위를 검증할 수 없다. |
| Suggested fix | 요구사항, 유저 플로우, DB 스키마를 기준으로 API 명세를 작성한다. |
| Required user decision | API 리소스 경로, 인증 방식, 에러 응답 형식, 파일 업로드 방식 결정 |

### B-02

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/screen-design.md` |
| Problem | 화면 설계가 `TODO` 상태다. 화면별 입력 필드, 버튼, 검증, 에러 표시, 화면 전환이 정의되어 있지 않다. |
| Why it matters | 화면 액션과 API/백엔드 동작을 매핑할 수 없어 API 명세와 구현 계획을 확정할 수 없다. |
| Suggested fix | 유저 플로우를 기준으로 화면별 필드, 액션, 검증 규칙, 에러 메시지, 연결 API를 작성한다. |
| Required user decision | 각 화면에 노출할 필드와 버튼, 실패/성공 시 화면 이동 방식 결정 |

## 4. MAJOR

### M-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/logical-schema.md` |
| Problem | 아이디 찾기와 비밀번호 찾기의 “본인확인 정보”가 정의되어 있지 않고, 유저 플로우와 DB 스키마에도 해당 흐름/필드가 없다. |
| Why it matters | 계정 찾기 기능의 입력값, 검증 기준, API, 저장 데이터가 확정되지 않는다. |
| Suggested fix | 본인확인에 사용할 정보와 절차를 요구사항, 유저 플로우, API 명세에 반영한다. |
| Required user decision | 본인확인 정보로 이메일만 사용할지, 이름+이메일 등 복합 정보를 사용할지 결정 |

### M-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 요구사항은 비밀번호 수정만 명시하지만, 유저 플로우는 “회원 정보 수정”으로 더 넓게 표현한다. |
| Why it matters | 수정 가능한 회원 정보 범위가 불명확하면 API 필드와 권한 검증이 흔들린다. |
| Suggested fix | 회원 정보 수정 가능 범위를 요구사항과 화면 설계에 명시한다. |
| Required user decision | 비밀번호만 수정 가능한지, 이름/이메일/닉네임 등도 수정 가능한지 결정 |

### M-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/logical-schema.md` |
| Problem | 유저 플로우에는 회원 탈퇴가 있으나 요구사항에는 회원 탈퇴 기능과 정책이 명시되어 있지 않다. |
| Why it matters | `users.status`, `deleted_at`의 사용 조건과 탈퇴 후 로그인/조회 가능 여부를 구현할 수 없다. |
| Suggested fix | 회원 탈퇴 요구사항과 탈퇴 후 상태 전이를 추가한다. |
| Required user decision | 탈퇴를 soft delete로 처리할지, 탈퇴 후 자동 로그아웃할지 결정 |

### M-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 알림 조회 요구사항은 있으나 유저 플로우에는 알림 화면, 진입 경로, 읽음 처리 흐름이 없다. |
| Why it matters | 알림 API와 화면 연결, `notification.is_read` 변경 시점이 확정되지 않는다. |
| Suggested fix | 알림 목록 진입, 알림 상세 또는 관련 게시글 이동, 읽음 처리 흐름을 유저 플로우와 화면 설계에 추가한다. |
| Required user decision | 알림 클릭 시 읽음 처리 여부와 이동 대상 결정 |

### M-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 게시글 추천 요구사항은 있으나 유저 플로우에는 추천 액션이 없다. |
| Why it matters | 추천 버튼 위치, 중복 추천 실패 처리, 추천 취소 가능 여부를 API/화면에 반영할 수 없다. |
| Suggested fix | 게시글 상세 페이지에 추천 흐름을 추가한다. |
| Required user decision | 추천 취소 기능을 허용할지, 중복 추천 시 메시지를 어떻게 처리할지 결정 |

### M-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/erd.md` |
| Problem | 대댓글 작성/수정/삭제 요구사항과 DB 구조는 있으나 유저 플로우에는 대댓글 흐름이 없다. |
| Why it matters | `comments.parent_comment`를 사용하는 화면/API 동작이 명확하지 않다. |
| Suggested fix | 댓글 하위에 대댓글 작성, 수정, 삭제, 공개 여부 확인 흐름을 추가한다. |
| Required user decision | 대댓글 깊이를 1단계로 제한할지, 대댓글의 공개/비밀 정책을 댓글과 동일하게 적용할지 결정 |

### M-07

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 개인 일정 조회/수정/삭제 요구사항은 있으나 유저 플로우는 일정 추가만 명확히 표현한다. |
| Why it matters | 개인 일정 API와 화면 동작 중 조회, 수정, 삭제 범위가 빠진다. |
| Suggested fix | 개인 캘린더에서 일정 선택, 상세 조회, 수정, 삭제 흐름을 추가한다. |
| Required user decision | 일정 선택 시 상세 화면을 사용할지, 모달/인라인 편집을 사용할지 결정 |

### M-08

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md` |
| Problem | 그룹 일정 삭제 요구사항은 있으나 유저 플로우에는 그룹 일정 삭제 흐름이 명확하지 않다. |
| Why it matters | 그룹 일정 삭제 권한과 화면/API 동작을 검증하기 어렵다. |
| Suggested fix | 그룹 캘린더 또는 그룹 관리 페이지에 그룹 일정 삭제 흐름을 추가한다. |
| Required user decision | 그룹원 누구나 삭제 가능한지, 작성자/리더만 삭제 가능한지 결정 |

## 5. DB 스키마 검토 결과

`erd.md`, `logical-schema.md`, `physical-schema.md`, `dbml.md` 간의 이전 DB 구조 충돌은 현재 기준으로 해소되었다.

- `users.status`: `ACTIVE`, `DELETED` 상태값으로 통일
- `file`: `id`, `file_url` 복합 PK와 `id → post.id` 참조로 통일
- `notification`: `commented_post_id → post.id`, `commented_user_id → users.id`, `commented_id → comments.id`로 통일
- `notification.comment_content`: FK가 아닌 알림 표시용 댓글 내용으로 통일
