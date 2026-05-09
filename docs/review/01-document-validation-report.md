# Document Validation Report

검토일: 2026-05-09
검토 범위: `docs/source` 전체 문서
검토 기준: 최신 `requirements.md`, `screen-design.md`, `api-spec.md`

## 1. 문서 인벤토리

| 문서 | 상태 | 검토 결과 |
|---|---|---|
| `docs/source/requirements.md` | 작성됨 | 계정 찾기, 로그아웃, 회원 탈퇴, 검색/페이지네이션, 파일 업로드, 추천 취소, 대댓글 깊이, 그룹 생성자 역할, 그룹 일정 권한, 채팅 제외 정책이 확정되었다. |
| `docs/source/user-flow.md` | 작성됨 | 최신 요구사항/API/화면 기준으로 계정 찾기, 알림, 추천, 대댓글, 일정 수정/삭제, 탈퇴 후 이동 흐름이 부족하거나 오래된 표현이 남아 있다. |
| `docs/source/screen-design.md` | 작성됨 | 최신 요구사항과 API 명세 기준으로 갱신되었다. 일부 UI 방식 결정은 Open Questions로 남아 있다. |
| `docs/source/api-spec.md` | 작성됨 | 최신 요구사항과 화면 설계 기준으로 갱신되었다. 공통 에러, 비밀번호 재설정 토큰, 일부 보안/UI 정책은 Open Questions로 남아 있다. |
| `docs/source/erd.md` | 작성됨 | 기존 DB 문서끼리의 구조는 대체로 일치하지만, 최신 요구사항의 이메일 유니크, 삭제 대기/비활성화/위임 정책을 표현하지 못한다. |
| `docs/source/logical-schema.md` | 작성됨 | 최신 요구사항의 일부 제약과 상태 전이가 반영되지 않았다. |
| `docs/source/physical-schema.md` | 작성됨 | 최신 요구사항의 이메일 유니크, 비활성화, 삭제 대기, 그룹장 위임 기준을 지원하는 제약/컬럼이 부족하다. |
| `docs/source/dbml.md` | 작성됨 | 물리/논리 스키마와 같은 한계를 가진다. |

## 2. 요약

| 심각도 | 건수 | 내용 |
|---|---:|---|
| BLOCKER | 3 | 최신 요구사항이 DB 스키마로 구현 불가능하거나 직접 충돌하는 항목이 있다. |
| MAJOR | 6 | 유저 플로우 보완과 구현 전 API 세부 결정이 필요하다. |
| MINOR | 3 | 명칭, 조회수, 문서 정리 수준의 보완 항목이 있다. |
| QUESTION | 0 | 결정 필요 사항은 `03-missing-decisions.md`에 별도로 기록했다. |

## 3. BLOCKER

### B-01

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 요구사항과 API는 탈퇴 회원의 게시글, 댓글, 개인 캘린더, 개인 일정, 유일 그룹의 비활성화와 6개월 삭제 대기를 요구하지만 DB 스키마에는 `post`, `comments`, `schedules`, `groups`의 상태/삭제대기/삭제예정일 컬럼이 없다. |
| Why it matters | 탈퇴 후 데이터 생명주기와 조회 제외 정책을 실제 저장소에서 구현하거나 테스트할 수 없다. |
| Suggested fix | 탈퇴/비활성화/삭제대기 상태를 표현할 DB 컬럼과 상태값을 ERD, 논리/물리 스키마, DBML에 반영한다. |
| Required user decision | 비활성화 상태값, 삭제 대기 시작/만료 일시 컬럼, 6개월 후 실제 삭제 처리 방식 |

### B-02

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 요구사항과 API는 그룹장 탈퇴 시 가장 먼저 가입한 다른 그룹원에게 자동 위임한다고 정의하지만 `group_members`에는 가입 순서를 판단할 `created_at`/`joined_at` 컬럼이 없다. 또한 위임 시 `groups.creator_id`를 유지할지 변경할지도 확정되지 않았다. |
| Why it matters | 그룹장 자동 위임을 결정적으로 수행할 기준이 없고, 그룹 생성자와 현재 리더의 의미가 충돌할 수 있다. |
| Suggested fix | 그룹 가입 일시 컬럼, 리더 위임 기준, `groups.creator_id`와 `group_members.role`의 역할 분리를 스키마/API에 반영한다. |
| Required user decision | 위임 기준 컬럼과 위임 후 `groups.creator_id` 갱신 여부 |

### B-03

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 요구사항과 API는 회원 아이디와 이메일이 모두 중복될 수 없다고 하지만 DB 스키마는 `login_id`만 Unique로 정의하고 `email_address` 유니크 제약이 없다. |
| Why it matters | 요구사항의 가입 검증과 데이터 무결성을 DB에서 보장할 수 없다. |
| Suggested fix | `users.email_address` 유니크 제약을 ERD, 논리/물리 스키마, DBML에 추가한다. |
| Required user decision | 없음. 요구사항/API 기준으로 DB 문서 동기화 필요 |

## 4. MAJOR

### M-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 이메일 기반 아이디/비밀번호 찾기가 정의되었지만 유저 플로우에는 계정 찾기 화면과 상세 흐름이 없다. |
| Why it matters | 로그인 전 계정 복구 동작의 시작 상태, 사용자 액션, 시스템 응답, 종료 상태를 검증할 수 없다. |
| Suggested fix | 로그인 페이지에서 아이디 찾기, 비밀번호 찾기, 비밀번호 재설정 화면 이동 흐름을 `user-flow.md`에 추가한다. |
| Required user decision | 아이디 찾기 결과를 전체 노출할지 마스킹할지 결정 |

### M-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API는 이름/이메일/비밀번호 수정과 탈퇴 후 로그인 페이지 이동을 정의하지만 유저 플로우는 회원 정보 수정 범위가 좁고 탈퇴 후 메인 페이지 이동으로 남아 있다. |
| Why it matters | 회원 정보 관리와 탈퇴 후 인증 상태가 유저 플로우와 충돌한다. |
| Suggested fix | 유저 플로우를 회원 정보 수정 필드와 탈퇴 후 즉시 로그아웃 및 로그인 페이지 이동 기준으로 갱신한다. |
| Required user decision | 비밀번호 변경 시 현재 비밀번호 입력 필요 여부 |

### M-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항과 API는 알림 클릭 후 대상 위치 정상 도달 시 읽음 처리한다고 확정했지만 유저 플로우에는 알림 목록 진입과 클릭 이동 흐름이 없다. |
| Why it matters | 알림 조회/읽음 API가 어느 화면에서 호출되는지 검증하기 어렵다. |
| Suggested fix | 알림 목록 진입, 알림 클릭, 게시글/댓글 위치 이동, 읽음 처리 흐름을 유저 플로우에 추가한다. |
| Required user decision | 알림 목록을 어느 화면에서 진입할지 결정 |

### M-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 추천/추천 취소와 대댓글 1단계 제한이 반영되었지만 유저 플로우에는 추천 액션과 대댓글 흐름이 없다. |
| Why it matters | 게시글 상세 화면의 핵심 상호작용을 흐름 기준으로 검증할 수 없다. |
| Suggested fix | 게시글 상세 흐름에 추천 등록/취소, 대댓글 작성, 대댓글 수정/삭제, 대댓글 깊이 제한 흐름을 추가한다. |
| Required user decision | 없음 |

### M-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 요구사항, 화면 설계, API에는 개인 일정 조회/수정/삭제와 그룹 일정 삭제가 있으나 유저 플로우에는 개인 일정 추가 중심 흐름과 그룹 일정 관리 흐름만 있다. |
| Why it matters | 일정 수정/삭제 권한과 화면 전환을 플로우 기준으로 검증하기 어렵다. |
| Suggested fix | 개인 일정 선택/수정/삭제, 그룹 일정 조회/등록/수정/삭제 흐름을 유저 플로우에 추가한다. |
| Required user decision | 개인 일정 상세/수정 UI를 별도 페이지로 둘지 모달로 둘지 결정 |

### M-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | API 공통 에러 응답 형식이 아직 확정되지 않았다. |
| Why it matters | API 실패 응답과 화면 오류 메시지 처리가 일관되지 않을 수 있다. |
| Suggested fix | 모든 API가 공유할 에러 body 형식과 필드명을 확정한다. |
| Required user decision | 예: `code`, `message`, `details` 포함 여부와 필드명 |

## 5. MINOR

### m-01

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/dbml.md` |
| Problem | 회원 이름 필드가 요구사항/API/화면에서는 이름으로 정리되었지만 DBML note에는 닉네임으로 표현된다. |
| Why it matters | 필드명은 같지만 문서 라벨이 달라질 수 있다. |
| Suggested fix | `users.name`의 표시 의미를 이름으로 통일한다. |
| Required user decision | 없음 |

### m-02

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md` |
| Problem | `post.view_count`는 DB/API/화면에 있으나 조회수 증가 조건은 요구사항에 명시되지 않았다. |
| Why it matters | 상세 조회마다 증가할지, 같은 사용자의 반복 조회를 제한할지 구현 방식이 달라질 수 있다. |
| Suggested fix | 조회수 증가 정책을 요구사항 또는 API에 보완한다. |
| Required user decision | 게시글 상세 조회 시 조회수를 언제 증가시킬지 결정 |

### m-03

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md` |
| Problem | 그룹 캘린더 유지 정보 목록 끝에 빈 bullet `-`가 남아 있다. |
| Why it matters | 요구사항 목록에 의도하지 않은 누락 항목이 있는지 오해될 수 있다. |
| Suggested fix | 빈 bullet을 제거하거나 누락 항목이 있다면 내용을 채운다. |
| Required user decision | 없음 |

## 6. 해소되었거나 요구사항/API/화면에서 확정된 항목

- 본인확인 정보: 이메일로 확정 및 API/화면 반영
- 비밀번호 찾기 성공 후 처리: 비밀번호 재설정 화면 이동으로 확정 및 API/화면 반영
- 로그아웃 방식: 서버 세션 무효화로 확정 및 API 반영
- 회원 탈퇴: `status = DELETED`, `deleted_at` 기록, 즉시 로그아웃, 로그인 페이지 이동으로 확정 및 API/화면 반영
- 알림 읽음 처리: 알림 클릭 후 대상 위치 정상 도달 시 읽음 처리로 API/화면 반영
- 파일 업로드: `multipart/form-data` 직접 업로드 방식으로 API/화면 반영
- 게시글 작성/삭제 후 이동: 게시판 첫 번째 페이지로 화면/API 클라이언트 동작 반영
- 게시글 목록: 페이지 번호 기반, 최신순, 단일 필터 사용으로 API/화면 반영
- 추천: 등록 API와 취소 API로 반영
- 대댓글 깊이: 대댓글에는 다시 대댓글을 작성할 수 없음으로 API/화면 반영
- 그룹 생성자: 생성과 동시에 `group_members`에 `LEADER`로 등록으로 API/화면 반영
- 그룹 채팅: 구현하지 않음으로 API/화면 반영
- 그룹 일정 권한: 모든 그룹원이 조회/등록/수정/삭제 가능으로 API/화면 반영

## 7. DB 스키마 검토 결과

`erd.md`, `logical-schema.md`, `physical-schema.md`, `dbml.md` 사이의 기존 구조 정합성은 대체로 유지된다. 다만 최신 요구사항과 API 기준으로는 다음 스키마 보완이 필요하다.

- `users.email_address` 유니크 제약 추가 필요
- `post`, `comments`, `schedules`, `groups`의 비활성화/삭제대기 상태 표현 필요
- `group_members`의 가입 순서 판단 컬럼 필요
- 그룹장 위임 후 현재 리더와 최초 생성자 의미 분리 필요

## 8. 다음 단계 판단

요구사항, 화면 설계, API 명세는 주요 정책 기준으로 정렬되었다. 백엔드 구현 전에는 DB 스키마 계약을 먼저 갱신해야 한다.
