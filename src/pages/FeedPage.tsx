import { CreatePostForm } from '@features/post/components/CreatePostForm'
import { PostFeed } from '@features/post/components/PostFeed'
import { SearchPage } from './SearchPage'

export const FeedPage = () => {
  return (
    <div className="min-h-screen bg-background">
      <div className="max-w-2xl mx-auto py-8 px-4 md:px-0 flex flex-col gap-6 pb-24 md:pb-8">
        <SearchPage />
        <CreatePostForm />
        <PostFeed />
      </div>
    </div>
  )
}