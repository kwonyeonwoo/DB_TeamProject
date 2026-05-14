import { Link } from 'react-router-dom'

const quickLinks = [
  {
    title: '게시글',
    description: '학업 자료 목록, 상세 조회, 작성 화면으로 이동합니다.',
    to: '/posts',
    action: '목록 보기',
  },
  {
    title: '일정',
    description: '개인 일정과 그룹 일정을 캘린더 화면으로 연결합니다.',
    to: '/schedule',
    action: '일정 보기',
  },
  {
    title: '그룹',
    description: '그룹 목록, 생성, 참여, 그룹 일정 화면의 기준이 됩니다.',
    to: '/groups',
    action: '그룹 보기',
  },
]

export function MainPage() {
  return (
    <>
      <section className="page-header">
        <p className="eyebrow">학업 자료 공유 플랫폼</p>
        <h1 className="page-title">자료와 일정을 한곳에서 관리합니다</h1>
        <p className="page-description">
          문서에 정의된 인증, 게시글, 댓글, 알림, 일정, 그룹, 신고 관리 흐름을
          연결하기 위한 프론트엔드 기본 화면입니다. 실제 데이터 연결 전까지는
          임시 UI로 구조와 화면 이동을 먼저 안정화합니다.
        </p>
      </section>

      <section className="content-grid">
        {quickLinks.map((link) => (
          <article className="panel" key={link.to}>
            <h2>{link.title}</h2>
            <p>{link.description}</p>
            <div className="button-row">
              <Link className="button secondary" to={link.to}>
                {link.action}
              </Link>
            </div>
          </article>
        ))}
      </section>
    </>
  )
}
