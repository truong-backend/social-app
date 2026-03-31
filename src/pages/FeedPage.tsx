import { CreatePostForm } from '@features/post/components/CreatePostForm'
import { PostFeed } from '@features/post/components/PostFeed'
import styles from './FeedPage.module.scss'

export const FeedPage = () => {
  return (
    <div className={styles['feed-page']}>
  <div className={styles['feed-page__container']}>
    <CreatePostForm />
    <PostFeed />
  </div>
</div>
  )
}