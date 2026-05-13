# 02. Spec Conflict List

검증일: 2026-05-12

검증 범위:

- `docs/source/requirements.md`
- `docs/source/user-flow.md`
- `docs/source/screen-design.md`
- `docs/source/api-spec.md`
- `docs/source/erd.md`
- `docs/source/logical-schema.md`
- `docs/source/physical-schema.md`
- `docs/source/dbml.md`
- 참고: `docs/normalized/*.md`

직접 충돌 기준 판정: **PASS**

현재 미해결 직접 충돌은 없다. 신고 생성 권한은 일반 사용자(USER) 전용으로 확정되었고, 관리자(ADMIN)는 신고 생성 API를 호출할 수 없도록 문서화되었다.

## 1. 현재 미해결 직접 충돌

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 현재 검증 대상 문서 간 직접 충돌 없음 | - | - | 없음 |

## 2. 해결된 충돌/해석 차이

| 이전 ID | 이전 Severity | 현재 상태 | 확인 근거 |
|---|---|---|---|
| C-Q01 | QUESTION | 해결 | `requirements.md`와 `api-spec.md`가 신고 생성은 USER만 가능하고 ADMIN은 신고 생성 API를 호출할 수 없다고 명시한다. `user-flow.md`, `screen-design.md`, `docs/normalized` 문서의 USER 전용 정책과 일치한다. |

## 3. 직접 충돌 아님으로 확인한 항목

| 항목 | 확인 결과 |
|---|---|
| 공통 오류 응답 | 요구사항, API, 화면 설계 모두 `code`, `message` 필수와 `details` 선택 정책으로 일치한다. |
| 회원 탈퇴 | 요구사항, API, 플로우, 화면, DB가 `status = DELETED`, `deleted_at`, 6개월 후 개인정보 NULL 처리, 작성물 유지, 개인 일정 삭제, 그룹장 위임을 일관되게 표현한다. |
| 탈퇴/익명 작성자 표시 | 탈퇴 작성자는 `탈퇴한 유저`가 익명 표시보다 우선한다는 정책이 일치한다. |
| 파일 업로드 | 직접 업로드, `/uploads/posts/{post_id}/{UUID}`, `file_url`만 저장, 파일 메타데이터 미저장 정책이 일치한다. |
| 게시글/댓글 삭제 후 신고 이력 | API, DB 문서가 신고 이력 유지와 관리자 목록 `삭제된 대상` 표시를 반영한다. 요구사항 3-5와 충돌하지 않는다. |
| 신고 처리 | 처리 API는 `report.status`, `processed_by`, `processed_at`만 변경하며 대상 삭제를 자동 수행하지 않는다는 정책이 일치한다. |
| 대댓글 모델 | 댓글과 대댓글은 `comments` 테이블과 `parent_comment`로 표현되며, 대댓글에 다시 대댓글 작성 불가 규칙은 API/DB 구현 메모로 보완되어 있다. |
| 일정 모델 | 요구사항의 날짜/시작 시간/종료 시간은 API/DB의 `start_at`, `end_at`으로 정규화되어 표현된다. 충돌은 없다. |
| 그룹 생성/가입 | `group_code`, `group_link` 동일값, `leader_id`, `group_members.role = LEADER`, 코드 입력 방식이 일치한다. |
| 그룹 채팅 | 요구사항, 화면, API 모두 구현 제외로 일치한다. |

## 4. Source 문서 간 정합성

### Requirement ↔ User Flow

- PASS.
- 신고 생성 권한은 USER 전용으로 일치한다.

### Requirement ↔ Screen Design

- PASS.
- 일반 사용자 신고 생성과 관리자 신고 조회/처리 분리가 일치한다.

### Requirement ↔ API

- PASS.
- R-01은 USER 전용 신고 생성으로 정의되어 있고, ADMIN 호출 불가가 명시되어 있다.

### API ↔ DB Schema

- PASS.
- 신고 다형 참조, 신고 이력 유지, deleted target 표시, cascade 정책은 구현 가능한 계약으로 정리되어 있다.

### ERD ↔ Logical Schema ↔ Physical Schema ↔ DBML

- PASS.
- 주요 테이블, 컬럼, enum, FK, unique, nullable 정책이 일치한다.

### User Flow ↔ API / Screen ↔ API

- PASS.
- 유저 플로우와 화면 설계는 API의 신고 권한 해석과 같은 방향이다.

## 5. 파생 정규화 문서 영향

`docs/normalized` 문서들은 이미 신고 생성 권한을 USER 전용으로 작성하고 있어 추가 수정이 필요하지 않다.

## 6. 구현 가능 여부

직접 충돌 기준: **PASS**

전체 구현 착수 기준: **PASS**
