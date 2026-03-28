import { createContext, useContext, createSignal, createEffect, onMount, onCleanup } from 'solid-js';
import axios from 'axios';
import { config, getAuthUrl } from '../config';

const AuthContext = createContext();

const ROLES = {
  GUEST: 'GUEST',
  USER: 'USER',
  ADMIN: 'ADMIN',
};

const REFRESH_THRESHOLD_MS = 60000;
const CHECK_INTERVAL_MS = 10000;

export function AuthProvider(props) {
  const [accessToken, setAccessToken] = createSignal(null);
  const [refreshToken, setRefreshToken] = createSignal(null);
  const [user, setUser] = createSignal(null);
  const [isLoading, setIsLoading] = createSignal(true);
  let refreshTimeoutId = null;
  let checkIntervalId = null;

  const parseJwt = (token) => {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (e) {
      console.error('Ошибка при парсинге JWT:', e);
      return null;
    }
  };

  const extractRoles = (payload) => {
    const roles = payload?.realm_access?.roles || [];

    if (roles.includes('ROLE_ADMIN')) {
      return [ROLES.ADMIN, ROLES.USER];
    }
    if (roles.includes('ROLE_USER')) {
      return [ROLES.USER];
    }
    return [ROLES.GUEST];
  };

  const clearTimers = () => {
    if (refreshTimeoutId) {
      clearTimeout(refreshTimeoutId);
      refreshTimeoutId = null;
    }
    if (checkIntervalId) {
      clearInterval(checkIntervalId);
      checkIntervalId = null;
    }
  };

  const scheduleTokenRefresh = () => {
    clearTimers();

    const currentUser = user();
    if (!currentUser?.exp) return;

    const now = Date.now();
    const expiryTime = currentUser.exp * 1000;
    const timeUntilExpiry = expiryTime - now;
    const refreshTime = timeUntilExpiry - REFRESH_THRESHOLD_MS;

    if (refreshTime <= 0) {
      refreshAccessToken();
      return;
    }

    refreshTimeoutId = setTimeout(() => {
      refreshAccessToken();
    }, refreshTime);
  };

  const startTokenMonitoring = () => {
    clearTimers();

    checkIntervalId = setInterval(() => {
      const currentUser = user();
      if (!currentUser?.exp) return;

      const now = Date.now();
      const expiryTime = currentUser.exp * 1000;
      const timeUntilExpiry = expiryTime - now;

      if (timeUntilExpiry <= REFRESH_THRESHOLD_MS && timeUntilExpiry > 0) {
        refreshAccessToken();
      }
    }, CHECK_INTERVAL_MS);
  };

  const setTokens = (access, refresh) => {
    setAccessToken(access);
    setRefreshToken(refresh);
    localStorage.setItem('access_token', access);
    localStorage.setItem('refresh_token', refresh);

    const payload = parseJwt(access);
    if (payload) {
      setUser({
        id: payload.sub,
        username: payload.preferred_username,
        email: payload.email,
        roles: extractRoles(payload),
        exp: payload.exp,
        iat: payload.iat,
      });
    }
  };

  const hasRole = (role) => {
    const userRoles = user()?.roles || [];
    return userRoles.includes(role);
  };

  const isAuthenticated = () => {
    const token = accessToken();
    if (!token) return false;

    const payload = parseJwt(token);
    if (!payload) return false;

    return payload.exp * 1000 > Date.now() + 30000;
  };

  const isTokenExpired = () => {
    const token = accessToken();
    if (!token) return true;

    const payload = parseJwt(token);
    if (!payload) return true;

    return payload.exp * 1000 <= Date.now();
  };

  const logout = () => {
    clearTimers();
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  };

  const login = async (username, password) => {
    try {
      const response = await axios.post(
        getAuthUrl(),
        new URLSearchParams({
          grant_type: config.grantType,
          client_id: config.clientId,
          username: username,
          password: password,
        }),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }
      );

      const { access_token, refresh_token } = response.data;
      setTokens(access_token, refresh_token);

      return { success: true };
    } catch (error) {
      console.error('Ошибка при входе:', error);
      return {
        success: false,
        error: error.response?.data?.error_description || 'Ошибка при входе',
      };
    }
  };

  const refreshAccessToken = async () => {
    const refresh = refreshToken();
    if (!refresh) {
      logout();
      return false;
    }

    try {
      const refreshPayload = parseJwt(refresh);
      if (refreshPayload && refreshPayload.exp * 1000 <= Date.now()) {
        console.warn('Refresh token истёк');
        logout();
        return false;
      }
    } catch (e) {
    }

    try {
      const response = await axios.post(
        getAuthUrl(),
        new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: config.clientId,
          refresh_token: refresh,
        }),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }
      );

      const { access_token, refresh_token } = response.data;
      setTokens(access_token, refresh_token);

      return true;
    } catch (error) {
      console.error('Ошибка при обновлении токена:', error);
      logout();
      return false;
    }
  };

  onMount(() => {
    const storedAccess = localStorage.getItem('access_token');
    const storedRefresh = localStorage.getItem('refresh_token');

    if (storedAccess && storedRefresh) {
      const payload = parseJwt(storedAccess);

      if (payload && payload.exp * 1000 > Date.now()) {
        setTokens(storedAccess, storedRefresh);
        setIsLoading(false);
      } else if (payload) {
        refreshAccessToken().then(() => {
          startTokenMonitoring();
        });
        setIsLoading(false);
        return;
      } else {
        logout();
        setIsLoading(false);
      }
    } else {
      setIsLoading(false);
    }
    
    startTokenMonitoring();
  });

  createEffect(() => {
    const currentUser = user();
    if (currentUser?.exp) {
      startTokenMonitoring();
    }
  });

  onCleanup(() => {
    clearTimers();
  });

  const value = {
    accessToken,
    refreshToken,
    user,
    isLoading,
    setTokens,
    login,
    logout,
    refreshAccessToken,
    hasRole,
    isAuthenticated,
    isTokenExpired,
    ROLES,
  };

  return (
    <AuthContext.Provider value={value}>
      {props.children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}