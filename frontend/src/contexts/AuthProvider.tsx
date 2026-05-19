import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { authApi } from '../api/auth'
import { getErrorMessage } from '../api/errors'
import type { LoginRequest, SignupRequest, UpdateMeRequest, User } from '../api/auth'
import { AuthContext } from './authContext'
import type { AuthStatus } from './authContext'

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null)
  const [status, setStatus] = useState<AuthStatus>('loading')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const clearError = useCallback(() => {
    setErrorMessage(null)
  }, [])

  const refreshMe = useCallback(async () => {
    try {
      const currentUser = await authApi.getMe()
      setUser(currentUser)
      setStatus('authenticated')
    } catch {
      setUser(null)
      setStatus('unauthenticated')
    }
  }, [])

  useEffect(() => {
    let isMounted = true

    authApi
      .getMe()
      .then((currentUser) => {
        if (!isMounted) {
          return
        }

        setUser(currentUser)
        setStatus('authenticated')
      })
      .catch(() => {
        if (!isMounted) {
          return
        }

        setUser(null)
        setStatus('unauthenticated')
      })

    return () => {
      isMounted = false
    }
  }, [])

  const login = useCallback(async (request: LoginRequest) => {
    try {
      setErrorMessage(null)
      const currentUser = await authApi.login(request)
      setUser(currentUser)
      setStatus('authenticated')
    } catch (error) {
      setStatus('unauthenticated')
      setErrorMessage(getErrorMessage(error))
      throw error
    }
  }, [])

  const signup = useCallback(async (request: SignupRequest) => {
    try {
      setErrorMessage(null)
      await authApi.signup(request)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
      throw error
    }
  }, [])

  const logout = useCallback(async () => {
    try {
      setErrorMessage(null)
      await authApi.logout()
    } finally {
      setUser(null)
      setStatus('unauthenticated')
    }
  }, [])

  const updateMe = useCallback(async (request: UpdateMeRequest) => {
    try {
      setErrorMessage(null)
      const currentUser = await authApi.updateMe(request)
      setUser(currentUser)
      setStatus('authenticated')
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
      throw error
    }
  }, [])

  const deleteMe = useCallback(async () => {
    try {
      setErrorMessage(null)
      await authApi.deleteMe()
    } finally {
      setUser(null)
      setStatus('unauthenticated')
    }
  }, [])

  const value = useMemo(
    () => ({
      user,
      status,
      errorMessage,
      clearError,
      refreshMe,
      login,
      signup,
      logout,
      updateMe,
      deleteMe,
    }),
    [
      user,
      status,
      errorMessage,
      clearError,
      refreshMe,
      login,
      signup,
      logout,
      updateMe,
      deleteMe,
    ],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
