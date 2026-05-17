# Feature Change Bundles

검증일: 2026-05-17

## Summary

통합 검증 전환 대신 실제 기능별 commit 묶음으로 변경 범위를 분리했다. 각 리뷰 문서의 `CR-001` 범위 분리 이슈는 아래 commit과 보조 patch 산출물을 기준으로 해결된 것으로 판정한다.

## Commit Map

| Commit | Bundle | 내용 |
|---|---|---|
| `819ba07` | COMMENT/notification contract | 알림 스냅샷/FK 정책 문서와 COMMENT 테스트/리뷰 정리 |
| `b72e335` | REPORT | 신고 생성, 관리자 신고 조회/처리 구현과 테스트 |
| `1930022` | POST | 게시글 응답/필터/파일/삭제 cascade/추천 회귀 테스트 보강 |
| `e8c9e33` | GROUP | 그룹 생성, 가입, 목록, 상세 구현과 테스트 |
| `a440ee0` | CAL/GCAL | 개인 일정과 그룹 일정 API 구현 및 테스트 |
| `979b12f` | USER | 내 정보 조회/수정, 회원 탈퇴 생명주기 구현 및 테스트 |
| `f377511` | Auth-test | 인증 인프라 테스트 보강과 AUTH 리뷰 문서 정리 |
| `0f69c61` | NOTI review | NOTI 리뷰 문서명/내용 정리 |
| `dfa534b` | Integration review | 통합 리뷰 지적사항 해결: 탈퇴 작성자 표시와 CSRF 계약 문서화 |

## Bundle Map

| Bundle | Commit | Patch | Ownership |
|---|---|---|---|
| POST | `1930022`, `dfa534b` | `docs/review/change-bundles/post.patch` | `PostResponse`, `PostService` 탈퇴 작성자 표시, `PostControllerTest`, POST/통합 리뷰 문서 |
| NOTI | `0f69c61` | `docs/review/change-bundles/noti.patch` | NOTI 리뷰 문서와 NOTI 소유 검증 범위 기록. 현재 코드 변경은 COMMENT 연동 지점에서 검증 |
| COMMENT | `819ba07` | `docs/review/change-bundles/comment.patch` | `CommentService`, `CommentControllerTest`, notification snapshot/FK 정책 문서와 migration |
| REPORT | `b72e335` | `docs/review/change-bundles/report.patch` | REPORT controller/service/dto/domain/repository/test와 REPORT 리뷰 문서 |
| CAL/GCAL | `a440ee0` | `docs/review/change-bundles/cal.patch` | 개인 일정과 그룹 일정 controller/service/dto/domain/repository/test |
| GROUP | `e8c9e33` | `docs/review/change-bundles/group.patch` | GROUP controller/service/dto/domain/repository/test |
| USER | `979b12f` | `docs/review/change-bundles/user.patch` | USER controller/service/dto/domain/repository/test와 USER-003 생명주기 |
| Auth-test | `f377511` | `docs/review/change-bundles/auth-test.patch` | 인증 인프라 테스트 보강과 AUTH 리뷰 문서 |
| Integration review | `dfa534b` | 없음 | `feature-통합-review.md`, CSRF 계약 문서화, 탈퇴 작성자 표시 문자열 정리 |

## Review Resolution

- `POST-CR-001`: RESOLVED. REPORT 변경은 `report.patch`로 분리했다.
- `COMMENT-CR-001`: RESOLVED. POST/REPORT/Auth-test 변경은 각각 별도 patch로 분리했다.
- `REPORT-CR-001`: RESOLVED. COMMENT/POST/Auth-test 변경은 각각 별도 patch로 분리했다.
- `CAL-CR-001`: RESOLVED. POST/COMMENT/REPORT/Auth-test/notification 정책 변경은 각각 별도 patch 또는 별도 검증 범위로 분리했다.
- `NOTI-001-CR-001`: 기존 nullable 응답 필드 이슈이며 범위 분리 이슈가 아니다. NOTI 리뷰 문서에는 별도 분리 묶음과 연동 지점만 보강했다.
- `GROUP-CR-001`: RESOLVED. GROUP 변경은 `group.patch`로 분리했고 REPORT/CAL/POST/COMMENT/Auth-test/notification FK 변경은 GROUP 묶음에서 제외했다.
- `USER-CR-001`: RESOLVED. USER 변경은 `user.patch`로 분리했고 REPORT/CAL endpoint/POST/COMMENT/notification FK 변경은 USER 묶음에서 제외했다.

## Notes

- commit 묶음이 현재 RW-002의 1차 변경 단위다.
- patch 파일은 리뷰와 재적용을 위한 보조 산출물이며, 현재 작업 브랜치의 commit을 되돌리거나 대체하지 않는다.
- `memo.md`는 기능 검증 범위에 포함하지 않고 진행 메모 문서 변경으로만 취급한다.
