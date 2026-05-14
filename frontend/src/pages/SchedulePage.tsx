const schedules = [
  {
    title: '데이터베이스 과제 제출',
    time: '오늘 18:00',
    type: '개인',
  },
  {
    title: '팀 프로젝트 주간 회의',
    time: '금요일 14:00',
    type: '그룹',
  },
  {
    title: 'SQL 실습 퀴즈',
    time: '다음 주 월요일',
    type: '개인',
  },
]

export function SchedulePage() {
  return (
    <>
      <section className="page-header">
        <p className="eyebrow">일정</p>
        <h1 className="page-title">학업 일정</h1>
        <p className="page-description">
          개인 일정과 그룹 일정을 분리해 연결할 화면입니다. 상세 입력은 모달
          흐름으로 확장합니다.
        </p>
      </section>

      <section className="content-grid">
        {schedules.map((schedule) => (
          <article className="panel" key={schedule.title}>
            <span className="badge">{schedule.type}</span>
            <h2>{schedule.title}</h2>
            <p>{schedule.time}</p>
          </article>
        ))}
      </section>
    </>
  )
}
