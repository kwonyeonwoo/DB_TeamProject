import { Link } from 'react-router-dom'

const posts = [
  {
    id: 1,
    title: '데이터베이스 정규화 요약 자료',
    description: '1NF부터 BCNF까지 핵심 개념을 정리한 임시 게시글입니다.',
    category: '자료',
  },
  {
    id: 2,
    title: '팀 프로젝트 회의록 템플릿',
    description: '회의록 작성 양식과 역할 분담 예시를 담을 예정입니다.',
    category: '스터디',
  },
  {
    id: 3,
    title: '중간고사 대비 SQL 문제 모음',
    description: '조회, 조인, 그룹화 문제를 배치할 목록 화면 샘플입니다.',
    category: '질문',
  },
]

export function PostListPage() {
  return (
    <>
      <section className="page-header">
        <p className="eyebrow">게시글</p>
        <h1 className="page-title">게시글 목록</h1>
        <p className="page-description">
          실제 API 연결 전까지 임시 데이터로 목록, 검색, 상세 이동 구조를 확인합니다.
        </p>
        <div className="button-row">
          <Link className="button" to="/posts/write">
            새 글 작성
          </Link>
        </div>
      </section>

      <section className="toolbar">
        <label className="field compact">
          검색어
          <input type="search" placeholder="제목 또는 내용 검색" />
        </label>
        <label className="field compact">
          분류
          <select defaultValue="">
            <option value="">전체</option>
            <option value="자료">자료</option>
            <option value="질문">질문</option>
            <option value="스터디">스터디</option>
          </select>
        </label>
      </section>

      <section className="post-list">
        {posts.map((post) => (
          <Link className="post-item" key={post.id} to={`/posts/${post.id}`}>
            <h2>{post.title}</h2>
            <p>{post.description}</p>
            <div className="post-meta">
              <span className="badge">{post.category}</span>
              <span>좋아요 0</span>
              <span>조회 0</span>
            </div>
          </Link>
        ))}
      </section>
    </>
  )
}
