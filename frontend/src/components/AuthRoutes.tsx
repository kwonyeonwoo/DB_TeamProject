import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/useAuth'

interface RouteGuardProps {
  children: ReactNode
}

interface RequireAuthProps extends RouteGuardProps {
  adminOnly?: boolean
}

export function RequireAuth({ children, adminOnly = false }: RequireAuthProps) {
  const location = useLocation()
  const { status, user } = useAuth()

  if (status === 'loading') {
    return <div className="route-state">사용자 정보를 확인하고 있습니다.</div>
  }

  if (status === 'unauthenticated') {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (adminOnly && user?.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  return children
}

export function GuestOnlyRoute({ children }: RouteGuardProps) {
  const { status } = useAuth()

  if (status === 'loading') {
    return <div className="route-state">사용자 정보를 확인하고 있습니다.</div>
  }

  if (status === 'authenticated') {
    return <Navigate to="/" replace />
  }

  return children
}
