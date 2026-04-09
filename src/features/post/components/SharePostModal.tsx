import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import { sharePostApi } from '../api/postActions.api'
import { POST_QUERY_KEYS } from '../constants/post.constants'
import { extractErrorMessage } from '@utils/api-response'
import type { Privacy } from '@/types/api.types'

interface SharePostModalProps {
  originalPostId: string
  onClose:        () => void
}

export const SharePostModal = ({ originalPostId, onClose }: SharePostModalProps) => {
  const [content, setContent] = useState('')
  const [privacy, setPrivacy] = useState<Privacy>('PUBLIC')
  const queryClient = useQueryClient()

  const share = useMutation({
    mutationFn: () => sharePostApi(originalPostId, { content, privacy }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: POST_QUERY_KEYS.feed() })
      toast.success('Đã chia sẻ bài viết')
      onClose()
    },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })

  return (
    <div className="mt-3 p-4 bg-surface-container-lowest rounded-xl border border-outline-variant/15 shadow-sm">
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <span
          className="font-bold text-on-surface text-sm"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Share Post
        </span>
        <button
          onClick={onClose}
          aria-label="Đóng"
          className="p-1.5 hover:bg-surface-container-high rounded-lg text-on-surface-variant transition-colors"
        >
          <span className="material-symbols-outlined text-sm">close</span>
        </button>
      </div>

      {/* Textarea */}
      <textarea
        placeholder="Nói gì đó về bài viết này..."
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows={3}
        className="w-full px-4 py-3 bg-surface-container-low border-none rounded-xl text-sm text-on-surface placeholder:text-on-surface-variant/60 focus:ring-2 focus:ring-primary/20 outline-none transition-all resize-none mb-3"
      />

      {/* Actions */}
      <div className="flex items-center gap-3 justify-end">
        <select
          value={privacy}
          onChange={(e) => setPrivacy(e.target.value as Privacy)}
          className="px-3 py-2 rounded-xl bg-surface-container-low border-none text-sm text-on-surface focus:ring-2 focus:ring-primary/20 outline-none"
        >
          <option value="PUBLIC">Công khai</option>
          <option value="FRIENDS">Bạn bè</option>
          <option value="PRIVATE">Chỉ mình tôi</option>
        </select>

        <button
          onClick={() => share.mutate()}
          disabled={share.isPending}
          className="px-5 py-2 bg-gradient-to-r from-primary to-primary-container text-on-primary text-sm font-bold rounded-xl shadow-md active:scale-95 transition-all disabled:opacity-50 disabled:pointer-events-none"
        >
          {share.isPending ? 'Đang chia sẻ...' : 'Share'}
        </button>
      </div>
    </div>
  )
}
