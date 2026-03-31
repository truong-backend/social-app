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
    // faux overlay — dùng div thường để không vỡ layout
    <div
      style={{
        marginTop: 12,
        padding: 16,
        border: '1px solid var(--color-border-secondary)',
        borderRadius: 8,
        background: 'var(--color-background-secondary)',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
        <span style={{ fontWeight: 500 }}>Chia sẻ bài viết</span>
        <button onClick={onClose} aria-label="Đóng" style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
          ✕
        </button>
      </div>

      <textarea
        placeholder="Nói gì đó về bài viết này..."
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows={3}
        style={{ width: '100%', boxSizing: 'border-box', marginBottom: 8, padding: 8, borderRadius: 6, border: '1px solid var(--color-border-tertiary)', background: 'var(--color-background-primary)', color: 'var(--color-text-primary)', resize: 'vertical' }}
      />

      <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
        <select
          value={privacy}
          onChange={(e) => setPrivacy(e.target.value as Privacy)}
          style={{ padding: '4px 8px', borderRadius: 6, border: '1px solid var(--color-border-tertiary)', background: 'var(--color-background-primary)', color: 'var(--color-text-primary)' }}
        >
          <option value="PUBLIC">Công khai</option>
          <option value="FRIENDS">Bạn bè</option>
          <option value="PRIVATE">Chỉ mình tôi</option>
        </select>

        <button
          onClick={() => share.mutate()}
          disabled={share.isPending}
          style={{ padding: '6px 16px', borderRadius: 6, background: 'var(--color-background-info)', color: 'var(--color-text-info)', border: 'none', cursor: share.isPending ? 'not-allowed' : 'pointer', fontWeight: 500 }}
        >
          {share.isPending ? 'Đang chia sẻ...' : 'Chia sẻ'}
        </button>
      </div>
    </div>
  )
}