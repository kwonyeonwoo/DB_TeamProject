## 1. 릴레이션 스키마 리스트

### [1] 사용자 (Users)
*   users (**id**, login_id, password, name, identity_info, created_at, deleted_at, status, role)
    *   PK: `id` (int)
    *   Unique: `login_id`
    *   Note: `status` (ACTIVE, DELETED)

### [2] 게시물 (post)
*   post (**id**, user_id, category, title, content, file_url, created_at, is_updated, view_count, is_reported)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)

### [3] 추천 (likes)
*   likes (**id**, user_id, post_id, created_at)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `post_id` → post(id)

### [4] 댓글 (comments)
*   comments (**id**, user_id, post_id, comment_id, content, is_public, created_at, is_updated)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `post_id` → post(id)
    *       `comment_id' → comments(id) //대댓글을 위한 자기참조

### [5] 스터디 그룹 (Groups)
*   groups (**id**, group_link, name, creator_id, created_at)
    *   PK: `id` (int)
    *   Unique: `group_link`
    *   FK: `creator_id` → users(id)

### [6] 그룹 멤버 (Members)
*   members (**group_id**, **user_id**, role)
    *   PK: (`group_id`, `user_id`)
    *   FK: `group_id` → groups(id)
    *       `user_id` → users(id)
    *   Note: `role` (LEADER, MEMBER)

### [7] 통합 일정 (Schedules)
*   schedules (**id**, user_id, group_id, title, start_at, end_at, description, type)
    *   PK: `id` (int)
    *   FK: `user_id` → users(id)
    *       `group_id` → groups(id) (NULL이면 개인 일정)

### [8] 첨부파일(file)
*   file (**id**, file_url)
    *   PK: (id, file_url)
    *   FK: 'id' → post(id)