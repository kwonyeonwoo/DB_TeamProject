# Spec Conflict List

검토일: 2026-05-11
검토 범위: `docs/source/user-flow.md`, `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`

구현 가능 여부: **PASS**

## 1. 현재 미해결 직접 충돌

현재 검토 기준에서 **문서 간 직접 충돌은 발견되지 않았다.**

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 미해결 직접 충돌 없음 | - | - | - |

## 2. 이전 충돌 및 미결정 해소 상태

| 이전 항목 | 현재 상태 | 반영 위치 |
|---|---|---|
| 공통 에러 응답 body 형식 미정 | 해결 | `requirements.md` 0-1, `api-spec.md` 공통 규칙/공통 상태 코드, `screen-design.md` 공통 화면 규칙 |
| 파일 업로드 저장 경로/DB 저장 값/수정 정책 및 메타데이터 정책 | 해결 | `requirements.md` 3-1, `api-spec.md` P-03/P-04, `user-flow.md` UF-09/UF-10, `screen-design.md` SC-10, 스키마 문서의 `file` 설명 |
| 관리자 권한 부여 방식 미정 | 해결 | `requirements.md` 1-5, `api-spec.md` Authorization/A-01, `logical-schema.md`, `erd.md`, `physical-schema.md`의 `users.role` 설명 |
| 관리자 신고 처리 결과 저장 여부 미정 | 해결 | `requirements.md` 3-5, `api-spec.md` R-02/R-03, `user-flow.md` UF-18, `screen-design.md` SC-18, `report` 스키마 |
| 그룹 가입 코드 공유 UX 미정 | 해결 | `requirements.md` 6-2, `api-spec.md` G-02/G-03, `user-flow.md` UF-16, `screen-design.md` SC-14/SC-15, `groups.group_code` 설명 |

## 3. 신규 충돌 검토

| 검토 영역 | 결과 |
|---|---|
| Requirement ↔ User Flow | 일치. 신고, 파일 업로드, 그룹 코드, 관리자 처리 흐름이 요구사항과 맞다. |
| Requirement ↔ Screen Design | 일치. 화면 실패 메시지, 신고 처리, 파일 정책, 그룹 코드 표시가 요구사항과 맞다. |
| Requirement ↔ API | 일치. 공통 에러 응답, 파일 업로드, 관리자 신고 처리 API, ADMIN 부여 방식이 API에 반영되었다. |
| API ↔ DB Schema | 일치. `report.status`, `processed_by`, `processed_at`이 API와 스키마에 모두 존재한다. |
| ERD ↔ Logical Schema | 일치. 신고 처리 이력과 파일/그룹 정책 설명이 맞다. |
| Logical Schema ↔ Physical Schema | 일치. 신고 처리 컬럼, FK, 상태값, 파일 테이블 구성이 맞다. |
| User Flow ↔ API | 일치. 유저 플로우의 처리 흐름과 API 엔드포인트가 맞다. |
| Screen Design ↔ API | 일치. `SC-18`은 `GET /api/admin/reports`, `PATCH /api/admin/reports/{report_id}`와 연결된다. |

## 4. 충돌 아님으로 확인한 항목

- `details`는 공통 에러 응답의 선택값이므로 기본 응답에서 제외되어도 충돌이 아니다.
- 파일 크기와 확장자 제한은 최소 구현 범위에서 별도 요구사항으로 두지 않기로 확정되어, 스키마에 파일 메타데이터 컬럼이 없는 것은 충돌이 아니다.
- 신고 처리는 게시글/댓글 삭제를 자동 수행하지 않고 `report` 상태만 변경하므로, 게시글/댓글 테이블에 신고 처리 컬럼이 없는 것은 충돌이 아니다.
- `group_link`는 별도 URL이 아니라 `group_code`와 같은 값으로 정리되어, 별도 링크 컬럼이 없는 것은 충돌이 아니다.
- ADMIN 부여 API가 없는 것은 요구사항상 제외로 확정되어 충돌이 아니다.

## 5. 구현 가능 여부

직접 충돌 기준: **PASS**

전체 구현 착수 기준: **PASS**

현재 검토 대상 source 문서만 기준으로 할 때 구현을 막는 충돌은 없다.
