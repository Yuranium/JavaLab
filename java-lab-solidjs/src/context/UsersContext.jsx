import { createContext, useContext, createSignal, createMemo } from 'solid-js';
import axios from 'axios';
import { config } from '../config';
import { useAuth } from './AuthContext';

const UsersContext = createContext();

const DEFAULT_PAGE_SIZE = 30;

export function UsersProvider(props) {
  const { accessToken } = useAuth();
  
  const [users, setUsers] = createSignal([]);
  const [blockedUsers, setBlockedUsers] = createSignal(new Set());
  const [currentPage, setCurrentPage] = createSignal(0);
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
      
      if (append) {
        setUsers(prev => [...prev, ...data]);
      } else {
        setUsers(data);
      }
      
      setCurrentPage(page);
      setHasMore(data.length === DEFAULT_PAGE_SIZE);
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

  const blockUser = (userId, reason, duration) => {
    console.log(`Блокировка пользователя ${userId}: ${reason}, срок: ${duration}`);
    setBlockedUsers(prev => new Set([...prev, userId]));
    // TODO: отправить запрос на сервер для блокировки
  };

  const unblockUser = (userId) => {
    setBlockedUsers(prev => {
      const newSet = new Set(prev);
      newSet.delete(userId);
      return newSet;
    });
    // TODO: отправить запрос на сервер для разблокировки
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
    blockUser,
    unblockUser,
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
