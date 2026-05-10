"use client"
import { useEffect, useState, useCallback, useRef, useMemo } from "react"
import { useRouter } from "next/navigation"
import PostCard from "@/components/social-app-component/PostCard"
import api from "@/utils/axios"
import toast from "react-hot-toast"
import usePostActions from "@/hooks/usePostAction"
import PostSkeleton from "@/components/social-app-component/PostCardSkeleton"
import { pageMetadata, usePageMetadata } from "@/utils/clientMetadata"

export default function HomePage() {
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [hasMore, setHasMore] = useState(true)
  const [skip, setSkip] = useState(0)
  const [currentUser, setCurrentUser] = useState(null)
  const [filterType, setFilterType] = useState("RELEVANT")
  const containerRef = useRef(null)
  const abortControllerRef = useRef(null)
  const isInitialLoadRef = useRef(true)
  const isMountedRef = useRef(true)
  const router = useRouter()

  const loadMoreRef = useRef(null)
  const observerRef = useRef(null)

  const LIMIT = 20

  const { toggleLike } = usePostActions({ posts, setPosts })

  usePageMetadata(pageMetadata.home())

  useEffect(() => {
    isMountedRef.current = true
    return () => {
      isMountedRef.current = false
      if (abortControllerRef.current) {
        abortControllerRef.current.abort()
      }
      if (observerRef.current) {
        observerRef.current.disconnect()
      }
    }
  }, [])

  useEffect(() => {
    if (typeof window !== "undefined") {
      const storedUsername = localStorage.getItem("userName")
      const storedUserId = localStorage.getItem("userId")
      if (storedUsername && storedUserId) {
        setCurrentUser({ username: storedUsername, id: storedUserId })
      }
    }
  }, [])

  const filteredPosts = useMemo(() => {
    if (!posts.length || !currentUser) return []
    return posts.filter((post) => {
      if (post.author?.username === currentUser.username || post.author?.id === currentUser.id) return true
      switch (post.privacy) {
        case "PUBLIC": return true
        case "FRIEND": return post.author?.isFriend === true
        case "PRIVATE": return false
        default: return true
      }
    })
  }, [posts, currentUser])

  const fetchPosts = useCallback(
    async (skipValue = 0, isLoadMore = false, type = filterType) => {
      if (!isMountedRef.current) return
      try {
        if (abortControllerRef.current) abortControllerRef.current.abort()
        abortControllerRef.current = new AbortController()
        if (isLoadMore) setLoadingMore(true)
        else setLoading(true)

        const res = await api.get(`/v1/posts/newsfeed?skip=${skipValue}&limit=${LIMIT}&type=${type}`, {
          signal: abortControllerRef.current.signal,
        })

        if (!isMountedRef.current) return

        const newPosts = res.data.body || []
        if (newPosts.length === 0 || newPosts.length < LIMIT) setHasMore(false)

        if (isLoadMore) {
          setPosts((prevPosts) => {
            const existingIds = new Set(prevPosts.map((p) => p.id))
            const uniqueNewPosts = newPosts.filter((p) => !existingIds.has(p.id))
            return [...prevPosts, ...uniqueNewPosts]
          })
          setSkip((prevSkip) => prevSkip + newPosts.length)
        } else {
          setPosts(newPosts)
          setSkip(newPosts.length)
        }

        if (isInitialLoadRef.current) isInitialLoadRef.current = false
      } catch (err) {
        if (err.name !== "AbortError" && isMountedRef.current && !isInitialLoadRef.current) {
          console.error("Failed to fetch newsfeed:", err)
          toast.error("Failed to load posts.")
        }
      } finally {
        if (isMountedRef.current) {
          setLoading(false)
          setLoadingMore(false)
        }
      }
    },
    [filterType]
  )

  const handleFilterChange = useCallback(
    (newType) => {
      if (newType === filterType) return
      setFilterType(newType)
      setPosts([])
      setSkip(0)
      setHasMore(true)
      isInitialLoadRef.current = true
      if (currentUser) fetchPosts(0, false, newType)
    },
    [filterType, currentUser, fetchPosts]
  )

  const handleIntersection = useCallback(
    (entries) => {
      const [entry] = entries
      if (entry.isIntersecting && hasMore && !loadingMore && !loading && currentUser) {
        fetchPosts(skip, true)
      }
    },
    [hasMore, loadingMore, loading, currentUser, skip, fetchPosts]
  )

  useEffect(() => {
    if (!loadMoreRef.current) return
    if (observerRef.current) observerRef.current.disconnect()
    observerRef.current = new IntersectionObserver(handleIntersection, {
      root: null,
      rootMargin: "100px",
      threshold: 0.1,
    })
    observerRef.current.observe(loadMoreRef.current)
    return () => { if (observerRef.current) observerRef.current.disconnect() }
  }, [handleIntersection])

  useEffect(() => {
    if (currentUser && isInitialLoadRef.current && isMountedRef.current) {
      fetchPosts(0, false)
    }
  }, [currentUser, fetchPosts])

  const loadingSkeletons = useMemo(
    () => Array.from({ length: 3 }).map((_, index) => <PostSkeleton key={index} />),
    []
  )

  const loadingMoreSkeletons = useMemo(
    () => Array.from({ length: 3 }).map((_, index) => <PostSkeleton key={`loading-${index}`} />),
    []
  )

  // Instagram-style filter tabs
  const FilterToggle = useMemo(() => {
    const filters = [
      { key: "RELEVANT", label: "Dành cho bạn" },
      { key: "TIME", label: "Mới nhất" },
      { key: "FRIEND_ONLY", label: "Bạn bè" },
    ]
    return (
      <div className="w-full border-b border-[var(--border)] mb-0">
        <div className="flex">
          {filters.map((filter) => (
            <button
              key={filter.key}
              onClick={() => handleFilterChange(filter.key)}
              className={`flex-1 py-3 text-sm font-semibold transition-all duration-200 relative ${
                filterType === filter.key
                  ? "text-[var(--foreground)]"
                  : "text-[var(--muted-foreground)] hover:text-[var(--foreground)]"
              }`}
            >
              {filter.label}
              {filterType === filter.key && (
                <div className="absolute bottom-0 left-0 right-0 h-[1px] bg-[var(--foreground)]" />
              )}
            </button>
          ))}
        </div>
      </div>
    )
  }, [filterType, handleFilterChange])

  const renderContent = useMemo(() => {
    if (!currentUser) {
      return (
        <div className="space-y-0 w-full flex flex-col items-center">
          {loadingSkeletons}
        </div>
      )
    }

    if (loading) {
      return (
        <div className="space-y-0 w-full flex flex-col items-center">
          {loadingSkeletons}
        </div>
      )
    }

    if (filteredPosts.length > 0) {
      return (
        <>
          {filteredPosts.map((post, index) => (
            <PostCard
              key={post.id}
              post={post}
              liked={post.liked}
              likeCount={post.likeCount}
              onLikeToggle={() => toggleLike(post.id)}
              isOwnPost={post.author?.username === currentUser?.username || post.author?.id === currentUser?.id}
              isPriority={index < 3}
            />
          ))}
          {hasMore && (
            <div ref={loadMoreRef} className="w-full flex justify-center py-6">
              {loadingMore && <div className="w-full space-y-0">{loadingMoreSkeletons}</div>}
            </div>
          )}
          {!hasMore && filteredPosts.length > 0 && (
            <div className="flex justify-center py-10">
              <div className="text-center">
                <div className="w-14 h-14 rounded-full border-2 border-[var(--border)] flex items-center justify-center mx-auto mb-3">
                  <svg className="w-7 h-7 text-[var(--muted-foreground)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <p className="text-sm font-semibold text-[var(--foreground)]">Bạn đã xem hết rồi</p>
                <p className="text-xs text-[var(--muted-foreground)] mt-1">Hãy theo dõi thêm để xem nội dung mới</p>
              </div>
            </div>
          )}
        </>
      )
    }

    if (posts.length > 0) {
      return (
        <div className="flex flex-col items-center justify-center py-16 px-4">
          <div className="text-center max-w-sm">
            <div className="w-16 h-16 rounded-full border-2 border-[var(--border)] flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-[var(--muted-foreground)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <h3 className="text-base font-semibold text-[var(--foreground)] mb-1">Không có bài viết hiển thị</h3>
            <p className="text-sm text-[var(--muted-foreground)]">Bài viết bị ẩn do cài đặt quyền riêng tư.</p>
          </div>
        </div>
      )
    }

    return (
      <div className="flex flex-col items-center justify-center py-16 px-4">
        <div className="text-center max-w-sm">
          <div className="w-16 h-16 rounded-full border-2 border-[var(--border)] flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-[var(--muted-foreground)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <h3 className="text-base font-semibold text-[var(--foreground)] mb-1">Chưa có bài viết</h3>
          <p className="text-sm text-[var(--muted-foreground)]">Theo dõi bạn bè hoặc đăng bài để bắt đầu.</p>
        </div>
      </div>
    )
  }, [
    loading,
    filteredPosts,
    posts.length,
    loadingMore,
    hasMore,
    currentUser,
    loadingSkeletons,
    loadingMoreSkeletons,
    toggleLike,
  ])

  return (
    <div ref={containerRef} className="flex flex-col items-center w-full">
      {FilterToggle}
      <div className="w-full">
        {renderContent}
      </div>
    </div>
  )
}