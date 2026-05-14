import { apiClient, useMockApi } from './client'

export interface Notification {
  id: number
  is_read: boolean
  comment_content: string
  commented_post_id: number
  commented_user_id: number
  commented_id: number | null
  created_at: string
}

export interface NotificationListResponse {
  items: Notification[]
}

const mockNotificationKey = 'db-teamproject:mock-notifications'

const seedNotifications: Notification[] = [
  {
    id: 1,
    is_read: false,
    comment_content: 'BCNF 분해 예제도 추가되면 좋겠습니다.',
    commented_post_id: 1,
    commented_user_id: 4,
    commented_id: null,
    created_at: '2026-05-12T10:05:00',
  },
  {
    id: 2,
    is_read: false,
    comment_content: '다음 수정 때 예제를 보강해 보겠습니다.',
    commented_post_id: 1,
    commented_user_id: 2,
    commented_id: 2,
    created_at: '2026-05-12T10:30:00',
  },
]

function readMockNotifications() {
  const rawNotifications = localStorage.getItem(mockNotificationKey)

  if (!rawNotifications) {
    localStorage.setItem(mockNotificationKey, JSON.stringify(seedNotifications))
    return seedNotifications
  }

  return JSON.parse(rawNotifications) as Notification[]
}

function writeMockNotifications(notifications: Notification[]) {
  localStorage.setItem(mockNotificationKey, JSON.stringify(notifications))
}

async function waitMockResponse() {
  await new Promise((resolve) => {
    window.setTimeout(resolve, 180)
  })
}

export const notificationsApi = {
  async listNotifications() {
    if (useMockApi) {
      await waitMockResponse()
      const items = readMockNotifications().sort(
        (left, right) =>
          new Date(right.created_at).getTime() - new Date(left.created_at).getTime(),
      )
      writeMockNotifications(
        items.map((notification) => ({ ...notification, is_read: true })),
      )
      return { items } satisfies NotificationListResponse
    }

    const response = await apiClient.get<NotificationListResponse>('/notifications')
    return response.data
  },
}
