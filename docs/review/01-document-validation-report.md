# 01. Document Validation Report

검증일: 2026-05-12

검증 대상:

- `docs/source/user-flow.md`
- `docs/source/requirements.md`
- `docs/source/screen-design.md`
- `docs/source/api-spec.md`
- `docs/source/erd.md`
- `docs/source/logical-schema.md`
- `docs/source/physical-schema.md`
- `docs/source/dbml.md`

구현 가능 여부: **PASS**

판단: 이전 검증에서 남아 있던 ERD Mermaid 관계 라벨 불일치와 게시글/댓글 hard delete 이후 신고 이력 처리 정책 미결정 사항이 모두 해소되었다. 현재 source 문서 기준으로 구현을 막는 BLOCKER, MAJOR, QUESTION은 확인되지 않았다.

최종 재확인: 2026-05-12 기준 원본 문서 간 직접 충돌은 추가로 발견되지 않았다. 요구사항, 유저 플로우, 화면 설계, API, ERD, 논리 스키마, 물리 스키마, DBML의 주요 계약은 일치한다.

## 1. 문서 인벤토리

| 문서 | 상태 | 검증 메모 |
|---|---|---|
| `docs/source/requirements.md` | 일치 | `groups.leader_id` 현재 그룹장, 개인 일정 즉시 삭제, 유일 그룹원 그룹 즉시 삭제, UUID 파일 경로, 신고 대상 삭제 후 신고 이력 유지 정책이 반영되어 있다. |
| `docs/source/user-flow.md` | 일치 | 신고 생성 후 대상이 삭제되어도 이력을 유지하고 관리자 신고 목록에서 `삭제된 대상`으로 표시하는 흐름이 반영되어 있다. |
| `docs/source/screen-design.md` | 일치 | 관리자 신고 목록의 삭제된 대상 표시 정책과 신고 처리 시 상태만 변경하는 정책이 함께 반영되어 있다. |
| `docs/source/api-spec.md` | 일치 | `target_display_name` 응답 필드, 신고 생성 시 대상 존재 검증, 대상 삭제 후 report 유지 및 `삭제된 대상` 표시 규칙이 반영되어 있다. |
| `docs/source/erd.md` | 일치 | Mermaid의 `users -> groups` 관계 라벨이 `groups.leader_id = 현재 그룹장`으로 수정되었고, report 대상 삭제 후 이력 유지 정책이 반영되어 있다. |
| `docs/source/logical-schema.md` | 일치 | `report.target_id`의 생성 시점 존재 검증과 삭제 후 report 유지 정책이 반영되어 있다. |
| `docs/source/physical-schema.md` | 일치 | report 대상은 단일 FK 없이 다형 참조로 유지하며, 대상 삭제 후 report 이력을 보존하는 정책이 반영되어 있다. |
| `docs/source/dbml.md` | 일치 | DBML 주석과 `target_id` note에 신고 대상 hard delete 이후 report 이력 유지 정책이 반영되어 있다. |

## 2. 이전 이슈 해결 여부

| 이전 ID | 이전 Severity | 현재 상태 | 확인 결과 |
|---|---|---|---|
| V-M01 / D-01 | MAJOR | 해결 유지 | 공통 오류 응답은 `code`, `message` 필수, `details` 선택으로 정리되어 있다. |
| V-M02 / D-02 | MAJOR | 해결 유지 | 파일 업로드는 직접 업로드 방식이며 저장 경로는 `/uploads/posts/{post_id}/{UUID}`, DB 저장값은 `file_url`로 통일되어 있다. |
| V-Q01 / D-03 | QUESTION | 해결 유지 | `users.role` 기본값은 `USER`, ADMIN은 DB seed 또는 운영 DB 변경으로만 부여한다. 신고 처리는 `report.status`, `processed_by`, `processed_at`만 변경한다. |
| V-Q02 / D-04 | QUESTION | 해결 유지 | `group_link`는 별도 URL이 아니라 `group_code`와 동일한 값이며, 복잡한 초대 링크/공유 기능은 제외되어 있다. |
| V-N01 / D-M01 | MAJOR | 해결 | ERD Mermaid의 `users -> groups` 관계 라벨이 `groups.leader_id = 현재 그룹장`으로 변경되었다. |
| V-N02 / D-01 | QUESTION | 해결 | 게시글/댓글 hard delete 이후에도 기존 `report` 이력은 유지하고, 관리자 신고 목록에서는 삭제된 대상을 `삭제된 대상`으로 표시하도록 요구사항, API, 화면, ERD, 논리/물리 스키마, DBML에 반영되었다. |

## 3. 신규 검증 결과

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 현재 검증 대상 문서 간 신규 충돌 없음 | - | - | 없음 |

## 4. 체크리스트 결과

### Requirements

- 기능 요구사항, CRUD 정책, 권한, 검증 규칙, 주요 예외가 구현 가능한 수준으로 정리되어 있다.
- 회원 탈퇴 시 회원 row는 유지하되 개인 일정과 유일 그룹원 그룹은 즉시 삭제하는 정책이 반영되어 있다.
- 게시글/댓글은 hard delete 대상이며, 삭제된 게시글/댓글을 가리키던 신고 이력은 유지한다.

### User Flow

- 주요 사용자 흐름은 요구사항과 API에 연결되어 있다.
- 신고 생성 후 대상 삭제 시 관리자 신고 목록에서 `삭제된 대상`으로 표시하는 흐름이 명시되어 있다.

### Screen Design

- 화면 액션은 API 또는 백엔드 동작과 연결되어 있다.
- 관리자 신고 관리 화면은 삭제된 신고 대상을 `삭제된 대상`으로 표시한다.

### API

- 신고 목록 응답은 `target_display_name`을 포함한다.
- 신고 생성은 대상 존재를 검증하고, 신고 생성 후 대상이 삭제되어도 report 이력을 유지한다.
- 게시글/댓글 삭제 API는 대상 관련 신고 이력을 삭제하지 않는 정책을 명시한다.

### Database

- ERD, logical schema, physical schema, DBML의 주요 테이블과 관계가 일치한다.
- `report.target_id`는 단일 FK 없이 다형 참조로 유지하며, 생성 시점에 대상 존재를 검증한다.
- 게시글/댓글 삭제 후에도 report row는 유지된다.

### Cross-Document Consistency

- Requirement ↔ User Flow: 일치.
- Requirement ↔ Screen Design: 일치.
- Requirement ↔ API: 일치.
- API ↔ DB Schema: 일치.
- ERD ↔ Logical Schema ↔ Physical Schema ↔ DBML: 일치.
- User Flow ↔ API: 일치.
- Screen Design ↔ API: 일치.

## 5. 구현 시작 판단

**PASS**

현재 문서 기준으로 구현 전 반드시 결정해야 할 BLOCKER, MAJOR, QUESTION은 없다. 정규화 명세와 구현 계획을 기준으로 다음 단계 진행이 가능하다.
