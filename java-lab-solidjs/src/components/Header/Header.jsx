import { createSignal } from 'solid-js';
import { useTheme } from '../../context/ThemeContext';
import './Header.css';

export default function Header() {
  const { theme, toggleTheme } = useTheme();
  const [isAdminMenuOpen, setIsAdminMenuOpen] = createSignal(false);

  return (
    <header class="header">
      <div class="header-container">
        <a href="/" class="logo">
          <img src="/java-logo.png" alt="Java" class="logo-icon-img" />
          <span class="logo-text">JavaLab</span>
        </a>

        <nav class="nav">
          <a href="/tasks" class="nav-link">Задачи</a>
          <a href="/profile" class="nav-link">Профиль</a>
          
          <div
            class="nav-dropdown"
            onMouseEnter={() => setIsAdminMenuOpen(true)}
            onMouseLeave={() => setIsAdminMenuOpen(false)}
          >
            <button class="nav-link nav-dropdown-trigger">
              Админ-панель
              <svg class="dropdown-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 9l6 6 6-6" />
              </svg>
            </button>
            <div class="dropdown-menu" classList={{ 'dropdown-menu--open': isAdminMenuOpen() }}>
              <a href="/admin/users" class="dropdown-item">Пользователи</a>
              <a href="/admin/tasks" class="dropdown-item">Задачи</a>
            </div>
          </div>
          
          <a href="/login" class="nav-link nav-link--secondary">Войти</a>
          <a href="/register" class="nav-link nav-link--primary">Зарегистрироваться</a>
        </nav>

        <button class="theme-toggle" onClick={toggleTheme} aria-label="Переключить тему">
          {theme() === 'dark' ? '☀️' : '🌙'}
        </button>
      </div>
    </header>
  );
}
