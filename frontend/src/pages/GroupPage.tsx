const groups = [
  {
    name: 'DB 기초 스터디',
    description: '정규화와 SQL 기본기를 함께 공부하는 그룹입니다.',
    status: '참여중',
    members: '4명',
  },
  {
    name: '팀 프로젝트 A',
    description: '학업자료공유 서비스 기획과 구현을 진행하는 그룹입니다.',
    status: '리더',
    members: '5명',
  },
  {
    name: 'SQL 문제 풀이',
    description: '시험 대비 SQL 문제를 함께 풀이하는 그룹입니다.',
    status: '참여 가능',
    members: '2명',
  },
]

export function GroupPage() {
  return (
    <>
      <section className="page-header">
        <p className="eyebrow">그룹</p>
        <h1 className="page-title">스터디 그룹</h1>
        <p className="page-description">
          내 그룹 목록, 그룹 생성, 코드로 참여하는 흐름을 연결할 화면입니다.
        </p>
      </section>

      <section className="toolbar">
        <button className="button" type="button">
          그룹 만들기
        </button>
        <button className="button secondary" type="button">
          코드로 참여
        </button>
      </section>

      <section className="content-grid">
        {groups.map((group) => (
          <article className="panel" key={group.name}>
            <h2>{group.name}</h2>
            <p>{group.description}</p>
            <div className="post-meta">
              <span className="badge">{group.status}</span>
              <span>{group.members}</span>
            </div>
          </article>
        ))}
      </section>
    </>
  )
}
