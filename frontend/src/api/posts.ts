import { apiClient, useMockApi } from './client'

export interface PostFile {
  id: number
  file_url: string
}

export interface Post {
  id: number
  user_id: number
  author_display_name: string
  title: string
  content: string
  created_at: string
  updated_at: string | null
  view_count: number
  main_category: string
  sub_category: string
  is_anonymous: boolean
  files: PostFile[]
  liked_by_me: boolean
  like_count: number
}

export interface PostPage {
  items: Post[]
  page: number
  size: number
  total_count: number
  total_pages: number
}

export interface ListPostsParams {
  page?: number
  size?: number
  keyword?: string
  author?: string
  main_category?: string
  sub_category?: string
}

export interface SavePostRequest {
  title: string
  content: string
  main_category: string
  sub_category: string
  is_anonymous: boolean
  files?: File[]
}

interface LikeResponse {
  id: number
  user_id: number
  post_id: number
  created_at: string
}

const mockPostKey = 'db-teamproject:mock-posts'

const seedPosts: Post[] = [
  {
    id: 1,
    user_id: 2,
    author_display_name: '익명_1',
    title: '데이터베이스 정규화 요약 자료',
    content:
      '1NF부터 BCNF까지 핵심 개념을 정리한 자료입니다. 함수 종속성과 분해 기준을 함께 확인할 수 있도록 구성했습니다.',
    created_at: '2026-05-10T09:00:00',
    updated_at: null,
    view_count: 12,
    main_category: '컴퓨터공학과',
    sub_category: '데이터베이스',
    is_anonymous: true,
    files: [{ id: 1, file_url: '/uploads/posts/1/normalization-summary.pdf' }],
    liked_by_me: false,
    like_count: 4,
  },
  {
    id: 2,
    user_id: 3,
    author_display_name: '김수현',
    title: '팀 프로젝트 회의록 템플릿',
    content:
      '회의 안건, 결정 사항, 역할 분담, 다음 회의 전까지의 TODO를 나눠 적을 수 있는 템플릿입니다.',
    created_at: '2026-05-11T14:20:00',
    updated_at: null,
    view_count: 7,
    main_category: '컴퓨터공학과',
    sub_category: '소프트웨어공학',
    is_anonymous: false,
    files: [],
    liked_by_me: true,
    like_count: 2,
  },
  {
    id: 3,
    user_id: 4,
    author_display_name: '이민재',
    title: '중간고사 대비 SQL 문제 모음',
    content:
      'SELECT, JOIN, GROUP BY, HAVING을 중심으로 만든 연습 문제입니다. 풀이 과정은 댓글로 이어서 공유할 예정입니다.',
    created_at: '2026-05-12T18:10:00',
    updated_at: null,
    view_count: 21,
    main_category: '컴퓨터공학과',
    sub_category: '데이터베이스',
    is_anonymous: false,
    files: [{ id: 2, file_url: '/uploads/posts/3/sql-practice.zip' }],
    liked_by_me: false,
    like_count: 6,
  },
]

function readMockPosts() {
  const rawPosts = localStorage.getItem(mockPostKey)

  if (!rawPosts) {
    localStorage.setItem(mockPostKey, JSON.stringify(seedPosts))
    return seedPosts
  }

  return JSON.parse(rawPosts) as Post[]
}

function writeMockPosts(posts: Post[]) {
  localStorage.setItem(mockPostKey, JSON.stringify(posts))
}

async function waitMockResponse() {
  await new Promise((resolve) => {
    window.setTimeout(resolve, 180)
  })
}

function buildPostFormData(request: SavePostRequest) {
  const formData = new FormData()
  formData.append('title', request.title)
  formData.append('content', request.content)
  formData.append('main_category', request.main_category)
  formData.append('sub_category', request.sub_category)
  formData.append('is_anonymous', String(request.is_anonymous))

  request.files?.forEach((file) => {
    formData.append('files[]', file)
  })

  return formData
}

function sortByLatest(posts: Post[]) {
  return [...posts].sort(
    (left, right) =>
      new Date(right.created_at).getTime() - new Date(left.created_at).getTime(),
  )
}

function filterPosts(posts: Post[], params: ListPostsParams) {
  let nextPosts = posts
  const keyword = params.keyword?.trim()
  const author = params.author?.trim()
  const mainCategory = params.main_category?.trim()
  const subCategory = params.sub_category?.trim()

  if (keyword) {
    nextPosts = nextPosts.filter(
      (post) => post.title.includes(keyword) || post.content.includes(keyword),
    )
  } else if (author) {
    nextPosts = nextPosts.filter(
      (post) =>
        !post.is_anonymous &&
        post.author_display_name !== '탈퇴한 유저' &&
        post.author_display_name.includes(author),
    )
  } else if (mainCategory || subCategory) {
    nextPosts = nextPosts.filter((post) => {
      const matchesMain = mainCategory ? post.main_category === mainCategory : true
      const matchesSub = subCategory ? post.sub_category.includes(subCategory) : true
      return matchesMain && matchesSub
    })
  }

  return sortByLatest(nextPosts)
}

function createMockFiles(files?: File[]) {
  return (
    files?.map((file, index) => ({
      id: Date.now() + index,
      file_url: `/uploads/posts/mock/${file.name}`,
    })) ?? []
  )
}

export const postsApi = {
  async listPosts(params: ListPostsParams = {}) {
    if (useMockApi) {
      await waitMockResponse()
      const page = params.page ?? 1
      const size = params.size ?? 10
      const filteredPosts = filterPosts(readMockPosts(), params)
      const offset = (page - 1) * size
      const items = filteredPosts.slice(offset, offset + size)

      return {
        items,
        page,
        size,
        total_count: filteredPosts.length,
        total_pages: Math.max(1, Math.ceil(filteredPosts.length / size)),
      } satisfies PostPage
    }

    const response = await apiClient.get<PostPage>('/posts', { params })
    return response.data
  },

  async getPost(postId: number) {
    if (useMockApi) {
      await waitMockResponse()
      const posts = readMockPosts()
      const post = posts.find((item) => item.id === postId)

      if (!post) {
        throw new Error('게시글을 찾을 수 없습니다.')
      }

      const nextPost = { ...post, view_count: post.view_count + 1 }
      writeMockPosts(posts.map((item) => (item.id === postId ? nextPost : item)))
      return nextPost
    }

    const response = await apiClient.get<Post>(`/posts/${postId}`)
    return response.data
  },

  async createPost(request: SavePostRequest) {
    if (useMockApi) {
      await waitMockResponse()
      const posts = readMockPosts()
      const now = new Date().toISOString()
      const nextPost: Post = {
        id: Math.max(0, ...posts.map((post) => post.id)) + 1,
        user_id: 2,
        author_display_name: request.is_anonymous ? '익명_1' : '테스트 사용자',
        title: request.title,
        content: request.content,
        created_at: now,
        updated_at: null,
        view_count: 0,
        main_category: request.main_category,
        sub_category: request.sub_category,
        is_anonymous: request.is_anonymous,
        files: createMockFiles(request.files),
        liked_by_me: false,
        like_count: 0,
      }

      writeMockPosts([nextPost, ...posts])
      return nextPost
    }

    const response = await apiClient.post<Post>('/posts', buildPostFormData(request), {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  async updatePost(postId: number, request: SavePostRequest) {
    if (useMockApi) {
      await waitMockResponse()
      const posts = readMockPosts()
      const post = posts.find((item) => item.id === postId)

      if (!post) {
        throw new Error('게시글을 찾을 수 없습니다.')
      }

      const nextPost: Post = {
        ...post,
        title: request.title,
        content: request.content,
        main_category: request.main_category,
        sub_category: request.sub_category,
        is_anonymous: request.is_anonymous,
        files: request.files?.length ? createMockFiles(request.files) : post.files,
        updated_at: new Date().toISOString(),
      }

      writeMockPosts(posts.map((item) => (item.id === postId ? nextPost : item)))
      return nextPost
    }

    const hasFiles = Boolean(request.files?.length)
    const body = hasFiles ? buildPostFormData(request) : request
    const response = await apiClient.patch<Post>(`/posts/${postId}`, body, {
      headers: hasFiles
        ? {
            'Content-Type': 'multipart/form-data',
          }
        : undefined,
    })
    return response.data
  },

  async deletePost(postId: number) {
    if (useMockApi) {
      await waitMockResponse()
      writeMockPosts(readMockPosts().filter((post) => post.id !== postId))
      return
    }

    await apiClient.delete(`/posts/${postId}`)
  },

  async likePost(postId: number) {
    if (useMockApi) {
      await waitMockResponse()
      const posts = readMockPosts()
      const post = posts.find((item) => item.id === postId)

      if (!post) {
        throw new Error('게시글을 찾을 수 없습니다.')
      }

      const nextPost = {
        ...post,
        liked_by_me: true,
        like_count: post.liked_by_me ? post.like_count : post.like_count + 1,
      }
      writeMockPosts(posts.map((item) => (item.id === postId ? nextPost : item)))
      return {
        id: Date.now(),
        user_id: 2,
        post_id: postId,
        created_at: new Date().toISOString(),
      } satisfies LikeResponse
    }

    const response = await apiClient.post<LikeResponse>(`/posts/${postId}/likes`)
    return response.data
  },

  async unlikePost(postId: number) {
    if (useMockApi) {
      await waitMockResponse()
      const posts = readMockPosts()
      const post = posts.find((item) => item.id === postId)

      if (!post) {
        throw new Error('게시글을 찾을 수 없습니다.')
      }

      const nextPost = {
        ...post,
        liked_by_me: false,
        like_count: post.liked_by_me ? Math.max(0, post.like_count - 1) : post.like_count,
      }
      writeMockPosts(posts.map((item) => (item.id === postId ? nextPost : item)))
      return
    }

    await apiClient.delete(`/posts/${postId}/likes`)
  },
}
