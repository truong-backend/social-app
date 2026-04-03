import { Link } from 'react-router-dom'

export const NotFoundPage = () => (
  <div className="min-h-screen bg-background flex flex-col items-center justify-center px-4 relative overflow-hidden">
    {/* Decorative blobs */}
    <div className="absolute top-[-15%] left-[-10%] w-[600px] h-[600px] rounded-full bg-gradient-to-br from-primary/10 to-tertiary/5 blur-3xl pointer-events-none" />
    <div className="absolute bottom-[-10%] right-[-10%] w-[400px] h-[400px] rounded-full bg-gradient-to-tl from-secondary/10 to-primary-container/10 blur-3xl pointer-events-none" />

    <div className="relative z-10 flex flex-col items-center gap-6 text-center max-w-sm">
      {/* Large 404 */}
      <div className="relative">
        <p
          className="text-[160px] font-extrabold leading-none select-none"
          style={{
            fontFamily: "'Plus Jakarta Sans', sans-serif",
            background: 'linear-gradient(135deg, #4647d3 0%, #9396ff 50%, #963776 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
          }}
        >
          404
        </p>
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
          <span className="material-symbols-outlined text-5xl text-on-surface/10">search_off</span>
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <h1
          className="text-2xl font-extrabold text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Trang không tồn tại
        </h1>
        <p className="text-on-surface-variant text-sm leading-relaxed">
          Trang bạn đang tìm kiếm có thể đã bị xóa, đổi tên hoặc tạm thời không khả dụng.
        </p>
      </div>

      <Link
        to="/feed"
        className="flex items-center gap-2 px-8 py-3 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary font-bold shadow-lg active:scale-95 transition-all"
      >
        <span className="material-symbols-outlined text-lg">home</span>
        Về trang chủ
      </Link>
    </div>
  </div>
)