import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import AuthStatusCard from '../../components/AuthStatusCard/AuthStatusCard'
import Navbar from "../../components/Navbar/Navbar.jsx"
import styles from './ConfirmEmailPage.module.css'

import { API_BASE } from "../../utils/api";

// ________________________________________________________

function parseResponseMessage(text) {
  try {
    const parsed = JSON.parse(text)

    if (typeof parsed === 'string') {
      return parsed
    }

    return parsed.message || text
  } catch {
    return text
  }
}

// ________________________________________________________

function ConfirmEmailPage() {
  const [searchParams] = useSearchParams()

  const [status, setStatus] = useState('loading')
  const [message, setMessage] = useState('Confirming your email address...')

  const [showResendForm, setShowResendForm] = useState(false)
  const [email, setEmail] = useState('')
  const [resendLoading, setResendLoading] = useState(false)
  const [resendMessage, setResendMessage] = useState('')
  const [resendError, setResendError] = useState('')

  const token = searchParams.get('token')

  // ________________________________________________________

  useEffect(() => {
    const confirmEmail = async () => {
      if (!token) {
        setStatus('error')
        setMessage('The confirmation token is missing.')
        return
      }

      try {
        const response = await fetch(
          `${API_BASE}/users/auth/confirm-email?token=${encodeURIComponent(token)}`,
          {
            method: 'GET',
            headers: {
              Accept: 'application/json',
            },
          },
        )

        const text = await response.text()
        const responseMessage = parseResponseMessage(text)

        if (!response.ok) {
          setStatus('error')
          setMessage(
            responseMessage ||
            'The confirmation link is invalid or has expired.'
          )
          return
        }

        setStatus('success')
        setMessage(responseMessage || 'Your email has been confirmed.')
      } catch {
        setStatus('error')
        setMessage('Could not connect to the server. Please try again.')
      }
    }

    confirmEmail()
  }, [token])

  // ________________________________________________________

  const handleResendEmail = async (e) => {
    e.preventDefault()

    setResendError('')
    setResendMessage('')

    if (!email.trim()) {
      setResendError('Please enter your email address.')
      return
    }

    setResendLoading(true)

    try {
      const response = await fetch(
        `${API_BASE}/users/auth/resend-confirmation`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
          body: JSON.stringify({
            email: email.trim(),
          }),
        },
      )

      const text = await response.text()
      const responseMessage = parseResponseMessage(text)

      if (!response.ok) {
        setResendError(
          responseMessage ||
          'Could not resend the confirmation email.'
        )
        return
      }

      setResendMessage(
        responseMessage ||
        'If the email address belongs to an unverified account, we will send a verification email.'
      )

      setEmail('')
    } catch {
      setResendError('Could not connect to the server. Please try again.')
    } finally {
      setResendLoading(false)
    }
  }

  // ________________________________________________________

  return (
    <div className={styles.confirmPage}>
      <div className={styles.confirmDecor} />

      <Navbar hideButtons />

      <div className={styles.confirmContent}>
        <AuthStatusCard
          status={status}
          message={message}
        />

        {status === 'success' && (
          <p className={styles.confirmSecondary}>
            Your account is ready. <Link to="/login">Log in</Link>
          </p>
        )}

        {status === 'error' && (
          <>
            {!showResendForm && (
              <p className={styles.confirmSecondary}>
                Having trouble?{' '}
                <button
                  type="button"
                  className={styles.resendLink}
                  onClick={() => setShowResendForm(true)}
                >
                  Resend email
                </button>
              </p>
            )}

            {showResendForm && (
              <form className={styles.resendForm} onSubmit={handleResendEmail}>
                <label htmlFor="resendEmail">Email address</label>

                <input
                  id="resendEmail"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                />

                {resendError && (
                  <p className={styles.resendError}>
                    {resendError}
                  </p>
                )}

                {resendMessage && (
                  <p className={styles.resendSuccess}>{resendMessage}</p>
                )}

                <button type="submit" disabled={resendLoading}>
                  {resendLoading
                    ? 'Sending...'
                    : 'Send new confirmation email'
                  }
                </button>
              </form>
            )}
          </>
        )}
      </div>
    </div>
)
}

export default ConfirmEmailPage
