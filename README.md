# social-app-ui — Frontend
> Đề tài: **Xây dựng website mạng xã hội**  
> Người hướng dẫn: ThS. Khuất Bá Duy Lâm  
> Sinh viên thực hiện: **Nguyễn Thanh Trường**

---

## Giới thiệu

Frontend của ứng dụng mạng xã hội, xây dựng bằng **React 19 + Vite + TypeScript** theo kiến trúc **Feature-Sliced Design (FSD)**. Ứng dụng cung cấp trải nghiệm mạng xã hội đầy đủ: news feed, bài viết, bình luận, nhắn tin realtime, thông báo realtime và quản lý bạn bè — hướng đến người dùng Việt Nam với giao diện tối giản, thân thiện.

---

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Framework | React 19 |
| Build tool | Vite 8 |
| Ngôn ngữ | TypeScript 5.9 |
| Routing | React Router v7 |
| Server state | TanStack Query (React Query) v5 |
| Client state | Zustand v5 + Immer |
| HTTP Client | Axios + axios-retry |
| Form validation | React Hook Form + Zod v4 |
| Styling | Tailwind CSS 3 + SCSS Modules |
| WebSocket | STOMP.js + SockJS |
| Date | date-fns |
| Infinite scroll | react-intersection-observer |
| Toast | react-hot-toast |
| Video call | ZegoCloud UIKit |
| Linting | ESLint 9 |
| IDE | Visual Studio Code |

---

## Kiến trúc

Dự án áp dụng **Feature-Sliced Design (FSD)** với sự phân tách rõ ràng giữa server state (TanStack Query), client state (Zustand) và side effects (WebSocket).

### Cấu trúc thư mục

```
src/
├── pages/                  # Route-level components
│   ├── FeedPage.tsx
│   ├── ProfilePage.tsx
│   ├── EditProfilePage.tsx
│   ├── PostDetailPage.tsx
│   ├── MessagesPage.tsx
│   ├── FriendsPage.tsx
│   ├── SearchPage.tsx
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── ConfirmEmailPage.tsx
│   ├── ResetPasswordPage.tsx
│   ├── AdminPage.tsx
│   └── NotFoundPage.tsx
│
├── layouts/
│   ├── MainLayout.tsx       # Layout chính (sidebar, navbar)
│   └── AuthLayout.tsx       # Layout trang đăng nhập/đăng ký
│
├── features/               # Domain features (FSD)
│   ├── auth/
│   │   ├── api/
│   │   │   ├── auth.api.ts          # logout, confirmEmail, resetPassword
│   │   │   ├── login.api.ts         # login
│   │   │   └── register.api.ts      # register
│   │   ├── hooks/
│   │   │   ├── useAuthMutations.ts  # useMutation wrappers
│   │   │   └── useLogin.ts
│   │   ├── types/auth.types.ts
│   │   ├── constants/auth.constants.ts
│   │   └── index.ts
│   │
│   ├── post/
│   │   ├── api/
│   │   │   ├── createPost.api.ts
│   │   │   ├── getPost.api.ts
│   │   │   ├── updatePost.api.ts
│   │   │   └── postActions.api.ts   # like, unlike, share, delete
│   │   ├── components/
│   │   │   ├── PostCard.tsx
│   │   │   ├── PostFeed.tsx
│   │   │   ├── CreatePostForm.tsx
│   │   │   └── SharePostModal.tsx
│   │   ├── hooks/
│   │   │   ├── useInfiniteFeed.ts   # Infinite scroll feed
│   │   │   ├── usePost.ts
│   │   │   ├── usePostsByAuthor.ts
│   │   │   ├── useCreatePost.ts
│   │   │   └── useDeletePost.ts
│   │   ├── types/post.types.ts
│   │   └── index.ts
│   │
│   ├── comment/
│   │   ├── api/comment.api.ts
│   │   ├── components/CommentItem.tsx, CommentList.tsx
│   │   ├── hooks/useComments.ts, useCreateComment.ts,
│   │   │         useUpdateComment.ts, useDeleteComment.ts
│   │   ├── types/comment.types.ts
│   │   └── index.ts
│   │
│   ├── like/
│   │   ├── api/like.api.ts
│   │   ├── hooks/useLikePost.ts, useLikeComment.ts
│   │   ├── types/like.types.ts
│   │   └── index.ts
│   │
│   ├── chat/
│   │   ├── api/chat.api.ts
│   │   ├── components/
│   │   │   ├── ChatSidebar.tsx      # Danh sách đoạn chat
│   │   │   ├── ChatWindow.tsx       # Cửa sổ chat
│   │   │   └── MessageBubble.tsx
│   │   ├── hooks/
│   │   │   ├── useChatList.ts
│   │   │   ├── useChatMessages.ts
│   │   │   ├── useChatWebSocket.ts  # WebSocket integration
│   │   │   └── useSendMessage.ts
│   │   ├── store/chat.store.ts      # Zustand store cho realtime messages
│   │   ├── types/chat.types.ts
│   │   └── index.ts
│   │
│   ├── notification/
│   │   ├── api/notification.api.ts
│   │   ├── components/
│   │   │   ├── NotificationBell.tsx     # Icon + badge trên navbar
│   │   │   └── NotificationDropdown.tsx
│   │   ├── hooks/
│   │   │   ├── useNotifications.ts
│   │   │   └── useNotificationWebSocket.ts
│   │   ├── store/notification.store.ts  # Zustand store cho realtime notifications
│   │   ├── types/notification.types.ts
│   │   └── index.ts
│   │
│   ├── relationship/
│   │   ├── api/relationship.api.ts
│   │   ├── hooks/useRelationship.ts     # All relationship mutations & queries
│   │   ├── constants/relationship.constants.ts
│   │   └── index.ts
│   │
│   └── user/
│       ├── api/user.api.ts
│       ├── components/UserAvatar.tsx, UserProfileCard.tsx
│       ├── hooks/useProfile.ts, useSearchUsers.ts, useUpdateProfile.ts
│       ├── types/user.types.ts
│       └── index.ts
│
├── components/             # Shared UI components
│   ├── ui/
│   │   ├── Avatar.tsx
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   └── Textarea.tsx
│   └── feedback/
│       ├── Spinner.tsx
│       └── ErrorBoundary.tsx
│
├── layouts/
│   ├── MainLayout.tsx
│   └── AuthLayout.tsx
│
├── routes/
│   ├── AppRouter.tsx        # Route definitions
│   ├── ProtectedRoute.tsx   # Yêu cầu đăng nhập
│   ├── AdminRoute.tsx       # Yêu cầu role ADMIN
│   └── index.ts
│
├── providers/
│   ├── QueryProvider.tsx    # TanStack Query setup
│   └── WebSocketProvider.tsx # WebSocket lifecycle (connect/disconnect)
│
├── stores/
│   └── session.store.ts     # Zustand: auth session (accountId, userId, role)
│
├── services/
│   ├── axios.instance.ts    # Axios singleton + interceptors
│   └── websocket.service.ts # WebSocket singleton (STOMP client)
│
├── config/
│   ├── environment.ts       # Env variables typed
│   └── query-client.ts      # TanStack Query client config
│
├── hooks/                   # Global custom hooks
│   ├── useDebounce.ts
│   ├── useClickOutside.tsx
│   └── useLocalStorage.ts
│
├── utils/
│   ├── api-response.ts      # unwrapData helper
│   ├── date.formatter.ts    # format date với date-fns
│   └── token.storage.ts     # Centralized localStorage access
│
└── types/
    └── api.types.ts         # Shared API types
```

---

## Chức năng đã thực hiện

### Xác thực
- [x] Đăng ký tài khoản với form validation (React Hook Form + Zod)
- [x] Đăng nhập → lưu token vào `tokenStorage` (centralized localStorage)
- [x] Xác nhận email sau đăng ký (ConfirmEmailPage)
- [x] Quên mật khẩu → nhập mã reset → đổi mật khẩu (ResetPasswordPage)
- [x] Đăng xuất — xóa session Zustand + token localStorage
- [x] Axios interceptor tự động gắn Authorization header + xử lý 401
- [x] axios-retry — tự động thử lại khi mạng lỗi tạm thời

### News Feed & Bài viết
- [x] Infinite scroll news feed (TanStack Query `useInfiniteQuery` + `useInfiniteScrollFeed`)
- [x] Đăng bài viết mới (text + ảnh) — `CreatePostForm`
- [x] Thích / bỏ thích bài viết (optimistic update)
- [x] Chia sẻ bài viết (SharePostModal)
- [x] Xem chi tiết bài viết — `PostDetailPage`
- [x] Chỉnh sửa bài viết (nội dung, quyền riêng tư)
- [x] Xóa bài viết
- [x] Tìm kiếm bài viết (SearchPage)

### Bình luận
- [x] Xem danh sách bình luận (CommentList)
- [x] Đăng bình luận mới
- [x] Trả lời bình luận (nested)
- [x] Thích / bỏ thích bình luận
- [x] Chỉnh sửa bình luận
- [x] Xóa bình luận

### Nhắn tin Realtime
- [x] Danh sách đoạn chat (ChatSidebar)
- [x] Gửi & nhận tin nhắn realtime qua WebSocket (STOMP)
- [x] Hiển thị tin nhắn tức thì không cần reload (Zustand `chat.store`)
- [x] Xóa tin nhắn
- [x] Chỉnh sửa tin nhắn
- [x] WebSocket singleton với pending subscription queue — xử lý race condition khi component subscribe trước khi WS connect

### Thông báo Realtime
- [x] Nhận thông báo realtime qua WebSocket (`useNotificationWebSocket`)
- [x] NotificationBell — icon + badge hiển thị số thông báo chưa đọc
- [x] NotificationDropdown — xem danh sách thông báo
- [x] Zustand `notification.store` — quản lý state thông báo realtime

### Hồ sơ cá nhân
- [x] Xem hồ sơ người dùng — `ProfilePage`
- [x] Xem bài viết của user trên trang cá nhân
- [x] Chỉnh sửa thông tin hồ sơ (EditProfilePage)
- [x] Cập nhật tên, username, ngày sinh, tiểu sử
- [x] Cập nhật ảnh đại diện
- [x] UserProfileCard — component hiển thị tóm tắt hồ sơ

### Tìm kiếm
- [x] Tìm kiếm người dùng theo tên/username (SearchPage)
- [x] useDebounce — tránh gọi API liên tục khi gõ

### Quản lý mối quan hệ
- [x] Xem danh sách bạn bè (FriendsPage)
- [x] Gửi / hủy lời mời kết bạn
- [x] Chấp nhận / từ chối lời mời kết bạn
- [x] Hủy kết bạn
- [x] Chặn / bỏ chặn người dùng
- [x] Xem lời mời đã nhận / đã gửi / đã chặn
- [x] `useRelationship` — hook tổng hợp tất cả mutation & query quan hệ

### Admin
- [x] AdminRoute bảo vệ — redirect nếu không có role ADMIN
- [x] AdminPage — quản trị viên xem thống kê / quản lý người dùng

### Kỹ thuật
- [x] WebSocketProvider — tập trung lifecycle connect/disconnect cho toàn app
- [x] QueryProvider — TanStack Query với staleTime và retry được cấu hình
- [x] `tokenStorage.ts` — tập trung toàn bộ localStorage access (dễ refactor)
- [x] `environment.ts` — typed env variables (tránh truy cập `import.meta.env` trực tiếp)
- [x] `query-client.ts` — query client singleton với cấu hình thống nhất
- [x] ErrorBoundary — bắt lỗi React runtime, tránh crash toàn bộ app
- [x] `vercel.json` — cấu hình rewrite để SPA hoạt động đúng khi deploy Vercel

---

## Cài đặt & Chạy dự án

### Yêu cầu
- Node.js 18+
- npm

### 1. Clone repository

```bash
git clone <repo-url>
cd social-app-ui
```

### 2. Cài đặt dependencies

```bash
npm install
```

### 3. Cấu hình môi trường

Tạo file `.env` ở thư mục gốc:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=http://localhost:8080/ws
VITE_ZEGOCLOUD_APP_ID=your_zegocloud_app_id
```

> ⚠️ **Không commit file `.env` lên Git.** Thêm vào `.gitignore`.

### 4. Chạy development server

```bash
npm run dev
```

Ứng dụng mặc định chạy tại: `http://localhost:5173`

### 5. Build production

```bash
npm run build
```

### 6. Deploy Vercel

File `vercel.json` đã cấu hình rewrite cho SPA routing. Chỉ cần connect repo lên Vercel và set environment variables.

---

## Thông tin tác giả

- **Sinh viên:** Nguyễn Thanh Trường
- **Trường:** ĐH Công Nghệ Sài Gòn — Khoa CNTT
- **Năm học:** 2024–2025
