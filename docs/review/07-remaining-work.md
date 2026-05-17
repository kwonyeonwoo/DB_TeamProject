# 07. Remaining Work Register

정리일: 2026-05-17

## 1. 현재 결론

현재 문서 검증과 기능별 리뷰 기준으로 구현을 막는 `BLOCKER`, `MAJOR`, `QUESTION`급 미해결 이슈는 없다.

확인 근거:

- `docs/review/01-document-validation-report.md`: 구현 착수 판정 `PASS`
- `docs/review/02-spec-conflict-list.md`: 현재 미해결 직접 충돌 없음
- `docs/review/03-missing-decisions.md`: 현재 구현 전 필수 결정 없음
- `docs/review/04-normalized-spec-review.md`: `APPROVED: 구현 가능`
- 기능별 리뷰 문서: AUTH, POST, COMMENT, NOTI, REPORT, CAL, GROUP, USER, 통합 리뷰가 모두 `RESOLVED` 또는 `APPROVED`
- normalized 문서의 Open Questions: 현재 source 문서 기준 미해결 Open Question 없음
- 최근 검증: `backend`에서 `.\gradlew.bat test --rerun-tasks` PASS, 102 tests, 0 failures, 0 errors, 0 skipped

## 2. 남은 후속 작업

| ID | Priority | Status | Area | 작업 | 근거 | 완료 기준 |
|---|---|---|---|---|---|---|
| RW-001 | P1 | OPEN | Security hardening | 운영 환경용 세션 쿠키 `Secure`, `SameSite` 설정을 명시할지 결정하고, 결정 시 `application.yml` 또는 환경별 설정 문서에 반영한다. | `feature-AUTH-code-review.md`의 Remaining risk. CSRF 방어는 해결됐지만 세션 쿠키 운영 설정은 별도 하드닝 항목이다. | 운영/로컬 환경별 쿠키 정책이 문서화되고, 필요한 경우 설정과 검증 테스트가 추가된다. |
| RW-002 | P1 | RESOLVED | Change management | 현재 작업 트리의 기능별 변경 묶음을 실제 커밋/PR 단위로 정리한다. | `06-feature-change-bundles.md`에 기능별 commit map을 기록했다. | `codex/rw-002-feature-commits` 브랜치에 기능별 커밋이 생성됐고, 보조 patch 묶음도 `docs/review/change-bundles/`에 남아 있다. |
| RW-003 | P2 | OPEN | UX decision | 알림 클릭 시 원본 댓글 또는 부모 댓글이 삭제된 경우의 화면 fallback을 결정한다. | `05-notification-source-conflict-report.md`의 Remaining Implementation Review Risk. 백엔드 스냅샷/FK 정책은 해결됐지만, 삭제된 댓글 위치로 이동할 때의 프론트 UX는 별도 결정이 필요할 수 있다. | 화면/사용자 흐름 문서에 fallback 동작이 명시되거나, 백엔드 범위 밖으로 명확히 제외된다. |
| RW-004 | P2 | OPEN | Release verification | 최종 변경 묶음 정리 후 전체 테스트를 다시 실행하고 결과를 리뷰 문서 또는 릴리스 메모에 기록한다. | 현재 테스트는 통과했지만, 기능별 patch/커밋 정리 과정에서 충돌 또는 누락이 생길 수 있다. | 최종 상태에서 `.\gradlew.bat test --rerun-tasks`가 통과하고 테스트 수/실패 수가 기록된다. |

## 3. 해결 확인된 항목

아래 항목은 추가 작업으로 남기지 않는다.

| 항목 | 확인 결과 |
|---|---|
| 탈퇴 작성자 표시 문자열 | 게시글과 댓글 모두 `탈퇴한 유저` 기준으로 정리됐다. |
| CSRF 요구 문서화 | `api-contract.md`, `auth-policy.md`, `acceptance-criteria.md`에 상태 변경 API CSRF 요구와 `403 ACCESS_DENIED` 실패 조건이 반영됐다. |
| `notification.commented_id` FK cascade | migration에서 `commented_id -> comments.id` FK가 제거되고 index-only navigation hint로 정리됐다. |
| 댓글/대댓글 삭제 후 알림 스냅샷 유지 | COMMENT 리뷰와 테스트 기준으로 기존 알림 row 및 `comment_content` 유지가 검증됐다. |
| NOTI 알림 생성 책임 | `NotificationService`와 `CommentService` 연동으로 댓글/대댓글 알림 생성 및 자기 대상 알림 미생성이 검증됐다. |
| normalized Open Questions | 현재 source 문서 기준 미해결 Open Question은 없다. |

## 4. 구현 제외로 유지할 항목

아래는 남은 작업이 아니라 명세상 제외 범위다. 범위가 변경되기 전까지 구현하거나 테스트 기대값으로 만들지 않는다.

| 제외 ID | 항목 |
|---|---|
| X-001 | 아이디 찾기 |
| X-002 | 비밀번호 찾기/재설정 |
| X-003 | ADMIN 권한 부여/회수 API |
| X-004 | 그룹 채팅 |
| X-005 | 복잡한 초대 링크/외부 공유/만료/재발급 |
| X-006 | 파일 메타데이터 저장 |
| X-007 | 신고 처리 시 게시글/댓글 자동 삭제 |

## 5. 다음 권장 순서

1. `RW-004` 전체 테스트를 최종 커밋 상태에서 재실행한다.
2. 배포 또는 운영 설정 단계에서 `RW-001` 세션 쿠키 정책을 확정한다.
3. 프론트엔드 화면 흐름을 다루는 단계에서 `RW-003` 알림 fallback UX를 결정한다.
