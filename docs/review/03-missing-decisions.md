# Missing Decisions

검토일: 2026-05-09
범위: 최신 요구사항, 화면 설계, API 명세 반영 후 구현 전 결정이 필요한 항목

## 1. BLOCKER 결정 사항

### D-01

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 탈퇴 회원 관련 게시글, 댓글, 개인 캘린더, 개인 일정, 유일 그룹을 비활성화하고 6개월 삭제 대기 상태로 둔다고 했지만 이를 표현할 DB 상태값과 컬럼이 결정되지 않았다. |
| Why it matters | 탈퇴 후 조회 제외, 복구 가능성, 6개월 후 삭제 작업을 구현할 수 없다. |
| Suggested fix | 각 엔티티별 비활성화/삭제대기 상태값과 삭제 대기 시작/만료 일시 컬럼을 정한다. |
| Required user decision | 상태값 이름, 적용 엔티티, 삭제 대기 시작일/만료일 저장 방식 |

### D-02

| 항목 | 내용 |
|---|---|
| Severity | BLOCKER |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/dbml.md` |
| Problem | 그룹장 탈퇴 시 "가장 먼저 가입한 그룹원"에게 위임한다고 했지만 가입 순서를 저장할 기준과 위임 후 `groups.creator_id` 처리 방식이 결정되지 않았다. |
| Why it matters | 자동 위임 대상을 결정할 수 없고, 최초 생성자와 현재 리더 의미가 충돌할 수 있다. |
| Suggested fix | `group_members.joined_at` 같은 가입 시각과 위임 후 리더/생성자 필드 정책을 정한다. |
| Required user decision | 가입 순서 기준 컬럼, 위임 후 `groups.creator_id` 갱신 여부 |

## 2. MAJOR 결정 사항

### D-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 공통 에러 응답 형식이 아직 결정되지 않았다. |
| Why it matters | API 실패 응답과 화면 오류 메시지 처리가 일관되지 않는다. |
| Suggested fix | 모든 API가 공유할 에러 body 형식과 필드명을 확정한다. |
| Required user decision | 예: `code`, `message`, `details` 포함 여부와 필드명 |

### D-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 비밀번호 변경 시 현재 비밀번호 입력이 필요한지 결정되지 않았다. |
| Why it matters | 회원 정보 수정 API request body와 보안 검증이 달라진다. |
| Suggested fix | 현재 비밀번호 입력 필요 여부를 요구사항/API/화면 설계에 명시한다. |
| Required user decision | 현재 비밀번호 필수 여부 |

### D-05

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 아이디 찾기 성공 시 아이디 전체를 노출할지 일부 마스킹할지 결정되지 않았다. |
| Why it matters | 계정 찾기 응답과 개인정보 노출 정책이 달라진다. |
| Suggested fix | 아이디 찾기 성공 응답 표시 정책을 정한다. |
| Required user decision | 전체 아이디 노출 또는 마스킹 표시 |

### D-06

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md` |
| Problem | 비밀번호 재설정 화면에서 사용할 검증 토큰 또는 임시 세션 방식이 결정되지 않았다. |
| Why it matters | 비밀번호 찾기 성공 후 실제 재설정 API를 안전하게 연결할 수 없다. |
| Suggested fix | 재설정 화면 진입 권한을 검증할 토큰/임시 세션 정책과 후속 API를 정한다. |
| Required user decision | 비밀번호 재설정 토큰 또는 임시 세션 방식 |

### D-07

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md` |
| Problem | 알림 목록의 진입 위치가 정해지지 않았다. |
| Why it matters | 알림 조회 API를 어느 화면에서 호출하고 알림 클릭 후 어디로 복귀할지 정하기 어렵다. |
| Suggested fix | 메인, 마이페이지, 게시글 상세 등 알림 목록 진입 화면을 확정한다. |
| Required user decision | 알림 목록 진입 위치 |

### D-08

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/api-spec.md`, `docs/source/erd.md` |
| Problem | 게시글 조회수 증가 정책이 명시되어 있지 않다. |
| Why it matters | 상세 조회마다 증가할지, 같은 사용자의 반복 조회를 제한할지 구현이 달라진다. |
| Suggested fix | 조회수 증가 조건을 요구사항 또는 API에 추가한다. |
| Required user decision | 조회수 증가 시점과 중복 조회 처리 방식 |

### D-09

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/screen-design.md`, `docs/source/user-flow.md` |
| Problem | 개인 일정 상세/수정 UI를 별도 페이지로 둘지 모달로 둘지 결정되지 않았다. |
| Why it matters | 개인 일정 선택 후 화면 전환과 유저 플로우가 달라진다. |
| Suggested fix | 개인 일정 상세/수정 UI 방식을 확정하고 유저 플로우에 반영한다. |
| Required user decision | 별도 페이지 또는 모달 |

## 3. MINOR 결정 사항

### d-10

| 항목 | 내용 |
|---|---|
| Severity | MINOR |
| Related document | `docs/source/requirements.md`, `docs/source/dbml.md` |
| Problem | `users.name`의 표시 의미가 대부분 이름으로 정리되었지만 DBML note에는 닉네임으로 남아 있다. |
| Why it matters | UI 라벨과 문서 설명이 달라질 수 있다. |
| Suggested fix | 회원명 필드의 표시 명칭을 이름으로 통일한다. |
| Required user decision | 없음 |

## 4. 더 이상 미결정이 아닌 항목

| 이전 항목 | 현재 상태 |
|---|---|
| 아이디/비밀번호 찾기 본인확인 정보 | 이메일로 확정 및 API/화면 반영 |
| 비밀번호 찾기 성공 후 처리 | 비밀번호 재설정 화면 이동으로 확정 및 API/화면 반영 |
| 로그아웃 인증 무효화 방식 | 서버 세션 무효화로 확정 및 API 반영 |
| 회원 탈퇴 포함 여부와 탈퇴 후 이동 | 포함, 즉시 로그아웃, 로그인 페이지 이동으로 확정 및 API/화면 반영 |
| 알림 클릭 시 이동/읽음 처리 | 대상 위치 정상 도달 후 읽음 처리로 API/화면 반영 |
| 게시글 작성 후 이동 | 게시판 첫 번째 페이지로 확정 및 화면/API client behavior 반영 |
| 게시글 삭제 후 이동 | 게시판 첫 번째 페이지로 확정 및 화면/API client behavior 반영 |
| 댓글/대댓글 수정·삭제 후 갱신 | 전체 화면 갱신으로 확정 및 API/화면 반영 |
| 게시글 목록 페이지네이션 | 페이지 번호 기반으로 확정 및 API/화면 반영 |
| 직접 파일 업로드 방식 | `POST /api/posts`의 `multipart/form-data`로 반영 |
| 게시글 추천 취소 허용 여부 | `DELETE /api/posts/{post_id}/likes`로 반영 |
| 대댓글 깊이 제한 | 대댓글에는 다시 대댓글 작성 불가로 API/화면 반영 |
| 그룹 생성자 멤버십/역할 | `group_members`에 `LEADER`로 자동 등록 API/화면 반영 |
| 그룹 채팅 포함 여부 | 구현하지 않음으로 API/화면 반영 |
| 그룹 일정 수정/삭제 권한 | 모든 그룹원 가능으로 API/화면 반영 |
