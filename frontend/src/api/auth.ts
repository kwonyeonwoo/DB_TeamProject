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

    const response = await apiClient.get<User>('/users/me')
    return response.data
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

    const response = await apiClient.post<LoginResponse>('/auth/login', request)
    return response.data.user
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

    await apiClient.post('/auth/logout')
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

    const response = await apiClient.patch<User>('/users/me', request)
    return response.data
  },

  async deleteMe() {
    if (useMockApi) {
      await waitMockResponse()
      writeMockUser(null)
      return
    }

    await apiClient.delete('/users/me')
  },
}
