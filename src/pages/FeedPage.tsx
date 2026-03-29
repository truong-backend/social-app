import { CreatePostForm } from '@features/post/components/CreatePostForm'
import { PostFeed } from '@features/post/components/PostFeed'

export const FeedPage = () => {
  return (
    <div className="feed-page">
      <div className="feed-page__container">
        <CreatePostForm />
        <PostFeed />
      </div>
    </div>
  )
}