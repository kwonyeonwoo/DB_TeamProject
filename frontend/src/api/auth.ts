import axios from 'axios'
import { apiClient, useMockApi } from './client'

export type UserRole = 'USER' | 'ADMIN'

export interface User {
  id: number
  login_id: string
  name: string
  email_address: string
  role: UserRole
  status?: 'ACTIVE' | 'DELETED'
}

export interface LoginRequest {
  login_id: string
  password: string
}

export interface SignupRequest {
  login_id: string
  password: string
  name: string
  email_address: string
}

export interface UpdateMeRequest {
  name?: string
  email_address?: string
  current_password?: string
  new_password?: string
}

interface LoginResponse {
  user: User
}

const mockUserKey = 'db-teamproject:mock-user'
const demoUserKey = 'db-teamproject:demo-user'
const useDemoAuthFallback =
  import.meta.env.DEV && import.meta.env.VITE_ENABLE_DEMO_AUTH_FALLBACK !== 'false'

const demoAccounts: Record<string, { password: string; user: User }> = {
  admin: {
    password: 'admin123',
    user: {
      id: 1,
      login_id: 'admin',
      name: '관리자',
      email_address: 'admin@example.com',
      role: 'ADMIN',
      status: 'ACTIVE',
    },
  },
  user1: {
    password: 'user123',
    user: {
      id: 2,
      login_id: 'user1',
      name: '테스트 사용자 1',
      email_address: 'user1@example.com',
      role: 'USER',
      status: 'ACTIVE',
    },
  },
  user2: {
    password: 'user123',
    user: {
      id: 3,
      login_id: 'user2',
      name: '테스트 사용자 2',
      email_address: 'user2@example.com',
      role: 'USER',
      status: 'ACTIVE',
    },
  },
  user3: {
    password: 'user123',
    user: {
      id: 4,
      login_id: 'user3',
      name: '테스트 사용자 3',
      email_address: 'user3@example.com',
      role: 'USER',
      status: 'ACTIVE',
    },
  },
}

function readMockUser() {
  const rawUser = sessionStorage.getItem(mockUserKey)
  return rawUser ? (JSON.parse(rawUser) as User) : null
}

function writeMockUser(user: User | null) {
  if (user) {
    sessionStorage.setItem(mockUserKey, JSON.stringify(user))
    return
  }

  sessionStorage.removeItem(mockUserKey)
}

function readDemoUser() {
  const rawUser = sessionStorage.getItem(demoUserKey)
  return rawUser ? (JSON.parse(rawUser) as User) : null
}

function writeDemoUser(user: User | null) {
  if (user) {
    sessionStorage.setItem(demoUserKey, JSON.stringify(user))
    return
  }

  sessionStorage.removeItem(demoUserKey)
}

function isBackendUnavailable(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return false
  }

  return !error.response || (error.response.status >= 500 && error.response.status < 600)
}

function loginDemoUser(request: LoginRequest) {
  const account = demoAccounts[request.login_id]

  if (!account || account.password !== request.password) {
    throw new Error('임시 계정 정보가 올바르지 않습니다.')
  }

  writeDemoUser(account.user)
  return account.user
}

function createMockUser(request: SignupRequest): User {
  return {
    id: Date.now(),
    login_id: request.login_id,
    name: request.name,
    email_address: request.email_address,
    role: request.login_id === 'admin' ? 'ADMIN' : 'USER',
    status: 'ACTIVE',
  }
}

async function waitMockResponse() {
  await new Promise((resolve) => {
    window.setTimeout(resolve, 180)
  })
}

export const authApi = {
  async getMe() {
    if (useMockApi) {
      await waitMockResponse()
      const user = readMockUser()

      if (!user) {
        throw new Error('로그인이 필요합니다.')
      }

      return user
    }

    try {
      const response = await apiClient.get<User>('/users/me')
      writeDemoUser(null)
      return response.data
    } catch (error) {
      const demoUser = readDemoUser()

      if (useDemoAuthFallback && demoUser && isBackendUnavailable(error)) {
        return demoUser
      }

      throw error
    }
  },

  async login(request: LoginRequest) {
    if (useMockApi) {
      await waitMockResponse()

      if (!request.login_id || !request.password) {
        throw new Error('아이디와 비밀번호를 입력해 주세요.')
      }

      const user: User = {
        id: request.login_id === 'admin' ? 1 : 2,
        login_id: request.login_id,
        name: request.login_id === 'admin' ? '관리자' : '테스트 사용자',
        email_address: `${request.login_id}@example.com`,
        role: request.login_id === 'admin' ? 'ADMIN' : 'USER',
        status: 'ACTIVE',
      }
      writeMockUser(user)
      return user
    }

    try {
      const response = await apiClient.post<LoginResponse>('/auth/login', request)
      writeDemoUser(null)
      return response.data.user
    } catch (error) {
      if (useDemoAuthFallback && isBackendUnavailable(error)) {
        return loginDemoUser(request)
      }

      throw error
    }
  },

  async signup(request: SignupRequest) {
    if (useMockApi) {
      await waitMockResponse()

      if (!request.login_id || !request.password || !request.name || !request.email_address) {
        throw new Error('회원가입 정보를 모두 입력해 주세요.')
      }

      return createMockUser(request)
    }

    const response = await apiClient.post<User>('/auth/signup', request)
    return response.data
  },

  async logout() {
    if (useMockApi) {
      await waitMockResponse()
      writeMockUser(null)
      return
    }

    try {
      await apiClient.post('/auth/logout')
    } finally {
      writeDemoUser(null)
    }
  },

  async updateMe(request: UpdateMeRequest) {
    if (useMockApi) {
      await waitMockResponse()
      const user = readMockUser()

      if (!user) {
        throw new Error('로그인이 필요합니다.')
      }

      const nextUser = {
        ...user,
        name: request.name ?? user.name,
        email_address: request.email_address ?? user.email_address,
      }
      writeMockUser(nextUser)
      return nextUser
    }

    try {
      const response = await apiClient.patch<User>('/users/me', request)
      writeDemoUser(null)
      return response.data
    } catch (error) {
      const demoUser = readDemoUser()

      if (useDemoAuthFallback && demoUser && isBackendUnavailable(error)) {
        const nextUser = {
          ...demoUser,
          name: request.name ?? demoUser.name,
          email_address: request.email_address ?? demoUser.email_address,
        }
        writeDemoUser(nextUser)
        return nextUser
      }

      throw error
    }
  },

  async deleteMe() {
    if (useMockApi) {
      await waitMockResponse()
      writeMockUser(null)
      return
    }

    try {
      await apiClient.delete('/users/me')
    } finally {
      writeDemoUser(null)
    }
  },
}
