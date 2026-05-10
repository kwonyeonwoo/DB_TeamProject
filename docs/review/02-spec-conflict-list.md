# Spec Conflict List

검토일: 2026-05-10
검토 범위: `docs/source/user-flow.md`, `docs/source/requirements.md`, `docs/source/screen-design.md`, `docs/source/api-spec.md`, `docs/source/erd.md`, `docs/source/logical-schema.md`, `docs/source/physical-schema.md`

## 1. 현재 미해결 직접 충돌

현재 검토 기준에서 **문서 간 직접 충돌은 발견되지 않았다.**

요구사항 변경으로 아이디 찾기와 비밀번호 찾기 기능이 제거되었고, 다음 문서도 함께 갱신되었다.

| 항목 | 현재 상태 |
|---|---|
| 유저 플로우 | 로그인 화면의 아이디 찾기/비밀번호 찾기 분기와 `UF-04` 계정 찾기 흐름 제거 |
| 화면 설계 | `SC-03` 아이디 찾기, `SC-04` 비밀번호 찾기, `SC-05` 비밀번호 재설정 화면 제거 |
| API 명세 | `POST /api/auth/find-login-id`, `POST /api/auth/find-password`, 비밀번호 재설정 관련 Open Questions 제거 |
| 리뷰 문서 | 비밀번호 재설정 API와 아이디 마스킹 미결정 항목 제거 |

## 2. 기존 충돌 해소 상태

| 이전 충돌 | 현재 상태 |
|---|---|
| 알림 읽음 처리 방식 불일치 | 요구사항/플로우/화면/API 모두 알림 목록 팝업 조회 시 읽음 처리로 정리됨 |
| `notification.commented_id` nullable 불일치 | 요구사항/ERD/논리/물리/API 모두 댓글 알림 NULL, 대댓글 알림 부모 댓글 id로 정리됨 |
| 게시글/댓글 익명 필드 불일치 | `post.is_anonymous`, `comments.is_anonymous` 기준으로 정리됨 |
| `is_updated` vs `updated_at` | API/ERD/논리/물리 모두 `updated_at` 기준으로 정리됨 |
| 탈퇴 회원 작성물 노출 정책 불일치 | 유지 및 조회/검색/페이지네이션 포함, 작성자명 `탈퇴한 유저` 표시로 정리됨 |
| `users.email_address` unique 누락 | 논리/물리/ERD에 unique 반영됨 |
| 그룹장 위임 기준 누락 | `group_members.joined_at` 기준으로 반영됨 |
| `users.name` 라벨 불일치 | 이름으로 통일됨 |
| ERD의 오래된 정합성 메모 | 최신 구현 메모로 갱신됨 |
| 물리 스키마 알림 관계 카디널리티 | `0 또는 1:N`으로 nullable 관계 반영됨 |

## 3. 충돌 아님, 구현 전 결정 필요

다음 항목은 문서 간 충돌은 아니지만 구현 전 확정이 필요하다. 상세는 `docs/review/03-missing-decisions.md`에 기록했다.

- 공통 에러 응답 body 형식
- 일정 종류 `type` 값 목록
- 비밀번호 변경 시 현재 비밀번호 필요 여부
- 파일 업로드 저장 위치, 제한, 수정 정책
- 예약 필드 또는 미정 기능 필드 사용 여부
- 개인 일정 상세/수정 UI 방식
- 그룹 링크 공유 UX

## 4. 구현 가능 여부

직접 충돌 기준으로는 **PASS**다.

전체 구현 착수 기준으로는 남은 결정 사항 때문에 **CONDITIONAL_PASS**다.
