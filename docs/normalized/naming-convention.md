# 이 프로젝트의 네이밍 규칙 가이드

## 목적

- 프론트엔드, 백엔드, DB, API 문서의 이름 규칙을 통일한다.
- 팀원마다 다른 명명 습관 때문에 생기는 혼동을 줄인다.
- 현재 정규화 명세와 구현 제외 범위를 반영한 네이밍 기준을 제공한다.

검증 상태: READY

주의:

- 그룹 채팅, 복잡한 초대 링크, 아이디 찾기, 비밀번호 재설정, ADMIN 권한 부여/회수 API는 구현 제외 항목이므로 예시 이름으로도 사용하지 않는다.
- API JSON 필드는 현재 API 계약에 맞춰 snake_case를 사용한다.
- 예시 API URL은 `api-contract.md`에 존재하는 경로만 사용한다.

## 1. 기본 규칙

- 화면에 보이는 메뉴명은 한글 사용
- 코드, API, DB는 영어 사용
- 한 개의 개념에는 한 개의 대표 용어만 사용
- 축약어는 꼭 필요한 경우만 사용

## 2. 레이어별 규칙

### 프론트엔드

- 컴포넌트명: PascalCase
- 변수명/함수명: camelCase
- 페이지명은 역할이 드러나게 작성

예시:
- `LoginPage`
- `PostListPage`
- `PostDetailPage`
- `CalendarPage`
- `GroupDetailPage`
- `ReportAdminPage`
- `NotificationPopup`

### 백엔드

- 클래스명: PascalCase
- 메서드명: camelCase
- 컨트롤러명은 자원 기준으로 작성

예시:
- `AuthController`
- `UserController`
- `PostController`
- `CommentController`
- `LikeController`
- `ReportController`
- `NotificationController`
- `ScheduleController`
- `GroupController`

메서드 예시:
- `login`
- `logout`
- `signup`
- `deleteUser`
- `getPostList`
- `getPostDetail`
- `createPost`
- `updatePost`
- `deletePost`
- `createLike`
- `deleteLike`
- `createReport`
- `processReport`
- `createNotification`
- `getNotifications`
- `uploadFile`

### API URL

- 소문자 사용
- 단어 구분은 하이픈보다 자원 구조 우선
- 복수형 명사 사용
- 행위보다 자원 중심으로 설계

예시:
- `/auth/login`
- `/auth/logout`
- `/users/me`
- `/posts`
- `/posts/{postId}`
- `/posts/{postId}/comments`
- `/posts/{postId}/likes`
- `/comments/{commentId}/replies`
- `/comments/{commentId}`
- `/reports`
- `/admin/reports`
- `/admin/reports/{reportId}`
- `/notifications`
- `/me/schedules`
- `/groups`
- `/groups/join`
- `/groups/{groupId}`
- `/groups/{groupId}/schedules`

### DB

- 테이블명: snake_case 권장.
- 컬럼명: snake_case
- 기본키는 `id` 또는 `xxx_id` 중 하나로 통일
- 이 프로젝트는 컬럼 기준 `xxx_id` 방식 권장

테이블 예시:
- `users`
- `post`
- `comments`
- `likes`
- `schedules`
- `groups`
- `group_members`
- `report`
- `file`
- `notification`

컬럼 예시:
- `user_id`
- `post_id`
- `comment_id`
- `group_id`
- `reporter_id`
- `target_type`
- `target_id`
- `reason_type`
- `view_count`
- `created_at`
- `updated_at`
- `deleted_at` (현재 원본 스키마에서는 `users`에만 사용)
- `processed_at`
- `joined_at`

## 3. ID 네이밍 규칙

- 사용자: `userId`
- 게시글: `postId`
- 댓글: `commentId`
- 일정: `scheduleId`
- 그룹: `groupId`
- 신고: `reportId`
- 파일: `fileId` 또는 첨부파일이 속한 게시글의 `postId`
- 첨부파일 URL: `fileUrl`

DB에서는 다음처럼 대응:

- `user_id`
- `post_id`
- `comment_id`
- `schedule_id`
- `group_id`
- `report_id`
- `file_id`

주의: 현재 DB `file` 테이블은 원본 스키마에 따라 게시글 참조 컬럼명을 `id`로 사용한다. 코드/DTO에서는 혼동 방지를 위해 `postId` 또는 `post_id`로 매핑명을 명확히 둔다.

## 4. 날짜/시간 네이밍 규칙

- 생성일: `createdAt`
- 수정일: `updatedAt`
- 삭제일: `deletedAt` (현재 원본 스키마에서는 회원 탈퇴 일시에만 사용)
- 처리일시: `processedAt`
- 시작일시: `startAt`
- 종료일시: `endAt`
- 참여일: `joinedAt`

DB 예시:

- `created_at`
- `updated_at`
- `deleted_at` (회원 탈퇴 일시)
- `processed_at`
- `start_at`
- `end_at`
- `joined_at`

## 5. Boolean 네이밍 규칙

- 참/거짓 값은 의미가 드러나게 작성
- 프론트와 API는 `is`, `has`, `can` 접두어 사용 가능

예시:
- `isRead`
- `isMine`
- `hasFile`
- `canEdit`

## 6. Enum 네이밍 규칙

- Enum 값은 모두 대문자 사용
- 단일 단어 또는 언더스코어 구분 사용 가능

예시:
- `EXAM`
- `ASSIGNMENT`
- `STUDY`
- `LEADER`
- `MEMBER`
- `PENDING`
- `PROCESSED`
- `ACTIVE`
- `DELETED`

## 7. 혼용 금지 규칙

- `board` 대신 `post`
- `reply` 대신 `comment`
- `event` 대신 `schedule`
- `team` 대신 `group`
- `writer` 대신 `author`
- `regDate` 대신 `createdAt`
- `reaction` 대신 이 프로젝트에서는 게시글 추천을 의미하는 `like` 사용
- `group_link`는 별도 URL이 아니라 `group_code`와 같은 값으로 취급
- `reply`는 API 경로에서 대댓글 작성 행위를 표현할 때만 사용하고, 저장 모델은 `comments.parent_comment`로 통일

## 8. 추천 패턴

- 목록 조회: `getPostList`, `getGroupList`
- 상세 조회: `getPostDetail`, `getUserProfile`
- 생성: `createPost`, `createSchedule`, `createGroup`, `createLike`, `createReport`
- 수정: `updatePost`, `updateSchedule`, `updateUser`
- 삭제: `deletePost`, `deleteComment`, `deleteSchedule`
- 그룹 가입: `joinGroupByCode`
- 신고 처리: `processReport`
- 파일 저장: `storePostFile`, `replacePostFiles`

## 9. 팀 적용 방식

1. API 명세서에 나온 필드명을 기준으로 화면 폼 이름을 맞춘다.
2. API 요청/응답 JSON 키는 `api-contract.md`의 snake_case를 유지한다.
3. 백엔드 언어 내부 DTO가 camelCase를 쓰더라도 직렬화 경계에서는 API 계약 필드명과 일치시킨다.
4. DB 컬럼은 API 이름과 1:1로 바로 대응되도록 설계한다.
5. 새 기능 추가 시 먼저 용어부터 확정하고 구현에 들어간다.

## 10. 최종 권장 기준

- 화면: 한글
- API URL: 영어 복수형 자원명
- API JSON: snake_case
- DTO 내부 필드: 언어 관례를 따르되 API 직렬화명은 snake_case
- Java 클래스: PascalCase
- DB: snake_case
- 상태값: 대문자 Enum
