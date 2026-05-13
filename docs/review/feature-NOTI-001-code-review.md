# feature-NOTI-001 Code Review

판정: NEEDS_FIX

검증 범위:
- `backend/src/main/java/com/academicshare/backend/notification`
- notification 관련 Entity/Repository, 조회 API, 읽음 처리, 인증 연동
- 관련 테스트: `backend/src/test/java/com/academicshare/backend/notification`

테스트 결과:
- `backend`: `.\gradlew.bat test --tests "*NotificationControllerTest" --rerun-tasks` PASS

## Summary

- `GET /api/notifications` endpoint, method, 인증 요구, `{ "items": [...] }` 응답 구조는 API contract와 대체로 일치합니다.
- `commented_user_id = current_user_id` 조건으로 본인 알림만 조회하고, 조회된 알림만 `is_read = true`로 변경하는 흐름은 명세 방향과 맞습니다.
- DB migration과 `Notification` Entity의 기본 컬럼 매핑은 `db-schema-contract.md`와 일치합니다.
- 다만 nullable 응답 필드 표현과 알림 생성 책임이 명세와 맞지 않습니다.

## Issues

### NOTI-001-CR-001

- Severity: MAJOR
- Related document: `docs/normalized/api-contract.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/db-schema-contract.md`
- Related code:
  - `backend/src/main/resources/application.yml:9`
  - `backend/src/main/resources/application.yml:10`
  - `backend/src/main/java/com/academicshare/backend/notification/dto/NotificationResponse.java:6`
  - `backend/src/main/java/com/academicshare/backend/notification/dto/NotificationResponse.java:12`
  - `backend/src/test/java/com/academicshare/backend/notification/controller/NotificationControllerTest.java:88`
- Problem: 댓글 알림의 `commented_id`는 contract상 `NULL`이어야 하지만, 전역 Jackson 설정이 `non_null`이라 null 값이면 응답 필드 자체가 누락됩니다. 현재 테스트도 `commented_id`가 null로 존재하는지 확인하지 않고, JSONPath 결과가 비어 있는지만 확인합니다.
- Why it matters: `commented_id = null`은 댓글 알림과 대댓글 알림을 구분하는 계약 필드입니다. 필드 누락과 명시적 null은 클라이언트에서 다르게 처리될 수 있어 알림 이동 로직이 흔들릴 수 있습니다.
- Suggested fix: `NotificationResponse`의 `commentedId` 필드에는 null이어도 `commented_id: null`이 직렬화되도록 `@JsonInclude(JsonInclude.Include.ALWAYS)`를 적용하세요. 전역 `non_null`은 유지해도 됩니다. 만약 null 필드 생략을 의도한다면 `api-contract.md`에 nullable 필드 생략 규칙을 명시하고 acceptance criteria도 수정해야 합니다.
- Required user decision: nullable 응답 필드를 명시적 `null`로 보낼지, 생략할지 결정 필요. 현재 명세 기준 추천은 `commented_id: null` 포함입니다.
- Required tests:
  - 댓글 알림 응답에서 `commented_id` 필드가 존재하고 값이 `null`인지 검증
  - 대댓글 알림 응답에서 `commented_id`가 부모 댓글 id로 반환되는지 유지 검증

### NOTI-001-CR-002

- Severity: MAJOR
- Related document: `docs/normalized/product-spec.md`, `docs/normalized/feature-list.md`, `docs/normalized/domain-model.md`, `docs/normalized/acceptance-criteria.md`, `docs/normalized/implementation-plan.md`
- Related code:
  - `backend/src/main/java/com/academicshare/backend/notification/service/NotificationService.java:22`
  - `backend/src/main/java/com/academicshare/backend/notification/service/NotificationService.java:23`
  - `backend/src/test/java/com/academicshare/backend/notification/controller/NotificationControllerTest.java:61`
  - `backend/src/test/java/com/academicshare/backend/notification/controller/NotificationControllerTest.java:67`
- Problem: 명세는 NOTI-001을 조회/읽음 처리뿐 아니라 댓글/대댓글 작성 시 생성되는 알림 데이터와 연결합니다. 현재 `NotificationService`에는 조회 메서드만 있고, 댓글 알림 생성, 대댓글 알림 생성, 자기 게시글/댓글에 대한 알림 미생성 규칙을 담당하는 helper 또는 service API가 없습니다. 테스트도 알림을 직접 저장한 뒤 조회만 검증합니다.
- Why it matters: 이후 댓글 기능이 구현될 때 각 서비스가 알림 생성 규칙을 임의로 재구현할 가능성이 큽니다. 특히 `commented_id = null` vs 부모 댓글 id, 자기 대상 알림 미생성 규칙은 NOTI-001의 핵심 추적 조건입니다.
- Suggested fix: Notification 도메인에 명시적인 생성 메서드를 추가하세요. 예: `createCommentNotification(commentContent, postId, postAuthorId, actorUserId)`와 `createReplyNotification(commentContent, postId, parentCommentId, parentCommentAuthorId, actorUserId)`. 두 메서드는 수신자와 작성자가 같으면 저장하지 않고, 댓글 알림은 `commented_id = null`, 대댓글 알림은 `commented_id = parentCommentId`로 저장해야 합니다. COMMENT-002/COMMENT-003 구현 시 이 helper만 사용하도록 연결하세요.
- Required user decision: 이번 notification 구현 범위를 `GET /api/notifications` 조회 전용으로 볼지, NOTI-001 전체로 볼지 결정 필요. 조회 전용이라면 이 생성 책임을 후속 COMMENT 단계의 필수 작업으로 문서화해야 합니다.
- Required tests:
  - 다른 사용자의 게시글에 댓글 작성 시 게시글 작성자에게 알림 생성
  - 자기 게시글에 댓글 작성 시 알림 미생성
  - 다른 사용자의 댓글에 대댓글 작성 시 부모 댓글 작성자에게 알림 생성
  - 자기 댓글에 대댓글 작성 시 알림 미생성
  - 생성된 댓글 알림은 `commented_id = null`, 대댓글 알림은 `commented_id = parentCommentId`

## Notes

- `403 다른 회원 알림 접근` 조건은 현재 API가 사용자 id나 알림 id를 입력받지 않고 현재 세션 사용자 기준으로만 조회하므로 직접 호출 경로가 없습니다. 현재 테스트는 다른 사용자의 알림이 응답에서 제외되는 것을 검증하고 있습니다.
- notification 전용 추가 보안 위험은 확인되지 않았습니다. 다만 세션 기반 상태 변경 API의 CSRF 정책은 `feature-AUTH-001-code-review.md`의 보안 이슈를 따릅니다.

