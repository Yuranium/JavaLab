import { createContext, useContext, createSignal, createMemo } from 'solid-js';
import axios from 'axios';
import { config } from '../config';
import { useAuth } from './AuthContext';

const UsersContext = createContext();

const DEFAULT_PAGE_SIZE = 20;

export function UsersProvider(props) {
  const { accessToken } = useAuth();

  const [users, setUsers] = createSignal([]);
  const [blockedUsers, setBlockedUsers] = createSignal(new Set());
  const [currentPage, setCurrentPage] = createSignal(0);
  const [totalElements, setTotalElements] = createSignal(0);
  const [totalPages, setTotalPages] = createSignal(0);
  const [hasMore, setHasMore] = createSignal(true);
  const [isLoading, setIsLoading] = createSignal(false);
  const [error, setError] = createSignal(null);

  const [filters, setFilters] = createSignal({
    activity: null,
    notifyEnabled: null,
  });

  const loadUsers = async (page = 0, append = false) => {
    if (isLoading()) return;

    setIsLoading(true);
    setError(null);

    try {
      const currentFilters = filters();
      const params = new URLSearchParams({
        page: page.toString(),
        size: DEFAULT_PAGE_SIZE.toString(),
      });

      if (currentFilters.activity !== null) {
        params.append('activity', currentFilters.activity.toString());
      }
      if (currentFilters.notifyEnabled !== null) {
        params.append('notifyEnabled', currentFilters.notifyEnabled.toString());
      }

      const response = await axios.get(`${config.backendUrl}/api/v1/user`, {
        params,
        headers: {
          'Authorization': `Bearer ${accessToken()}`,
        },
      });

      const data = response.data;
      const content = data.content || [];
      const pageData = data.page || {};

      if (append) {
        setUsers(prev => [...prev, ...content]);
      } else {
        setUsers(content);
      }

      setCurrentPage(pageData.number ?? page);
      setTotalElements(pageData.totalElements ?? 0);
      setTotalPages(pageData.totalPages ?? 0);
      setHasMore((pageData.number ?? page) < (pageData.totalPages ?? 1) - 1);
    } catch (err) {
      console.error('Ошибка при загрузке пользователей:', err);
      setError('Не удалось загрузить данные');
      setTimeout(() => {
        setError(null);
      }, 5000);
    } finally {
      setIsLoading(false);
    }
  };

  const loadMore = async () => {
    if (!hasMore() || isLoading()) return;
    await loadUsers(currentPage() + 1, true);
  };

  const setFilter = (filterName, value) => {
    setFilters(prev => ({
      ...prev,
      [filterName]: value,
    }));
    setCurrentPage(0);
    setUsers([]);
    loadUsers(0, false);
  };

  const resetFilters = () => {
    setFilters({
      activity: null,
      notifyEnabled: null,
    });
    setCurrentPage(0);
    setUsers([]);
    loadUsers(0, false);
  };

  const updateUserInList = (userId, newActivityStatus) => {
    setUsers(prevUsers =>
      prevUsers.map(user =>
        user.id === userId ? { ...user, activity: newActivityStatus } : user
      )
    );
  };

  const isUserBlocked = (userId) => {
    return blockedUsers().has(userId);
  };

  const activeFilters = createMemo(() => {
    const currentFilters = filters();
    const active = {};
    if (currentFilters.activity !== null) {
      active.activity = currentFilters.activity;
    }
    if (currentFilters.notifyEnabled !== null) {
      active.notifyEnabled = currentFilters.notifyEnabled;
    }
    return active;
  });

  const isFilterActive = (filterName) => {
    return filters()[filterName] !== null;
  };

  const value = {
    users,
    blockedUsers,
    currentPage,
    hasMore,
    isLoading,
    error,
    filters,
    loadUsers,
    loadMore,
    setFilter,
    resetFilters,
    updateUserInList,
    isUserBlocked,
    activeFilters,
    isFilterActive,
  };

  return (
    <UsersContext.Provider value={value}>
      {props.children}
    </UsersContext.Provider>
  );
}

export function useUsers() {
  return useContext(UsersContext);
}
