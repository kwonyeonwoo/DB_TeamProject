import { Link } from 'react-router-dom'
import { UserSummaryCard } from '../components/UserSummaryCard'

const quickLinks = [
  {
    title: '게시글',
    description: '학습 자료를 검색하고, 필요한 글을 확인하거나 새 자료를 공유합니다.',
    to: '/posts',
    action: '목록 보기',
    metric: '자료 공유',
  },
  {
    title: '일정',
    description: '개인 일정과 그룹 일정을 캘린더에서 확인하고 바로 수정합니다.',
    to: '/schedule',
    action: '일정 보기',
    metric: '캘린더',
  },
  {
    title: '그룹',
    description: '스터디 그룹을 만들고 참여 코드로 합류해 팀 일정을 관리합니다.',
    to: '/groups',
    action: '그룹 보기',
    metric: '스터디',
  },
]

const workflowItems = [
  '자료를 올리기 전에 저작권과 공유 가능 여부를 확인합니다.',
  '중요 일정은 캘린더에 등록해 마감일을 놓치지 않습니다.',
  '그룹 일정은 팀원과 함께 볼 수 있도록 그룹 화면에서 관리합니다.',
]

export function MainPage() {
  return (
    <>
      <section className="home-hero">
        <div className="home-hero-copy">
          <p className="eyebrow">학업 자료 공유 플랫폼</p>
          <h1 className="page-title">자료와 일정을 한곳에서 관리합니다</h1>
          <p className="page-description">
            게시글, 댓글, 알림, 일정, 그룹 기능을 연결해 학습 흐름을 빠르게 확인할 수
            있는 작업 공간입니다.
          </p>
          <div className="home-hero-actions">
            <Link className="button" to="/posts">
              자료 둘러보기
            </Link>
            <Link className="button secondary" to="/schedule">
              캘린더 열기
            </Link>
          </div>
        </div>
        <UserSummaryCard className="home-user-card" />
      </section>

      <section className="home-dashboard">
        <div className="home-feature-grid">
          {quickLinks.map((link) => (
            <article className="home-feature-card" key={link.to}>
              <span>{link.metric}</span>
              <h2>{link.title}</h2>
              <p>{link.description}</p>
              <Link className="button secondary" to={link.to}>
                {link.action}
              </Link>
            </article>
          ))}
        </div>

        <aside className="home-guide-panel">
          <h2>오늘 확인할 것</h2>
          <ul>
            {workflowItems.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        </aside>
      </section>
    </>
  )
}
