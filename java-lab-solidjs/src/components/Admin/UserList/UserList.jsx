import { Show } from 'solid-js';
import { useUsers } from '../../../context/UsersContext';
import UserListItem from './UserListItem';
import ErrorToast from './ErrorToast';
import './UserList.css';

export default function UserList(props) {
  const {
    users,
    hasMore,
    isLoading,
    loadMore,
    setFilter,
    resetFilters,
    isFilterActive,
    filters,
  } = useUsers();

  const handleLoadMore = () => {
    loadMore();
  };

  return (
    <div class="user-list">
      <ErrorToast />

      <div class="user-list-filters">
        <div class="user-list-filter-group">
          <span class="user-list-filter-label">Активность:</span>
          <select
            class="user-list-filter-select"
            value={filters().activity ?? ''}
            onChange={(e) => {
              const value = e.target.value;
              setFilter('activity', value === '' ? null : value === 'true');
            }}
          >
            <option value="">Все</option>
            <option value="true">Активные</option>
            <option value="false">Неактивные</option>
          </select>
        </div>

        <div class="user-list-filter-group">
          <span class="user-list-filter-label">Уведомления:</span>
          <select
            class="user-list-filter-select"
            value={filters().notifyEnabled ?? ''}
            onChange={(e) => {
              const value = e.target.value;
              setFilter('notifyEnabled', value === '' ? null : value === 'true');
            }}
          >
            <option value="">Все</option>
            <option value="true">Включены</option>
            <option value="false">Отключены</option>
          </select>
        </div>

        <Show when={isFilterActive('activity') || isFilterActive('notifyEnabled')}>
          <button
            class="user-list-filter-reset"
            onClick={resetFilters}
          >
            Сбросить фильтры
          </button>
        </Show>
      </div>

      <div class="user-list-container">
        <Show
          when={users().length > 0}
          fallback={
            <div class="user-list-empty">
              {isLoading() ? 'Загрузка...' : 'Нет пользователей для отображения'}
            </div>
          }
        >
          {users().map((user) => (
            <UserListItem user={user} onBlockClick={props.onBlockClick} />
          ))}
        </Show>
      </div>

      <Show when={hasMore() && !isLoading()}>
        <div class="user-list-load-more">
          <button
            class="user-list-load-more-btn"
            onClick={handleLoadMore}
          >
            Загрузить ещё
          </button>
        </div>
      </Show>

      <Show when={isLoading()}>
        <div class="user-list-loading">
          <div class="spinner"></div>
        </div>
      </Show>
    </div>
  );
}
