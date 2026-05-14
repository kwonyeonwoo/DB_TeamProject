import { Link, useParams } from 'react-router-dom'

export function PostDetailPage() {
  const { postId } = useParams()

  return (
    <>
      <section className="page-header">
        <p className="eyebrow">게시글</p>
        <h1 className="page-title">게시글 상세</h1>
        <p className="page-description">
          현재 게시글 ID: {postId}. 상세 조회 API가 연결되면 본문, 파일, 좋아요,
          댓글 영역을 이 화면에서 표시합니다.
        </p>
      </section>

      <article className="panel wide-panel">
        <div className="post-meta">
          <span className="badge">임시 게시글</span>
          <span>익명_1</span>
          <span>조회 0</span>
        </div>
        <h2>데이터베이스 정규화 요약 자료</h2>
        <p className="detail-body">
          게시글 상세 화면의 임시 본문입니다. 백엔드 API가 준비되면 세션 쿠키를
          포함한 요청으로 게시글 상세, 첨부 파일, 좋아요 상태, 댓글 목록을
          순서대로 연결합니다.
        </p>
        <div className="button-row">
          <button className="button" type="button">
            좋아요
          </button>
          <button className="button secondary" type="button">
            신고
          </button>
          <Link className="button secondary" to="/posts">
            목록으로
          </Link>
        </div>
      </article>

      <section className="panel wide-panel comments-preview">
        <h2>댓글</h2>
        <p className="muted">
          댓글과 대댓글은 다음 단계에서 문서의 익명 표시 규칙과 함께 연결합니다.
        </p>
      </section>
    </>
  )
}
