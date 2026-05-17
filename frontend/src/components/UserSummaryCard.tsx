import { useState } from 'react'
import { useAuth } from '../contexts/useAuth'
import { ProfileModal } from './ProfileModal'

interface UserSummaryCardProps {
  className?: string
}

export function UserSummaryCard({ className = '' }: UserSummaryCardProps) {
  const { user } = useAuth()
  const [isProfileOpen, setIsProfileOpen] = useState(false)

  if (!user) {
    return null
  }

  const roleLabel = user.role === 'ADMIN' ? '관리자' : '일반 사용자'

  return (
    <>
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
        <button
          className="button secondary"
          type="button"
          onClick={() => setIsProfileOpen(true)}
        >
          마이페이지
        </button>
      </aside>
      {isProfileOpen && <ProfileModal onClose={() => setIsProfileOpen(false)} />}
    </>
  )
}
