import { useState, useRef, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import type { Post } from '../types/post.types'
import type { Privacy } from '@/types/api.types'
import { useDeletePost } from '../hooks/useDeletePost'
import { useLikePost } from '@features/like/hooks/useLikePost'
import { formatRelativeTime } from '@utils/date.formatter'
import { CommentList } from '@features/comment/components/CommentList'
import { SharePostModal } from './SharePostModal'
import { updatePostPrivacyApi } from '../api/updatePost.api'
import { POST_QUERY_KEYS, POST_PRIVACY_LABELS } from '../constants/post.constants'
import { getProfileApi } from '@features/user/api/user.api'
import { extractErrorMessage } from '@utils/api-response'
import toast from 'react-hot-toast'

const PRIVACY_ICONS: Record<string, string> = {
  PUBLIC: 'public',
  FRIENDS: 'group',
  PRIVATE: 'lock',
}

interface PostCardProps {
  post: Post
  currentUserId: string
}

export const PostCard = ({ post, currentUserId }: PostCardProps) => {
  const [showComments, setShowComments]       = useState(false)
  const [showShareModal, setShowShareModal]   = useState(false)
  const [showMenu, setShowMenu]               = useState(false)
  const [showPrivacyMenu, setShowPrivacyMenu] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  const isOwner    = post.authorId === currentUserId
  const likePost   = useLikePost()
  const deletePost = useDeletePost()
  const queryClient = useQueryClient()

  // Fetch profile của tác giả để lấy tên + ảnh thật
  const { data: authorProfile } = useQuery({
    queryKey: ['user', 'profile', post.authorId],
    queryFn:  () => getProfileApi(post.authorId),
    staleTime: 1000 * 60 * 5,
    enabled:  !!post.authorId,
  })

  const displayName: string =
    (authorProfile
      ? (`${authorProfile.familyName ?? ''} ${authorProfile.givenName ?? ''}`).trim() ||
        authorProfile.username ||
        post.authorUsername ||
        'Unknown'
      : post.authorUsername || 'Unknown') as string

  const avatarUrl: string | undefined =
    (authorProfile?.profilePictureUrl ?? post.authorProfilePic) ?? undefined

  const updatePrivacy = useMutation({
    mutationFn: (privacy: Privacy) => updatePostPrivacyApi(post.id, { privacy }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.feed() })
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.detail(post.id) })
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.byAuthor(post.authorId) })
      toast.success('Đã cập nhật quyền riêng tư')
      setShowPrivacyMenu(false)
      setShowMenu(false)
    },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setShowMenu(false)
        setShowPrivacyMenu(false)
      }
    }
    if (showMenu) document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [showMenu])

  return (
    <article className="bg-surface-container-lowest rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow mb-6">

      {/* Header */}
      <div className="p-6 pb-4">
        <div className="flex justify-between items-start mb-4">
          <Link to={`/profile/${post.authorId}`} className="flex items-center gap-3">
            {avatarUrl ? (
              <img src={avatarUrl} alt={displayName} className="w-10 h-10 rounded-full object-cover" />
            ) : (
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center font-bold text-sm">
                {displayName.charAt(0).toUpperCase()}
              </div>
            )}
            <div>
              <h4 className="font-bold text-on-surface">{displayName}</h4>
              <p className="text-xs text-on-surface-variant font-medium flex items-center gap-1">
                {formatRelativeTime(post.createdAt)}
                {' '}•{' '}
                <span className="material-symbols-outlined text-[12px] align-middle">
                  {PRIVACY_ICONS[post.privacy] ?? 'public'}
                </span>
                <span>{POST_PRIVACY_LABELS[post.privacy] ?? post.privacy}</span>
              </p>
            </div>
          </Link>

          {/* More menu */}
          <div className="relative" ref={menuRef}>
            <button onClick={() => setShowMenu((p) => !p)}
              className="p-2 hover:bg-surface-container-low rounded-full text-on-surface-variant transition-colors">
              <span className="material-symbols-outlined">more_horiz</span>
            </button>

            {showMenu && (
              <div className="absolute right-0 top-full mt-1 w-52 bg-surface-container-lowest rounded-xl shadow-xl border border-outline-variant/10 z-20 overflow-hidden">
                {isOwner ? (
                  <>
                    <button className="w-full flex items-center gap-3 px-4 py-3 text-sm text-on-surface hover:bg-surface-container-low transition-colors"
                      onClick={() => setShowPrivacyMenu((p) => !p)}>
                      <span className="material-symbols-outlined text-lg">{PRIVACY_ICONS[post.privacy] ?? 'public'}</span>
                      <span className="flex-1 text-left">Quyền riêng tư</span>
                      <span className="material-symbols-outlined text-sm">chevron_right</span>
                    </button>

                    {showPrivacyMenu && (
                      <div className="bg-surface-container-low border-t border-outline-variant/10">
                        {(['PUBLIC', 'FRIENDS', 'PRIVATE'] as Privacy[]).map((p) => (
                          <button key={p}
                            className={`w-full flex items-center gap-3 px-6 py-2.5 text-sm transition-colors ${
                              post.privacy === p ? 'text-primary font-bold bg-primary/5' : 'text-on-surface hover:bg-surface-container'
                            }`}
                            onClick={() => updatePrivacy.mutate(p)}
                            disabled={updatePrivacy.isPending}>
                            <span className="material-symbols-outlined text-base">{PRIVACY_ICONS[p]}</span>
                            {POST_PRIVACY_LABELS[p]}
                            {post.privacy === p && <span className="material-symbols-outlined text-sm ml-auto">check</span>}
                          </button>
                        ))}
                      </div>
                    )}

                    <div className="border-t border-outline-variant/10" />

                    <button
                      className="w-full flex items-center gap-3 px-4 py-3 text-sm text-error hover:bg-error/5 transition-colors"
                      onClick={() => { setShowMenu(false); if (window.confirm('Xóa bài viết này?')) deletePost.mutate(post.id) }}
                      disabled={deletePost.isPending}>
                      <span className="material-symbols-outlined text-lg">delete</span>
                      Xóa bài viết
                    </button>
                  </>
                ) : (
                  <button className="w-full flex items-center gap-3 px-4 py-3 text-sm text-on-surface hover:bg-surface-container-low transition-colors"
                    onClick={() => setShowMenu(false)}>
                    <span className="material-symbols-outlined text-lg">flag</span>
                    Báo cáo bài viết
                  </button>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Shared label */}
        {post.isShared && post.sharedFromPostId && (
          <p className="text-sm text-on-surface-variant mb-2">
            Đã chia sẻ{' '}
            <Link to={`/posts/${post.sharedFromPostId}`} className="text-primary underline">bài viết gốc</Link>
          </p>
        )}

        {/* Content */}
        <Link to={`/posts/${post.id}`}>
          <p className="text-on-surface leading-relaxed">{post.content}</p>
        </Link>
      </div>

      {/* Media */}
      {post.attachedFileUrls.length > 0 && (
        <div className="px-6 pb-2">
          <div className={`grid gap-2 rounded-md overflow-hidden ${post.attachedFileUrls.length === 1 ? 'grid-cols-1' : 'grid-cols-2'}`}>
            {post.attachedFileUrls.map((url) => (
              <img key={url} src={url} alt="attachment" className="w-full h-80 object-cover" />
            ))}
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="p-4 px-6 border-t border-slate-50 flex items-center justify-between">
        <div className="flex gap-6">
          <button onClick={() => likePost.toggle(post.id, post.isLiked)} disabled={likePost.isPending}
            className={`flex items-center gap-2 hover:text-primary transition-colors ${post.isLiked ? 'text-primary' : 'text-on-surface-variant'}`}>
            <span className="material-symbols-outlined" style={post.isLiked ? { fontVariationSettings: "'FILL' 1" } : undefined}>favorite</span>
            <span className="text-sm font-semibold">{post.likeCount}</span>
          </button>

          <button onClick={() => setShowComments((p) => !p)}
            className="flex items-center gap-2 text-on-surface-variant hover:text-primary transition-colors">
            <span className="material-symbols-outlined">chat_bubble</span>
            <span className="text-sm font-semibold">{post.commentCount}</span>
          </button>

          <button onClick={() => setShowShareModal(true)}
            className="flex items-center gap-2 text-on-surface-variant hover:text-primary transition-colors">
            <span className="material-symbols-outlined">share</span>
            <span className="text-sm font-semibold">Share</span>
          </button>
        </div>

        <button className="text-on-surface-variant hover:text-primary cursor-pointer transition-colors">
          <span className="material-symbols-outlined">bookmark</span>
        </button>
      </div>

      {/* Comments */}
      {showComments && (
        <div className="px-6 pb-4 border-t border-outline-variant/10">
          <CommentList postId={post.id} />
        </div>
      )}

      {showShareModal && (
        <SharePostModal originalPostId={post.id} onClose={() => setShowShareModal(false)} />
      )}
    </article>
  )
}
