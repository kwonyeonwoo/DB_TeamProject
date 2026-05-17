import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getErrorMessage } from '../api/errors'
import { postsApi } from '../api/posts'
import type { Post } from '../api/posts'

export function PostWritePage() {
  const navigate = useNavigate()
  const { postId } = useParams()
  const numericPostId = Number(postId)
  const isEditMode = Boolean(postId)
  const [editingPost, setEditingPost] = useState<Post | null>(null)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [mainCategory, setMainCategory] = useState('')
  const [subCategory, setSubCategory] = useState('')
  const [isAnonymous, setIsAnonymous] = useState(false)
  const [files, setFiles] = useState<File[]>([])
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    if (!isEditMode || Number.isNaN(numericPostId)) {
      return () => {
        isMounted = false
      }
    }

    postsApi
      .getPost(numericPostId)
      .then((post) => {
        if (!isMounted) {
          return
        }

        setEditingPost(post)
        setTitle(post.title)
        setContent(post.content)
        setMainCategory(post.main_category)
        setSubCategory(post.sub_category)
        setIsAnonymous(post.is_anonymous)
        setErrorMessage(null)
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        setErrorMessage(getErrorMessage(error))
      })

    return () => {
      isMounted = false
    }
  }, [isEditMode, numericPostId])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setErrorMessage(null)

    try {
      const request = {
        title,
        content,
        main_category: mainCategory,
        sub_category: subCategory,
        is_anonymous: isAnonymous,
        files: files.length ? files : undefined,
      }
      const savedPost =
        isEditMode && !Number.isNaN(numericPostId)
          ? await postsApi.updatePost(numericPostId, request)
          : await postsApi.createPost(request)

      navigate(`/posts/${savedPost.id}`, { replace: true })
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
      setIsSubmitting(false)
    }
  }

  return (
    <>
      {errorMessage && <p className="form-error">{errorMessage}</p>}

      <div className="post-write-layout">
        <aside className="writing-guide" aria-labelledby="writing-guide-title">
          <h2 id="writing-guide-title">작성 가이드</h2>
          <p>자료 공유가 포함된 게시글은 저작권과 이용자 책임을 함께 고려해야 합니다.</p>
          <ul>
            <li>강의자료, 교재, 유료 콘텐츠, 타인의 제작물을 무단으로 업로드하거나 배포하지 마세요.</li>
            <li>직접 작성한 정리본이나 공유 가능한 자료인지 먼저 확인한 뒤 게시해주세요.</li>
            <li>저작권 침해, 초상권 침해, 개인정보 노출 문제가 발생할 경우 게시한 이용자에게 책임이 있을 수 있습니다.</li>
            <li>문제가 있는 자료는 관리자 검토 후 숨김 또는 삭제될 수 있습니다.</li>
          </ul>
        </aside>

        <form className="form-panel" onSubmit={handleSubmit}>
        <div className="field-stack">
          <label className="field">
            대주제 (학과)
            <input
              type="text"
              value={mainCategory}
              name="main_category"
              placeholder="예: 컴퓨터공학과"
              onChange={(event) => setMainCategory(event.target.value)}
              required
            />
          </label>
          <label className="field">
            소주제 (과목)
            <input
              type="text"
              name="sub_category"
              placeholder="예: 데이터베이스"
              value={subCategory}
              onChange={(event) => setSubCategory(event.target.value)}
              required
            />
          </label>
          <label className="field">
            제목
            <input
              type="text"
              name="title"
              placeholder="제목을 입력하세요"
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              required
            />
          </label>
          <label className="field">
            내용
            <textarea
              name="content"
              placeholder="공유할 내용을 입력하세요"
              value={content}
              onChange={(event) => setContent(event.target.value)}
            />
          </label>
          <label className="field">
            첨부 파일
            <input
              type="file"
              multiple
              onChange={(event) => setFiles(Array.from(event.target.files ?? []))}
            />
          </label>
          {isEditMode && editingPost?.files.length ? (
            <div className="attached-summary">
              <strong>기존 파일</strong>
              <ul>
                {editingPost.files.map((file) => (
                  <li key={file.id}>{file.file_url.split('/').at(-1)}</li>
                ))}
              </ul>
            </div>
          ) : null}
          <label className="check-field">
            <input
              type="checkbox"
              name="is_anonymous"
              checked={isAnonymous}
              onChange={(event) => setIsAnonymous(event.target.checked)}
            />
            익명으로 작성
          </label>
        </div>
        <div className="button-row">
          <button className="button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '저장 중' : isEditMode ? '수정' : '등록'}
          </button>
          <Link className="button secondary" to={isEditMode ? `/posts/${postId}` : '/posts'}>
            취소
          </Link>
        </div>
        </form>
      </div>
    </>
  )
}
