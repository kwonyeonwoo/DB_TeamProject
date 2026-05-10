// 기준 문서: docs/source/requirements.md, docs/source/logical-schema.md, docs/source/erd.md, docs/source/physical-schema.md
// 최신 요구사항과 물리 스키마 기준으로 생명주기 상태, 익명 여부, 그룹 가입 시각, 알림 nullable 규칙을 반영한다.
// post.is_anonymous는 요구사항/물리 스키마 기준으로 포함했으며, logical-schema.md에는 아직 보완이 필요하다.

Enum user_status {
  ACTIVE
  INACTIVE
  DELETED
}

Enum active_deleted_status {
  ACTIVE
  DELETED
}

Enum group_status {
  ACTIVE
  INACTIVE
  DELETED
}

Enum group_member_role {
  LEADER
  MEMBER
}

// [1] 사용자 (Users)
Table users {
  id int [pk, increment, not null, note: '회원 식별자']
  login_id varchar [unique, note: '로그인 아이디. 가입 시 필수, 탈퇴 후 NULL 가능']
  password varchar [note: '비밀번호. 가입 시 필수, 탈퇴 후 NULL 또는 식별 불가 값 가능']
  name varchar [note: '이름. 가입 시 필수, 탈퇴 후 NULL 또는 식별 불가 값 가능']
  email_address varchar [unique, note: '이메일. 가입 시 필수, 탈퇴 후 NULL 가능']
  created_at timestamp [default: `now()`, not null, note: '가입 일시']
  deleted_at timestamp [note: '회원 탈퇴 일시']
  status user_status [default: 'ACTIVE', not null, note: '회원 상태']
  role varchar [note: '관리자 역할 구분']

  indexes {
    status [name: 'idx_users_status']
    deleted_at [name: 'idx_users_deleted_at']
  }
}

// [2] 게시물 (post)
Table post {
  id int [pk, increment, not null, note: '게시글 식별자']
  user_id int [not null, ref: > users.id, note: '게시글 작성자']
  title varchar [not null, note: '게시글 제목']
  content text [note: '게시글 본문']
  created_at timestamp [default: `now()`, not null, note: '작성 일시']
  updated_at timestamp [note: '수정 일시. NULL이 아니면 수정된 게시글로 판단']
  deleted_at timestamp [note: '삭제 일시']
  status active_deleted_status [default: 'ACTIVE', not null, note: '게시글 상태']
  view_count int [default: 0, not null, note: '조회수']
  is_reported boolean [default: false, not null, note: '신고 여부']
  main_category varchar [not null, note: '대주제, 학과']
  sub_category varchar [not null, note: '소주제, 과목']
  is_anonymous boolean [default: false, not null, note: '익명 작성 여부']

  indexes {
    user_id [name: 'idx_post_user_id']
    (status, created_at) [name: 'idx_post_status_created_at']
    (main_category, sub_category) [name: 'idx_post_category']
    created_at [name: 'idx_post_created_at']
    (user_id, is_anonymous, status) [name: 'idx_post_author_filter']
  }
}

// [3] 추천 (likes)
Table likes {
  id int [pk, increment, not null, note: '추천 식별자']
  user_id int [not null, ref: > users.id, note: '추천한 회원']
  post_id int [not null, ref: > post.id, note: '추천받은 게시글']
  created_at timestamp [default: `now()`, not null, note: '추천 일시']

  indexes {
    (user_id, post_id) [unique]
    post_id [name: 'idx_likes_post_id']
  }
}

// [4] 댓글 및 대댓글 (Comments)
Table comments {
  id int [pk, increment, not null, note: '댓글 식별자']
  user_id int [not null, ref: > users.id, note: '댓글 작성자']
  post_id int [not null, ref: > post.id, note: '댓글이 작성된 게시글']
  parent_comment int [ref: > comments.id, note: '대댓글의 부모 댓글. NULL이면 일반 댓글']
  content text [not null, note: '댓글 또는 대댓글 내용']
  is_anonymous boolean [default: false, not null, note: '익명 작성 여부']
  created_at timestamp [default: `now()`, not null, note: '작성 일시']
  updated_at timestamp [note: '수정 일시. NULL이 아니면 수정된 댓글로 판단']
  deleted_at timestamp [note: '삭제 일시']
  status active_deleted_status [default: 'ACTIVE', not null, note: '댓글 상태']

  indexes {
    user_id [name: 'idx_comments_user_id']
    post_id [name: 'idx_comments_post_id']
    parent_comment [name: 'idx_comments_parent_comment']
    (status, created_at) [name: 'idx_comments_status_created_at']
    (post_id, is_anonymous) [name: 'idx_comments_anonymous']
  }
}

// [5] 스터디 그룹 (Groups)
Table groups {
  id int [pk, increment, not null, note: '그룹 식별자']
  group_link varchar [unique, not null, note: '그룹 링크']
  name varchar [not null, note: '그룹명']
  creator_id int [not null, ref: > users.id, note: '최초 그룹 생성자. 현재 그룹장은 group_members.role = LEADER로 판단']
  created_at timestamp [default: `now()`, not null, note: '그룹 생성 일시']
  deleted_at timestamp [note: '그룹 삭제 또는 비활성화 일시']
  status group_status [default: 'ACTIVE', not null, note: '그룹 상태']

  indexes {
    creator_id [name: 'idx_groups_creator_id']
    status [name: 'idx_groups_status']
  }
}

// [6] 그룹 멤버 (group_members)
Table group_members {
  group_id int [not null, ref: > groups.id, note: '그룹 식별자']
  user_id int [not null, ref: > users.id, note: '회원 식별자']
  role group_member_role [not null, note: '그룹 내 역할']
  joined_at timestamp [default: `now()`, not null, note: '그룹 가입 일시. 그룹장 자동 위임 기준']

  indexes {
    (group_id, user_id) [pk]
    user_id [name: 'idx_group_members_user_id']
    (group_id, role, joined_at) [name: 'idx_group_members_leader_transfer']
  }
}

// [7] 통합 일정 (Schedules)
Table schedules {
  id int [pk, increment, not null, note: '일정 식별자']
  user_id int [not null, ref: > users.id, note: '일정 작성자 또는 소유자']
  group_id int [ref: > groups.id, note: '그룹 일정의 그룹. NULL이면 개인 일정']
  title varchar [not null, note: '일정 제목']
  start_at timestamp [not null, note: '일정 시작 일시']
  end_at timestamp [not null, note: '일정 종료 일시']
  description varchar [note: '일정 설명 또는 메모']
  type int [not null, note: '일정 종류']
  created_at timestamp [default: `now()`, not null, note: '생성 일시']
  updated_at timestamp [note: '수정 일시']
  deleted_at timestamp [note: '삭제 일시']
  status active_deleted_status [default: 'ACTIVE', not null, note: '일정 상태']

  indexes {
    (user_id, start_at, end_at) [name: 'idx_schedules_user_period']
    (group_id, start_at, end_at) [name: 'idx_schedules_group_period']
    type [name: 'idx_schedules_type']
    status [name: 'idx_schedules_status']
  }
}

// [8] 첨부파일(file)
Table file {
  id int [not null, ref: > post.id, note: '첨부파일이 속한 게시글']
  file_url varchar [not null, note: '업로드된 첨부파일의 저장 위치']

  indexes {
    (id, file_url) [pk]
    id [name: 'idx_file_post_id']
  }
}

// [9] 알림(notification)
Table notification {
  id int [pk, increment, not null, note: '알림 식별자']
  is_read boolean [default: false, not null, note: '수신자의 알림 확인 여부']
  comment_content text [not null, note: '알림에 표시할 댓글 내용']
  commented_post_id int [not null, ref: > post.id, note: '댓글이 달린 게시글 id']
  commented_user_id int [not null, ref: > users.id, note: '수신자 유저 id']
  commented_id int [ref: > comments.id, note: '댓글 알림이면 NULL, 대댓글 알림이면 부모 댓글 id']
  created_at timestamp [default: `now()`, not null, note: '알림 생성 일시']

  indexes {
    (commented_user_id, is_read, created_at) [name: 'idx_notification_receiver']
    commented_post_id [name: 'idx_notification_post_id']
    commented_id [name: 'idx_notification_comment_id']
  }
}
