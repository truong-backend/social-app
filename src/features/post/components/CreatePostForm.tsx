import { useState, useRef } from 'react'
import { useCreatePost } from '../hooks/useCreatePost'
import type { Privacy } from '@/types/api.types'

export const CreatePostForm = () => {
  const [content, setContent] = useState('')
  const [privacy, setPrivacy] = useState<Privacy>('PUBLIC')
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const fileInputRef = useRef<HTMLInputElement>(null)

  const createPost = useCreatePost()

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    if (!content.trim() && selectedFiles.length === 0) return

    createPost.mutate(
      { payload: { content, privacy }, files: selectedFiles },
      {
        onSuccess: () => {
          setContent('')
          setSelectedFiles([])
        },
      },
    )
  }

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? [])
    setSelectedFiles((previous) => [...previous, ...files])
  }

  const removeFile = (index: number) => {
    setSelectedFiles((previous) => previous.filter((_, i) => i !== index))
  }

  return (
    <form className="create-post-form" onSubmit={handleSubmit}>
      <textarea
        className="create-post-form__textarea"
        placeholder="Bạn đang nghĩ gì?"
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows={3}
      />

      {/* File previews */}
      {selectedFiles.length > 0 && (
        <div className="create-post-form__file-preview">
          {selectedFiles.map((file, index) => (
            <div key={index} className="create-post-form__file-item">
              <span>{file.name}</span>
              <button type="button" onClick={() => removeFile(index)}>✕</button>
            </div>
          ))}
        </div>
      )}

      <div className="create-post-form__controls">
        <select
          className="create-post-form__privacy-select"
          value={privacy}
          onChange={(e) => setPrivacy(e.target.value as Privacy)}
        >
          <option value="PUBLIC">Công khai</option>
          <option value="FRIENDS">Bạn bè</option>
          <option value="PRIVATE">Chỉ mình tôi</option>
        </select>

        <button
          type="button"
          className="create-post-form__attach-btn"
          onClick={() => fileInputRef.current?.click()}
        >
          📎
        </button>
        <input
          ref={fileInputRef}
          type="file"
          multiple
          accept="image/*,video/*"
          className="create-post-form__file-input"
          onChange={handleFileChange}
        />

        <button
          type="submit"
          className="create-post-form__submit-btn"
          disabled={createPost.isPending || (!content.trim() && selectedFiles.length === 0)}
        >
          {createPost.isPending ? 'Đang đăng...' : 'Đăng'}
        </button>
      </div>
    </form>
  )
}