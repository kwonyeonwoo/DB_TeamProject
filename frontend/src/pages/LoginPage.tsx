import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/useAuth'

interface LocationState {
  from?: {
    pathname?: string
  }
  signupComplete?: boolean
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { errorMessage, login } = useAuth()
  const locationState = location.state as LocationState | null
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)

    try {
      await login({
        login_id: loginId,
        password,
      })
      navigate(locationState?.from?.pathname ?? '/', { replace: true })
    } catch {
      setIsSubmitting(false)
    }
  }

  return (
    <>
      <form className="auth-card" onSubmit={handleSubmit}>
        {locationState?.signupComplete && (
          <p className="form-success">회원가입이 완료되었습니다. 로그인해 주세요.</p>
        )}
        {errorMessage && <p className="form-error">{errorMessage}</p>}
        <div className="field-stack">
          <label className="field">
            아이디
            <input
              type="text"
              name="login_id"
              placeholder="아이디"
              value={loginId}
              onChange={(event) => setLoginId(event.target.value)}
              autoComplete="username"
              required
            />
          </label>
          <label className="field">
            비밀번호
            <input
              type="password"
              name="password"
              placeholder="비밀번호"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </label>
        </div>
        <div className="button-row">
          <button className="button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '로그인 중' : '로그인'}
          </button>
          <Link className="button secondary" to="/signup">
            회원가입
          </Link>
        </div>
      </form>
    </>
  )
}
