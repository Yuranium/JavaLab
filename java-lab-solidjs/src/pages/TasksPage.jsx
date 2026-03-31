import { createSignal, createMemo, For, Show } from 'solid-js';
import TaskCard from '../components/Tasks/TaskCard/TaskCard';
import TaskFilters from '../components/Tasks/TaskFilters/TaskFilters';
import './TasksPage.css';

const MOCK_TASKS = [
  {
    idTask: 1,
    title: "Реализация собственного ArrayList",
    difficulty: "MEDIUM",
    createdAt: "2024-01-15T10:30:00Z",
    updatedAt: "2024-01-15T14:45:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174000",
    categories: [
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 2,
    title: "Написание утилиты для работы с датами",
    difficulty: "EASY",
    createdAt: "2024-01-16T09:00:00Z",
    updatedAt: "2024-01-16T11:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174001",
    categories: [
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 3,
    title: "Реализация кастомного компаратора для сортировки объектов",
    difficulty: "MEDIUM",
    createdAt: "2024-01-17T14:20:00Z",
    updatedAt: "2024-01-18T10:15:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174002",
    categories: [
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 4,
    title: "Создание функционального интерфейса Predicate",
    difficulty: "EASY",
    createdAt: "2024-01-18T08:45:00Z",
    updatedAt: "2024-01-18T09:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174003",
    categories: [
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 5,
    title: "Фильтрация коллекции сотрудников с помощью Stream API",
    difficulty: "MEDIUM",
    createdAt: "2024-01-19T11:00:00Z",
    updatedAt: "2024-01-19T15:20:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174004",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 6,
    title: "Реализация собственного HashMap",
    difficulty: "HARD",
    createdAt: "2024-01-20T10:00:00Z",
    updatedAt: "2024-01-21T16:45:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174005",
    categories: [
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 7,
    title: "Обработка исключений в многопоточной среде",
    difficulty: "HARD",
    createdAt: "2024-01-21T09:30:00Z",
    updatedAt: "2024-01-22T14:00:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174006",
    categories: [
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 8,
    title: "Использование метод-референсов для упрощения кода",
    difficulty: "MEDIUM",
    createdAt: "2024-01-22T13:15:00Z",
    updatedAt: "2024-01-22T17:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174007",
    categories: [
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 9,
    title: "Группировка элементов с помощью Collectors.groupingBy",
    difficulty: "MEDIUM",
    createdAt: "2024-01-23T10:45:00Z",
    updatedAt: "2024-01-23T12:00:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174008",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 10,
    title: "Создание неизменяемого списка",
    difficulty: "EASY",
    createdAt: "2024-01-24T08:00:00Z",
    updatedAt: "2024-01-24T08:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174009",
    categories: [
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 11,
    title: "Параллельная обработка данных в Stream API",
    difficulty: "HARD",
    createdAt: "2024-01-25T11:30:00Z",
    updatedAt: "2024-01-26T09:45:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174010",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 12,
    title: "Реализация паттерна Стратегия с использованием лямбда-выражений",
    difficulty: "MEDIUM",
    createdAt: "2024-01-26T14:00:00Z",
    updatedAt: "2024-01-26T16:20:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174011",
    categories: [
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 13,
    title: "Сортировка коллекции по нескольким полям",
    difficulty: "EASY",
    createdAt: "2024-01-27T09:15:00Z",
    updatedAt: "2024-01-27T10:00:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174012",
    categories: [
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 14,
    title: "Оптимизация производительности с помощью flatMap",
    difficulty: "HARD",
    createdAt: "2024-01-28T10:30:00Z",
    updatedAt: "2024-01-29T11:45:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174013",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 15,
    title: "Работа с Optional для избежания NullPointerException",
    difficulty: "EASY",
    createdAt: "2024-01-29T13:00:00Z",
    updatedAt: "2024-01-29T14:15:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174014",
    categories: [
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 16,
    title: "Создание конвейера операций для обработки данных",
    difficulty: "MEDIUM",
    createdAt: "2024-01-30T08:45:00Z",
    updatedAt: "2024-01-30T12:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174015",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 17,
    title: "Реализация собственного LinkedHashMap",
    difficulty: "HARD",
    createdAt: "2024-01-31T11:00:00Z",
    updatedAt: "2024-02-01T15:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174016",
    categories: [
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 18,
    title: "Использование reduce для агрегации данных",
    difficulty: "MEDIUM",
    createdAt: "2024-02-01T09:30:00Z",
    updatedAt: "2024-02-01T11:00:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174017",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 19,
    title: "Создание фабричных методов с помощью лямбда-выражений",
    difficulty: "EASY",
    createdAt: "2024-02-02T10:15:00Z",
    updatedAt: "2024-02-02T11:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174018",
    categories: [
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 20,
    title: "Реализация кэша с использованием WeakHashMap",
    difficulty: "HARD",
    createdAt: "2024-02-03T14:00:00Z",
    updatedAt: "2024-02-04T10:45:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174019",
    categories: [
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 21,
    title: "Многопоточная обработка коллекций",
    difficulty: "HARD",
    createdAt: "2024-02-04T09:00:00Z",
    updatedAt: "2024-02-05T16:00:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174020",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 22,
    title: "Валидация данных с помощью Predicate",
    difficulty: "EASY",
    createdAt: "2024-02-05T11:30:00Z",
    updatedAt: "2024-02-05T12:45:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174021",
    categories: [
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: true
  },
  {
    idTask: 23,
    title: "Сравнение производительности ArrayList и LinkedList",
    difficulty: "MEDIUM",
    createdAt: "2024-02-06T08:15:00Z",
    updatedAt: "2024-02-06T14:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174022",
    categories: [
      { title: "JAVA_COLLECTIONS", description: "Работа со структурами данных: List, Set, Map, их реализациями", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 24,
    title: "Обработка ошибок в Stream API",
    difficulty: "MEDIUM",
    createdAt: "2024-02-07T10:00:00Z",
    updatedAt: "2024-02-07T15:20:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174023",
    categories: [
      { title: "JAVA_STREAM_API", description: "Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения", createdAt: "2024-01-15T10:30:00Z" },
      { title: "JAVA_CORE", description: "Основы языка, ООП, синтаксис, примитивные типы, исключения", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  },
  {
    idTask: 25,
    title: "Создание DSL с использованием лямбда-выражений",
    difficulty: "HARD",
    createdAt: "2024-02-08T13:45:00Z",
    updatedAt: "2024-02-09T09:30:00Z",
    idAuthor: "123e4567-e89b-12d3-a456-426614174024",
    categories: [
      { title: "JAVA_LAMBDAS", description: "Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы", createdAt: "2024-01-15T10:30:00Z" }
    ],
    isSolved: false
  }
];

const ITEMS_PER_PAGE = 20;

export default function TasksPage() {
  const [filters, setFilters] = createSignal({
    search: '',
    category: '',
    difficulty: '',
    sort: 'id_asc',
    showSolved: false,
  });

  const [visibleCount, setVisibleCount] = createSignal(ITEMS_PER_PAGE);
  const [isLoading, setIsLoading] = createSignal(false);

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
    setVisibleCount(ITEMS_PER_PAGE);
  };

  const handleResetFilters = () => {
    setFilters({
      search: '',
      category: '',
      difficulty: '',
      sort: 'id_asc',
      showSolved: false,
    });
    setVisibleCount(ITEMS_PER_PAGE);
  };

  const filteredAndSortedTasks = createMemo(() => {
    const currentFilters = filters();
    let result = [...MOCK_TASKS];

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

    if (currentFilters.sort === 'id_asc') {
      result.sort((a, b) => a.idTask - b.idTask);
    } else if (currentFilters.sort === 'id_desc') {
      result.sort((a, b) => b.idTask - a.idTask);
    }

    return result;
  });

  const displayedTasks = createMemo(() => {
    return filteredAndSortedTasks().slice(0, visibleCount());
  });

  const hasMore = createMemo(() => {
    return visibleCount() < filteredAndSortedTasks().length;
  });

  const handleLoadMore = () => {
    setIsLoading(true);
    setTimeout(() => {
      setVisibleCount(prev => prev + ITEMS_PER_PAGE);
      setIsLoading(false);
    }, 500);
  };

  return (
    <div class="tasks-page">
      <div class="tasks-header">
        <h1 class="tasks-title">Задачи</h1>
        <p class="tasks-subtitle">Выбирайте задачи по категориям и сложности, отслеживайте свой прогресс</p>
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

      <div class="tasks-list">
        <Show
          when={displayedTasks().length > 0}
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
          <For each={displayedTasks()}>
            {(task) => <TaskCard task={task} />}
          </For>
        </Show>
      </div>

      <Show when={hasMore()}>
        <div class="tasks-load-more-container">
          <button
            class="tasks-load-more-btn"
            onClick={handleLoadMore}
            disabled={isLoading()}
          >
            <Show when={!isLoading()} fallback="Загрузка...">
              Загрузить ещё
            </Show>
          </button>
        </div>
      </Show>
    </div>
  );
}
