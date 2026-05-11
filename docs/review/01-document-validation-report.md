# Document Validation Report

검토일: 2026-05-11
검토 범위:

- `docs/source/user-flow.md`
- `docs/source/requirements.md`
- `docs/source/screen-design.md`
- `docs/source/api-spec.md`
- `docs/source/erd.md`
- `docs/source/logical-schema.md`
- `docs/source/physical-schema.md`

구현 가능 여부: **PASS**

판단: 이전 검증에서 남아 있던 BLOCKER는 없었고, MAJOR 2건과 QUESTION 2건은 최신 source 문서에 모두 반영되었다. 공통 에러 응답, 파일 업로드 최소 정책, 관리자 권한 부여 정책, 신고 처리 결과 저장 정책, 그룹 가입 코드 UX가 요구사항, 유저 플로우, 화면 설계, API 명세, ERD, 논리 스키마, 물리 스키마에 일관되게 반영되었다. 새로 생긴 BLOCKER/MAJOR/QUESTION 충돌은 발견되지 않았다.

## 1. 문서 인벤토리

| 문서 | 상태 | 검증 결과 |
|---|---|---|
| `docs/source/requirements.md` | 작성됨 | 공통 에러 응답, 파일 업로드 정책, ADMIN 부여 정책, 신고 처리 이력, 그룹 코드 UX가 명시되어 있다. |
| `docs/source/user-flow.md` | 작성됨 | 게시글 파일 업로드, 신고 생성/처리, 그룹 생성/가입 흐름이 요구사항과 일치한다. |
| `docs/source/screen-design.md` | 작성됨 | 실패 메시지, 파일 정책, 관리자 신고 처리 화면, 그룹 코드 표시 방식이 API/요구사항과 일치한다. |
| `docs/source/api-spec.md` | 작성됨 | 공통 에러 응답, 신고 처리 API, 파일 업로드/수정 정책, 그룹 코드 API 계약이 반영되어 있다. |
| `docs/source/erd.md` | 작성됨 | `report.status`, `processed_by`, `processed_at`과 파일/그룹 코드 정책이 반영되어 있다. |
| `docs/source/logical-schema.md` | 작성됨 | 신고 처리 이력 컬럼, ADMIN 부여 정책, 파일 저장 범위, 그룹 코드 정책이 반영되어 있다. |
| `docs/source/physical-schema.md` | 작성됨 | 신고 처리 컬럼, 제약, FK, 인덱스가 논리 스키마와 일치한다. |

## 2. 이전 이슈 처리 결과

| 이전 ID | 이전 Severity | 현재 상태 | 근거 |
|---|---|---|---|
| V-M01 / D-01 | MAJOR | 해결 | 공통 에러 응답은 `code`, `message` 필수, `details` 선택으로 `requirements.md`, `api-spec.md`, `screen-design.md`에 반영되었다. |
| V-M02 / D-02 | MAJOR | 해결 | 실제 파일은 `/uploads/posts/{post_id}/...` 로컬 경로에 저장하고 DB에는 `file_url`만 저장한다. 수정 시 새 파일이 있으면 전체 교체, 없으면 유지한다. 파일 메타데이터는 저장하지 않는 것으로 확정되었다. |
| V-Q01 / D-03 | QUESTION | 해결 | `users.role` 기본값은 `USER`, ADMIN은 DB seed 또는 운영자 DB 변경으로만 부여한다. 신고 처리는 `report.status`, `processed_by`, `processed_at`만 변경한다. |
| V-Q02 / D-04 | QUESTION | 해결 | 그룹 생성 시 `group_code`를 화면에 보여주고 가입자는 코드를 입력한다. `group_link`는 그룹 가입 코드와 같은 값이며 복잡한 초대 링크/공유 기능은 제외되었다. |

## 3. 검증 요약

| Severity | 건수 | 내용 |
|---|---:|---|
| BLOCKER | 0 | 구현을 중단해야 하는 직접 충돌 없음 |
| MAJOR | 0 | 구현 전 반드시 추가 결정해야 하는 주요 정책 없음 |
| MINOR | 0 | 현재 검토 범위에서 별도 경미 이슈 없음 |
| QUESTION | 0 | 사용자 결정을 기다리는 항목 없음 |

## 4. 체크리스트 결과

### Requirements

- 기능 요구사항은 현재 구현 범위 기준으로 명확하다.
- CRUD 범위는 회원, 게시글, 댓글/대댓글, 신고, 일정, 그룹에 대해 문서화되어 있다.
- 역할과 권한은 USER/ADMIN, 관리자 신고 처리 권한, 그룹원 일정 권한 기준으로 정의되어 있다.
- 주요 검증 규칙과 오류 상황은 API 명세 및 화면 설계에 반영되어 있다.

### User Flow

- 주요 흐름은 요구사항과 API에 매핑된다.
- 신고 처리 흐름은 상태 변경으로 제한되어 있으며 게시글/댓글 삭제를 자동 수행하지 않는다.
- 그룹 가입은 단순 코드 입력 흐름으로 정리되어 있다.

### Screen Design

- 화면 액션은 API 또는 구현 제외 항목과 매핑된다.
- 파일 업로드, 실패 메시지, 신고 처리, 그룹 코드 표시 정책이 요구사항과 일치한다.
- 그룹 채팅은 구현 제외로 일관되게 처리되어 있다.

### API

- 모든 핵심 엔드포인트는 요구사항 및 화면 흐름과 연결되어 있다.
- 공통 에러 응답 형식이 확정되어 있다.
- 신고 처리 API `PATCH /api/admin/reports/{report_id}`가 DB 스키마와 trace된다.
- Open Questions는 남아 있지 않다.

### Database

- `report` 처리 이력 컬럼은 논리/물리/ERD 간 일치한다.
- `users.role` 기본값과 ADMIN 부여 정책은 API/스키마 설명에 반영되어 있다.
- `file`은 `file_url`만 저장하는 최소 정책과 일치한다.
- ERD, 논리 스키마, 물리 스키마 간 직접 mismatch는 발견되지 않았다.

## 5. 구현 시작 판단

**PASS**

현재 검토 대상 source 문서 기준으로 백엔드 구현 계획 단계로 이동할 수 있다. 다음 단계에서 실제 구현을 시작할 경우, AGENTS.md의 단계 규칙에 따라 normalized specification과 implementation plan을 최신 source 문서에 맞춰 갱신한 뒤 한 기능 그룹씩 구현해야 한다.
