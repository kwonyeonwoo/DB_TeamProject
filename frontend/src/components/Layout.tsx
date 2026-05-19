import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/useAuth'
import { NotificationPopup } from './NotificationPopup'

const authenticatedNavItems = [
  { to: '/', label: '홈' },
  { to: '/posts', label: '게시글' },
  { to: '/schedule', label: '일정' },
  { to: '/groups', label: '그룹' },
]

const guestNavItems = [
  { to: '/login', label: '로그인' },
  { to: '/signup', label: '회원가입' },
]

export function Layout() {
  const navigate = useNavigate()
  const { logout, status, user } = useAuth()
  const navItems =
    status === 'authenticated'
      ? [
          ...authenticatedNavItems,
          ...(user?.role === 'ADMIN'
            ? [{ to: '/admin/reports', label: '신고관리' }]
            : []),
        ]
      : guestNavItems

  const handleLogout = async () => {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-inner">
          <NavLink to="/" className="brand">
            학업자료공유
          </NavLink>
          <nav aria-label="주요 메뉴">
            <ul className="nav-list">
              {navItems.map((item) => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    className={({ isActive }) =>
                      isActive ? 'nav-link active' : 'nav-link'
                    }
                    end={item.to === '/'}
                  >
                    {item.label}
                  </NavLink>
                </li>
              ))}
            </ul>
          </nav>
          {status === 'authenticated' && (
            <div className="auth-summary">
              <NotificationPopup />
              <span>{user?.name}님</span>
              <button className="text-button" type="button" onClick={handleLogout}>
                로그아웃
              </button>
            </div>
          )}
        </div>
      </header>
      <main className="page-container">
        <Outlet />
      </main>
    </div>
  )
}
