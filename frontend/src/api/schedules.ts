import { apiClient, useMockApi } from './client'

export interface Schedule {
  id: number
  user_id: number
  group_id: number | null
  title: string
  start_at: string
  end_at: string
  description: string | null
  type: number
  created_at: string
  updated_at: string | null
}

export interface ScheduleListResponse {
  items: Schedule[]
}

export interface ListSchedulesParams {
  start_at?: string
  end_at?: string
}

export interface SaveScheduleRequest {
  title: string
  start_at: string
  end_at: string
  description?: string
  type: number
}

const mockScheduleKey = 'db-teamproject:mock-schedules'

const seedSchedules: Schedule[] = [
  {
    id: 1,
    user_id: 2,
    group_id: null,
    title: '데이터베이스 과제 제출',
    start_at: '2026-05-15T18:00:00',
    end_at: '2026-05-15T19:00:00',
    description: '정규화 보고서와 SQL 실습 파일 제출',
    type: 1,
    created_at: '2026-05-10T09:00:00',
    updated_at: null,
  },
  {
    id: 2,
    user_id: 2,
    group_id: null,
    title: 'SQL 실습 퀴즈 준비',
    start_at: '2026-05-18T10:00:00',
    end_at: '2026-05-18T12:00:00',
    description: 'JOIN, GROUP BY 문제 다시 풀기',
    type: 2,
    created_at: '2026-05-11T13:00:00',
    updated_at: null,
  },
  {
    id: 3,
    user_id: 2,
    group_id: null,
    title: '프로젝트 자료 정리',
    start_at: '2026-05-20T14:00:00',
    end_at: '2026-05-20T16:00:00',
    description: null,
    type: 3,
    created_at: '2026-05-12T11:00:00',
    updated_at: null,
  },
]

function readMockSchedules() {
  const rawSchedules = localStorage.getItem(mockScheduleKey)

  if (!rawSchedules) {
    localStorage.setItem(mockScheduleKey, JSON.stringify(seedSchedules))
    return seedSchedules
  }

  return JSON.parse(rawSchedules) as Schedule[]
}

function writeMockSchedules(schedules: Schedule[]) {
  localStorage.setItem(mockScheduleKey, JSON.stringify(schedules))
}

async function waitMockResponse() {
  await new Promise((resolve) => {
    window.setTimeout(resolve, 180)
  })
}

function assertValidSchedule(request: SaveScheduleRequest) {
  if (!request.title || !request.start_at || !request.end_at || !request.type) {
    throw new Error('일정 제목, 시작, 종료, 유형을 모두 입력해 주세요.')
  }

  if (request.type < 1 || request.type > 5) {
    throw new Error('일정 유형은 1부터 5 사이여야 합니다.')
  }

  if (new Date(request.end_at).getTime() < new Date(request.start_at).getTime()) {
    throw new Error('종료 시간은 시작 시간보다 빠를 수 없습니다.')
  }
}

function overlapsRange(schedule: Schedule, params: ListSchedulesParams) {
  const scheduleStart = new Date(schedule.start_at).getTime()
  const scheduleEnd = new Date(schedule.end_at).getTime()
  const filterStart = params.start_at ? new Date(params.start_at).getTime() : null
  const filterEnd = params.end_at ? new Date(params.end_at).getTime() : null

  if (filterStart !== null && filterEnd !== null) {
    return scheduleStart <= filterEnd && scheduleEnd >= filterStart
  }

  if (filterStart !== null) {
    return scheduleEnd >= filterStart
  }

  if (filterEnd !== null) {
    return scheduleStart <= filterEnd
  }

  return true
}

function sortSchedules(schedules: Schedule[]) {
  return [...schedules].sort(
    (left, right) =>
      new Date(left.start_at).getTime() - new Date(right.start_at).getTime(),
  )
}

export const schedulesApi = {
  async listSchedules(params: ListSchedulesParams = {}) {
    if (useMockApi) {
      await waitMockResponse()
      const items = sortSchedules(
        readMockSchedules().filter(
          (schedule) => schedule.group_id === null && overlapsRange(schedule, params),
        ),
      )
      return { items } satisfies ScheduleListResponse
    }

    const response = await apiClient.get<ScheduleListResponse>('/me/schedules', {
      params,
    })
    return response.data
  },

  async createSchedule(request: SaveScheduleRequest) {
    if (useMockApi) {
      await waitMockResponse()
      assertValidSchedule(request)
      const schedules = readMockSchedules()
      const now = new Date().toISOString()
      const schedule: Schedule = {
        id: Math.max(0, ...schedules.map((item) => item.id)) + 1,
        user_id: 2,
        group_id: null,
        title: request.title,
        start_at: request.start_at,
        end_at: request.end_at,
        description: request.description?.trim() || null,
        type: request.type,
        created_at: now,
        updated_at: null,
      }

      writeMockSchedules([...schedules, schedule])
      return schedule
    }

    const response = await apiClient.post<Schedule>('/me/schedules', request)
    return response.data
  },

  async updateSchedule(scheduleId: number, request: SaveScheduleRequest) {
    if (useMockApi) {
      await waitMockResponse()
      assertValidSchedule(request)
      const schedules = readMockSchedules()
      const schedule = schedules.find((item) => item.id === scheduleId)

      if (!schedule) {
        throw new Error('일정을 찾을 수 없습니다.')
      }

      const nextSchedule: Schedule = {
        ...schedule,
        title: request.title,
        start_at: request.start_at,
        end_at: request.end_at,
        description: request.description?.trim() || null,
        type: request.type,
        updated_at: new Date().toISOString(),
      }

      writeMockSchedules(
        schedules.map((item) => (item.id === scheduleId ? nextSchedule : item)),
      )
      return nextSchedule
    }

    const response = await apiClient.patch<Schedule>(
      `/me/schedules/${scheduleId}`,
      request,
    )
    return response.data
  },

  async deleteSchedule(scheduleId: number) {
    if (useMockApi) {
      await waitMockResponse()
      writeMockSchedules(
        readMockSchedules().filter((schedule) => schedule.id !== scheduleId),
      )
      return
    }

    await apiClient.delete(`/me/schedules/${scheduleId}`)
  },
}
