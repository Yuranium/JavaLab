import { createSignal, createMemo, Show } from 'solid-js';
import { useTheme } from '../../context/ThemeContext';
import { useAuth } from '../../context/AuthContext';
import './Header.css';

export default function Header() {
  const { theme, toggleTheme } = useTheme();
  const auth = useAuth();
  const [isAdminMenuOpen, setIsAdminMenuOpen] = createSignal(false);

  const menuItems = createMemo(() => {
    const isAuthenticated = auth.isAuthenticated();
    const isAdmin = auth.hasRole(auth.ROLES.ADMIN);
    const isUser = auth.hasRole(auth.ROLES.USER);

    const items = [];

    items.push({ path: '/tasks', label: 'Задачи', show: true });

    if (isAuthenticated) {
      items.push({ path: '/profile', label: 'Профиль', show: true });
      items.push({ path: '/progress', label: 'Прогресс', show: true });
    }

    if (isAdmin) {
      items.push({
        path: null,
        label: 'Админ-панель',
        show: true,
        isDropdown: true,
        children: [
          { path: '/admin/users', label: 'Пользователи' },
          { path: '/admin/tasks', label: 'Задачи' },
        ],
      });
    }

    if (!isAuthenticated) {
      items.push({ path: '/login', label: 'Войти', show: true, isAuth: true });
      items.push({ path: '/register', label: 'Зарегистрироваться', show: true, isAuth: true });
    } else {
      items.push({ path: '/logout', label: 'Выйти', show: true, isLogout: true });
    }

    return items;
  });

  const handleLogout = (e) => {
    e.preventDefault();
    auth.logout();
    window.location.href = '/';
  };

  return (
    <header class="header">
      <div class="header-container">
        <a href="/" class="logo">
          <img src="/java-logo.png" alt="Java" class="logo-icon-img" />
          <span class="logo-text">JavaLab</span>
        </a>

        <nav class="nav">
          {menuItems().map(item => (
            <Show when={item.show}>
              {item.isDropdown ? (
                <div
                  class="nav-dropdown"
                  onMouseEnter={() => setIsAdminMenuOpen(true)}
                  onMouseLeave={() => setIsAdminMenuOpen(false)}
                >
                  <button class="nav-link nav-dropdown-trigger">
                    {item.label}
                    <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M6 9l6 6 6-6" />
                    </svg>
                  </button>
                  <div class="dropdown-menu" classList={{ 'dropdown-menu--open': isAdminMenuOpen() }}>
                    {item.children.map(child => (
                      <a href={child.path} class="dropdown-item">{child.label}</a>
                    ))}
                  </div>
                </div>
              ) : item.isLogout ? (
                <a href="#" class="nav-link nav-link--secondary" onClick={handleLogout}>
                  {item.label}
                </a>
              ) : (
                <a
                  href={item.path}
                  class="nav-link"
                  classList={{
                    'nav-link--secondary': item.isAuth,
                  }}
                >
                  {item.label}
                </a>
              )}
            </Show>
          ))}
        </nav>

        <button class="theme-toggle" onClick={toggleTheme} aria-label="Переключить тему">
          <Show when={theme() === 'dark'} fallback={
            <svg class="theme-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
          }>
            <svg class="theme-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"/>
              <line x1="12" y1="1" x2="12" y2="3"/>
              <line x1="12" y1="21" x2="12" y2="23"/>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
              <line x1="1" y1="12" x2="3" y2="12"/>
              <line x1="21" y1="12" x2="23" y2="12"/>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
          </Show>
        </button>
      </div>
    </header>
  );
}
