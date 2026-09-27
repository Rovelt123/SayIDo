import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { getToken } from '../../utils/storage'
import styles from './CreateWeddingPage.module.css'

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

function CreateWeddingPage() {
  const [form, setForm] = useState({
    title: '',
    date: '',
    location: '',
    budget: '',
    description: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const navigate = useNavigate()

  const today = new Date().toISOString().slice(0, 10)

  // ________________________________________________________

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  // ________________________________________________________

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    const body = {
      title: form.title,
      date: form.date,
      location: form.location,
      description: form.description,
    }

    body.budget = form.budget === '' ? '0' : form.budget

    try {
      const response = await fetch(`${API_BASE}/weddings`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify(body),
      })

      if (!response.ok) {
        const text = await response.text()
        setError(parseErrorMessage(text) || 'Could not create the wedding')
        return
      }

      navigate('/homepage')
    } catch {
      setError('Could not connect to the server')
    } finally {
      setLoading(false)
    }
  }

  // ________________________________________________________

  return (
    <div className={styles.createPage}>
      <div className={styles.createDecor} aria-hidden="true"></div>

      <Link className={styles.createWordmark} to="/homepage">
        <img src="/logo.svg" alt="" />
        <span>Say <em>I Do</em></span>
      </Link>

      <form className={styles.createCard} onSubmit={handleSubmit}>
        <h1>Create your wedding</h1>

        {error && <p className={styles.createError}>{error}</p>}

        <label htmlFor="title">Title</label>
        <input
          id="title"
          name="title"
          type="text"
          value={form.title}
          onChange={handleChange}
          required
        />

        <label htmlFor="date">Date</label>
        <input
          id="date"
          name="date"
          type="date"
          value={form.date}
          onChange={handleChange}
          min={today}
          required
        />

        <label htmlFor="location">Location</label>
        <input
          id="location"
          name="location"
          type="text"
          value={form.location}
          onChange={handleChange}
        />

        <label htmlFor="budget">Budget</label>
        <input
          id="budget"
          name="budget"
          type="number"
          min="0"
          step="1"
          value={form.budget}
          onChange={handleChange}
        />

        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          name="description"
          rows="4"
          value={form.description}
          onChange={handleChange}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Creating...' : 'Create wedding'}
        </button>
      </form>
    </div>
  )
}

export default CreateWeddingPage
