import { useEffect, useMemo, useRef, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getErrorMessage } from '../api/errors'
import { postsApi } from '../api/posts'
import type { Post, PostFile } from '../api/posts'

const categoryOptions = [
  {
    department: '컴퓨터공학과',
    subjects: ['자료구조', '운영체제', '데이터베이스', '컴퓨터네트워크', '알고리즘'],
  },
  {
    department: '소프트웨어학과',
    subjects: ['웹프로그래밍', '소프트웨어공학', '객체지향프로그래밍', '모바일프로그래밍'],
  },
  {
    department: '정보통신공학과',
    subjects: ['정보통신개론', '디지털통신', '네트워크보안', '신호및시스템'],
  },
  {
    department: '인공지능학과',
    subjects: ['인공지능개론', '머신러닝', '딥러닝', '자연어처리'],
  },
]

function getSubjectOptions(department: string) {
  return (
    categoryOptions.find((option) => option.department === department)?.subjects ?? []
  )
}

const allSubjectOptions = Array.from(
  new Set(categoryOptions.flatMap((option) => option.subjects)),
)

function isImageFile(file: File) {
  return file.type.startsWith('image/')
}

function isImageFileUrl(fileUrl: string) {
  return /\.(apng|avif|gif|jpe?g|png|svg|webp)$/i.test(fileUrl.split('?')[0] ?? '')
}

function getPostFileUrl(file: PostFile) {
  if (file.preview_url) {
    return file.preview_url
  }

  if (/^(https?:|data:|blob:)/i.test(file.file_url)) {
    return file.file_url
  }

  if (file.file_url.startsWith('/api/uploads/')) {
    return file.file_url
  }

  if (file.file_url.startsWith('/uploads/')) {
    return `/api${file.file_url}`
  }

  return file.file_url
}

function getPostFileName(file: PostFile) {
  return file.file_name ?? file.file_url.split('/').at(-1) ?? '첨부 이미지'
}

export function PostWritePage() {
  const navigate = useNavigate()
  const { postId } = useParams()
  const numericPostId = Number(postId)
  const isEditMode = Boolean(postId)
  const contentTextareaRef = useRef<HTMLTextAreaElement | null>(null)
  const [editingPost, setEditingPost] = useState<Post | null>(null)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [mainCategory, setMainCategory] = useState(categoryOptions[0].department)
  const [subCategory, setSubCategory] = useState(categoryOptions[0].subjects[0])
  const [isAnonymous, setIsAnonymous] = useState(false)
  const [files, setFiles] = useState<File[]>([])
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const mainCategoryOptions = useMemo(
    () => categoryOptions.map((option) => option.department),
    [],
  )
  const subCategoryOptions = getSubjectOptions(mainCategory)
  const fallbackSubCategoryOptions = subCategoryOptions.length
    ? subCategoryOptions
    : allSubjectOptions
  const selectedImageFiles = useMemo(() => files.filter(isImageFile), [files])
  const selectedImagePreviews = useMemo(
    () =>
      selectedImageFiles.map((file) => ({
        file,
        url: URL.createObjectURL(file),
      })),
    [selectedImageFiles],
  )
  const existingImageFiles = useMemo(
    () =>
      editingPost?.files
        .map((file, index) => ({ file, index }))
        .filter(
          ({ file }) =>
            file.content_type?.startsWith('image/') ||
            isImageFileUrl(file.file_name ?? file.file_url),
        ) ?? [],
    [editingPost?.files],
  )
  const contentPreviewNodes = useMemo(() => {
    const imageTokenPattern = /\[[^\]:]+:(.+?)\]/g
    const nodes: ReactNode[] = []
    let lastIndex = 0
    let match: RegExpExecArray | null

    const resolvePreviewImage = (marker: string) => {
      const normalizedMarker = marker.trim()
      const imageIndex = Number(normalizedMarker)

      if (Number.isInteger(imageIndex) && imageIndex > 0) {
        const selectedPreview = selectedImagePreviews.find(
          ({ file }) => files.indexOf(file) + 1 === imageIndex,
        )

        if (selectedPreview) {
          return {
            name: selectedPreview.file.name,
            url: selectedPreview.url,
          }
        }

        const existingImage = existingImageFiles.find(
          ({ index }) => index + 1 === imageIndex,
        )

        if (existingImage) {
          return {
            name: getPostFileName(existingImage.file),
            url: getPostFileUrl(existingImage.file),
          }
        }
      }

      const selectedPreview = selectedImagePreviews.find(
        ({ file }) => file.name === normalizedMarker,
      )

      if (selectedPreview) {
        return {
          name: selectedPreview.file.name,
          url: selectedPreview.url,
        }
      }

      const existingImage = existingImageFiles.find(
        ({ file }) =>
          getPostFileName(file) === normalizedMarker ||
          file.file_url.endsWith(normalizedMarker),
      )

      return existingImage
        ? {
            name: getPostFileName(existingImage.file),
            url: getPostFileUrl(existingImage.file),
          }
        : null
    }

    while ((match = imageTokenPattern.exec(content)) !== null) {
      const text = content.slice(lastIndex, match.index)

      if (text.trim()) {
        nodes.push(<p key={`text-${match.index}`}>{text}</p>)
      }

      const image = resolvePreviewImage(match[1])

      if (image) {
        nodes.push(
          <figure className="post-inline-image" key={`image-${match.index}`}>
            <img src={image.url} alt={image.name} />
            <figcaption>{image.name}</figcaption>
          </figure>,
        )
      } else {
        nodes.push(
          <p className="muted" key={`missing-image-${match.index}`}>
            선택한 이미지를 찾을 수 없습니다.
          </p>,
        )
      }

      lastIndex = match.index + match[0].length
    }

    const remainingText = content.slice(lastIndex)

    if (remainingText.trim()) {
      nodes.push(<p key="text-last">{remainingText}</p>)
    }

    return nodes
  }, [content, existingImageFiles, files, selectedImagePreviews])

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

  useEffect(
    () => () => {
      selectedImagePreviews.forEach((preview) => URL.revokeObjectURL(preview.url))
    },
    [selectedImagePreviews],
  )

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

  const insertImageMarker = (marker: string) => {
    const token = `[이미지:${marker}]`
    const textarea = contentTextareaRef.current

    if (!textarea) {
      setContent((previousContent) =>
        previousContent ? `${previousContent}\n${token}` : token,
      )
      return
    }

    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const before = content.slice(0, start)
    const after = content.slice(end)
    const needsLeadingLine = before.length > 0 && !before.endsWith('\n')
    const needsTrailingLine = after.length > 0 && !after.startsWith('\n')
    const nextContent = `${before}${needsLeadingLine ? '\n' : ''}${token}${
      needsTrailingLine ? '\n' : ''
    }${after}`
    const nextCursorPosition =
      before.length + (needsLeadingLine ? 1 : 0) + token.length

    setContent(nextContent)

    window.requestAnimationFrame(() => {
      textarea.focus()
      textarea.setSelectionRange(nextCursorPosition, nextCursorPosition)
    })
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
              list="main-category-options"
              value={mainCategory}
              name="main_category"
              placeholder="학과를 입력하세요"
              onChange={(event) => setMainCategory(event.target.value)}
              required
            />
            <datalist id="main-category-options">
              {mainCategoryOptions.map((option) => (
                <option key={option} value={option} />
              ))}
            </datalist>
          </label>
          <label className="field">
            소주제 (과목)
            <input
              type="text"
              list="sub-category-options"
              name="sub_category"
              value={subCategory}
              placeholder="과목을 입력하세요"
              onChange={(event) => setSubCategory(event.target.value)}
              required
            />
            <datalist id="sub-category-options">
              {fallbackSubCategoryOptions.map((option) => (
                <option key={option} value={option} />
              ))}
            </datalist>
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
              ref={contentTextareaRef}
              name="content"
              placeholder="공유할 내용을 입력하세요"
              value={content}
              onChange={(event) => setContent(event.target.value)}
            />
          </label>
          {contentPreviewNodes.length > 0 && (
            <details className="post-content-preview">
              <summary>본문 미리보기</summary>
              <div className="detail-body">{contentPreviewNodes}</div>
            </details>
          )}
          {(selectedImageFiles.length > 0 || existingImageFiles.length > 0) && (
            <div className="image-placement-panel">
              <strong>본문 이미지 위치</strong>
              <p>커서를 원하는 위치에 둔 뒤 이미지를 선택하면 게시글 본문에 표시됩니다.</p>
              <div className="image-placement-list">
                {selectedImagePreviews.map(({ file, url }) => {
                  const fileIndex = files.indexOf(file) + 1
                  return (
                    <button
                      className="image-placement-preview"
                      type="button"
                      key={`${file.name}-${file.lastModified}`}
                      onClick={() => insertImageMarker(String(fileIndex))}
                    >
                      <img src={url} alt={file.name} />
                      <span>{file.name}</span>
                    </button>
                  )
                })}
                {existingImageFiles.map(({ file, index }) => (
                  <button
                    className="text-button"
                    type="button"
                    key={`${file.file_url}-${index}`}
                    onClick={() => insertImageMarker(String(index + 1))}
                  >
                    {file.file_name ?? file.file_url.split('/').at(-1)}
                  </button>
                ))}
              </div>
            </div>
          )}
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
                  <li key={file.file_url}>{file.file_name ?? file.file_url.split('/').at(-1)}</li>
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
