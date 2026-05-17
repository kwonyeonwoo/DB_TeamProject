# feature-NOTI-001 Code Review

판정: RESOLVED

재검증일: 2026-05-16

## 검증 범위

- `backend/src/main/java/com/academicshare/backend/notification`
- `backend/src/main/java/com/academicshare/backend/comment/service/CommentService.java`
- 관련 테스트: `backend/src/test/java/com/academicshare/backend/notification`
- 분리 묶음: `docs/review/change-bundles/noti.patch`
- 비고: `CommentService`는 NOTI 소유 코드가 아니라 댓글/대댓글 생성 시 알림 생성 API를 호출하는 연동 지점으로만 검증한다.

## 테스트 결과

- `backend`: `.\gradlew.bat test` PASS

## Summary

- 기존 리뷰의 `NOTI-001-CR-001`, `NOTI-001-CR-002`는 현재 코드 기준으로 해결된 상태입니다.
- 알림 응답의 nullable 필드 `commented_id`는 `null`일 때도 응답에 포함됩니다.
- 댓글/대댓글 작성 시 알림을 생성하는 명시적 서비스 API가 추가되어 있고, `CommentService`에서 해당 API를 호출합니다.
- NOTI 변경 묶음은 `noti.patch`로 분리했습니다. COMMENT의 알림 snapshot/FK 정책, POST, REPORT, Auth-test 변경은 NOTI 묶음에 포함하지 않습니다.

## Issues

### NOTI-001-CR-001

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/api-contract.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/db-schema-contract.md`
- Related code:
  - `backend/src/main/resources/application.yml:10`
  - `backend/src/main/java/com/academicshare/backend/notification/dto/NotificationResponse.java:7`
  - `backend/src/test/java/com/academicshare/backend/notification/controller/NotificationControllerTest.java:89`
  - `backend/src/test/java/com/academicshare/backend/notification/controller/NotificationControllerTest.java:91`
- Original problem: 댓글 알림의 `commented_id`는 contract상 `NULL`이어야 하지만 전역 Jackson `non_null` 설정 때문에 필드 자체가 누락될 수 있었습니다.
- Current finding: `NotificationResponse`에 `@JsonInclude(JsonInclude.Include.ALWAYS)`가 적용되어 `commented_id: null`이 유지됩니다.
- Evidence:
  - 일반 댓글 알림 응답에서 `commented_id == null` 검증
  - 대댓글 알림 응답에서 `commented_id == parentComment.id` 검증
- Required user decision: 없음.

### NOTI-001-CR-002

- Severity: MAJOR
- Current status: RESOLVED
- Related document: `docs/normalized/product-spec.md`, `docs/normalized/feature-list.md`, `docs/normalized/domain-model.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/notification/service/NotificationService.java:37`
  - `backend/src/main/java/com/academicshare/backend/notification/service/NotificationService.java:56`
  - `backend/src/main/java/com/academicshare/backend/comment/service/CommentService.java:80`
  - `backend/src/main/java/com/academicshare/backend/comment/service/CommentService.java:107`
  - `backend/src/test/java/com/academicshare/backend/notification/service/NotificationServiceTest.java:41`
  - `backend/src/test/java/com/academicshare/backend/notification/service/NotificationServiceTest.java:64`
  - `backend/src/test/java/com/academicshare/backend/notification/service/NotificationServiceTest.java:80`
  - `backend/src/test/java/com/academicshare/backend/notification/service/NotificationServiceTest.java:112`
- Original problem: NOTI-001 명세에는 댓글/대댓글 작성 시 알림 생성 규칙이 포함되어 있지만, 기존 구현에는 조회/읽음 처리만 있고 생성 책임이 없었습니다.
- Current finding: `NotificationService`에 댓글 알림과 대댓글 알림 생성 메서드가 추가되어 있습니다. 작성자와 수신자가 같으면 알림을 생성하지 않고, 일반 댓글은 `commented_id = null`, 대댓글은 `commented_id = parentCommentId`로 저장합니다. `CommentService`도 댓글/대댓글 생성 후 해당 알림 메서드를 호출합니다.
- Evidence:
  - `createCommentNotificationIfNeeded`
  - `createReplyNotificationIfNeeded`
  - 자기 게시글 댓글 알림 미생성 테스트
  - 자기 댓글 대댓글 알림 미생성 테스트
  - 일반 댓글 알림 `commented_id = null` 테스트
  - 대댓글 알림 `commented_id = parentCommentId` 테스트
- Required user decision: 없음.

## Checklist Result

1. 알림 조회 API contract: PASS
2. 본인 알림만 조회: PASS
3. 조회된 알림 읽음 처리: PASS
4. `commented_id` nullable 응답 필드 직렬화: PASS
5. 댓글 알림 생성 책임: PASS
6. 대댓글 알림 생성 책임: PASS
7. 자기 자신 알림 미생성 규칙: PASS
8. NOTI 변경 묶음 분리: PASS - NOTI 소유 변경과 COMMENT 연동 지점을 구분해 기록
