# Missing Decisions

검토일: 2026-05-11
범위: 최신 `docs/source` 문서 재검증 후 구현 전 결정 필요 항목

구현 가능 여부: **PASS**

## 1. 현재 구현 전 결정 필요 사항

현재 검토 대상 source 문서 기준으로 **구현 전 추가 사용자 결정이 필요한 항목은 없다.**

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 미결정 항목 없음 | - | - | - |

## 2. 이전 미결정 항목 해소 결과

### D-01

| 항목 | 내용 |
|---|---|
| 이전 Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md`, `docs/source/requirements.md` |
| 이전 Problem | 공통 에러 응답 body 형식이 정해지지 않았다. |
| 현재 상태 | 해결 |
| 반영 내용 | 공통 에러 응답은 `code`, `message`를 필수로 사용하고 `details`는 선택값으로만 사용한다. |
| Required user decision | 없음 |

### D-02

| 항목 | 내용 |
|---|---|
| 이전 Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| 이전 Problem | 파일 크기/확장자 제한과 파일 메타데이터 관리 방식이 정의되지 않았다. |
| 현재 상태 | 해결 |
| 반영 내용 | 실제 파일은 `/uploads/posts/{post_id}/...` 로컬 경로에 저장하고 DB에는 `file_url`만 저장한다. 파일명, 원본 파일명, 파일 크기, MIME 타입, 확장자 등 별도 메타데이터는 DB에 저장하지 않는다. 파일 크기와 확장자 제한 정책은 최소 구현 범위에서 별도 요구사항으로 두지 않는다. |
| Required user decision | 없음 |

### D-03

| 항목 | 내용 |
|---|---|
| 이전 Severity | QUESTION |
| Related document | `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/api-spec.md`, `docs/source/requirements.md` |
| 이전 Problem | 관리자 권한 부여 방식과 관리자 신고 처리 결과 저장 여부가 명확하지 않았다. |
| 현재 상태 | 해결 |
| 반영 내용 | `users.role` 기본값은 `USER`다. ADMIN은 회원 가입/API로 부여하지 않고 DB seed 또는 운영자 DB 변경으로만 부여한다. 신고 처리는 `report.status`, `processed_by`, `processed_at`으로 최소 이력만 저장하고 게시글/댓글 삭제를 자동 수행하지 않는다. |
| Required user decision | 없음 |

### D-04

| 항목 | 내용 |
|---|---|
| 이전 Severity | QUESTION |
| Related document | `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| 이전 Problem | 그룹 가입 코드 입력, 복사, 공유 UX의 구체 형태가 정해지지 않았다. |
| 현재 상태 | 해결 |
| 반영 내용 | 그룹 생성 시 `group_code`를 생성해 화면에 보여주고, 가입자는 해당 코드를 입력해 가입한다. `group_link`는 별도 URL이 아니라 그룹 가입 코드와 같은 값이다. 복잡한 초대 링크/외부 공유/만료/재발급은 제외한다. |
| Required user decision | 없음 |

## 3. 구현 범위에서 제외 또는 확정된 항목

| 항목 | 현재 상태 |
|---|---|
| 아이디 찾기 | 구현 범위 제외 |
| 비밀번호 찾기 및 재설정 | 구현 범위 제외 |
| ADMIN 권한 부여 API | 구현 범위 제외 |
| 그룹 채팅 | 구현 범위 제외 |
| 복잡한 초대 링크/외부 공유/만료/재발급 | 구현 범위 제외 |
| 파일 메타데이터 저장 | 구현 범위 제외 |
| 신고 처리 시 게시글/댓글 자동 삭제 | 구현 범위 제외 |
| 공통 에러 응답 | `code`, `message`, 선택 `details`로 확정 |
| 신고 처리 상태 | `PENDING`, `PROCESSED`로 확정 |
| 그룹 가입 UX | 생성자가 코드를 확인하고 가입자가 코드를 입력하는 방식으로 확정 |

## 4. 구현 시작 판단

**PASS**

현재 검토 대상 source 문서 기준으로 구현 전 결정이 필요한 항목은 없다. 구현을 시작할 때는 최신 source 문서를 기준으로 normalized specification과 implementation plan을 갱신한 뒤 진행하면 된다.
