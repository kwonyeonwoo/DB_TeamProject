# 01. Document Validation Report

검증일: 2026-05-12

검증 목적: 수정된 `docs/source/requirements.md`를 기준으로 다른 원천 문서와 파생 정규화 문서에 충돌 또는 미결정 사항이 생겼는지 확인한다.

검증 대상:

- `docs/source/requirements.md`
- `docs/source/user-flow.md`
- `docs/source/screen-design.md`
- `docs/source/api-spec.md`
- `docs/source/erd.md`
- `docs/source/logical-schema.md`
- `docs/source/physical-schema.md`
- `docs/source/dbml.md`
- 참고 점검: `docs/normalized/*.md`

구현 착수 판정: **PASS**

판단: 수정된 요구사항 기준으로 BLOCKER, MAJOR, QUESTION급 미해결 충돌은 발견되지 않았다. 신고 생성 권한은 일반 사용자(USER) 전용으로 확정되었고, 관리자(ADMIN)는 신고 생성 API를 호출할 수 없도록 요구사항과 API 문서에 명시되었다.

## 1. 문서 인벤토리

| 문서 | 상태 | 검증 메모 |
|---|---|---|
| `docs/source/requirements.md` | 일치 | 신고 생성 권한이 USER 전용으로 명시되었다. 회원 탈퇴, 파일 업로드, 신고, 그룹장 위임, 그룹 채팅 제외 정책을 포함한다. |
| `docs/source/user-flow.md` | 일치 | 신고 생성 시작 상태가 일반 사용자로 표현되어 최신 요구사항과 일치한다. |
| `docs/source/screen-design.md` | 일치 | 화면 액션과 API 매핑이 유지되며, 일반 사용자 신고 생성과 관리자 신고 조회/처리 분리가 일치한다. |
| `docs/source/api-spec.md` | 일치 | R-01 신고 생성 API가 `role = USER` 전용이고 ADMIN 호출 불가로 명시되었다. |
| `docs/source/erd.md` | 일치 | 주요 엔티티, 관계, 제약이 요구사항과 맞는다. |
| `docs/source/logical-schema.md` | 일치 | 논리 릴레이션과 주요 제약이 요구사항과 맞는다. |
| `docs/source/physical-schema.md` | 일치 | FK, cascade, nullable, enum, index 정책이 논리 스키마와 맞는다. |
| `docs/source/dbml.md` | 일치 | 물리 스키마와 같은 계약을 표현한다. |
| `docs/normalized/*.md` | 일치 | 정규화 문서의 USER 전용 신고 생성 정책과 source 문서가 일치한다. |

## 2. 발견 이슈

| ID | Severity | Related document | Problem | Why it matters | Suggested fix | Required user decision |
|---|---|---|---|---|---|---|
| 없음 | - | - | 현재 검증 대상 문서 간 미해결 충돌 없음 | - | - | 없음 |

## 3. 해결된 확인 사항

| ID | 이전 Severity | 현재 상태 | 확인 결과 |
|---|---|---|---|
| V-Q01 | QUESTION | 해결 | 신고 생성은 일반 사용자(USER)만 가능하고, 관리자(ADMIN)는 신고 생성 API를 호출할 수 없다고 `requirements.md`와 `api-spec.md`에 명시되었다. 기존 정규화 문서의 USER 전용 정책과도 일치한다. |

## 4. 주요 일치 확인

### Requirements

- 공통 오류 응답은 `code`, `message` 필수, `details` 선택으로 API와 화면 규칙에 반영되어 있다.
- 회원 가입, 로그인/로그아웃, 정보 수정, 탈퇴 생명주기, 역할 정책은 API와 DB 스키마에 반영되어 있다.
- 신고 생성 권한은 일반 사용자(USER) 전용이며 ADMIN은 신고 생성 API를 호출할 수 없다.
- 파일 업로드는 직접 업로드, `/uploads/posts/{post_id}/{UUID}`, DB `file_url`만 저장하는 방식으로 모든 관련 문서가 일치한다.
- 신고 상태, 신고 이력 유지, 삭제된 신고 대상 표시, 신고 처리 시 대상 자동 삭제 금지는 API, 화면, DB 문서가 일치한다.
- 그룹 생성, 가입 코드, 현재 그룹장 `leader_id`, 그룹장 위임, 유일 그룹원 탈퇴 시 그룹 삭제 정책은 API, 플로우, 화면, DB 문서가 일치한다.
- 그룹 채팅은 구현 제외로 일치한다.

### User Flow

- 회원 탈퇴, 게시글/댓글/대댓글, 신고, 일정, 그룹 흐름은 요구사항의 주요 정책과 연결되어 있다.
- 신고 생성 흐름은 `일반 사용자로 로그인한 회원`으로 시작하므로 USER 전용 정책과 맞는다.

### Screen Design

- 화면별 API 매핑은 현재 API 명세와 맞는다.
- 신고 화면은 일반 사용자 신고 생성, 관리자 신고 조회/처리로 분리되어 있다.
- 탈퇴 작성자 표시, 익명 표시, 그룹 채팅 제외, 파일 정책이 요구사항과 일치한다.

### API

- 각 주요 endpoint는 요구사항, 유저 플로우, 화면 설계, DB 스키마로 추적된다.
- R-01 신고 생성 권한은 USER 전용으로 명확하다.
- API 문서의 Open Questions는 현재 요구사항 기준으로 남아 있지 않다.

### Database

- ERD, 논리 스키마, 물리 스키마, DBML의 테이블과 주요 컬럼은 서로 일치한다.
- `report.target_id`는 다형 참조로 두고 생성 시점 검증은 서비스 로직 또는 트리거로 처리한다는 점이 일치한다.
- `users.status`, `users.deleted_at`, `groups.leader_id`, `group_members.joined_at`, `schedules.type`, `notification.commented_id` nullable 정책이 요구사항과 맞는다.

## 5. Cross-Document Consistency

| 구간 | 결과 | 메모 |
|---|---|---|
| Requirement ↔ User Flow | PASS | 신고 생성은 USER 전용으로 일치한다. |
| Requirement ↔ Screen Design | PASS | 일반 사용자 신고 생성, 관리자 신고 조회/처리로 일치한다. |
| Requirement ↔ API | PASS | `POST /api/reports`는 USER 전용이며 ADMIN 호출 불가로 일치한다. |
| API ↔ DB Schema | PASS | 신고 다형 참조, cascade, 상태, 권한 저장 필드가 일치한다. |
| ERD ↔ Logical Schema | PASS | 주요 엔티티/관계가 일치한다. |
| Logical Schema ↔ Physical Schema | PASS | 컬럼, FK, unique, nullable, enum 정책이 일치한다. |
| User Flow ↔ API | PASS | 유저 플로우는 API의 USER 신고 생성 정책과 맞는다. |
| Screen Design ↔ API | PASS | 화면은 API의 USER 신고 생성, ADMIN 신고 관리 정책과 맞는다. |

## 6. 구현 시작 판단

**PASS**

현재 수정된 요구사항 기준으로 구현 전 반드시 결정해야 할 BLOCKER, MAJOR, QUESTION은 없다. 정규화 명세와 구현 계획을 기준으로 다음 단계 진행이 가능하다.
