import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useDebounce } from '@hooks/useDebounce'
import { useSearchUsers } from '@features/user/hooks/useSearchUsers'
import { searchPostsApi } from '@features/post/api/postActions.api'
import { PostCard } from '@features/post/components/PostCard'
import { Avatar } from '@components/ui/Avatar'
import { Spinner } from '@components/feedback/Spinner'
import { useSessionStore } from '@stores/session.store'
import type { Post } from '@features/post/types/post.types'

type SearchTab = 'users' | 'posts'

export const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const initialQ = searchParams.get('q') ?? ''

  const [keyword, setKeyword]     = useState(initialQ)
  const [activeTab, setActiveTab] = useState<SearchTab>('users')
  const debouncedKeyword          = useDebounce(keyword, 400)
  const currentUserId             = useSessionStore((state) => state.userId) ?? ''

  // Sync URL param → input khi navigate từ search bar header
  useEffect(() => {
    const q = searchParams.get('q') ?? ''
    if (q !== keyword) setKeyword(q)
  }, [searchParams])

  // Sync keyword → URL
  useEffect(() => {
    const trimmed = debouncedKeyword.trim()
    if (trimmed.length >= 2) {
      setSearchParams({ q: trimmed }, { replace: true })
    } else if (trimmed.length === 0) {
      setSearchParams({}, { replace: true })
    }
  }, [debouncedKeyword])

  const showResults = debouncedKeyword.trim().length >= 2

  // Fetch cả 2 query không phụ thuộc vào tab - để switch tab không bị loading lại
  const { data: users, isLoading: usersLoading } = useSearchUsers(debouncedKeyword)

  const { data: posts, isLoading: postsLoading } = useQuery({
    queryKey: ['posts', 'search', debouncedKeyword],
    queryFn:  () => searchPostsApi(debouncedKeyword),
    enabled:  showResults,   // Luôn fetch khi có keyword đủ dài, không chờ tab
    staleTime: 1000 * 30,
  })

  const isLoading = activeTab === 'users' ? usersLoading : postsLoading

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 pb-24 md:pb-8 flex flex-col gap-4">
      {/* Search input */}
      <div className="relative">
        <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant">
          search
        </span>
        <input
          type="search"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Tìm người dùng hoặc bài viết..."
          className="w-full pl-12 pr-4 py-2.5 bg-slate-100/50 border-none rounded-full text-sm focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all outline-none placeholder:text-slate-400"
          autoFocus
        />
      </div>

      {/* Tabs */}
      {showResults && (
        <div className="flex gap-2" role="tablist">
          {(['users', 'posts'] as const).map((tab) => (
            <button
              key={tab}
              role="tab"
              aria-selected={activeTab === tab}
              onClick={() => setActiveTab(tab)}
              className={`px-5 py-2 rounded-full text-sm font-bold transition-all ${
                activeTab === tab
                  ? 'bg-surface-container-highest text-primary shadow-sm'
                  : 'text-on-surface-variant hover:bg-surface-container-low'
              }`}
            >
              {tab === 'users' ? `Người dùng${users?.length ? ` (${users.length})` : ''}` : `Bài viết${posts?.length ? ` (${posts.length})` : ''}`}
            </button>
          ))}
        </div>
      )}

      {!showResults && (
        <div className="flex flex-col items-center justify-center py-16 gap-3 text-on-surface-variant">
          <span className="material-symbols-outlined text-5xl opacity-30">manage_search</span>
          <p className="text-sm">Nhập ít nhất 2 ký tự để tìm kiếm</p>
        </div>
      )}

      {/* Loading chung */}
      {showResults && isLoading && (
        <div className="flex justify-center py-8"><Spinner size="md" /></div>
      )}

      {/* Users tab */}
      {showResults && activeTab === 'users' && !usersLoading && (
        <div className="flex flex-col gap-2">
          {!users?.length ? (
            <div className="flex flex-col items-center justify-center py-12 gap-3 text-on-surface-variant">
              <span className="material-symbols-outlined text-4xl opacity-30">person_search</span>
              <p className="text-sm">Không tìm thấy người dùng nào</p>
            </div>
          ) : (
            users.map((user) => (
              <Link
                key={user.id}
                to={`/profile/${user.id}`}
                className="flex items-center gap-3 p-3 bg-surface-container-lowest rounded-xl hover:bg-white/40 border border-outline-variant/10 transition-colors shadow-sm"
              >
                <Avatar src={user.profilePictureUrl} alt={user.username} size="md" />
                <div>
                  <p className="font-bold text-on-surface text-sm">
                    {user.familyName} {user.givenName}
                  </p>
                  <p className="text-xs text-on-surface-variant">@{user.username}</p>
                </div>
              </Link>
            ))
          )}
        </div>
      )}

      {/* Posts tab */}
      {showResults && activeTab === 'posts' && !postsLoading && (
        <div className="flex flex-col gap-4">
          {!posts?.length ? (
            <div className="flex flex-col items-center justify-center py-12 gap-3 text-on-surface-variant">
              <span className="material-symbols-outlined text-4xl opacity-30">article</span>
              <p className="text-sm">Không tìm thấy bài viết nào</p>
            </div>
          ) : (
            posts.map((post) => (
              <PostCard key={post.id} post={post} currentUserId={currentUserId} />
            ))
          )}
        </div>
      )}
    </div>
  )
}
