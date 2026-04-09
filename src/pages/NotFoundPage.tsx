import { Link } from 'react-router-dom'

export const NotFoundPage = () => (
  <div className="min-h-screen bg-surface flex flex-col items-center justify-center px-4 relative overflow-hidden">
    {/* Decorative blobs */}
    <div className="absolute top-[-15%] left-[-10%] w-[600px] h-[600px] rounded-full bg-primary/10 blur-[120px] pointer-events-none" />
    <div className="absolute bottom-[-10%] right-[-10%] w-[400px] h-[400px] rounded-full bg-secondary/10 blur-[100px] pointer-events-none" />

    <div className="relative z-10 flex flex-col items-center gap-6 text-center max-w-sm">
      {/* Large 404 */}
      <p
        className="text-[160px] font-extrabold leading-none select-none"
        style={{
          fontFamily: "'Plus Jakarta Sans', sans-serif",
          background: 'linear-gradient(135deg, #0058bb 0%, #6c9fff 50%, #3853b7 100%)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          backgroundClip: 'text',
        }}
      >
        404
      </p>

      <div className="flex flex-col gap-2">
        <h1
          className="text-2xl font-extrabold text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Page not found
        </h1>
        <p className="text-on-surface-variant leading-relaxed text-sm">
          The page you're looking for doesn't exist or has been moved.
        </p>
      </div>

      <Link
        to="/feed"
        className="mt-2 px-8 py-3 bg-gradient-to-r from-primary to-primary-container text-on-primary font-bold rounded-xl shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center gap-2"
      >
        <span className="material-symbols-outlined text-sm">home</span>
        Back to Feed
      </Link>
    </div>
  </div>
)
