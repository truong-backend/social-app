import { Link } from 'react-router-dom'

export const NotFoundPage = () => (
  <div className="not-found-page">
    <h1 className="not-found-page__code">404</h1>
    <p className="not-found-page__message">Trang bạn tìm không tồn tại</p>
    <Link to="/feed" className="not-found-page__home-link">
      Về trang chủ
    </Link>
  </div>
)