"use client";

import { useState, useRef, useEffect } from "react";
import Modal from "../ui-components/Modal";
import ImagePreview from "../ui-components/ImagePreview";
import toast from "react-hot-toast";
import api from "@/utils/axios";

export default function EditPostModal({ isOpen, onClose, post, onPostUpdated }) {
  const fileInputRef = useRef(null);
  const textareaRef = useRef(null);

  const [newContent, setNewContent] = useState("");
  const [newPrivacy, setNewPrivacy] = useState("PUBLIC");
  const [filesToDelete, setFilesToDelete] = useState([]);
  const [newFiles, setNewFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [zoomIndex, setZoomIndex] = useState(null);

  const handleContentChange = (e) => {
    setNewContent(e.target.value);
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.max(textareaRef.current.scrollHeight, 96)}px`;
    }
  };

  useEffect(() => {
    if (isOpen && post) {
      setNewContent(post.content || "");
      setNewPrivacy(post.privacy || "PUBLIC");
      setFilesToDelete([]);
      setNewFiles([]);
    } else if (!isOpen) {
      setFilesToDelete([]);
      setNewFiles([]);
      setZoomIndex(null);
    }
  }, [isOpen, post]);

  useEffect(() => {
    if (textareaRef.current && newContent) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.max(textareaRef.current.scrollHeight, 96)}px`;
    }
  }, [newContent]);

  const displayMedia = [
    ...(post?.files || [])
      .filter(url => !filesToDelete.includes(url))
      .map(url => ({
        preview: url,
        type: url.includes('.mp4') || url.includes('.webm') || url.includes('.mov') ? "video" : "image",
        isOld: true,
        url: url,
      })),
    ...newFiles.map(fileObj => ({
      preview: fileObj.preview,
      type: fileObj.type,
      isOld: false,
      file: fileObj.file,
    }))
  ];

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files).map((file) => ({
      file,
      preview: URL.createObjectURL(file),
      type: file.type.startsWith("video/") ? "video" : "image",
    }));
    setNewFiles(prev => [...prev, ...files]);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const files = Array.from(e.dataTransfer.files).map((file) => ({
      file,
      preview: URL.createObjectURL(file),
      type: file.type.startsWith("video/") ? "video" : "image",
    }));
    setNewFiles(prev => [...prev, ...files]);
  };

  const handleAddFiles = () => {
    fileInputRef.current?.click();
  };

  const handleRemoveMedia = (index) => {
    const item = displayMedia[index];
    if (item.isOld) {
      setFilesToDelete(prev => [...prev, item.url]);
    } else {
      const newFileIndex = newFiles.findIndex(f => f.preview === item.preview);
      if (newFileIndex !== -1) {
        setNewFiles(prev => prev.filter((_, i) => i !== newFileIndex));
      }
    }
  };

  const handleSaveEdit = async () => {
    setLoading(true);
    try {
      let updatedPost = { ...post };
      const hasContentChange = newContent !== post.content;
      const hasPrivacyChange = newPrivacy !== post.privacy;
      const hasFileChanges = canEditFiles && (filesToDelete.length > 0 || newFiles.length > 0);

      if (hasPrivacyChange) {
        const privacyRes = await api.patch(`/v1/posts/update-privacy/${post.id}?privacy=${newPrivacy}`);
        if (privacyRes.data.code !== 200) throw new Error(privacyRes.data.message || "Lỗi khi cập nhật privacy!");
        updatedPost.privacy = newPrivacy;
      }

      if (hasContentChange || hasFileChanges) {
        const formData = new FormData();
        formData.append("content", newContent);
        if (canEditFiles) {
          filesToDelete.forEach((url) => formData.append("deleteOldFileUrls", url));
          newFiles.forEach((fileObj) => formData.append("newFiles", fileObj.file));
        }
        const contentRes = await api.patch(`/v1/posts/update-content/${post.id}`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        if (contentRes.data.code !== 200) throw new Error(contentRes.data.message || "Lỗi khi cập nhật content!");
        updatedPost.content = newContent;
        if (canEditFiles && hasFileChanges) {
          const remainingOldFiles = (post.files || []).filter(url => !filesToDelete.includes(url));
          const newFilesFromServer = contentRes.data.body?.files || [];
          updatedPost.files = [...remainingOldFiles, ...newFilesFromServer];
        }
      }

      toast.success("Cập nhật bài viết thành công!");
      onPostUpdated?.(updatedPost);
      onClose();
    } catch (error) {
      toast.error(error.message || "Lỗi kết nối hoặc máy chủ.");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const isSharedPost = post?.sharedPost;
  const canEditFiles = !isSharedPost;

  return (
    <>
      <Modal isOpen={isOpen} onClose={onClose}>
        <div className="relative w-full max-w-lg mx-auto px-4 sm:px-5 py-4">

          {/* Header */}
          <div className="flex justify-center items-center mb-4 pb-3 border-b border-[var(--border)]">
            <h2 className="text-[17px] font-bold">
              {isSharedPost ? "Chỉnh sửa bài chia sẻ" : "Chỉnh sửa bài viết"}
            </h2>
          </div>

          {canEditFiles && displayMedia.length === 0 ? (
            <div className="space-y-4">
              {/* Upload area */}
              <div
                onClick={handleAddFiles}
                onDrop={handleDrop}
                onDragOver={(e) => e.preventDefault()}
                className="flex flex-col items-center justify-center border-2 border-dashed border-[var(--border)] rounded-xl p-8 sm:p-10 text-gray-500 hover:border-[var(--primary)] cursor-pointer transition-colors space-y-2"
              >
                <p className="text-sm text-center">Chọn ảnh hoặc video, hoặc kéo thả vào đây</p>
                <div className="text-4xl">📁</div>
                <input type="file" accept="image/*,video/*" multiple ref={fileInputRef} onChange={handleFileChange} hidden />
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">Privacy</label>
                <select
                  value={newPrivacy}
                  onChange={(e) => setNewPrivacy(e.target.value)}
                  className="w-full px-3 py-2 border rounded-md bg-[var(--input)] text-[var(--foreground)]"
                >
                  <option value="PUBLIC">🌍 Public</option>
                  <option value="FRIEND">👥 Friends</option>
                  <option value="PRIVATE">🔒 Only me</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium mb-1">What's on your mind?</label>
                <textarea
                  ref={textareaRef}
                  value={newContent}
                  onChange={handleContentChange}
                  rows={4}
                  placeholder="Viết điều gì đó..."
                  className="w-full px-3 py-2 border rounded-md bg-[var(--input)] text-[var(--foreground)] resize-none overflow-hidden"
                  style={{ minHeight: '96px' }}
                />
              </div>

              <div className="flex justify-end">
                <button
                  onClick={handleSaveEdit}
                  disabled={loading}
                  className="px-6 py-2 rounded-lg font-semibold text-sm bg-[var(--primary)] text-white hover:bg-opacity-90 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? "Đang lưu..." : "💾 Lưu"}
                </button>
              </div>
            </div>
          ) : (
            /* Khi có media: stack dọc mobile, ngang desktop */
            <div className={`flex gap-4 ${canEditFiles && displayMedia.length > 0 ? 'flex-col sm:flex-row' : 'flex-col'}`}>
              {canEditFiles && displayMedia.length > 0 && (
                <div className="w-full sm:w-1/2">
                  <ImagePreview
                    images={displayMedia}
                    onImageClick={(i) => setZoomIndex(i)}
                    onDelete={handleRemoveMedia}
                    onAdd={handleAddFiles}
                  />
                  <input type="file" accept="image/*,video/*" multiple ref={fileInputRef} onChange={handleFileChange} hidden />
                </div>
              )}

              <div className={`${canEditFiles && displayMedia.length > 0 ? 'w-full sm:w-1/2' : 'w-full'} flex flex-col gap-4`}>
                <div>
                  <label className="block text-sm font-medium mb-1">Privacy</label>
                  <select
                    value={newPrivacy}
                    onChange={(e) => setNewPrivacy(e.target.value)}
                    className="w-full px-3 py-2 border rounded-md bg-[var(--input)] text-[var(--foreground)]"
                  >
                    <option value="PUBLIC">🌍 Public</option>
                    <option value="FRIEND">👥 Friends</option>
                    <option value="PRIVATE">🔒 Only me</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-1">
                    {isSharedPost ? "Nội dung chia sẻ" : "Content"}
                  </label>
                  <textarea
                    ref={textareaRef}
                    value={newContent}
                    onChange={handleContentChange}
                    rows={4}
                    placeholder={isSharedPost ? "Bạn muốn nói gì về bài viết này?" : "Viết điều gì đó..."}
                    className="w-full px-3 py-2 border rounded-md bg-[var(--input)] text-[var(--foreground)] resize-none overflow-hidden"
                    style={{ minHeight: '96px' }}
                  />
                </div>

                {/*{isSharedPost && post.originalPost && (...)}*/}

                <div className="flex justify-end mt-auto">
                  <button
                    onClick={handleSaveEdit}
                    disabled={loading}
                    className="px-6 py-2 rounded-lg font-semibold text-sm bg-[var(--primary)] text-white hover:bg-opacity-90 transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? "Đang lưu..." : "💾 Lưu"}
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </Modal>

      {zoomIndex !== null && canEditFiles && (
        <Modal isOpen={zoomIndex !== null} onClose={() => setZoomIndex(null)}>
          <div className="relative w-full h-[80vh] flex items-center justify-center bg-black">
            {displayMedia[zoomIndex]?.type === "video" ? (
              <video src={displayMedia[zoomIndex].preview} className="max-h-full max-w-full" controls autoPlay />
            ) : (
              <img src={displayMedia[zoomIndex].preview} className="max-h-full max-w-full object-contain" alt={`Preview ${zoomIndex}`} />
            )}
          </div>
        </Modal>
      )}
    </>
  );
}