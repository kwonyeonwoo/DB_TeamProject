export function PostWritePage() {
  return (
    <>
      <section className="page-header">
        <p className="eyebrow">게시글</p>
        <h1 className="page-title">게시글 작성</h1>
        <p className="page-description">
          작성 API는 multipart/form-data 기준으로 연결합니다. 새 파일을 올리는
          수정 요청은 문서 기준에 따라 기존 파일 목록을 대체합니다.
        </p>
      </section>

      <form className="form-panel">
        <div className="field-stack">
          <label className="field">
            주 분류
            <select defaultValue="자료" name="main_category">
              <option>자료</option>
              <option>질문</option>
              <option>스터디</option>
              <option>공지</option>
            </select>
          </label>
          <label className="field">
            세부 분류
            <input type="text" name="sub_category" placeholder="예: 데이터베이스" />
          </label>
          <label className="field">
            제목
            <input type="text" name="title" placeholder="제목을 입력하세요" />
          </label>
          <label className="field">
            내용
            <textarea name="content" placeholder="공유할 내용을 입력하세요" />
          </label>
          <label className="check-field">
            <input type="checkbox" name="is_anonymous" />
            익명으로 작성
          </label>
        </div>
        <div className="button-row">
          <button className="button" type="button">
            등록
          </button>
          <button className="button secondary" type="button">
            임시 저장
          </button>
        </div>
      </form>
    </>
  )
}
