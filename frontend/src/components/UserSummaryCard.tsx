import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/useAuth'

interface UserSummaryCardProps {
  className?: string
}

export function UserSummaryCard({ className = '' }: UserSummaryCardProps) {
  const { user } = useAuth()

  if (!user) {
    return null
  }

  const roleLabel = user.role === 'ADMIN' ? '관리자' : '일반 사용자'

  return (
    <aside className={`user-summary-card ${className}`.trim()}>
      <div className="user-avatar" aria-hidden="true">
        {user.name.slice(0, 1)}
      </div>
      <div className="user-summary-body">
        <span className="user-role-badge">{roleLabel}</span>
        <strong>{user.name}</strong>
        <span>{user.login_id}</span>
        <span>{user.email_address}</span>
      </div>
      <Link className="button secondary" to="/mypage">
        마이페이지
      </Link>
    </aside>
  )
}
