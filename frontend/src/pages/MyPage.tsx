import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/useAuth'

export function MyPage() {
  const navigate = useNavigate()
  const { deleteMe, errorMessage, updateMe, user } = useAuth()
  const [name, setName] = useState(user?.name ?? '')
  const [emailAddress, setEmailAddress] = useState(user?.email_address ?? '')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setSuccessMessage(null)

    try {
      await updateMe({
        name,
        email_address: emailAddress,
        current_password: currentPassword || undefined,
        new_password: newPassword || undefined,
      })
      setCurrentPassword('')
      setNewPassword('')
      setSuccessMessage('내 정보가 수정되었습니다.')
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
      navigate('/login', { replace: true })
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <>
      <section className="page-header">
        <p className="eyebrow">내 정보</p>
        <h1 className="page-title">마이페이지</h1>
        <p className="page-description">
          내 프로필을 확인하고 수정하는 화면입니다. 회원 탈퇴 시 개인 일정과
          그룹 참여 정보는 문서에 정의된 정책에 따라 처리됩니다.
        </p>
      </section>

      <section className="profile-layout">
        <form className="form-panel" onSubmit={handleSubmit}>
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

        <aside className="panel profile-summary">
          <h2>계정 상태</h2>
          <dl className="info-list">
            <div>
              <dt>역할</dt>
              <dd>{user?.role}</dd>
            </div>
            <div>
              <dt>상태</dt>
              <dd>{user?.status ?? 'ACTIVE'}</dd>
            </div>
          </dl>
        </aside>
      </section>
    </>
  )
}
