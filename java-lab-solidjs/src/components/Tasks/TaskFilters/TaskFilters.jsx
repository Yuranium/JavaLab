import './TaskFilters.css';

const CATEGORIES = [
  { value: '', label: 'Все категории' },
  { value: 'JAVA_CORE', label: 'JAVA_CORE' },
  { value: 'JAVA_COLLECTIONS', label: 'JAVA_COLLECTIONS' },
  { value: 'JAVA_LAMBDAS', label: 'JAVA_LAMBDAS' },
  { value: 'JAVA_STREAM_API', label: 'JAVA_STREAM_API' },
];

const DIFFICULTIES = [
  { value: '', label: 'Все сложности' },
  { value: 'EASY', label: 'EASY' },
  { value: 'MEDIUM', label: 'MEDIUM' },
  { value: 'HARD', label: 'HARD' },
];

const SORT_OPTIONS = [
  { value: 'id_asc', label: 'По возрастанию номера' },
  { value: 'id_desc', label: 'По убыванию номера' },
];

export default function TaskFilters(props) {
  return (
    <div class="task-filters">
      <div class="task-filters-row">
        <div class="task-filter-group">
          <label class="task-filter-label" for="category-filter">
            Категория
          </label>
          <select
            id="category-filter"
            class="task-filter-select"
            value={props.filters.category}
            onChange={(e) => props.onFilterChange('category', e.target.value)}
          >
            {CATEGORIES.map((cat) => (
              <option value={cat.value}>{cat.label}</option>
            ))}
          </select>
        </div>

        <div class="task-filter-group">
          <label class="task-filter-label" for="difficulty-filter">
            Сложность
          </label>
          <select
            id="difficulty-filter"
            class="task-filter-select"
            value={props.filters.difficulty}
            onChange={(e) => props.onFilterChange('difficulty', e.target.value)}
          >
            {DIFFICULTIES.map((diff) => (
              <option value={diff.value}>{diff.label}</option>
            ))}
          </select>
        </div>

        <div class="task-filter-group">
          <label class="task-filter-label" for="sort-filter">
            Сортировка
          </label>
          <select
            id="sort-filter"
            class="task-filter-select"
            value={props.filters.sort}
            onChange={(e) => props.onFilterChange('sort', e.target.value)}
          >
            {SORT_OPTIONS.map((opt) => (
              <option value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>

        <label class="task-checkbox-group">
          <input
            type="checkbox"
            class="task-checkbox"
            checked={props.filters.showSolved}
            onChange={(e) => props.onFilterChange('showSolved', e.target.checked)}
          />
          <span class="task-checkbox-label">Решенные задачи</span>
        </label>

        <div class="task-actions">
          <button class="task-btn task-btn-reset" onClick={props.onResetFilters}>
            <svg class="task-btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
            </svg>
            Сбросить фильтры
          </button>
        </div>
      </div>
    </div>
  );
}
