import { useQuery } from '@tanstack/react-query'
import { axiosInstance } from '@services/axios.instance'
import { unwrapData } from '@utils/api-response'
import { Spinner } from '@components/feedback/Spinner'

interface StatEntry {
  label:  string
  count:  number
  period: string
}

interface Statistics {
  commonStats:    StatEntry[]
  weekStats:      StatEntry[]
  monthStats:     StatEntry[]
  yearStats:      StatEntry[]
  allTimeStats:   StatEntry[]
}

const ADMIN_QUERY_KEYS = {
  userStats: ['admin', 'stats', 'users'] as const,
  postStats: ['admin', 'stats', 'posts'] as const,
}

const StatCard = ({ label, count, period }: StatEntry) => (
  <div className="stat-card">
    <p className="stat-card__period">{period}</p>
    <p className="stat-card__label">{label}</p>
    <p className="stat-card__count">{count.toLocaleString('vi-VN')}</p>
  </div>
)

const StatSection = ({ title, entries }: { title: string; entries: StatEntry[] }) => (
  <section className="admin-page__stat-section">
    <h3 className="admin-page__stat-section-title">{title}</h3>
    <div className="admin-page__stat-grid">
      {entries.map((entry, i) => <StatCard key={i} {...entry} />)}
    </div>
  </section>
)

export const AdminPage = () => {
  const userStats = useQuery({
    queryKey: ADMIN_QUERY_KEYS.userStats,
    queryFn: async () => {
      const res = await axiosInstance.get('/api/admin/statistics/users')
      return unwrapData<Statistics>(res)
    },
  })

  const postStats = useQuery({
    queryKey: ADMIN_QUERY_KEYS.postStats,
    queryFn: async () => {
      const res = await axiosInstance.get('/api/admin/statistics/posts')
      return unwrapData<Statistics>(res)
    },
  })

  if (userStats.isLoading || postStats.isLoading) {
    return <div className="admin-page__loading"><Spinner size="lg" /></div>
  }

  return (
    <div className="admin-page">
      <h1 className="admin-page__title">Thống kê hệ thống</h1>

      {/* User statistics */}
      <div className="admin-page__section">
        <h2 className="admin-page__section-title">👥 Người dùng</h2>
        {userStats.data ? (
          <>
            <StatSection title="Tổng quan"      entries={userStats.data.commonStats ?? []} />
            <StatSection title="Tuần này"       entries={userStats.data.weekStats ?? []} />
            <StatSection title="Tháng này"      entries={userStats.data.monthStats ?? []} />
            <StatSection title="Năm nay"        entries={userStats.data.yearStats ?? []} />
            <StatSection title="Toàn thời gian" entries={userStats.data.allTimeStats ?? []} />
          </>
        ) : (
          <p className="admin-page__error">Không tải được dữ liệu</p>
        )}
      </div>

      {/* Post statistics */}
      <div className="admin-page__section">
        <h2 className="admin-page__section-title">📝 Bài viết</h2>
        {postStats.data ? (
          <>
            <StatSection title="Tổng quan"      entries={postStats.data.commonStats ?? []} />
            <StatSection title="Tuần này"       entries={postStats.data.weekStats ?? []} />
            <StatSection title="Tháng này"      entries={postStats.data.monthStats ?? []} />
            <StatSection title="Năm nay"        entries={postStats.data.yearStats ?? []} />
            <StatSection title="Toàn thời gian" entries={postStats.data.allTimeStats ?? []} />
          </>
        ) : (
          <p className="admin-page__error">Không tải được dữ liệu</p>
        )}
      </div>
    </div>
  )
}