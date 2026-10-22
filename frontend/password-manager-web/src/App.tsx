
import { useMemo, useState, type FormEvent } from 'react'
import './App.css'

type Credential = {
  id: string
  appName: string
  username: string
  password: string
  category: string
  website: string
  favorite: boolean
  updatedAt: string
}

const initialCredentials: Credential[] = [
  {
    id: '1',
    appName: 'Google',
    username: 'alex@example.com',
    password: 'DemoPassword123!',
    category: 'Email',
    website: 'https://accounts.google.com',
    favorite: true,
    updatedAt: 'Today',
  },
  {
    id: '2',
    appName: 'GitHub',
    username: 'alex-dev',
    password: 'ExampleToken456!',
    category: 'Development',
    website: 'https://github.com',
    favorite: true,
    updatedAt: 'Yesterday',
  },
  {
    id: '3',
    appName: 'Instagram',
    username: 'alex.photos',
    password: 'SamplePassword789!',
    category: 'Social',
    website: 'https://instagram.com',
    favorite: false,
    updatedAt: 'Oct 18',
  },
  {
    id: '4',
    appName: 'Amazon',
    username: 'alex@example.com',
    password: 'NotARealPassword321!',
    category: 'Shopping',
    website: 'https://amazon.com',
    favorite: false,
    updatedAt: 'Oct 15',
  },
]

const categories = ['All Items', 'Favorites', 'Email', 'Development', 'Social', 'Shopping']

function initials(name: string) {
  return name.slice(0, 1).toUpperCase()
}

function App() {
  const [credentials, setCredentials] = useState(initialCredentials)
  const [selectedId, setSelectedId] = useState<string | null>('1')
  const [activeCategory, setActiveCategory] = useState('All Items')
  const [search, setSearch] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showAddForm, setShowAddForm] = useState(false)
  const [notice, setNotice] = useState('')

  const selected = credentials.find((item) => item.id === selectedId)

  const filteredCredentials = useMemo(() => {
    const term = search.trim().toLowerCase()

    return credentials.filter((item) => {
      const matchesCategory =
          activeCategory === 'All Items' ||
          (activeCategory === 'Favorites' && item.favorite) ||
          item.category === activeCategory

      const matchesSearch =
          !term ||
          item.appName.toLowerCase().includes(term) ||
          item.username.toLowerCase().includes(term)

      return matchesCategory && matchesSearch
    })
  }, [credentials, activeCategory, search])

  function toggleFavorite(id: string) {
    setCredentials((current) =>
        current.map((item) =>
            item.id === id ? { ...item, favorite: !item.favorite } : item,
        ),
    )
  }

  function handleAddCredential(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)

    const appName = String(form.get('appName') || '').trim()
    const username = String(form.get('username') || '').trim()
    const password = String(form.get('password') || '')
    const category = String(form.get('category') || 'Other')
    const website = String(form.get('website') || '').trim()

    if (!appName || !username || !password) return

    const item: Credential = {
      id: crypto.randomUUID(),
      appName,
      username,
      password,
      category,
      website,
      favorite: false,
      updatedAt: 'Just now',
    }

    setCredentials((current) => [item, ...current])
    setSelectedId(item.id)
    setActiveCategory('All Items')
    setSearch('')
    setShowPassword(false)
    setShowAddForm(false)
    setNotice('Added to this demo session. It has not been saved to the server.')
  }

  async function copyUsername() {
    if (!selected) return

    try {
      await navigator.clipboard.writeText(selected.username)
      setNotice('Username copied.')
    } catch {
      setNotice('Clipboard access is unavailable in this browser.')
    }
  }

  return (
      <main className="vault-app">
        <aside className="sidebar">
          <a className="vault-brand" href="/" aria-label="Password Manager home">
            <span className="brand-icon">✳</span>
            <span>Password<span className="brand-light">Vault</span></span>
          </a>

          <button className="add-button" onClick={() => {
            setNotice('')
            setShowAddForm(true)
          }}>
            <span>＋</span> Add credential
          </button>

          <p className="nav-label">YOUR VAULT</p>
          <nav className="category-nav" aria-label="Credential categories">
            {categories.map((category) => (
                <button
                    key={category}
                    className={`nav-item ${activeCategory === category ? 'active' : ''}`}
                    onClick={() => setActiveCategory(category)}
                >
              <span className="nav-symbol">
                {category === 'All Items' ? '▦'
                    : category === 'Favorites' ? '☆'
                        : category === 'Email' ? '✉'
                            : category === 'Development' ? '⌘'
                                : category === 'Social' ? '◎' : '◇'}
              </span>
                  {category}
                  {category === 'All Items' && (
                      <span className="nav-count">{credentials.length}</span>
                  )}
                </button>
            ))}
          </nav>

          <div className="sidebar-bottom">
            <div className="security-indicator">
              <span className="security-dot" />
              <div>
                <strong>Demo vault</strong>
                <span>Local session only</span>
              </div>
            </div>
            <p className="sidebar-disclaimer">
              Sample data only. Real credential storage isn't connected yet.
            </p>
          </div>
        </aside>

        <section className="workspace">
          <header className="workspace-header">
            <div>
              <p className="breadcrumb">PERSONAL MANAGER / VAULT</p>
              <h1>Password Manager</h1>
            </div>
            <div className="profile-chip" title="Demo profile">A</div>
          </header>

          <div className="vault-content">
            <div className="list-panel">
              <div className="list-heading">
                <div>
                  <h2>{activeCategory}</h2>
                  <p>{filteredCredentials.length} saved items in this demo</p>
                </div>
                <button
                    className="icon-button mobile-add"
                    aria-label="Add credential"
                    onClick={() => setShowAddForm(true)}
                >
                  +
                </button>
              </div>

              <label className="search-box">
                <span aria-hidden="true">⌕</span>
                <input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Search credentials..."
                    aria-label="Search credentials"
                />
                {search && (
                    <button aria-label="Clear search" onClick={() => setSearch('')}>×</button>
                )}
              </label>

              <div className="credential-list">
                {filteredCredentials.map((item) => (
                    <button
                        key={item.id}
                        className={`credential-row ${selectedId === item.id ? 'selected' : ''}`}
                        onClick={() => {
                          setSelectedId(item.id)
                          setShowPassword(false)
                          setNotice('')
                        }}
                    >
                  <span className={`app-avatar avatar-${item.category.toLowerCase()}`}>
                    {initials(item.appName)}
                  </span>
                      <span className="credential-summary">
                    <strong>{item.appName}</strong>
                    <span>{item.username}</span>
                  </span>
                      {item.favorite && <span className="favorite-mark" aria-label="Favorite">★</span>}
                    </button>
                ))}

                {filteredCredentials.length === 0 && (
                    <div className="list-empty">
                      <span>⌕</span>
                      <strong>No matching credentials</strong>
                      <p>Try another search or category.</p>
                    </div>
                )}
              </div>
            </div>

            <div className="detail-panel">
              {selected ? (
                  <>
                    <div className="detail-topline">
                      <span className="detail-type">LOGIN CREDENTIAL</span>
                      <button
                          className={`icon-button favorite-button ${selected.favorite ? 'is-favorite' : ''}`}
                          aria-label={selected.favorite ? 'Remove from favorites' : 'Add to favorites'}
                          title={selected.favorite ? 'Remove from favorites' : 'Add to favorites'}
                          onClick={() => toggleFavorite(selected.id)}
                      >
                        {selected.favorite ? '★' : '☆'}
                      </button>
                    </div>

                    <div className="detail-identity">
                  <span className={`detail-avatar avatar-${selected.category.toLowerCase()}`}>
                    {initials(selected.appName)}
                  </span>
                      <h2>{selected.appName}</h2>
                      <p>{selected.category}</p>
                    </div>

                    <div className="detail-fields">
                      <div className="detail-field">
                        <label>Username / Email</label>
                        <div className="field-value">
                          <span>{selected.username}</span>
                          <button onClick={() => void copyUsername()} title="Copy username">
                            Copy
                          </button>
                        </div>
                      </div>

                      <div className="detail-field">
                        <label>Password</label>
                        <div className="field-value">
                      <span className="password-value">
                        {showPassword ? selected.password : '••••••••••••'}
                      </span>
                          <button onClick={() => setShowPassword((value) => !value)}>
                            {showPassword ? 'Hide' : 'Show'}
                          </button>
                        </div>
                      </div>

                      <div className="detail-field">
                        <label>Website</label>
                        <div className="field-value">
                          {selected.website ? (
                              <a href={selected.website} target="_blank" rel="noreferrer">
                                {selected.website.replace(/^https?:\/\//, '')}
                                <span aria-hidden="true"> ↗</span>
                              </a>
                          ) : (
                              <span className="subtle-value">No website provided</span>
                          )}
                        </div>
                      </div>

                      <div className="detail-field">
                        <label>Last updated</label>
                        <div className="field-value">
                          <span>{selected.updatedAt}</span>
                        </div>
                      </div>
                    </div>

                    <div className="detail-footer">
                      <span className="lock-symbol">♧</span>
                      <p>Demo only — this credential exists in browser memory, not secure storage.</p>
                    </div>
                  </>
              ) : (
                  <div className="detail-empty">
                    <span>▤</span>
                    <h2>Select a credential</h2>
                    <p>Choose an item from your vault to view its details.</p>
                  </div>
              )}
            </div>
          </div>

          <footer className="workspace-footer">
            <span>Personal Manager · Password Vault</span>
            <span>UI prototype · No server storage</span>
          </footer>
        </section>

        {notice && (
            <div className="toast" role="status">
              <span>{notice}</span>
              <button onClick={() => setNotice('')} aria-label="Dismiss message">×</button>
            </div>
        )}

        {showAddForm && (
            <div className="modal-backdrop" onMouseDown={(event) => {
              if (event.target === event.currentTarget) setShowAddForm(false)
            }}>
              <section className="credential-modal" role="dialog" aria-modal="true" aria-labelledby="add-title">
                <div className="modal-heading">
                  <div>
                    <p className="breadcrumb">YOUR VAULT</p>
                    <h2 id="add-title">Add credential</h2>
                  </div>
                  <button className="icon-button" onClick={() => setShowAddForm(false)} aria-label="Close form">×</button>
                </div>

                <form className="credential-form" onSubmit={handleAddCredential}>
                  <label>
                    Application name *
                    <input name="appName" placeholder="e.g. Google" required autoFocus />
                  </label>
                  <label>
                    Username or email *
                    <input name="username" placeholder="you@example.com" required />
                  </label>
                  <label>
                    Password *
                    <input name="password" type="password" placeholder="Enter a demo password" required />
                  </label>
                  <label>
                    Website
                    <input name="website" type="url" placeholder="https://example.com" />
                  </label>
                  <label>
                    Category
                    <select name="category" defaultValue="Other">
                      <option>Other</option>
                      <option>Email</option>
                      <option>Development</option>
                      <option>Social</option>
                      <option>Shopping</option>
                      <option>Finance</option>
                    </select>
                  </label>

                  <p className="form-warning">
                    Prototype only: don't enter real credentials. This form doesn't save to the backend.
                  </p>
                  <div className="modal-actions">
                    <button type="button" className="secondary-button" onClick={() => setShowAddForm(false)}>
                      Cancel
                    </button>
                    <button type="submit" className="primary-button">Add to demo vault</button>
                  </div>
                </form>
              </section>
            </div>
        )}
      </main>
  )
}

export default App
