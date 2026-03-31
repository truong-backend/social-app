import { CreatePostForm } from '@features/post/components/CreatePostForm'
import { PostFeed } from '@features/post/components/PostFeed'
import styles from './FeedPage.module.scss'
import { SearchPage } from './SearchPage'

export const FeedPage = () => {
  return (
    <div className={styles['feed-page']}>
  <div className={styles['feed-page__container']}>
    <SearchPage />
    <CreatePostForm />
    <PostFeed />
  </div>
</div>
  )
}