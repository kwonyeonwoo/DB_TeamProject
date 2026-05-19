import { apiClient, useMockApi } from './client'

export interface Comment {
  id: number
  user_id: number
  author_display_name: string
  post_id: number
  parent_comment: number | null
  content: string
  is_anonymous: boolean
  created_at: string
  updated_at: string | null
}

export interface CommentListResponse {
  items: Comment[]
}

export interface SaveCommentRequest {
  content: string
  is_anonymous: boolean
}

const mockCommentKey = 'db-teamproject:mock-comments'

const seedComments: Comment[] = [
  {
    id: 1,
    user_id: 3,
    author_display_name: '김수현',
    post_id: 1,
    parent_comment: null,
    content: '정규화 단계별 예시가 있어서 이해하기 좋습니다.',
    is_anonymous: false,
    created_at: '2026-05-12T09:20:00',
    updated_at: null,
  },
  {
    id: 2,
    user_id: 4,
    author_display_name: '익명_2',
    post_id: 1,
    parent_comment: null,
    content: 'BCNF 분해 예제도 추가되면 좋겠습니다.',
    is_anonymous: true,
    created_at: '2026-05-12T10:05:00',
    updated_at: null,
  },
  {
    id: 3,
    user_id: 2,
    author_display_name: '테스트 사용자',
    post_id: 1,
    parent_comment: 2,
    content: '다음 수정 때 예제를 보강해 보겠습니다.',
    is_anonymous: false,
    created_at: '2026-05-12T10:30:00',
    updated_at: null,
  },
]

function readMockComments() {
  const rawComments = localStorage.getItem(mockCommentKey)

  if (!rawComments) {
    localStorage.setItem(mockCommentKey, JSON.stringify(seedComments))
    return seedComments
  }

  return JSON.parse(rawComments) as Comment[]
}

function writeMockComments(comments: Comment[]) {
  localStorage.setItem(mockCommentKey, JSON.stringify(comments))
}

async function waitMockResponse() {
  await new Promise((resolve) => {
    window.setTimeout(resolve, 180)
  })
}

function createMockComment(
  postId: number,
  request: SaveCommentRequest,
  parentComment: number | null,
) {
  const comments = readMockComments()
  const nextId = Math.max(0, ...comments.map((comment) => comment.id)) + 1
  const comment: Comment = {
    id: nextId,
    user_id: 2,
    author_display_name: request.is_anonymous ? '익명_1' : '테스트 사용자',
    post_id: postId,
    parent_comment: parentComment,
    content: request.content,
    is_anonymous: request.is_anonymous,
    created_at: new Date().toISOString(),
    updated_at: null,
  }

  writeMockComments([...comments, comment])
  return comment
}

export const commentsApi = {
  async listComments(postId: number) {
    if (useMockApi) {
      await waitMockResponse()
      const items = readMockComments()
        .filter((comment) => comment.post_id === postId)
        .sort(
          (left, right) =>
            new Date(left.created_at).getTime() - new Date(right.created_at).getTime(),
        )

      return { items } satisfies CommentListResponse
    }

    const response = await apiClient.get<CommentListResponse>(
      `/posts/${postId}/comments`,
    )
    return response.data
  },

  async createComment(postId: number, request: SaveCommentRequest) {
    if (useMockApi) {
      await waitMockResponse()
      return createMockComment(postId, request, null)
    }

    const response = await apiClient.post<Comment>(
      `/posts/${postId}/comments`,
      request,
    )
    return response.data
  },

  async createReply(commentId: number, request: SaveCommentRequest) {
    if (useMockApi) {
      await waitMockResponse()
      const parentComment = readMockComments().find((comment) => comment.id === commentId)

      if (!parentComment || parentComment.parent_comment !== null) {
        throw new Error('대댓글은 일반 댓글에만 작성할 수 있습니다.')
      }

      return createMockComment(parentComment.post_id, request, commentId)
    }

    const response = await apiClient.post<Comment>(
      `/comments/${commentId}/replies`,
      request,
    )
    return response.data
  },

  async updateComment(commentId: number, request: SaveCommentRequest) {
    if (useMockApi) {
      await waitMockResponse()
      const comments = readMockComments()
      const comment = comments.find((item) => item.id === commentId)

      if (!comment) {
        throw new Error('댓글을 찾을 수 없습니다.')
      }

      const nextComment: Comment = {
        ...comment,
        content: request.content,
        is_anonymous: request.is_anonymous,
        author_display_name: request.is_anonymous ? '익명_1' : '테스트 사용자',
        updated_at: new Date().toISOString(),
      }

      writeMockComments(
        comments.map((item) => (item.id === commentId ? nextComment : item)),
      )
      return nextComment
    }

    const response = await apiClient.patch<Comment>(
      `/comments/${commentId}`,
      request,
    )
    return response.data
  },

  async deleteComment(commentId: number) {
    if (useMockApi) {
      await waitMockResponse()
      const comments = readMockComments()
      const target = comments.find((comment) => comment.id === commentId)

      if (!target) {
        return
      }

      writeMockComments(
        comments.filter(
          (comment) =>
            comment.id !== commentId && comment.parent_comment !== commentId,
        ),
      )
      return
    }

    await apiClient.delete(`/comments/${commentId}`)
  },
}
