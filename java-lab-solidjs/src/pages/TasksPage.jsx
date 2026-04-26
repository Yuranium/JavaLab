import { createSignal, createMemo, createEffect, For, Show } from 'solid-js';
import { config } from '../config';
import { useAuth } from '../context/AuthContext';
import TaskCard from '../components/Tasks/TaskCard/TaskCard';
import TaskFilters from '../components/Tasks/TaskFilters/TaskFilters';
import CreateTaskModal from '../components/Tasks/CreateTask/CreateTaskModal';
import './TasksPage.css';

const DEFAULT_PAGE_SIZE = 30;

export default function TasksPage() {
  const auth = useAuth();
  const [isCreateModalOpen, setIsCreateModalOpen] = createSignal(false);

  const [filters, setFilters] = createSignal({
    search: '',
    category: '',
    difficulty: '',
    sort: 'created_desc',
    showSolved: false,
  });

  const [allTasks, setAllTasks] = createSignal([]);
  const [isLoading, setIsLoading] = createSignal(false);
  const [isLoadingMore, setIsLoadingMore] = createSignal(false);
  const [error, setError] = createSignal('');
  const [page, setPage] = createSignal(0);
  const [hasMore, setHasMore] = createSignal(true);

  const loadTasks = async (pageNum = 0, append = false) => {
    if (append) {
      setIsLoadingMore(true);
    } else {
      setIsLoading(true);
    }
    setError('');

    try {
      const params = new URLSearchParams({
        page: pageNum.toString(),
        size: DEFAULT_PAGE_SIZE.toString(),
      });

      const response = await fetch(`${config.backendUrl}/api/v1/task?${params}`);

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(
          errorData?.message || `Ошибка загрузки: ${response.status} ${response.statusText}`
        );
      }

      const data = await response.json();

      if (append) {
        setAllTasks(prev => [...prev, ...data]);
      } else {
        setAllTasks(data);
      }

      setHasMore(data.length === DEFAULT_PAGE_SIZE);
    } catch (err) {
      setError(err.message || 'Неизвестная ошибка');
      if (!append) {
        setAllTasks([]);
      }
    } finally {
      setIsLoading(false);
      setIsLoadingMore(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setPage(0);
    loadTasks(0, false);
  };

  const handleResetFilters = () => {
    setFilters({
      search: '',
      category: '',
      difficulty: '',
      sort: 'created_desc',
      showSolved: false,
    });
    setPage(0);
    loadTasks(0, false);
  };

  const handleLoadMore = () => {
    const nextPage = page() + 1;
    setPage(nextPage);
    loadTasks(nextPage, true);
  };

  createEffect(() => {
    loadTasks(0, false);
  });

  const filteredAndSortedTasks = createMemo(() => {
    const currentFilters = filters();
    let result = [...allTasks()];

    if (currentFilters.search) {
      const searchLower = currentFilters.search.toLowerCase();
      result = result.filter(task =>
        task.title.toLowerCase().includes(searchLower) ||
        task.idTask.toString().includes(searchLower)
      );
    }

    if (currentFilters.category) {
      result = result.filter(task =>
        task.categories.some(cat => cat.title === currentFilters.category)
      );
    }

    if (currentFilters.difficulty) {
      result = result.filter(task => task.difficulty === currentFilters.difficulty);
    }

    if (!currentFilters.showSolved) {
      result = result.filter(task => !task.isSolved);
    }

    if (currentFilters.sort === 'created_asc') {
      result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
    } else if (currentFilters.sort === 'created_desc') {
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (currentFilters.sort === 'updated_asc') {
      result.sort((a, b) => new Date(a.updatedAt) - new Date(b.updatedAt));
    } else if (currentFilters.sort === 'updated_desc') {
      result.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt));
    }

    return result;
  });

  return (
    <div class="tasks-page">
      <div class="tasks-header">
        <div class="tasks-header-text">
          <h1 class="tasks-title">Задачи</h1>
          <p class="tasks-subtitle">Выбирайте задачи по категориям и сложности, отслеживайте свой прогресс</p>
        </div>
        <Show when={auth.hasRole(auth.ROLES.ADMIN)}>
          <button
            class="tasks-create-btn"
            onClick={() => setIsCreateModalOpen(true)}
          >
            <svg class="tasks-create-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            Создать задачу
          </button>
        </Show>
      </div>

      <div class="tasks-controls">
        <div class="tasks-search">
          <svg class="tasks-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <input
            type="text"
            class="tasks-search-input"
            placeholder="Поиск задач по названию или номеру..."
            value={filters().search}
            onInput={(e) => handleFilterChange('search', e.target.value)}
          />
        </div>

        <TaskFilters
          filters={filters()}
          onFilterChange={handleFilterChange}
          onResetFilters={handleResetFilters}
        />
      </div>

      <Show when={error()}>
        <div class="tasks-error">
          <svg class="tasks-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <span class="tasks-error-text">{error()}</span>
        </div>
      </Show>

      <div class="tasks-list">
        <Show
          when={!isLoading()}
          fallback={
            <div class="tasks-loading">
              <div class="tasks-spinner"></div>
            </div>
          }
        >
          <Show
            when={filteredAndSortedTasks().length > 0}
            fallback={
              <div class="tasks-empty">
                <svg class="tasks-empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
                  <rect x="9" y="3" width="6" height="4" rx="1" />
                  <path d="M9 12h6" />
                  <path d="M9 16h6" />
                  <path d="M12 8v8" />
                </svg>
                <p class="tasks-empty-text">Задачи не найдены</p>
                <p class="tasks-empty-subtext">Попробуйте изменить параметры фильтрации</p>
              </div>
            }
          >
            <For each={filteredAndSortedTasks()}>
              {(task) => <TaskCard task={task} />}
            </For>
          </Show>
        </Show>
      </div>

      <Show when={hasMore() && filteredAndSortedTasks().length > 0}>
        <div class="tasks-load-more-container">
          <button
            class="tasks-load-more-btn"
            onClick={handleLoadMore}
            disabled={isLoadingMore()}
          >
            <Show when={!isLoadingMore()} fallback="Загрузка...">
              Загрузить ещё
            </Show>
          </button>
        </div>
      </Show>

      <Show when={auth.hasRole(auth.ROLES.ADMIN)}>
        <CreateTaskModal
          isOpen={isCreateModalOpen()}
          onClose={() => setIsCreateModalOpen(false)}
          onSuccess={() => loadTasks(0, false)}
        />
      </Show>
    </div>
  );
}
