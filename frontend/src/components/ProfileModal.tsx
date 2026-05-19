import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import type { UpdateMeRequest } from '../api/auth'
import { useAuth } from '../contexts/useAuth'

interface ProfileModalProps {
  onClose: () => void
}

export function ProfileModal({ onClose }: ProfileModalProps) {
  const navigate = useNavigate()
  const { clearError, deleteMe, errorMessage, updateMe, user } = useAuth()
  const [name, setName] = useState(user?.name ?? '')
  const [emailAddress, setEmailAddress] = useState(user?.email_address ?? '')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  useEffect(() => {
    clearError()
  }, [clearError])

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    clearError()
    setSuccessMessage(null)

    if (!user) {
      return
    }

    const nextName = name.trim()
    const nextEmailAddress = emailAddress.trim()
    const request: UpdateMeRequest = {}

    if (nextName !== user.name) {
      request.name = nextName
    }

    if (nextEmailAddress !== user.email_address) {
      request.email_address = nextEmailAddress
    }

    if (newPassword) {
      request.current_password = currentPassword
      request.new_password = newPassword
    }

    if (Object.keys(request).length === 0) {
      setSuccessMessage('변경된 정보가 없습니다.')
      return
    }

    setIsSubmitting(true)

    try {
      await updateMe(request)
      setCurrentPassword('')
      setNewPassword('')
      setSuccessMessage('내 정보가 수정되었습니다.')
      window.dispatchEvent(new CustomEvent('profile-updated'))
    } catch {
      setSuccessMessage(null)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async () => {
    const confirmed = window.confirm('회원 탈퇴를 진행할까요?')

    if (!confirmed) {
      return
    }

    setIsDeleting(true)

    try {
      await deleteMe()
      onClose()
      navigate('/login', { replace: true })
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="modal-panel profile-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="profile-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div>
            <p className="eyebrow">내 정보</p>
            <h2 id="profile-modal-title">마이페이지</h2>
          </div>
          <button className="text-button" type="button" onClick={onClose}>
            닫기
          </button>
        </div>

        <form className="profile-modal-form" onSubmit={handleSubmit}>
          {successMessage && <p className="form-success">{successMessage}</p>}
          {errorMessage && <p className="form-error">{errorMessage}</p>}
          <div className="field-stack">
            <label className="field">
              아이디
              <input type="text" value={user?.login_id ?? ''} disabled />
            </label>
            <label className="field">
              이름
              <input
                type="text"
                value={name}
                onChange={(event) => setName(event.target.value)}
                required
              />
            </label>
            <label className="field">
              이메일
              <input
                type="email"
                value={emailAddress}
                onChange={(event) => setEmailAddress(event.target.value)}
                required
              />
            </label>
            <label className="field">
              현재 비밀번호
              <input
                type="password"
                value={currentPassword}
                onChange={(event) => setCurrentPassword(event.target.value)}
                autoComplete="current-password"
              />
            </label>
            <label className="field">
              새 비밀번호
              <input
                type="password"
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
                autoComplete="new-password"
              />
            </label>
          </div>

          <dl className="info-list profile-modal-status">
            <div>
              <dt>역할</dt>
              <dd>{user?.role}</dd>
            </div>
            <div>
              <dt>상태</dt>
              <dd>{user?.status ?? 'ACTIVE'}</dd>
            </div>
          </dl>

          <div className="button-row">
            <button className="button" type="submit" disabled={isSubmitting}>
              {isSubmitting ? '저장 중' : '저장'}
            </button>
            <button
              className="button danger"
              type="button"
              onClick={handleDelete}
              disabled={isDeleting}
            >
              {isDeleting ? '탈퇴 처리 중' : '회원 탈퇴'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
