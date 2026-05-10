## 1. 릴레이션 스키마 리스트

### [1] 사용자 (Users)
*   users (**id**, login_id, password, name, email_address, created_at, deleted_at, status, role)
    *   PK: `id` (int)
    *   Unique: `login_id`, `email_address`
    *   Note: `status` (ACTIVE, INACTIVE, DELETED)

### [2] 게시물 (post)
*   post (**id**, user_id, title, content, created_at, updated_at, deleted_at, status, view_count, is_reported, main_category, sub_category, is_anonymous)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *   Note: `status` (ACTIVE, DELETED), `is_anonymous` (익명 작성 여부)

### [3] 추천 (likes)
*   likes (**id**, user_id, post_id, created_at)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `post_id` → post(id)
    *   Unique: ('user_id', 'post_id')

### [4] 댓글 (comments)
*   comments (**id**, user_id, post_id, parent_comment, content, is_anonymous, created_at, updated_at, deleted_at, status)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `post_id` → post(id)
    *       `parent_comment` → comments(id) //대댓글을 위한 자기참조
    *   Note: `status` (ACTIVE, DELETED)

### [5] 스터디 그룹 (Groups)
*   groups (**id**, group_link, name, creator_id, created_at, deleted_at, status)
    *   PK: `id` (int)
    *   Unique: `group_link`
    *   FK: `creator_id` → users(id)
    *   Note: `status` (ACTIVE, INACTIVE, DELETED)

### [6] 그룹 멤버 (Group_Members)
*   group_members (**group_id**, **user_id**, role, joined_at)
    *   PK: (`group_id`, `user_id`)
    *   FK: `group_id` → groups(id)
    *       `user_id` → users(id)
    *   Note: `role` (LEADER, MEMBER), `joined_at` 기준으로 리더 위임 판단
    *   Note: 그룹의 최초 생성자는 `groups.creator_id`, 현재 그룹장은 `group_members.role = LEADER`로 판단

### [7] 통합 일정 (Schedules)
*   schedules (**id**, user_id, group_id, title, start_at, end_at, description, type, created_at, updated_at, deleted_at, status)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `group_id` → groups(id) (NULL이면 개인 일정)
    *   Note: `status` (ACTIVE, DELETED)

### [8] 첨부파일(file) 
*   file (**id**, file_url)
    *   PK: (id, file_url)
    *   FK: 'id' → post(id)

### [9] 알림(notification)
*   notification(**id**, is_read, comment_content, commented_post_id, commented_user_id, commented_id, created_at)
    *   PK: (id)
    *   FK: `commented_post_id` → post(id)
    *       `commented_user_id` → users(id)
    *       `commented_id` → comments(id)
    *   Note: `commented_id`는 댓글 알림이면 NULL, 대댓글 알림이면 부모 댓글 id
    *   Note: `comment_content`는 알림에 표시할 댓글 내용을 보관한다.
