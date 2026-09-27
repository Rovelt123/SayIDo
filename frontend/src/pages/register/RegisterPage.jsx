import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { saveSession } from '../../utils/storage'
import styles from './RegisterPage.module.css'

import { API_BASE } from "../../utils/api";

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

function validatePassword(password) {
  if (password.length < 8 || password.length > 30) {
    return 'The password must be between 8 and 30 characters'
  }

  if (password.search(/[a-z]/) < 0) {
    return 'The password must contain a lowercase letter'
  }

  if (password.search(/[A-Z]/) < 0) {
    return 'The password must contain an uppercase letter'
  }

  if (password.search(/[^A-Za-z0-9]/) < 0) {
    return 'The password must contain a special character'
  }

  return ''
}

// ________________________________________________________

function RegisterPage() {
  const [form, setForm] = useState({
    first_name: '',
    last_name: '',
    email: '',
    password: '',
    repeat_password: '',
  })
  const [error, setError] = useState('')
  const [passwordError, setPasswordError] = useState('')
  const [loading, setLoading] = useState(false)

  const navigate = useNavigate()

  // ________________________________________________________

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  // ________________________________________________________

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setPasswordError('')

    const pwError = validatePassword(form.password)

    if (pwError) {
      setPasswordError(pwError)
      return
    }

    if (form.password !== form.repeat_password) {
      setPasswordError('The passwords do not match')
      return
    }

    setLoading(true)

    try {
      const response = await fetch(`${API_BASE}/users/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })

      if (!response.ok) {
        const text = await response.text()
        setError(parseErrorMessage(text) || 'Registration failed')
        return
      }

      const result = await response.json()

      saveSession(result.data.token, result.data.data, true)
      navigate('/homepage')
    } catch {
      setError('Could not connect to the server')
    } finally {
      setLoading(false)
    }
  }

  // ________________________________________________________

  return (
    <div className={styles.registerPage}>
      <div className={styles.registerDecor} aria-hidden="true"></div>

      <Link className={styles.registerWordmark} to="/">
        <img src="/logo.svg" alt="" />
        <span>Say <em>I Do</em></span>
      </Link>

      <form className={styles.registerCard} onSubmit={handleSubmit}>
        <h1>Create account</h1>
        <p className={styles.registerSubtitle}>Start planning your big day</p>

        {error && <p className={styles.registerError}>{error}</p>}

        <div className={styles.registerRow}>
          <div>
            <label htmlFor="first_name">First name</label>
            <input
              id="first_name"
              name="first_name"
              type="text"
              value={form.first_name}
              onChange={handleChange}
              required
            />
          </div>

          <div>
            <label htmlFor="last_name">Last name</label>
            <input
              id="last_name"
              name="last_name"
              type="text"
              value={form.last_name}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <label htmlFor="email">Email</label>
        <input
          id="email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          required
        />

        {passwordError && <p className={styles.registerError}>{passwordError}</p>}

        <label htmlFor="password">Password (8-30 characters, upper and lower case, one special character)</label>
        <input
          id="password"
          name="password"
          type="password"
          value={form.password}
          minLength={8}
          maxLength={30}
          onChange={handleChange}
          required
        />

        <label htmlFor="repeat_password">Repeat password</label>
        <input
          id="repeat_password"
          name="repeat_password"
          type="password"
          value={form.repeat_password}
          maxLength={30}
          onChange={handleChange}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Creating account...' : 'Create account'}
        </button>
      </form>

      <p className={styles.registerSecondary}>
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </div>
  )
}

export default RegisterPage
