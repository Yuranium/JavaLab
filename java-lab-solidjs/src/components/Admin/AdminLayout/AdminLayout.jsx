import './AdminLayout.css';

export default function AdminLayout(props) {
  return (
    <div class="admin-layout">
      <aside class="admin-sidebar">
        <div class="admin-sidebar-header">
          <span class="admin-sidebar-title">Админ-панель</span>
        </div>
        <nav class="admin-sidebar-nav">
          <a href="/admin/users" class="admin-nav-link" classList={{ 'admin-nav-link--active': props.activePage === 'users' }}>
            <svg class="admin-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
            Пользователи
          </a>
          <a href="/admin/tasks" class="admin-nav-link" classList={{ 'admin-nav-link--active': props.activePage === 'tasks' }}>
            <svg class="admin-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 11l3 3L22 4" />
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
            </svg>
            Задачи
          </a>
        </nav>
      </aside>

      <main class="admin-main">
        <header class="admin-header">
          <h1 class="admin-title">{props.title}</h1>
        </header>

        <div class="admin-content">
          {props.children}
        </div>
      </main>

      {props.modal}
    </div>
  );
}
