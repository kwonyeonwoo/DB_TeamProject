const reports = [
  {
    id: 1,
    target: '게시글',
    reason: '부적절한 내용',
    status: '대기',
  },
  {
    id: 2,
    target: '댓글',
    reason: '욕설 또는 비방',
    status: '대기',
  },
]

export function AdminReportPage() {
  return (
    <>
      <section className="page-header">
        <p className="eyebrow">관리자</p>
        <h1 className="page-title">신고 관리</h1>
        <p className="page-description">
          ADMIN 권한에서 신고 목록을 확인하고 처리 상태를 변경하는 화면입니다.
          일반 사용자의 신고 생성 흐름과 분리합니다.
        </p>
      </section>

      <section className="post-list">
        {reports.map((report) => (
          <article className="post-item" key={report.id}>
            <h2>신고 접수 #{report.id}</h2>
            <p>{report.reason}</p>
            <div className="post-meta">
              <span className="badge">{report.status}</span>
              <span>{report.target}</span>
            </div>
          </article>
        ))}
      </section>
    </>
  )
}
