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
  commonStats:  StatEntry[]
  weekStats:    StatEntry[]
  monthStats:   StatEntry[]
  yearStats:    StatEntry[]
  allTimeStats: StatEntry[]
}

const ADMIN_QUERY_KEYS = {
  userStats: ['admin', 'stats', 'users'] as const,
  postStats: ['admin', 'stats', 'posts'] as const,
}

const PERIOD_COLORS: Record<string, string> = {
  'Tuần này':       'text-primary',
  'Tháng này':      'text-secondary',
  'Năm nay':        'text-tertiary',
  'Toàn thời gian': 'text-on-surface',
  'Tổng quan':      'text-on-surface-variant',
}

const StatCard = ({ label, count, period }: StatEntry) => (
  <div className="flex flex-col gap-2 p-5 bg-surface-container-low rounded-2xl hover:bg-surface-container-high transition-colors">
    <p className={`text-[0.6875rem] font-bold uppercase tracking-widest ${PERIOD_COLORS[period] ?? 'text-on-surface-variant'}`}>
      {period}
    </p>
    <p className="text-sm text-on-surface-variant font-medium">{label}</p>
    <p
      className="text-3xl font-extrabold text-on-surface"
      style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
    >
      {count.toLocaleString('vi-VN')}
    </p>
  </div>
)

const StatSection = ({ title, entries }: { title: string; entries: StatEntry[] }) => {
  if (!entries.length) return null
  return (
    <div className="flex flex-col gap-3">
      <h3 className="text-sm font-bold text-on-surface-variant uppercase tracking-widest">{title}</h3>
      <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-3">
        {entries.map((entry, i) => <StatCard key={i} {...entry} />)}
      </div>
    </div>
  )
}

const SectionBlock = ({
  title,
  icon,
  data,
}: {
  title: string
  icon:  string
  data:  Statistics | undefined
}) => (
  <div className="flex flex-col gap-5">
    <div className="flex items-center gap-3">
      <div className="w-10 h-10 rounded-2xl bg-primary/10 flex items-center justify-center">
        <span className="material-symbols-outlined text-primary">{icon}</span>
      </div>
      <h2
        className="text-2xl font-extrabold text-on-surface"
        style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
      >
        {title}
      </h2>
    </div>

    {data ? (
      <div className="flex flex-col gap-6">
        <StatSection title="Tổng quan"      entries={data.commonStats ?? []} />
        <StatSection title="Tuần này"       entries={data.weekStats ?? []} />
        <StatSection title="Tháng này"      entries={data.monthStats ?? []} />
        <StatSection title="Năm nay"        entries={data.yearStats ?? []} />
        <StatSection title="Toàn thời gian" entries={data.allTimeStats ?? []} />
      </div>
    ) : (
      <div className="flex items-center gap-3 px-4 py-3 bg-error/10 rounded-xl text-error text-sm font-medium">
        <span className="material-symbols-outlined text-sm">warning</span>
        Không tải được dữ liệu
      </div>
    )}
  </div>
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
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    )
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-8 pb-24 md:pb-8 flex flex-col gap-10">
      {/* Header */}
      <div>
        <h1
          className="text-4xl font-extrabold tracking-tight text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Thống kê hệ thống
        </h1>
        <p className="mt-1 text-on-surface-variant text-sm">Tổng quan hoạt động của nền tảng</p>
      </div>

      {/* Divider */}
      <div className="h-px bg-outline-variant/30" />

      <SectionBlock title="Người dùng" icon="group"   data={userStats.data} />
      <div className="h-px bg-outline-variant/20" />
      <SectionBlock title="Bài viết"   icon="article" data={postStats.data} />
    </div>
  )
}