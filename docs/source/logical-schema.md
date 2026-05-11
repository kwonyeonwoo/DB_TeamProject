## 1. 릴레이션 스키마 리스트

### [1] 사용자 (Users)
*   users (**id**, login_id, password, name, email_address, created_at, deleted_at, status, role)
    *   PK: `id` (int)
    *   Unique: `login_id`, `email_address`
    *   Note: `status` (ACTIVE, DELETED)
    *   Note: `role` (USER, ADMIN), 회원 가입 기본값 USER. ADMIN은 DB seed 데이터 또는 운영자의 직접 DB 변경으로만 부여

### [2] 게시물 (post)
*   post (**id**, user_id, title, content, created_at, updated_at, view_count, main_category, sub_category, is_anonymous)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *   Note: `is_anonymous` (익명 작성 여부)

### [3] 추천 (likes)
*   likes (**id**, user_id, post_id, created_at)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `post_id` → post(id)
    *   Unique: ('user_id', 'post_id')

### [4] 댓글 (comments)
*   comments (**id**, user_id, post_id, parent_comment, content, is_anonymous, created_at, updated_at)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `post_id` → post(id)
    *       `parent_comment` → comments(id) //대댓글을 위한 자기참조

### [5] 신고 (report)
*   report (**id**, reporter_id, target_type, target_id, reason_type, created_at, status, processed_by, processed_at)
    *   PK: `id` (int)
    *   FK: `reporter_id` → users(id)
    *   FK: `processed_by` → users(id), nullable
    *   Unique: (`reporter_id`, `target_type`, `target_id`)
    *   Note: `target_type` (POST, COMMENT)
    *   Note: `target_id`는 `target_type`에 따라 post(id) 또는 comments(id)를 의미한다. COMMENT는 댓글과 대댓글을 모두 포함한다.
    *   Note: `reason_type` (1: 부적절한 내용, 2: 광고/도배, 3: 저작권 침해, 4: 기타)
    *   Note: `status` (PENDING, PROCESSED)
    *   Note: `processed_by`, `processed_at`은 관리자 신고 처리 시 기록하는 최소 처리 이력이다.
    *   Note: 다형 대상 참조(`target_type`, `target_id`)의 실제 대상 존재 여부는 구현 단계에서 서비스 로직 또는 트리거로 검증한다.

### [6] 스터디 그룹 (Groups)
*   groups (**id**, group_code, name, leader_id, created_at)
    *   PK: `id` (int)
    *   Unique: `group_code`
    *   FK: `creator_id` → users(id)
    *   Note: 화면에서 `group_link`라고 표현하는 값은 그룹 가입 코드(`group_code`)와 같은 값이다. 초대 URL, 외부 공유, 만료 시간, 재발급 기능은 제외한다.

### [7] 그룹 멤버 (Group_Members)
*   group_members (**group_id**, **user_id**, role, joined_at)
    *   PK: (`group_id`, `user_id`)
    *   FK: `group_id` → groups(id)
    *       `user_id` → users(id)
    *   Note: `role` (LEADER, MEMBER), `joined_at` 기준으로 리더 위임 판단
    *   Note: 그룹의 최초 생성자는 `groups.creator_id`, 현재 그룹장은 `group_members.role = LEADER`로 판단

### [8] 통합 일정 (Schedules)
*   schedules (**id**, user_id, group_id, title, start_at, end_at, description, type, created_at, updated_at)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `group_id` → groups(id) (NULL이면 개인 일정)
    *   Note: `type` (1: 수업, 2: 과제, 3: 시험, 4: 스터디, 5: 기타)

### [9] 첨부파일(file)
*   file (**id**, file_url)
    *   PK: (id, file_url)
    *   FK: 'id' → post(id)
    *   Note: 실제 파일은 `/uploads/posts/{post_id}/...` 형식의 서버 로컬 경로에 저장하고 DB에는 `file_url`만 저장한다. 별도 파일 메타데이터는 저장하지 않는다.

### [10] 알림(notification)
*   notification(**id**, is_read, comment_content, commented_post_id, commented_user_id, commented_id, created_at)
    *   PK: (id)
    *   FK: `commented_post_id` → post(id)
    *       `commented_user_id` → users(id)
    *       `commented_id` → comments(id)
    *   Note: `commented_id`는 댓글 알림이면 NULL, 대댓글 알림이면 부모 댓글 id
    *   Note: `comment_content`는 알림에 표시할 댓글 내용을 보관한다.
