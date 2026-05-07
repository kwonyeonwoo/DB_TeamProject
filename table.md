// [1] 사용자 (Users)
Table users {
  id int [pk, increment , note:'사용자 구분값']
  login_id varchar [unique, not null, note:'사용자 아이디']
  password varchar [not null, note:'비밀번호']
  name varchar [not null,note:'닉네임']
  email_adress varchar [note:'이메일']
  created_at timestamp [default: `now()`]
  deleted_at datetime [note : '회원탈퇴 일자']
  status boolean [note : '회원탈퇴여부']
  role varchar [note : '관리자 역할 구분']
}

// [2] 학업 자료 (post)
Table post {
  id int [pk, increment, note:'게시글 구분값']
  user_id int [ref: > users.id, note:'업로드한 사람']
  category varchar [note:'자료분류']
  title varchar [not null, note:'게시글 제목']
  content text [note:'게시글 본문']
  file_url varchar [note:'첨부파일']
  created_at timestamp [default: `now()`]
  is_updated boolean [default: false ,note : '수정여부확인']
  view_count int [note : '조회수']
  is_reported bool [note : '신고여부']
}

// [3] 자료 추천 (Likes)
Table likes {
  id int [pk, increment, note:'추천 수']
  user_id int [ref: > users.id]
  post_id int [ref: > post.id]
  created_at timestamp [default: `now()`]
  
  indexes {
    (user_id, post_id) [unique] // 1인 1추천 보장
  }
}

// [4] 댓글 및 오답노트 (Comments)
Table comments {
  id int [pk, increment, note : '댓글 구분값']  
  user_id int [ref: > users.id]
  post_id int [ref: > post.id]
  parent_comment int [ref: > comments.id, note :'대댓글의 부모댓글']
  content text [not null, note : '댓글 내용']
  is_public boolean [default: true, note : '댓글 공개여부']
  created_at timestamp [default: `now()` , note : "댓글 작성시간"]
  is_updated boolean [default: false, note: '댓글의 수정여부 확인']
}

// [5] 스터디 그룹 (Groups)
Table groups {
  id int [pk, increment, note :'그룹 구분값']
  group_link varchar [unique, not null, note : '초대링크 값']
  name varchar [not null, note : '그룹이름']
  creator_id int [ref: > users.id]
  created_at timestamp [default: `now()`, note : '그룹 생성일자']
}

// [6] 그룹 멤버 (group_members)
Table group_members {
  group_id int [ref: > groups.id]
  user_id int [ref: > users.id]
  role varchar // LEADER, MEMBER
  //group_id, user_id 둘다 기본키로 쓰는 것이 아닌 
  //인조키를 만들어서(구분하기 위한 값) 관리
  indexes {
    (group_id, user_id) [pk]
  }
}

// [7] 통합 일정 (Schedules)
Table schedules {
  id int [pk, increment ,note : '일정 구분값']
  user_id int [ref: > users.id]
  group_id int [ref: > groups.id]
  title varchar [not null , note :'일정 제목']
  start_at datetime [not null ,note :'일정 시작일']
  end_at datetime [not null , note : '일정 종료일']
  // 일정이 하루라면 시작일과 종료일이 동일하다.
  description varchar [note : '일정 설명/메모']
  type int [note : '일정의 유형(시험, 스터디 등)']
}
