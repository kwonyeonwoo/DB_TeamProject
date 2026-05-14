import { useState } from 'react'
import { Link } from 'react-router-dom'
import { getErrorMessage } from '../api/errors'
import { notificationsApi } from '../api/notifications'
import type { Notification } from '../api/notifications'

export function NotificationPopup() {
  const [isOpen, setIsOpen] = useState(false)
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const handleToggle = async () => {
    const nextOpen = !isOpen
    setIsOpen(nextOpen)

    if (!nextOpen) {
      return
    }

    setIsLoading(true)

    try {
      const response = await notificationsApi.listNotifications()
      setNotifications(response.items)
      setErrorMessage(null)
    } catch (error) {
      setErrorMessage(getErrorMessage(error))
    } finally {
      setIsLoading(false)
    }
  }

  const unreadCount = notifications.filter((notification) => !notification.is_read).length

  return (
    <div className="notification-wrap">
      <button className="text-button" type="button" onClick={handleToggle}>
        알림{unreadCount > 0 ? ` ${unreadCount}` : ''}
      </button>
      {isOpen && (
        <section className="notification-popover" aria-label="알림 목록">
          <div className="notification-header">
            <strong>알림</strong>
            <button className="text-button" type="button" onClick={() => setIsOpen(false)}>
              닫기
            </button>
          </div>
          {isLoading && <p className="muted">알림을 불러오고 있습니다.</p>}
          {errorMessage && <p className="form-error">{errorMessage}</p>}
          {!isLoading && notifications.length === 0 && (
            <p className="muted">새 알림이 없습니다.</p>
          )}
          <ul className="notification-list">
            {notifications.map((notification) => (
              <li key={notification.id}>
                <Link
                  to={`/posts/${notification.commented_post_id}${
                    notification.commented_id
                      ? `#comment-${notification.commented_id}`
                      : ''
                  }`}
                  onClick={() => setIsOpen(false)}
                >
                  <span className={notification.is_read ? 'read-dot' : 'unread-dot'} />
                  <span>
                    {notification.commented_id ? '대댓글' : '댓글'}: {notification.comment_content}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}
    </div>
  )
}
