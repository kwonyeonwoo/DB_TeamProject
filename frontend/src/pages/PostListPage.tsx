import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { getErrorMessage } from '../api/errors'
import { postsApi } from '../api/posts'
import type { ListPostsParams, PostPage } from '../api/posts'
import { UserSummaryCard } from '../components/UserSummaryCard'

type SearchType = 'keyword' | 'author' | 'category'

const pageSize = 10

function buildListParams(searchParams: URLSearchParams): ListPostsParams {
  const page = Number(searchParams.get('page') ?? '1')
  const searchType = searchParams.get('type') as SearchType | null
  const query = searchParams.get('query')?.trim() ?? ''
  const mainCategory = searchParams.get('main_category')?.trim() ?? ''
  const subCategory = searchParams.get('sub_category')?.trim() ?? ''

  return {
    page: Number.isNaN(page) ? 1 : page,
    size: pageSize,
    keyword: searchType === 'keyword' ? query : undefined,
    author: searchType === 'author' ? query : undefined,
    main_category: searchType === 'category' ? mainCategory : undefined,
    sub_category: searchType === 'category' ? subCategory : undefined,
  }
}

export function PostListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [postPage, setPostPage] = useState<PostPage | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [searchType, setSearchType] = useState<SearchType>(
    (searchParams.get('type') as SearchType | null) ?? 'keyword',
  )
  const [query, setQuery] = useState(searchParams.get('query') ?? '')
  const [mainCategory, setMainCategory] = useState(
    searchParams.get('main_category') ?? '',
  )
  const [subCategory, setSubCategory] = useState(searchParams.get('sub_category') ?? '')
  const listParams = useMemo(() => buildListParams(searchParams), [searchParams])

  useEffect(() => {
    let isMounted = true

    postsApi
      .listPosts(listParams)
      .then((page) => {
        if (!isMounted) {
          return
        }

        setPostPage(page)
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
  }, [listParams])

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const nextParams = new URLSearchParams()
    nextParams.set('page', '1')
    nextParams.set('type', searchType)

    if (searchType === 'category') {
      if (mainCategory) {
        nextParams.set('main_category', mainCategory)
      }
      if (subCategory) {
        nextParams.set('sub_category', subCategory)
      }
    } else if (query.trim()) {
      nextParams.set('query', query.trim())
    }

    setSearchParams(nextParams)
  }

  const movePage = (page: number) => {
    const nextParams = new URLSearchParams(searchParams)
    nextParams.set('page', String(page))
    setSearchParams(nextParams)
  }

  return (
    <>
      <div className="page-action-row">
        <Link className="button" to="/posts/write">
          글 작성
        </Link>
      </div>

      <div className="page-with-sidebar">
        <div className="page-main-column">
          <form className="toolbar" onSubmit={handleSearch}>
            <label className="field compact">
              검색 방식
              <select
                value={searchType}
                onChange={(event) => setSearchType(event.target.value as SearchType)}
              >
                <option value="keyword">제목/내용</option>
                <option value="author">작성자</option>
                <option value="category">학과/과목</option>
              </select>
            </label>
            {searchType === 'category' ? (
              <>
                <label className="field compact">
                  학과
                  <input
                    type="search"
                    value={mainCategory}
                    onChange={(event) => setMainCategory(event.target.value)}
                    placeholder="예: 컴퓨터공학과"
                  />
                </label>
                <label className="field compact">
                  과목
                  <input
                    type="search"
                    value={subCategory}
                    onChange={(event) => setSubCategory(event.target.value)}
                    placeholder="예: 데이터베이스"
                  />
                </label>
              </>
            ) : (
              <label className="field compact">
                검색어
                <input
                  type="search"
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder={searchType === 'author' ? '작성자 이름' : '제목 또는 내용'}
                />
              </label>
            )}
            <button className="button" type="submit">
              검색
            </button>
          </form>

          {errorMessage && <p className="form-error">{errorMessage}</p>}

          <section className="post-list">
            {!postPage && <p className="empty-state">게시글을 불러오고 있습니다.</p>}
            {postPage?.items.length === 0 && (
              <p className="empty-state">조건에 맞는 게시글이 없습니다.</p>
            )}
            {postPage?.items.map((post) => (
              <Link className="post-item" key={post.id} to={`/posts/${post.id}`}>
                <div className="post-meta">
                  <span className="badge">{post.main_category}</span>
                  <span>{post.sub_category}</span>
                  <span>{post.author_display_name}</span>
                </div>
                <h2>{post.title}</h2>
                <p>{post.content}</p>
                <div className="post-meta">
                  <span>좋아요 {post.like_count}</span>
                  <span>조회 {post.view_count}</span>
                  <span>{new Date(post.created_at).toLocaleString()}</span>
                </div>
              </Link>
            ))}
          </section>

          {postPage && postPage.total_pages > 1 && (
            <div className="pagination">
              <button
                className="button secondary"
                type="button"
                disabled={postPage.page <= 1}
                onClick={() => movePage(postPage.page - 1)}
              >
                이전
              </button>
              <span>
                {postPage.page} / {postPage.total_pages}
              </span>
              <button
                className="button secondary"
                type="button"
                disabled={postPage.page >= postPage.total_pages}
                onClick={() => movePage(postPage.page + 1)}
              >
                다음
              </button>
            </div>
          )}
        </div>

        <UserSummaryCard className="page-side-profile" />
      </div>
    </>
  )
}
