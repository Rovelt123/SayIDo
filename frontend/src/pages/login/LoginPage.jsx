import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { saveSession } from '../../utils/storage'
import styles from './LoginPage.module.css'

const API_BASE = 'https://sys2.roneu.dk/api'


// ________________________________________________________

function parseErrorMessage(text) {
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

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(true)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const navigate = useNavigate()

  // ________________________________________________________

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await fetch(`${API_BASE}/users/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      if (!response.ok) {
        const text = await response.text()
        setError(parseErrorMessage(text) || 'Login failed')
        return
      }

      const result = await response.json()

      saveSession(result.data.token, result.data.data, remember)
      navigate('/homepage')
    } catch {
      setError('Could not connect to the server')
    } finally {
      setLoading(false)
    }
  }

  // ________________________________________________________

  return (
    <div className={styles.loginPage}>
      <div className={styles.loginDecor} aria-hidden="true"></div>

      <Link className={styles.loginWordmark} to="/">
        <img src="/logo.svg" alt="" />
        <span>Say <em>I Do</em></span>
      </Link>

      <form className={styles.loginCard} onSubmit={handleSubmit}>
        <h1>Log in</h1>
        <p className={styles.loginSubtitle}>Welcome back</p>

        {error && <p className={styles.loginError}>{error}</p>}

        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <label className={styles.loginRemember}>
          <input
            type="checkbox"
            checked={remember}
            onChange={(e) => setRemember(e.target.checked)}
          />
          Remember me on this device
        </label>

        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Log in'}
        </button>
      </form>

      <p className={styles.loginSecondary}>
        No account yet? <Link to="/register">Register</Link>
      </p>
    </div>
  )
}

export default LoginPage
