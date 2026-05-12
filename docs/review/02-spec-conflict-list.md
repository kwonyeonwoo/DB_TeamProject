# 02. Spec Conflict List

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

최종 재확인: 2026-05-12 기준 원본 문서 간 직접 충돌은 추가로 발견되지 않았다.

## 1. 현재 미해결 직접 충돌

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 현재 검증 대상 문서 간 직접 충돌 없음 | - | - | 없음 |

## 2. 이전 충돌 해소 상태

| 이전 ID | 이전 Severity | 현재 상태 | 확인 근거 |
|---|---|---|---|
| C-M01 | MAJOR | 해결 | `docs/source/erd.md` Mermaid의 `users ||--o{ groups` 관계 라벨이 `groups.leader_id = 현재 그룹장`으로 수정되었다. requirements, logical schema, physical schema의 현재 그룹장 모델과 일치한다. |
| C-Q01 | QUESTION | 해결 | requirements, user-flow, screen-design, api-spec, erd, logical-schema, physical-schema, dbml에 게시글/댓글 hard delete 이후 기존 report 이력 유지 및 관리자 신고 목록의 `삭제된 대상` 표시 정책이 반영되었다. |
| 공통 오류 응답 body 형식 미정 | MAJOR | 해결 유지 | 요구사항과 API에서 실패 응답 body는 `code`, `message` 필수, `details` 선택으로 정리되어 있다. |
| 파일 업로드 저장 경로/DB 저장값 불일치 | MAJOR | 해결 유지 | `/uploads/posts/{post_id}/{UUID}`와 `file_url` 저장 정책으로 통일되어 있다. |
| 관리자 권한 부여 방식 미정 | QUESTION | 해결 유지 | `users.role` 기본값은 `USER`, ADMIN은 DB seed 또는 운영자 DB 변경으로만 부여한다. |
| 관리자 신고 처리 결과 저장 방식 미정 | QUESTION | 해결 유지 | 신고 처리는 `report.status`, `processed_by`, `processed_at`만 변경한다. |
| 그룹 가입 코드 공유 UX 미정 | QUESTION | 해결 유지 | 별도 URL 없이 `group_code`를 화면에 표시하고 사용자가 코드를 입력해 가입한다. |
| 일정/그룹 삭제 대기 상태 표현 | BLOCKER | 해결 유지 | 개인 일정과 유일 그룹원 그룹은 탈퇴 시 즉시 삭제하며, `groups.status`, `schedules.status`는 사용하지 않는다. |

## 3. 신규 충돌 검토

### Requirement ↔ User Flow

- 현재 그룹장 모델, 개인 일정 즉시 삭제, 유일 그룹원 그룹 즉시 삭제, 파일 경로, 신고 대상 삭제 후 report 유지 정책이 일치한다.

### Requirement ↔ Screen Design

- 관리자 신고 목록은 삭제된 신고 대상을 `삭제된 대상`으로 표시한다.
- 신고 처리는 상태만 변경하며 게시글/댓글 삭제를 자동 수행하지 않는다.

### Requirement ↔ API

- 신고 생성 시 대상 존재 검증, 대상 삭제 후 report 이력 유지, `target_display_name` 반환 정책이 일치한다.
- 게시글/댓글 삭제 API는 대상 삭제와 report 이력 유지 정책을 함께 명시한다.

### API ↔ DB Schema

- `report.target_id`는 단일 FK 없이 다형 참조로 유지한다.
- 생성 시점 대상 존재 검증은 서비스 로직 또는 트리거에서 수행하며, 대상 hard delete 이후에도 report row는 유지한다.

### ERD ↔ Logical ↔ Physical ↔ DBML

- `groups.leader_id`는 현재 그룹장 기준으로 일치한다.
- ERD Mermaid 라벨도 `groups.leader_id = 현재 그룹장`으로 정리되었다.
- report 다형 참조와 삭제된 대상 표시 정책이 일치한다.

### User Flow ↔ API / Screen ↔ API

- 신고 생성, 관리자 신고 목록 조회, 신고 처리 흐름이 API와 일치한다.
- 삭제된 신고 대상 표시값은 `삭제된 대상`으로 통일되어 있다.

## 4. 충돌 아님으로 확인한 항목

- `post`, `comments`, `groups`, `schedules`에 별도 `status`/`deleted_at` 컬럼이 없는 것은 최신 hard delete 정책과 충돌하지 않는다.
- 탈퇴 회원 row를 유지하고 `users.status`, `users.deleted_at`으로 처리하는 것은 회원 개인정보 삭제 대기 정책과 일치한다.
- 탈퇴 회원 작성 게시글/댓글/대댓글은 유지하고 작성자명을 `탈퇴한 유저`로 표시하는 정책은 문서 간 일치한다.
- 신고 대상 게시글/댓글이 삭제되어도 `report` 이력은 유지하는 것이 최신 정책이다.

## 5. 구현 가능 여부

직접 충돌 기준: **PASS**

전체 구현 착수 기준: **PASS**
