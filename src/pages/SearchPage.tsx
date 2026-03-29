import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useDebounce } from '@hooks/useDebounce'
import { useSearchUsers } from '@features/user/hooks/useSearchUsers'
import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import { PostCard } from '@features/post/components/PostCard'
import { Avatar } from '@components/ui/Avatar'
import { Input } from '@components/ui/Input'
import { Spinner } from '@components/feedback/Spinner'
import { useSessionStore } from '@stores/session.store'
import type { Post } from '@features/post/types/post.types'

type SearchTab = 'users' | 'posts'

export const SearchPage = () => {
  const [keyword, setKeyword]   = useState('')
  const [activeTab, setActiveTab] = useState<SearchTab>('users')
  const debouncedKeyword        = useDebounce(keyword, 400)
  const currentUserId           = useSessionStore((state) => state.userId) ?? ''

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
    <div className="search-page">
      <div className="search-page__header">
        <Input
          label="Tìm kiếm"
          type="search"
          placeholder="Tìm người dùng hoặc bài viết..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="search-page__input"
        />
      </div>

      {/* Tabs */}
      <div className="search-page__tabs" role="tablist">
        {(['users', 'posts'] as const).map((tab) => (
          <button
            key={tab}
            role="tab"
            aria-selected={activeTab === tab}
            className={`search-page__tab ${activeTab === tab ? 'search-page__tab--active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab === 'users' ? 'Người dùng' : 'Bài viết'}
          </button>
        ))}
      </div>

      {!showResults && (
        <p className="search-page__hint">Nhập ít nhất 2 ký tự để tìm kiếm</p>
      )}

      {/* Users tab */}
      {showResults && activeTab === 'users' && (
        <div className="search-page__results">
          {usersLoading ? (
            <Spinner size="md" />
          ) : !users?.length ? (
            <p className="search-page__empty">Không tìm thấy người dùng nào</p>
          ) : (
            <div className="search-page__user-list">
              {users.map((user) => (
                <Link key={user.id} to={`/profile/${user.id}`} className="search-page__user-item">
                  <Avatar src={user.profilePictureUrl} alt={user.username} size="md" />
                  <div className="search-page__user-info">
                    <span className="search-page__user-name">
                      {user.familyName} {user.givenName}
                    </span>
                    <span className="search-page__user-username">@{user.username}</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Posts tab */}
      {showResults && activeTab === 'posts' && (
        <div className="search-page__results">
          {postsLoading ? (
            <Spinner size="md" />
          ) : !posts?.length ? (
            <p className="search-page__empty">Không tìm thấy bài viết nào</p>
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