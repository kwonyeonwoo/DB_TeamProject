# Missing Decisions

검토일: 2026-05-10
범위: 요청된 최신 `docs/source` 문서 재검증 후 구현 전 결정이 필요한 항목

## 1. 구현 전 우선 결정 사항

### D-01

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/api-spec.md`, `docs/source/screen-design.md` |
| Problem | 공통 에러 응답 body 형식이 정해지지 않았다. |
| Why it matters | 실패 응답 처리와 화면 오류 표시, 테스트 기대값이 일관되지 않는다. |
| Suggested fix | 공통 에러 응답 예시와 필드명을 API 명세에 추가한다. |
| Required user decision | `code`, `message`, `details` 등 필드 구성 |

### D-02

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/physical-schema.md` |
| Problem | 일정 종류 `type`의 구체적인 값 목록이 없다. |
| Why it matters | 일정 등록/수정 validation과 화면 옵션을 구현할 수 없다. |
| Suggested fix | 정수 코드 또는 enum 문자열 기준으로 일정 종류 값을 정의한다. |
| Required user decision | 일정 종류 값 목록과 표시 라벨 |

### D-03

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 회원 정보 수정에서 비밀번호 변경 시 현재 비밀번호 입력이 필요한지 결정되지 않았다. |
| Why it matters | API request body, 보안 검증, 오류 응답, 화면 필드가 달라진다. |
| Suggested fix | 현재 비밀번호 필수 여부와 검증 실패 응답을 확정한다. |
| Required user decision | 현재 비밀번호 필수 여부 |

### D-04

| 항목 | 내용 |
|---|---|
| Severity | MAJOR |
| Related document | `docs/source/requirements.md`, `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 파일 직접 업로드는 확정되었지만 저장 위치, URL 생성 방식, 파일 크기/확장자 제한, 게시글 수정 시 파일 처리 방식이 정의되지 않았다. |
| Why it matters | 업로드 구현과 보안 검증, 테스트 범위가 달라진다. |
| Suggested fix | 파일 저장소와 허용 정책, 수정 시 추가/교체/삭제 규칙을 정의한다. |
| Required user decision | 파일 저장 및 수정 정책 |

## 2. 기능별 추가 결정 사항

### D-05

| 항목 | 내용 |
|---|---|
| Severity | QUESTION |
| Related document | `docs/source/logical-schema.md`, `docs/source/physical-schema.md`, `docs/source/api-spec.md`, `docs/source/requirements.md` |
| Problem | `users.role`, `users.status = INACTIVE`, `post.is_reported`의 요구사항상 사용처가 없다. |
| Why it matters | 구현자가 사용자 역할, 회원 비활성화, 신고 기능을 문서 없이 추정할 수 있다. |
| Suggested fix | 예약 필드로 유지할지, 제거할지, 별도 요구사항을 추가할지 결정한다. |
| Required user decision | 유지/삭제/예약 여부와 사용 규칙 |

### D-06

| 항목 | 내용 |
|---|---|
| Severity | QUESTION |
| Related document | `docs/source/user-flow.md`, `docs/source/screen-design.md` |
| Problem | 개인 일정 상세/수정 UI를 별도 페이지로 둘지 모달로 둘지 결정되지 않았다. |
| Why it matters | 백엔드 API 계약에는 큰 영향이 없지만 화면 전환과 사용자 흐름 상세가 달라진다. |
| Suggested fix | 별도 페이지 또는 모달 중 하나를 확정한다. |
| Required user decision | 개인 일정 상세/수정 UI 방식 |

### D-07

| 항목 | 내용 |
|---|---|
| Severity | QUESTION |
| Related document | `docs/source/user-flow.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md` |
| Problem | 그룹 링크 입력, 복사, 공유 UX의 구체 형태가 정해지지 않았다. |
| Why it matters | 백엔드 핵심 계약은 `group_link`로 충분하지만, 링크 생성/노출/복사 정책에 따라 화면 동작이 달라질 수 있다. |
| Suggested fix | 그룹 생성 후 링크 노출 방식과 가입 입력 방식을 확정한다. |
| Required user decision | 그룹 링크 공유 UX |

## 3. 더 이상 미결정이 아닌 항목

| 이전 항목 | 현재 상태 |
|---|---|
| 아이디 찾기 기능 | 요구사항에서 제거되어 구현 범위에서 제외 |
| 비밀번호 찾기 및 재설정 기능 | 요구사항에서 제거되어 구현 범위에서 제외 |
| 아이디 찾기 결과 마스킹 여부 | 기능 제거로 결정 불필요 |
| 비밀번호 재설정 토큰/임시 세션 방식 | 기능 제거로 결정 불필요 |
| 비밀번호 재설정 API 경로 | 기능 제거로 결정 불필요 |
| 탈퇴 생명주기 DB 컬럼 | `status`, `deleted_at` 기반으로 스키마와 요구사항에 반영. 삭제 예정일 컬럼은 사용하지 않음 |
| 삭제 대기 상태 의미 | `status`, `deleted_at` 기반 일반 비노출 상태로 source 문서에 반영 |
| 개인 캘린더 별도 엔티티 여부 | 별도 엔티티 없이 User id와 `schedules.group_id = NULL` 기준으로 표현 |
| 그룹장 자동 위임 기준 | `group_members.joined_at` 기준으로 반영 |
| 위임 후 현재 그룹장 판단 | 현재 그룹장은 `group_members.role = LEADER`, `groups.creator_id`는 최초 생성자로 반영 |
| 이메일 중복 제약 | `users.email_address` unique 반영 |
| 로그아웃 방식 | 서버 세션 무효화로 반영 |
| 회원 탈퇴 후 이동 | 즉시 로그아웃 및 로그인 페이지 이동으로 반영 |
| 알림 읽음 처리 | 본인 알림 목록 팝업 조회 시 읽음 처리로 반영 |
| 게시글 작성/삭제 후 이동 | 게시판 첫 번째 페이지 이동으로 반영 |
| 게시글 목록 페이지네이션/필터 | 페이지 번호 기반, 최신순, 단일 필터 기준으로 반영 |
| 조회수 증가 정책 | 게시글 상세 화면 접근 시 증가로 반영 |
| 게시글 추천 취소 | 추천 등록/취소 API와 플로우 반영 |
| 대댓글 깊이 제한 | 대댓글에는 다시 대댓글 작성 불가로 반영 |
| 그룹 생성자 멤버십 | 생성자를 `group_members`에 `LEADER`로 자동 등록하는 기준 반영 |
| 그룹 채팅 포함 여부 | 구현 제외로 반영 |
| 그룹 일정 수정/삭제 권한 | 모든 그룹원이 가능하도록 반영 |
