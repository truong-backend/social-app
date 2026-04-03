import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useDebounce } from '@hooks/useDebounce'
import { useSearchUsers } from '@features/user/hooks/useSearchUsers'
import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import { PostCard } from '@features/post/components/PostCard'
import { Avatar } from '@components/ui/Avatar'
import { Spinner } from '@components/feedback/Spinner'
import { useSessionStore } from '@stores/session.store'
import type { Post } from '@features/post/types/post.types'

type SearchTab = 'users' | 'posts'

export const SearchPage = () => {
  const [keyword, setKeyword]     = useState('')
  const [activeTab, setActiveTab] = useState<SearchTab>('users')
  const debouncedKeyword          = useDebounce(keyword, 400)
  const currentUserId             = useSessionStore((state) => state.userId) ?? ''

  const showResults = debouncedKeyword.trim().length >= 2

  const { data: users, isLoading: usersLoading } = useSearchUsers(debouncedKeyword)

  const { data: posts, isLoading: postsLoading } = useQuery({
    queryKey: ['posts', 'search', debouncedKeyword],
    queryFn: async () => {
      const res = await axiosInstance.get('/api/posts/search', {
        params: { q: debouncedKeyword },
      })
      return unwrapData<Post[]>(res) ?? []
    },
    enabled: showResults && activeTab === 'posts',
    staleTime: 1000 * 30,
  })

  return (
    <div className="flex flex-col gap-4">
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
          className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-full text-sm focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all outline-none placeholder:text-on-surface-variant/60"
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
                  ? 'bg-surface-container-highest text-primary'
                  : 'text-on-surface-variant hover:bg-surface-container-low'
              }`}
            >
              {tab === 'users' ? 'Người dùng' : 'Bài viết'}
            </button>
          ))}
        </div>
      )}

      {!showResults && (
        <p className="text-sm text-on-surface-variant px-1">Nhập ít nhất 2 ký tự để tìm kiếm</p>
      )}

      {/* Users tab */}
      {showResults && activeTab === 'users' && (
        <div className="flex flex-col gap-2">
          {usersLoading ? (
            <div className="flex justify-center py-8"><Spinner size="md" /></div>
          ) : !users?.length ? (
            <p className="text-center text-on-surface-variant py-8 text-sm">Không tìm thấy người dùng nào</p>
          ) : (
            users.map((user) => (
              <Link
                key={user.id}
                to={`/profile/${user.id}`}
                className="flex items-center gap-4 p-4 bg-surface-container-low rounded-xl hover:bg-surface-container-high transition-colors"
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
      {showResults && activeTab === 'posts' && (
        <div className="flex flex-col gap-4">
          {postsLoading ? (
            <div className="flex justify-center py-8"><Spinner size="md" /></div>
          ) : !posts?.length ? (
            <p className="text-center text-on-surface-variant py-8 text-sm">Không tìm thấy bài viết nào</p>
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