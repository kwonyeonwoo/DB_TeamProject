import { createContext } from 'react'
import type { LoginRequest, SignupRequest, UpdateMeRequest, User } from '../api/auth'

export type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated'

export interface AuthContextValue {
  user: User | null
  status: AuthStatus
  errorMessage: string | null
  clearError: () => void
  refreshMe: () => Promise<void>
  login: (request: LoginRequest) => Promise<void>
  signup: (request: SignupRequest) => Promise<void>
  logout: () => Promise<void>
  updateMe: (request: UpdateMeRequest) => Promise<void>
  deleteMe: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
