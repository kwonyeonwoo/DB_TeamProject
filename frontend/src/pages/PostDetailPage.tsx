import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { commentsApi } from '../api/comments'
import { getErrorMessage } from '../api/errors'
import { postsApi } from '../api/posts'
import { reportReasonLabels, reportsApi } from '../api/reports'
import { useAuth } from '../contexts/useAuth'
import type { Comment, SaveCommentRequest } from '../api/comments'
import type { Post } from '../api/posts'
import type { ReportTargetType } from '../api/reports'

interface ReportTarget {
  target_type: ReportTargetType
  target_id: number
  label: string
}

export function PostDetailPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { postId } = useParams()
  const numericPostId = Number(postId)
  const isInvalidPostId = Number.isNaN(numericPostId)
  const [post, setPost] = useState<Post | null>(null)
  const [comments, setComments] = useState<Comment[]>([])
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [commentErrorMessage, setCommentErrorMessage] = useState<string | null>(null)
  const [isMutating, setIsMutating] = useState(false)
  const [commentContent, setCommentContent] = useState('')
  const [commentAnonymous, setCommentAnonymous] = useState(false)
  const [replyingTo, setReplyingTo] = useState<number | null>(null)
  const [replyContent, setReplyContent] = useState('')
  const [replyAnonymous, setReplyAnonymous] = useState(false)
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null)
  const [editContent, setEditContent] = useState('')
  const [editAnonymous, setEditAnonymous] = useState(false)
  const [reportTarget, setReportTarget] = useState<ReportTarget | null>(null)
  const [reportReason, setReportReason] = useState(1)
  const canReport = user?.role === 'USER'
  const currentUserId = user?.id

  const loadComments = useCallback(async () => {
    if (isInvalidPostId) {
      return
    }

    const response = await commentsApi.listComments(numericPostId)
    setComments(response.items)
  }, [isInvalidPostId, numericPostId])

  useEffect(() => {
    let isMounted = true

    if (isInvalidPostId) {
      return () => {
        isMounted = false
      }
    }

    postsApi
      .getPost(numericPostId)
      .then((nextPost) => {
        if (!isMounted) {
          return
        }

        setPost(nextPost)
        setErrorMessage(null)
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        setErrorMessage(getErrorMessage(error))
      })

    commentsApi
      .listComments(numericPostId)
      .then((response) => {
        if (!isMounted) {
          return
        }

        setComments(response.items)
        setCommentErrorMessage(null)
      })
      .catch((error: unknown) => {
        if (!isMounted) {
          return
        }

        setCommentErrorMessage(getErrorMessage(error))
      })

    return () => {
      isMounted = false
    }
  }, [isInvalidPostId, numericPostId])

  const handleLike = async () => {
    if (!post) {
      return
    }

    setIsMutating(true)

    try {
      if (post.liked_by_me) {
        await postsApi.unlikePost(post.id)
      } else {
        await postsApi.likePost(post.id)
      }

      const nextPost = await postsApi.getPost(post.id)
      setPost(nextPost)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    } finally {
      setIsMutating(false)
    }
  }

  const handleDelete = async () => {
    if (!post) {
      return
    }

    const confirmed = window.confirm('게시글을 삭제할까요?')

    if (!confirmed) {
      return
    }

    setIsMutating(true)

    try {
      await postsApi.deletePost(post.id)
      navigate('/posts', { replace: true })
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
      setIsMutating(false)
    }
  }

  const handleCreateComment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    try {
      await commentsApi.createComment(numericPostId, {
        content: commentContent,
        is_anonymous: commentAnonymous,
      })
      setCommentContent('')
      setCommentAnonymous(false)
      setCommentErrorMessage(null)
      await loadComments()
    } catch (error) {
      setCommentErrorMessage(getErrorMessage(error))
    }
  }

  const handleCreateReply = async (
    event: FormEvent<HTMLFormElement>,
    commentId: number,
  ) => {
    event.preventDefault()

    try {
      await commentsApi.createReply(commentId, {
        content: replyContent,
        is_anonymous: replyAnonymous,
      })
      setReplyingTo(null)
      setReplyContent('')
      setReplyAnonymous(false)
      setCommentErrorMessage(null)
      await loadComments()
    } catch (error) {
      setCommentErrorMessage(getErrorMessage(error))
    }
  }

  const handleUpdateComment = async (
    event: FormEvent<HTMLFormElement>,
    commentId: number,
  ) => {
    event.preventDefault()
    const request: SaveCommentRequest = {
      content: editContent,
      is_anonymous: editAnonymous,
    }

    try {
      await commentsApi.updateComment(commentId, request)
      setEditingCommentId(null)
      setEditContent('')
      setEditAnonymous(false)
      setCommentErrorMessage(null)
      await loadComments()
    } catch (error) {
      setCommentErrorMessage(getErrorMessage(error))
    }
  }

  const handleDeleteComment = async (commentId: number) => {
    const confirmed = window.confirm('댓글을 삭제할까요?')

    if (!confirmed) {
      return
    }

    try {
      await commentsApi.deleteComment(commentId)
      setCommentErrorMessage(null)
      await loadComments()
    } catch (error) {
      setCommentErrorMessage(getErrorMessage(error))
    }
  }

  const handleCreateReport = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!reportTarget) {
      return
    }

    try {
      await reportsApi.createReport({
        target_type: reportTarget.target_type,
        target_id: reportTarget.target_id,
        reason_type: reportReason,
      })
      setReportTarget(null)
      setReportReason(1)
      setSuccessMessage('신고가 접수되었습니다.')
      setErrorMessage(null)
      setCommentErrorMessage(null)
    } catch (error) {
      const message = getErrorMessage(error)
      if (reportTarget.target_type === 'POST') {
        setErrorMessage(message)
      } else {
        setCommentErrorMessage(message)
      }
    }
  }

  const startEdit = (comment: Comment) => {
    setEditingCommentId(comment.id)
    setEditContent(comment.content)
    setEditAnonymous(comment.is_anonymous)
    setReplyingTo(null)
  }

  const openReportModal = (target: ReportTarget) => {
    setReportTarget(target)
    setReportReason(1)
    setSuccessMessage(null)
  }

  const parentComments = comments.filter((comment) => comment.parent_comment === null)

  return (
    <>
      {successMessage && <p className="form-success">{successMessage}</p>}
      {(isInvalidPostId || errorMessage) && (
        <p className="form-error">
          {isInvalidPostId ? '올바르지 않은 게시글 ID입니다.' : errorMessage}
        </p>
      )}
      {!post && !errorMessage && (
        <section className="panel wide-panel">
          <p className="muted">게시글을 불러오고 있습니다.</p>
        </section>
      )}

      {post && (
        <>
          <article className="panel wide-panel">
            <div className="post-meta">
              <span className="badge">{post.main_category}</span>
              <span>{post.sub_category}</span>
              <span>{post.author_display_name}</span>
              <span>조회 {post.view_count}</span>
              <span>좋아요 {post.like_count}</span>
            </div>
            <h2>{post.title}</h2>
            <p className="detail-body">{post.content}</p>

            {post.files.length > 0 && (
              <div className="attachment-list">
                <h3>첨부 파일</h3>
                <ul>
                  {post.files.map((file) => (
                    <li key={file.id}>
                      <a href={file.file_url}>{file.file_url.split('/').at(-1)}</a>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <div className="button-row">
              <button
                className={post.liked_by_me ? 'button secondary' : 'button'}
                type="button"
                onClick={handleLike}
                disabled={isMutating}
              >
                {post.liked_by_me ? '좋아요 취소' : '좋아요'}
              </button>
              {canReport && (
                <button
                  className="button secondary"
                  type="button"
                  onClick={() =>
                    openReportModal({
                      target_type: 'POST',
                      target_id: post.id,
                      label: post.title,
                    })
                  }
                >
                  신고
                </button>
              )}
              {currentUserId === post.user_id && (
                <>
                  <Link className="button secondary" to={`/posts/${post.id}/edit`}>
                    수정
                  </Link>
                  <button
                    className="button danger"
                    type="button"
                    onClick={handleDelete}
                    disabled={isMutating}
                  >
                    삭제
                  </button>
                </>
              )}
              <Link className="button secondary" to="/posts">
                목록으로
              </Link>
            </div>
          </article>

          <section className="panel wide-panel comments-preview">
            <h2>댓글</h2>
            <form className="comment-form" onSubmit={handleCreateComment}>
              <label className="field">
                댓글 작성
                <textarea
                  value={commentContent}
                  onChange={(event) => setCommentContent(event.target.value)}
                  placeholder="댓글을 입력하세요"
                  required
                />
              </label>
              <div className="comment-actions">
                <label className="check-field">
                  <input
                    type="checkbox"
                    checked={commentAnonymous}
                    onChange={(event) => setCommentAnonymous(event.target.checked)}
                  />
                  익명
                </label>
                <button className="button" type="submit">
                  등록
                </button>
              </div>
            </form>

            {commentErrorMessage && (
              <p className="form-error">{commentErrorMessage}</p>
            )}
            {parentComments.length === 0 && (
              <p className="empty-state">아직 댓글이 없습니다.</p>
            )}

            <div className="comment-list">
              {parentComments.map((comment) => {
                const replies = comments.filter(
                  (reply) => reply.parent_comment === comment.id,
                )
                const canEditComment = currentUserId === comment.user_id

                return (
                  <article className="comment-item" id={`comment-${comment.id}`} key={comment.id}>
                    {editingCommentId === comment.id ? (
                      <form onSubmit={(event) => handleUpdateComment(event, comment.id)}>
                        <label className="field">
                          댓글 수정
                          <textarea
                            value={editContent}
                            onChange={(event) => setEditContent(event.target.value)}
                            required
                          />
                        </label>
                        <div className="comment-actions">
                          <label className="check-field">
                            <input
                              type="checkbox"
                              checked={editAnonymous}
                              onChange={(event) => setEditAnonymous(event.target.checked)}
                            />
                            익명
                          </label>
                          <button className="button" type="submit">
                            저장
                          </button>
                          <button
                            className="button secondary"
                            type="button"
                            onClick={() => setEditingCommentId(null)}
                          >
                            취소
                          </button>
                        </div>
                      </form>
                    ) : (
                      <>
                        <div className="comment-header">
                          <strong>{comment.author_display_name}</strong>
                          <span>{new Date(comment.created_at).toLocaleString()}</span>
                        </div>
                        <p>{comment.content}</p>
                        <div className="comment-actions">
                          <button
                            className="text-button"
                            type="button"
                            onClick={() => {
                              setReplyingTo(comment.id)
                              setEditingCommentId(null)
                            }}
                          >
                            답글
                          </button>
                          {canEditComment && (
                            <>
                              <button
                                className="text-button"
                                type="button"
                                onClick={() => startEdit(comment)}
                              >
                                수정
                              </button>
                              <button
                                className="text-button"
                                type="button"
                                onClick={() => handleDeleteComment(comment.id)}
                              >
                                삭제
                              </button>
                            </>
                          )}
                          {canReport && (
                            <button
                              className="text-button"
                              type="button"
                              onClick={() =>
                                openReportModal({
                                  target_type: 'COMMENT',
                                  target_id: comment.id,
                                  label: comment.content,
                                })
                              }
                            >
                              신고
                            </button>
                          )}
                        </div>
                      </>
                    )}

                    {replyingTo === comment.id && (
                      <form
                        className="reply-form"
                        onSubmit={(event) => handleCreateReply(event, comment.id)}
                      >
                        <label className="field">
                          답글 작성
                          <textarea
                            value={replyContent}
                            onChange={(event) => setReplyContent(event.target.value)}
                            placeholder="답글을 입력하세요"
                            required
                          />
                        </label>
                        <div className="comment-actions">
                          <label className="check-field">
                            <input
                              type="checkbox"
                              checked={replyAnonymous}
                              onChange={(event) => setReplyAnonymous(event.target.checked)}
                            />
                            익명
                          </label>
                          <button className="button" type="submit">
                            등록
                          </button>
                          <button
                            className="button secondary"
                            type="button"
                            onClick={() => setReplyingTo(null)}
                          >
                            취소
                          </button>
                        </div>
                      </form>
                    )}

                    {replies.map((reply) => {
                      const canEditReply = currentUserId === reply.user_id

                      return (
                        <article
                          className="comment-item reply"
                          id={`comment-${reply.id}`}
                          key={reply.id}
                        >
                          {editingCommentId === reply.id ? (
                            <form onSubmit={(event) => handleUpdateComment(event, reply.id)}>
                              <label className="field">
                                답글 수정
                                <textarea
                                  value={editContent}
                                  onChange={(event) => setEditContent(event.target.value)}
                                  required
                                />
                              </label>
                              <div className="comment-actions">
                                <label className="check-field">
                                  <input
                                    type="checkbox"
                                    checked={editAnonymous}
                                    onChange={(event) =>
                                      setEditAnonymous(event.target.checked)
                                    }
                                  />
                                  익명
                                </label>
                                <button className="button" type="submit">
                                  저장
                                </button>
                                <button
                                  className="button secondary"
                                  type="button"
                                  onClick={() => setEditingCommentId(null)}
                                >
                                  취소
                                </button>
                              </div>
                            </form>
                          ) : (
                            <>
                              <div className="comment-header">
                                <strong>{reply.author_display_name}</strong>
                                <span>{new Date(reply.created_at).toLocaleString()}</span>
                              </div>
                              <p>{reply.content}</p>
                              <div className="comment-actions">
                                {canEditReply && (
                                  <>
                                    <button
                                      className="text-button"
                                      type="button"
                                      onClick={() => startEdit(reply)}
                                    >
                                      수정
                                    </button>
                                    <button
                                      className="text-button"
                                      type="button"
                                      onClick={() => handleDeleteComment(reply.id)}
                                    >
                                      삭제
                                    </button>
                                  </>
                                )}
                                {canReport && (
                                  <button
                                    className="text-button"
                                    type="button"
                                    onClick={() =>
                                      openReportModal({
                                        target_type: 'COMMENT',
                                        target_id: reply.id,
                                        label: reply.content,
                                      })
                                    }
                                  >
                                    신고
                                  </button>
                                )}
                              </div>
                            </>
                          )}
                        </article>
                      )
                    })}
                  </article>
                )
              })}
            </div>
          </section>
        </>
      )}

      {reportTarget && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel" aria-label="신고 입력">
            <div className="modal-header">
              <h2>신고하기</h2>
              <button
                className="text-button"
                type="button"
                onClick={() => setReportTarget(null)}
              >
                닫기
              </button>
            </div>
            <form className="field-stack" onSubmit={handleCreateReport}>
              <p className="muted">{reportTarget.label}</p>
              <label className="field">
                신고 사유
                <select
                  value={reportReason}
                  onChange={(event) => setReportReason(Number(event.target.value))}
                  required
                >
                  {Object.entries(reportReasonLabels).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>
              <div className="button-row">
                <button className="button" type="submit">
                  신고 접수
                </button>
                <button
                  className="button secondary"
                  type="button"
                  onClick={() => setReportTarget(null)}
                >
                  취소
                </button>
              </div>
            </form>
          </section>
        </div>
      )}
    </>
  )
}
