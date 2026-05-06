# 이 프로젝트의 네이밍 규칙 가이드

## 목적

- 프론트엔드, 백엔드, DB, API 문서의 이름 규칙을 통일한다.
- 팀원마다 다른 명명 습관 때문에 생기는 혼동을 줄인다.
- 요구사항 충족용 ERD 확장을 반영한 네이밍 기준을 제공한다.

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
- `InvitePage`
- `ChatPage`

### 백엔드

- 클래스명: PascalCase
- 메서드명: camelCase
- 컨트롤러명은 자원 기준으로 작성

예시:
- `AuthController`
- `UserController`
- `PostController`
- `CommentController`
- `ReactionController`
- `ScheduleController`
- `GroupController`
- `GroupInviteController`
- `MessageController`
- `FileController`

메서드 예시:
- `login`
- `logout`
- `signup`
- `findLoginId`
- `resetPassword`
- `deleteUser`
- `getPostList`
- `getPostDetail`
- `createPost`
- `updatePost`
- `deletePost`
- `createReaction`
- `cancelReaction`
- `inviteGroupMember`
- `acceptInvite`
- `rejectInvite`
- `sendMessage`
- `uploadFile`

### API URL

- 소문자 사용
- 단어 구분은 하이픈보다 자원 구조 우선
- 복수형 명사 사용
- 행위보다 자원 중심으로 설계

예시:
- `/auth/login`
- `/auth/logout`
- `/auth/refresh`
- `/users/me`
- `/posts`
- `/posts/{postId}`
- `/posts/{postId}/comments`
- `/posts/{postId}/reactions`
- `/comments/{commentId}/reactions`
- `/schedules`
- `/groups/{groupId}/members`
- `/groups/{groupId}/invite`
- `/groups/invites/{inviteId}/accept`
- `/groups/{groupId}/messages`
- `/files/{fileId}`

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
- `members`
- `group_invites`
- `messages`
- `files`
- `refresh_tokens`

컬럼 예시:
- `user_id`
- `post_id`
- `comment_id`
- `group_id`
- `reaction_type`
- `view_count`
- `created_at`
- `updated_at`
- `deleted_at`
- `expires_at`

## 3. ID 네이밍 규칙

- 사용자: `userId`
- 게시글: `postId`
- 댓글: `commentId`
- 일정: `scheduleId`
- 그룹: `groupId`
- 초대: `inviteId`
- 메시지: `messageId`
- 파일: `fileId`
- 첨부파일 URL: `fileUrl`

DB에서는 다음처럼 대응:

- `user_id`
- `post_id`
- `comment_id`
- `schedule_id`
- `group_id`
- `invite_id`
- `message_id`
- `file_id`

## 4. 날짜/시간 네이밍 규칙

- 생성일: `createdAt`
- 수정일: `updatedAt`
- 삭제일: `deletedAt`
- 만료일: `expiresAt`
- 전송일: `sentAt`
- 시작일시: `startDateTime`
- 종료일시: `endDateTime`
- 참여일: `joinedAt`

DB 예시:

- `created_at`
- `updated_at`
- `deleted_at`
- `expires_at`
- `sent_at`
- `start_datetime`
- `end_datetime`
- `joined_at`

## 5. Boolean 네이밍 규칙

- 참/거짓 값은 의미가 드러나게 작성
- 프론트와 API는 `is`, `has`, `can` 접두어 사용 가능

예시:
- `isRead`
- `isMine`
- `isDeleted`
- `hasFile`
- `canEdit`

## 6. Enum 네이밍 규칙

- Enum 값은 모두 대문자 사용
- 단일 단어 또는 언더스코어 구분 사용 가능

예시:
- `EXAM`
- `ASSIGNMENT`
- `STUDY`
- `TEAM`
- `PERSONAL`
- `LEADER`
- `MEMBER`
- `LIKE`
- `DISLIKE`
- `PENDING`
- `ACCEPTED`
- `REJECTED`
- `ACTIVE`
- `DELETED`

## 7. 혼용 금지 규칙

- `board` 대신 `post`
- `reply` 대신 `comment`
- `event` 대신 `schedule`
- `team` 대신 `group`
- `writer` 대신 `author`
- `regDate` 대신 `createdAt`
- `like/dislike`를 개별 필드로 만들지 않고 `reactionType`으로 통일
- `invite code`와 `invite request`를 문맥 없이 혼용하지 않음

## 8. 추천 패턴

- 목록 조회: `getPostList`, `getGroupList`
- 상세 조회: `getPostDetail`, `getUserProfile`
- 생성: `createPost`, `createSchedule`, `createGroup`, `createReaction`
- 수정: `updatePost`, `updateSchedule`, `updateUser`
- 삭제: `deletePost`, `deleteComment`, `deleteSchedule`
- 초대: `inviteGroupMember`, `acceptInvite`, `rejectInvite`
- 파일: `uploadFile`, `deleteFile`

## 9. 팀 적용 방식

1. API 명세서에 나온 필드명을 기준으로 화면 폼 이름을 맞춘다.
2. 백엔드 DTO와 응답 JSON 키를 같은 이름으로 유지한다.
3. DB 컬럼은 API 이름과 1:1로 바로 대응되도록 설계한다.
4. 새 기능 추가 시 먼저 용어부터 확정하고 구현에 들어간다.

## 10. 최종 권장 기준

- 화면: 한글
- API: 영어 복수형 자원명
- DTO: camelCase
- Java 클래스: PascalCase
- DB: snake_case
- 상태값: 대문자 Enum