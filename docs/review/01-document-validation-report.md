# Document Validation Report

검토일: 2026-05-10
검토 범위: 요청된 `docs/source` 문서
검토 대상: `user-flow.md`, `requirements.md`, `screen-design.md`, `api-spec.md`, `erd.md`, `logical-schema.md`, `physical-schema.md`

구현 가능 여부: **CONDITIONAL_PASS**

판단: 요구사항에서 아이디 찾기와 비밀번호 찾기 기능이 제거되었고, 이에 맞춰 유저 플로우, 화면 설계, API 명세에서도 해당 화면/흐름/API가 제거되었다. 현재 문서 간 직접 충돌은 발견되지 않는다. 다만 공통 에러 응답, 일정 `type`, 비밀번호 변경 검증, 파일 업로드 저장 정책 등 구현 전 결정이 남아 있어 전체 범위 구현은 조건부로 가능하다.

## 1. 문서 인벤토리

| 문서 | 상태 | 검토 결과 |
|---|---|---|
| `docs/source/requirements.md` | 작성됨 | 회원 관리에서 로그인/로그아웃만 유지하며 아이디 찾기와 비밀번호 찾기 요구사항은 제거되었다. |
| `docs/source/user-flow.md` | 작성됨 | 로그인 화면의 찾기 분기, 계정 찾기 흐름, 재설정 흐름이 제거되었다. |
| `docs/source/screen-design.md` | 작성됨 | 아이디 찾기, 비밀번호 찾기, 비밀번호 재설정 화면과 관련 API 매핑이 제거되었다. |
| `docs/source/api-spec.md` | 작성됨 | `POST /api/auth/find-login-id`, `POST /api/auth/find-password`, 비밀번호 재설정 관련 Open Questions가 제거되었다. |
| `docs/source/erd.md` | 작성됨 | 계정 찾기 제거로 인한 DB 구조 변경 필요는 없다. |
| `docs/source/logical-schema.md` | 작성됨 | 계정 찾기 제거로 인한 논리 스키마 변경 필요는 없다. |
| `docs/source/physical-schema.md` | 작성됨 | 계정 찾기 제거로 인한 물리 스키마 변경 필요는 없다. |

## 2. 이전 이슈 처리 결과

| 이전 이슈 | 상태 | 근거 |
|---|---|---|
| B-01 데이터 생명주기 스키마 누락 | 해결 | `status`, `deleted_at`과 삭제 대기 매핑이 반영되었다. |
| B-02 그룹장 위임 기준 누락 | 해결 | `group_members.joined_at`과 현재 그룹장 판단 기준이 반영되었다. |
| B-03 이메일 unique 누락 | 해결 | `users.email_address` unique가 논리/물리/ERD에 반영되었다. |
| M-01 계정 찾기 유저 플로우 누락 | 요구사항 변경으로 제거 | 아이디 찾기/비밀번호 찾기 기능이 요구사항에서 제거되어 더 이상 구현 대상이 아니다. |
| M-02 회원 정보 수정/탈퇴 플로우 충돌 | 해결 | `UF-05`, `UF-06`, `SC-11`, API 탈퇴 처리 기준이 일치한다. |
| M-03 알림 진입/읽음 플로우 누락 | 해결 | `UF-07`, `SC-07`, `N-01`이 알림 목록 조회 시 읽음 처리 기준으로 일치한다. |
| M-04 추천/대댓글 플로우 누락 | 해결 | 추천 등록/취소와 대댓글 깊이 제한이 요구사항/화면/API/플로우에 반영되었다. |
| M-05 일정 수정/삭제 플로우 누락 | 해결 | 개인/그룹 일정 조회/등록/수정/삭제 플로우와 API가 반영되었다. |
| M-06 공통 에러 응답 미정 | 미해결 | API 공통 규칙과 Open Questions에 남아 있다. |
| m-01 `users.name` 라벨 불일치 | 해결 | 이름으로 통일되었다. |
| m-02 조회수 증가 정책 누락 | 해결 | 상세 화면 접근 시 조회수 증가로 일치한다. |
| m-03 요구사항 빈 bullet | 해결 | 요구사항의 빈 항목은 제거되어 있다. |

## 3. 요약

| 심각도 | 건수 | 내용 |
|---|---:|---|
| BLOCKER | 0 | 구현을 전면 중단해야 하는 문서 간 직접 충돌은 없다. |
| MAJOR | 4 | 구현 전 API/검증/저장 정책 결정이 필요한 항목이 남아 있다. |
| MINOR | 0 | 해결된 과거 충돌 표기는 source 문서에서 정리되었다. |
| QUESTION | 3 | 구현 범위 또는 UI 정책을 명확히 해야 하는 항목이 남아 있다. |

## 4. MAJOR

### V-M01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 공통 에러 응답 body 형식이 아직 결정되지 않았다. |
| Why it matters | 실패 응답 테스트와 화면 오류 표시를 일관되게 구현할 수 없다. |
| Suggested fix | 모든 API가 공유할 에러 응답 필드와 예시를 API 명세에 추가한다. |
| Required user decision | `code`, `message`, `details` 포함 여부와 필드명 |

### V-M02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/physical-schema.md` |
| Problem | 일정 종류 `type`이 요구사항, 화면, API, 스키마에 존재하지만 가능한 값 목록과 의미가 정의되어 있지 않다. |
| Why it matters | 일정 등록/수정 검증과 화면 옵션을 구현자가 임의로 정하게 된다. |
| Suggested fix | 개인/그룹 일정에서 공통으로 사용할 `type` 값 목록과 라벨을 확정한다. |
| Required user decision | 일정 종류 코드 또는 enum 값 목록 |

### V-M03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 회원 정보 수정에서 비밀번호 변경 시 현재 비밀번호 입력이 필요한지 결정되지 않았다. |
| Why it matters | `PATCH /api/users/me`의 request body와 보안 검증, 화면 필드가 달라진다. |
| Suggested fix | 현재 비밀번호 필수 여부와 검증 실패 응답을 API/화면에 반영한다. |
| Required user decision | 현재 비밀번호 필수 여부 |

### V-M04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 파일 직접 업로드 방식은 확정되었지만 저장 위치, URL 생성 방식, 파일 크기/확장자 제한, 게시글 수정 시 파일 처리 방식이 정의되지 않았다. |
| Why it matters | 업로드 구현과 보안 검증, 파일 관련 테스트 범위가 달라진다. |
| Suggested fix | 파일 저장소, URL 생성, 허용 확장자/크기, 수정 시 추가/교체/삭제 정책을 정의한다. |
| Required user decision | 파일 저장 및 수정 정책 |

## 5. QUESTION

### V-Q01

| 항목 | 내용 |
|---|---|
| Severity | QUESTION |
| Related document | `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/api-spec.md`, `docs/source/requirements.md` |
| Problem | `users.role`, `users.status = INACTIVE`, `post.is_reported`는 스키마/API에 존재하지만 요구사항상 사용 시나리오와 권한/신고 기능이 정의되어 있지 않다. |
| Why it matters | 구현 단계에서 역할, 비활성 회원, 신고 정책을 임의로 만들 위험이 있다. |
| Suggested fix | 이번 범위에서 사용하지 않는 예약 필드인지, 실제 요구사항으로 추가할 필드인지 결정한다. |
| Required user decision | 유지/삭제/예약 필드 여부와 사용 규칙 |

### V-Q02

| 항목 | 내용 |
|---|---|
| Severity | QUESTION |
| Related document | `docs/source/user-flow.md`, `docs/source/screen-design.md` |
| Problem | 개인 일정 상세/수정 UI를 별도 페이지로 둘지 모달로 둘지 결정되지 않았다. |
| Why it matters | 백엔드 계약에는 큰 영향이 없지만 화면 전환과 유저 플로우 상세가 달라진다. |
| Suggested fix | 별도 페이지 또는 모달 중 하나로 확정하고 화면/플로우에 반영한다. |
| Required user decision | 개인 일정 상세/수정 UI 방식 |

### V-Q03

| 항목 | 내용 |
|---|---|
| Severity | QUESTION |
| Related document | `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 그룹 링크 입력, 복사, 공유 UX의 구체 형태가 정해지지 않았다. |
| Why it matters | 핵심 백엔드 계약은 `group_link`로 충분하지만 링크 노출/복사 방식에 따라 화면 동작이 달라질 수 있다. |
| Suggested fix | 그룹 생성 후 링크 노출 방식과 가입 입력 방식을 확정한다. |
| Required user decision | 그룹 링크 공유 UX |

## 6. 구현 시작 판단

**CONDITIONAL_PASS**로 판단한다.

- 결정이 완료된 기능 그룹인 회원 가입/로그인/로그아웃, 게시글, 추천, 댓글/대댓글, 알림 목록, 기본 일정, 그룹 생성/가입은 구현 계획 수립을 시작할 수 있다.
- 공통 에러 응답, 일정 타입 검증, 현재 비밀번호 검증, 파일 업로드 저장 정책은 구현 전에 먼저 확정해야 한다.
- 아이디 찾기와 비밀번호 찾기 기능은 요구사항에서 제거되었으므로 구현 범위에서 제외한다.
