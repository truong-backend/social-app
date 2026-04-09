import { CreatePostForm } from '@features/post/components/CreatePostForm'
import { PostFeed } from '@features/post/components/PostFeed'
import { SearchPage } from './SearchPage'

export const FeedPage = () => {
  return (
    <div className="min-h-screen bg-surface">
      <div className="flex">
        {/* Main Feed */}
        <main className="flex-1 px-4 md:px-8 py-6 max-w-2xl mx-auto lg:mx-0 lg:ml-0 w-full">
          {/* New Posts Notification */}

          {/* Search */}
          <SearchPage />

          {/* Create Post */}
          <div className="bg-surface-container-lowest rounded-xl p-6 shadow-sm mb-6 mt-4">
            <CreatePostForm />
          </div>

          {/* Feed */}
          <PostFeed />
        </main>

      </div>
    </div>
  )
}
