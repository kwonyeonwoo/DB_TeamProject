import { apiClient, useMockApi } from './client'
import type { Schedule, SaveScheduleRequest, ScheduleListResponse } from './schedules'

export interface Group {
  id: number
  group_code: string
  name: string
  leader_id: number
  created_at: string
}

export interface GroupMember {
  group_id: number
  user_id: number
  role: 'LEADER' | 'MEMBER'
  joined_at: string
  user_name?: string
}

export interface GroupListResponse {
  items: Group[]
}

export interface GroupDetailResponse {
  group: Group
  members: GroupMember[]
}

export interface CreateGroupResponse {
  group: Group
  membership: GroupMember
}

const currentUserId = 2
const mockGroupKey = 'db-teamproject:mock-groups'
const mockGroupMemberKey = 'db-teamproject:mock-group-members'
const mockGroupScheduleKey = 'db-teamproject:mock-group-schedules'

const seedGroups: Group[] = [
  {
    id: 1,
    group_code: 'DB2026',
    name: 'DB 기초 스터디',
    leader_id: 2,
    created_at: '2026-05-10T09:00:00',
  },
  {
    id: 2,
    group_code: 'TEAM-A',
    name: '팀 프로젝트 A',
    leader_id: 3,
    created_at: '2026-05-11T14:20:00',
  },
]

const seedMembers: GroupMember[] = [
  {
    group_id: 1,
    user_id: 2,
    role: 'LEADER',
    joined_at: '2026-05-10T09:00:00',
    user_name: '테스트 사용자',
  },
  {
    group_id: 1,
    user_id: 3,
    role: 'MEMBER',
    joined_at: '2026-05-10T09:30:00',
    user_name: '김수현',
  },
  {
    group_id: 2,
    user_id: 2,
    role: 'MEMBER',
    joined_at: '2026-05-11T15:00:00',
    user_name: '테스트 사용자',
  },
  {
    group_id: 2,
    user_id: 3,
    role: 'LEADER',
    joined_at: '2026-05-11T14:20:00',
    user_name: '김수현',
  },
]

const seedGroupSchedules: Schedule[] = [
  {
    id: 101,
    user_id: 2,
    group_id: 1,
    title: '정규화 발표 준비',
    start_at: '2026-05-17T13:00:00',
    end_at: '2026-05-17T15:00:00',
    description: '각자 맡은 정규화 파트 발표 자료 점검',
    type: 4,
    created_at: '2026-05-10T12:00:00',
    updated_at: null,
  },
  {
    id: 102,
    user_id: 2,
    group_id: 2,
    title: '프론트/백엔드 연동 회의',
    start_at: '2026-05-19T16:00:00',
    end_at: '2026-05-19T17:00:00',
    description: 'API 응답 필드와 화면 연결 방식 확인',
    type: 4,
    created_at: '2026-05-11T16:00:00',
    updated_at: null,
  },
]

function readStorage<T>(key: string, seed: T[]) {
  const rawItems = localStorage.getItem(key)

  if (!rawItems) {
    localStorage.setItem(key, JSON.stringify(seed))
    return seed
  }

  return JSON.parse(rawItems) as T[]
}

function writeStorage<T>(key: string, items: T[]) {
  localStorage.setItem(key, JSON.stringify(items))
}

function readMockGroups() {
  return readStorage<Group>(mockGroupKey, seedGroups)
}

function writeMockGroups(groups: Group[]) {
  writeStorage(mockGroupKey, groups)
}

function readMockMembers() {
  return readStorage<GroupMember>(mockGroupMemberKey, seedMembers)
}

function writeMockMembers(members: GroupMember[]) {
  writeStorage(mockGroupMemberKey, members)
}

function readMockGroupSchedules() {
  return readStorage<Schedule>(mockGroupScheduleKey, seedGroupSchedules)
}

function writeMockGroupSchedules(schedules: Schedule[]) {
  writeStorage(mockGroupScheduleKey, schedules)
}

async function waitMockResponse() {
  await new Promise((resolve) => {
    window.setTimeout(resolve, 180)
  })
}

function assertGroupMember(groupId: number) {
  const member = readMockMembers().find(
    (item) => item.group_id === groupId && item.user_id === currentUserId,
  )

  if (!member) {
    throw new Error('그룹 멤버만 접근할 수 있습니다.')
  }
}

function assertValidGroupSchedule(request: SaveScheduleRequest) {
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

function overlapsRange(schedule: Schedule, startAt?: string, endAt?: string) {
  const scheduleStart = new Date(schedule.start_at).getTime()
  const scheduleEnd = new Date(schedule.end_at).getTime()
  const filterStart = startAt ? new Date(startAt).getTime() : null
  const filterEnd = endAt ? new Date(endAt).getTime() : null

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

function createGroupCode(groupId: number) {
  return `GROUP-${String(groupId).padStart(3, '0')}`
}

export const groupsApi = {
  async listGroups() {
    if (useMockApi) {
      await waitMockResponse()
      const myGroupIds = new Set(
        readMockMembers()
          .filter((member) => member.user_id === currentUserId)
          .map((member) => member.group_id),
      )
      return {
        items: readMockGroups().filter((group) => myGroupIds.has(group.id)),
      } satisfies GroupListResponse
    }

    const response = await apiClient.get<GroupListResponse>('/groups')
    return response.data
  },

  async createGroup(name: string) {
    if (useMockApi) {
      await waitMockResponse()

      if (!name.trim()) {
        throw new Error('그룹명을 입력해 주세요.')
      }

      const groups = readMockGroups()
      const members = readMockMembers()
      const now = new Date().toISOString()
      const group: Group = {
        id: Math.max(0, ...groups.map((item) => item.id)) + 1,
        group_code: createGroupCode(Math.max(0, ...groups.map((item) => item.id)) + 1),
        name,
        leader_id: currentUserId,
        created_at: now,
      }
      const membership: GroupMember = {
        group_id: group.id,
        user_id: currentUserId,
        role: 'LEADER',
        joined_at: now,
        user_name: '테스트 사용자',
      }

      writeMockGroups([...groups, group])
      writeMockMembers([...members, membership])
      return { group, membership } satisfies CreateGroupResponse
    }

    const response = await apiClient.post<CreateGroupResponse>('/groups', { name })
    return response.data
  },

  async joinGroup(groupCode: string) {
    if (useMockApi) {
      await waitMockResponse()
      const group = readMockGroups().find((item) => item.group_code === groupCode)

      if (!group) {
        throw new Error('유효하지 않은 그룹 코드입니다.')
      }

      const members = readMockMembers()
      const alreadyJoined = members.some(
        (member) => member.group_id === group.id && member.user_id === currentUserId,
      )

      if (alreadyJoined) {
        throw new Error('이미 참여 중인 그룹입니다.')
      }

      const membership: GroupMember = {
        group_id: group.id,
        user_id: currentUserId,
        role: 'MEMBER',
        joined_at: new Date().toISOString(),
        user_name: '테스트 사용자',
      }
      writeMockMembers([...members, membership])
      return membership
    }

    const response = await apiClient.post<GroupMember>('/groups/join', {
      group_code: groupCode,
    })
    return response.data
  },

  async getGroup(groupId: number) {
    if (useMockApi) {
      await waitMockResponse()
      assertGroupMember(groupId)
      const group = readMockGroups().find((item) => item.id === groupId)

      if (!group) {
        throw new Error('그룹을 찾을 수 없습니다.')
      }

      return {
        group,
        members: readMockMembers().filter((member) => member.group_id === groupId),
      } satisfies GroupDetailResponse
    }

    const response = await apiClient.get<GroupDetailResponse>(`/groups/${groupId}`)
    return response.data
  },

  async listGroupSchedules(groupId: number, params: { start_at?: string; end_at?: string } = {}) {
    if (useMockApi) {
      await waitMockResponse()
      assertGroupMember(groupId)
      const items = readMockGroupSchedules()
        .filter(
          (schedule) =>
            schedule.group_id === groupId &&
            overlapsRange(schedule, params.start_at, params.end_at),
        )
        .sort(
          (left, right) =>
            new Date(left.start_at).getTime() - new Date(right.start_at).getTime(),
        )
      return { items } satisfies ScheduleListResponse
    }

    const response = await apiClient.get<ScheduleListResponse>(
      `/groups/${groupId}/schedules`,
      { params },
    )
    return response.data
  },

  async createGroupSchedule(groupId: number, request: SaveScheduleRequest) {
    if (useMockApi) {
      await waitMockResponse()
      assertGroupMember(groupId)
      assertValidGroupSchedule(request)
      const schedules = readMockGroupSchedules()
      const schedule: Schedule = {
        id: Math.max(100, ...schedules.map((item) => item.id)) + 1,
        user_id: currentUserId,
        group_id: groupId,
        title: request.title,
        start_at: request.start_at,
        end_at: request.end_at,
        description: request.description?.trim() || null,
        type: request.type,
        created_at: new Date().toISOString(),
        updated_at: null,
      }
      writeMockGroupSchedules([...schedules, schedule])
      return schedule
    }

    const response = await apiClient.post<Schedule>(
      `/groups/${groupId}/schedules`,
      request,
    )
    return response.data
  },

  async updateGroupSchedule(
    groupId: number,
    scheduleId: number,
    request: SaveScheduleRequest,
  ) {
    if (useMockApi) {
      await waitMockResponse()
      assertGroupMember(groupId)
      assertValidGroupSchedule(request)
      const schedules = readMockGroupSchedules()
      const schedule = schedules.find(
        (item) => item.id === scheduleId && item.group_id === groupId,
      )

      if (!schedule) {
        throw new Error('그룹 일정을 찾을 수 없습니다.')
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
      writeMockGroupSchedules(
        schedules.map((item) => (item.id === scheduleId ? nextSchedule : item)),
      )
      return nextSchedule
    }

    const response = await apiClient.patch<Schedule>(
      `/groups/${groupId}/schedules/${scheduleId}`,
      request,
    )
    return response.data
  },

  async deleteGroupSchedule(groupId: number, scheduleId: number) {
    if (useMockApi) {
      await waitMockResponse()
      assertGroupMember(groupId)
      writeMockGroupSchedules(
        readMockGroupSchedules().filter(
          (schedule) => !(schedule.id === scheduleId && schedule.group_id === groupId),
        ),
      )
      return
    }

    await apiClient.delete(`/groups/${groupId}/schedules/${scheduleId}`)
  },
}
