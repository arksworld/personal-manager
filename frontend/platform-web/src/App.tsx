
import { useState, type FormEvent } from 'react'
import {
  fetchDashboard,
  type BasicCredentials,
  type DashboardFeature,
  type DashboardResponse,
} from './services/dashboardApi'
import './App.css'


function FeatureCard({ feature }: { feature: DashboardFeature }) {
  const urls: Record<string, string | undefined> = {
    PASSWORD_MANAGER: import.meta.env.VITE_PASSWORD_MANAGER_URL,
  }

  const url = urls[feature.code]

  const content = (
      <>
        <div className="feature-icon" aria-hidden="true">
          {feature.code === 'PASSWORD_MANAGER'
              ? '🔐'
              : feature.code === 'STOCK_TRACKER'
                  ? '📈'
                  : '✦'}
        </div>

        <div className="feature-card-body">
          <h3>{feature.displayName}</h3>
          <p>{feature.description || 'Your personal workspace tool.'}</p>
        </div>

        <div className="feature-card-footer">
          <span className="status-dot" />
          <span>Available to you</span>
          <span className="feature-arrow" aria-hidden="true">↗</span>
        </div>
      </>
  )

  if (url) {
    return (
        <a
            className="feature-card feature-card-link"
            href={url}
            key={feature.code}
            aria-label={`Open ${feature.displayName}`}
        >
          {content}
        </a>
    )
  }

  return (
      <article className="feature-card" key={feature.code}>
        {content}
      </article>
  )
}

function App() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [credentials, setCredentials] = useState<BasicCredentials | null>(null)
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function loadDashboard(auth: BasicCredentials) {
    setLoading(true)
    setError('')

    try {
      const result = await fetchDashboard(auth)
      setDashboard(result)
      setCredentials(auth)
    } catch (err) {
      setDashboard(null)
      setError(
          err instanceof Error ? err.message : 'Unable to load your dashboard.',
      )
    } finally {
      setLoading(false)
    }
  }

  function handleSignIn(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void loadDashboard({ username, password })
  }

  function handleSignOut() {
    setCredentials(null)
    setDashboard(null)
    setUsername('')
    setPassword('')
    setError('')
  }

  if (!credentials && !loading) {
    return (
        <main className="auth-page">
          <section className="auth-card">
            <div className="brand-mark">PM</div>
            <p className="eyebrow">YOUR PERSONAL WORKSPACE</p>
            <h1>Welcome back</h1>
            <p className="muted">
              Sign in to see the tools available in your Personal Manager.
            </p>

            <form onSubmit={handleSignIn} className="auth-form">
              <label htmlFor="username">Username</label>
              <input
                  id="username"
                  autoComplete="username"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                  required
              />

              <label htmlFor="password">Password</label>
              <input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
              />

              {error && <p className="error-message" role="alert">{error}</p>}

              <button type="submit" disabled={loading}>
                {loading ? 'Signing in…' : 'Sign in'}
              </button>
            </form>

            <p className="security-note">
              Development sign-in using the platform API's current HTTP Basic
              authentication. Keycloak integration will replace this flow.
            </p>
          </section>
        </main>
    )
  }

  if (loading) {
    return (
        <main className="dashboard-page">
          <div className="loading-state" role="status">
            <div className="spinner" />
            <p>Loading your workspace…</p>
          </div>
        </main>
    )
  }

  if (error) {
    return (
        <main className="dashboard-page">
          <header className="topbar">
            <a className="brand" href="/">Personal Manager</a>
            <button className="button-secondary" onClick={handleSignOut}>
              Sign out
            </button>
          </header>
          <section className="content">
            <div className="error-panel" role="alert">
              <h2>We couldn't load your workspace</h2>
              <p>{error}</p>
              <button onClick={() => credentials && void loadDashboard(credentials)}>
                Try again
              </button>
            </div>
          </section>
        </main>
    )
  }

  if (!dashboard) {
    return null
  }

  return (
      <main className="dashboard-page">
        <header className="topbar">
          <a className="brand" href="/">Personal Manager</a>
          <div className="topbar-actions">
            <span className="user-label">{dashboard.userSubject}</span>
            <button className="button-secondary" onClick={handleSignOut}>
              Sign out
            </button>
          </div>
        </header>

        <section className="hero">
          <p className="eyebrow">YOUR PERSONAL WORKSPACE</p>
          <h1>One place for the things that matter.</h1>
          <p>
            Your personal tools, organized around what you need.
          </p>
        </section>

        <section className="content">
          <div className="section-heading">
            <div>
              <h2>Your workspace</h2>
              <p className="muted">
                Open one of your enabled tools to get started.
              </p>
            </div>
            <span className="feature-count">
            {dashboard.features.length}{' '}
              {dashboard.features.length === 1 ? 'tool' : 'tools'} available
          </span>
          </div>

          {dashboard.features.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">✦</div>
                <h3>Your workspace is ready to grow</h3>
                <p>
                  You don't have any active tools yet. Once a feature is enabled
                  for your account, it will appear here.
                </p>
              </div>
          ) : (
              <div className="feature-grid">
                {dashboard.features.map((feature) => (
                    <FeatureCard key={feature.code} feature={feature} />
                ))}
              </div>
          )}
        </section>

        <footer className="footer">
          <span>Personal Manager</span>
          <span>Your tools. Your workspace.</span>
        </footer>
      </main>
  )
}

export default App
