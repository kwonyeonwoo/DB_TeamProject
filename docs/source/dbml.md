// 기준 문서: docs/source/logical-schema.md, docs/source/erd.md, docs/source/physical-schema.md

Enum user_status {
  ACTIVE
  DELETED
}

Enum group_member_role {
  LEADER
  MEMBER
}

// [1] 사용자 (Users)
Table users {
  id int [pk, increment, not null, note: '사용자 구분값']
  login_id varchar [unique, not null, note: '사용자 아이디']
  password varchar [not null, note: '비밀번호']
  name varchar [not null, note: '닉네임']
  email_address varchar [not null, note: '이메일']
  created_at timestamp [default: `now()`, not null, note: '가입 일시']
  deleted_at timestamp [note: '회원 탈퇴 일시']
  status user_status [default: 'ACTIVE', not null, note: '회원 상태']
  role varchar [note: '관리자 역할 구분']
}

// [2] 게시물 (post)
Table post {
  id int [pk, increment, not null, note: '게시글 구분값']
  user_id int [not null, ref: > users.id, note: '게시글 작성자']
  title varchar [not null, note: '게시글 제목']
  content text [note: '게시글 본문']
  created_at timestamp [default: `now()`, not null, note: '작성 일시']
  is_updated boolean [default: false, not null, note: '수정 여부']
  view_count int [default: 0, not null, note: '조회수']
  is_reported boolean [default: false, not null, note: '신고 여부']
  main_category varchar [not null, note: '대주제']
  sub_category varchar [not null, note: '소주제']
}

// [3] 자료 추천 (Likes)
Table likes {
  id int [pk, increment, not null, note: '추천 구분값']
  user_id int [not null, ref: > users.id, note: '추천한 회원']
  post_id int [not null, ref: > post.id, note: '추천받은 게시글']
  created_at timestamp [default: `now()`, not null, note: '추천 일시']

  indexes {
    (user_id, post_id) [unique]
  }
}

// [4] 댓글 및 대댓글 (Comments)
Table comments {
  id int [pk, increment, not null, note: '댓글 구분값']
  user_id int [not null, ref: > users.id, note: '댓글 작성자']
  post_id int [not null, ref: > post.id, note: '댓글이 작성된 게시글']
  parent_comment int [ref: > comments.id, note: '대댓글의 부모 댓글']
  content text [not null, note: '댓글 내용']
  is_public boolean [default: true, not null, note: '댓글 공개 여부']
  created_at timestamp [default: `now()`, not null, note: '댓글 작성 일시']
  is_updated boolean [default: false, not null, note: '댓글 수정 여부']
}

// [5] 스터디 그룹 (Groups)
Table groups {
  id int [pk, increment, not null, note: '그룹 구분값']
  group_link varchar [unique, not null, note: '초대 링크 값']
  name varchar [not null, note: '그룹 이름']
  creator_id int [not null, ref: > users.id, note: '그룹 생성자']
  created_at timestamp [default: `now()`, not null, note: '그룹 생성 일시']
}

// [6] 그룹 멤버 (group_members)
Table group_members {
  group_id int [not null, ref: > groups.id, note: '그룹 식별자']
  user_id int [not null, ref: > users.id, note: '회원 식별자']
  role group_member_role [not null, note: '그룹 내 역할']

  indexes {
    (group_id, user_id) [pk]
  }
}

// [7] 통합 일정 (Schedules)
Table schedules {
  id int [pk, increment, not null, note: '일정 구분값']
  user_id int [not null, ref: > users.id, note: '일정 작성자 또는 소유자']
  group_id int [ref: > groups.id, note: '그룹 일정의 그룹. NULL이면 개인 일정']
  title varchar [not null, note: '일정 제목']
  start_at timestamp [not null, note: '일정 시작 일시']
  end_at timestamp [not null, note: '일정 종료 일시']
  description varchar [note: '일정 설명 또는 메모']
  type int [not null, note: '일정 유형']
}

// [8] 첨부파일(file)
Table file {
  id int [not null, ref: > post.id, note: '첨부파일이 속한 게시글']
  file_url varchar [not null, note: '첨부파일 URL']

  indexes {
    (id, file_url) [pk]
  }
}

// [9] 알림(notification)
Table notification {
  id int [pk, increment, not null, note: '알림 구분값']
  is_read boolean [default: false, not null, note: '수신자의 알림 확인 여부']
  comment_content text [not null, note: '알림에 표시할 댓글 내용']
  commented_post_id int [not null, ref: > post.id, note: '댓글이 달린 게시글 id']
  commented_user_id int [not null, ref: > users.id, note: '수신자 유저 id']
  commented_id int [not null, ref: > comments.id, note: '알림 대상 댓글 또는 대댓글 id']
  created_at timestamp [default: `now()`, not null, note: '알림 생성 일시']
}
