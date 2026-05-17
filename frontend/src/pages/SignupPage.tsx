import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/useAuth'

export function SignupPage() {
  const navigate = useNavigate()
  const { errorMessage, signup } = useAuth()
  const [loginId, setLoginId] = useState('')
  const [name, setName] = useState('')
  const [emailAddress, setEmailAddress] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)

    try {
      await signup({
        login_id: loginId,
        password,
        name,
        email_address: emailAddress,
      })
      navigate('/login', { replace: true, state: { signupComplete: true } })
    } catch {
      setIsSubmitting(false)
    }
  }

  return (
    <>
      <form className="auth-card" onSubmit={handleSubmit}>
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
            이름
            <input
              type="text"
              name="name"
              placeholder="이름"
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoComplete="name"
              required
            />
          </label>
          <label className="field">
            이메일
            <input
              type="email"
              name="email_address"
              placeholder="student@example.com"
              value={emailAddress}
              onChange={(event) => setEmailAddress(event.target.value)}
              autoComplete="email"
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
              autoComplete="new-password"
              required
            />
          </label>
        </div>
        <div className="button-row">
          <button className="button" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '가입 중' : '가입하기'}
          </button>
        </div>
      </form>
    </>
  )
}
