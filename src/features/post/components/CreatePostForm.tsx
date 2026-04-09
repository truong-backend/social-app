import { useState, useRef } from "react";
import { useCreatePost } from "../hooks/useCreatePost";
import type { Privacy } from "@/types/api.types";

export const CreatePostForm = () => {
  const [content, setContent] = useState("");
  const [privacy, setPrivacy] = useState<Privacy>("PUBLIC");
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const createPost = useCreatePost();

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!content.trim() && selectedFiles.length === 0) return;

    createPost.mutate(
      { payload: { content, privacy }, files: selectedFiles },
      {
        onSuccess: () => {
          setContent("");
          setSelectedFiles([]);
        },
      },
    );
  };

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? []);
    setSelectedFiles((previous) => [...previous, ...files]);
  };

  const removeFile = (index: number) => {
    setSelectedFiles((previous) => previous.filter((_, i) => i !== index));
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="flex gap-4">
        <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center font-bold flex-shrink-0">
          <span className="material-symbols-outlined">person</span>
        </div>
        <div className="flex-1">
          <textarea
            className="w-full bg-surface-container-low border-none rounded-xl p-4 text-on-surface placeholder:text-on-surface-variant/50 focus:ring-2 focus:ring-primary-fixed transition-all resize-none h-24 outline-none"
            placeholder="What's on your mind?"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={3}
          />

          {/* File previews */}
          {selectedFiles.length > 0 && (
            <div className="flex flex-wrap gap-2 mb-3 mt-2">
              {selectedFiles.map((file, index) => (
                <span
                  key={index}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-surface-container-high text-on-surface text-xs rounded-full font-medium"
                >
                  {file.name}
                  <button
                    type="button"
                    onClick={() => removeFile(index)}
                    className="ml-1 text-on-surface-variant hover:text-error"
                  >
                    <span className="material-symbols-outlined text-sm">
                      close
                    </span>
                  </button>
                </span>
              ))}
            </div>
          )}

          {/* Privacy selector */}
          <div className="flex items-center gap-2 mt-3">
            <span className="material-symbols-outlined text-base text-on-surface-variant">
              lock
            </span>
            <select
              value={privacy}
              onChange={(e) => setPrivacy(e.target.value as Privacy)}
              className="text-sm font-medium text-primary bg-surface-container border border-outline-variant/30 rounded-lg px-3 py-1.5 cursor-pointer outline-none focus:ring-2 focus:ring-primary/30 transition-all"
            >
              <option value="PUBLIC">🌐 Công khai</option>
              <option value="FRIENDS">👥 Bạn bè</option>
              <option value="PRIVATE">🔒 Chỉ mình tôi</option>
            </select>

            <div className="flex gap-2">
              <button
                type="button"
                className="p-2 hover:bg-surface-container-high rounded-lg text-primary transition-colors flex items-center gap-2 text-sm font-medium"
                onClick={() => fileInputRef.current?.click()}
              >
                <span className="material-symbols-outlined text-xl">image</span>
                Media
              </button>
            </div>
          </div>

          <div className="flex items-center justify-between mt-3">
            <button
              type="submit"
              className="bg-primary text-white font-bold px-6 py-2 rounded-xl hover:shadow-lg hover:shadow-primary/30 transition-all active:scale-95 disabled:opacity-50 disabled:pointer-events-none"
              disabled={
                createPost.isPending ||
                (!content.trim() && selectedFiles.length === 0)
              }
            >
              {createPost.isPending ? "Đang đăng..." : "Đăng"}
            </button>
          </div>
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="image/*,video/*"
        className="hidden"
        onChange={handleFileChange}
      />
    </form>
  );
};
