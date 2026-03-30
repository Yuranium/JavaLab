import { createContext, useContext, createSignal } from 'solid-js';
import { config } from '../config';
import axios from 'axios';

const ProfileContext = createContext();

const AVATAR_MAX_SIZE = 10 * 1024 * 1024; // 10 МБ

export function ProfileProvider(props) {
  const [avatarError, setAvatarError] = createSignal('');
  const [isUpdatingAvatar, setIsUpdatingAvatar] = createSignal(false);
  const [isUpdatingName, setIsUpdatingName] = createSignal(false);
  const [isUpdatingLastName, setIsUpdatingLastName] = createSignal(false);
  const [isUpdatingNotifications, setIsUpdatingNotifications] = createSignal(false);
  const [isUpdatingUsername, setIsUpdatingUsername] = createSignal(false);
  const [usernameError, setUsernameError] = createSignal('');

  const validateAvatar = (file) => {
    if (!file) {
      return { valid: true };
    }

    if (!file.type.startsWith('image/')) {
      return { valid: false, error: 'Файл должен быть изображением' };
    }

    if (file.size > AVATAR_MAX_SIZE) {
      return { valid: false, error: 'Размер файла не должен превышать 10 МБ' };
    }

    return { valid: true };
  };

  const updateProfileField = async (field, value, accessToken) => {
    const formData = new FormData();
    formData.append(field, value);

    try {
      const response = await axios.patch(`${config.backendUrl}/api/v1/auth`, formData, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      if (response.status >= 400) {
        throw new Error(response.data?.message || 'Ошибка при обновлении');
      }

      return { success: true, data: response.data };
    } catch (error) {
      console.error(`Ошибка при обновлении ${field}:`, error);
      throw error;
    }
  };

  const updateName = async (name, accessToken) => {
    setIsUpdatingName(true);
    try {
      const result = await updateProfileField('name', name, accessToken);
      return { ...result, name: result.data?.name ?? name };
    } finally {
      setIsUpdatingName(false);
    }
  };

  const updateLastName = async (lastName, accessToken) => {
    setIsUpdatingLastName(true);
    try {
      const result = await updateProfileField('lastName', lastName, accessToken);
      return { ...result, lastName: result.data?.lastName ?? lastName };
    } finally {
      setIsUpdatingLastName(false);
    }
  };

  const updateAvatar = async (file, accessToken) => {
    const validation = validateAvatar(file);

    if (!validation.valid) {
      setAvatarError(validation.error);
      return { success: false, error: validation.error };
    }

    setAvatarError('');
    setIsUpdatingAvatar(true);

    try {
      const formData = new FormData();
      formData.append('avatar', file);

      const response = await axios.patch(`${config.backendUrl}/api/v1/auth`, formData, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      if (response.status >= 400) {
        throw new Error(response.data?.message || 'Ошибка при загрузке аватара');
      }

      const avatarUrl = response.data.avatar;
      return { success: true, avatar: avatarUrl };
    } catch (error) {
      console.error('Ошибка при загрузке аватара:', error);
      setAvatarError(error.message || 'Ошибка при загрузке аватара');
      return { success: false, error: error.message };
    } finally {
      setIsUpdatingAvatar(false);
    }
  };

  const updateNotifications = async (enabled, accessToken) => {
    setIsUpdatingNotifications(true);
    try {
      const formData = new FormData();
      formData.append('notifyEnabled', enabled.toString());

      const response = await axios.patch(`${config.backendUrl}/api/v1/auth`, formData, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      if (response.status >= 400) {
        throw new Error(response.data?.message || 'Ошибка при обновлении уведомлений');
      }

      return { success: true };
    } catch (error) {
      console.error('Ошибка при обновлении уведомлений:', error);
      throw error;
    } finally {
      setIsUpdatingNotifications(false);
    }
  };

  const updateUsername = async (username, accessToken) => {
    setIsUpdatingUsername(true);
    setUsernameError('');
    try {
      const formData = new FormData();
      formData.append('username', username);

      const response = await axios.patch(`${config.backendUrl}/api/v1/auth`, formData, {
        headers: {
          'Authorization': `Bearer ${accessToken}`,
        },
      });

      if (response.status >= 400) {
        const errorMessage = response.data?.message || 'Ошибка при обновлении username';
        setUsernameError(errorMessage);
        throw new Error(errorMessage);
      }

      return { success: true, username: response.data?.username ?? username };
    } catch (error) {
      if (error.response?.status === 409) {
        setUsernameError('Этот username уже занят');
      } else if (error.response?.status >= 400 && error.response?.status < 500) {
        setUsernameError(error.response?.data?.message || 'Ошибка при обновлении username');
      } else {
        setUsernameError(error.message || 'Ошибка при обновлении username');
      }
      throw error;
    } finally {
      setIsUpdatingUsername(false);
    }
  };

  const clearUsernameError = () => {
    setUsernameError('');
  };

  const clearAvatarError = () => {
    setAvatarError('');
  };

  const value = {
    avatarError,
    usernameError,
    isUpdatingAvatar,
    isUpdatingName,
    isUpdatingLastName,
    isUpdatingNotifications,
    isUpdatingUsername,
    validateAvatar,
    updateName,
    updateLastName,
    updateAvatar,
    updateNotifications,
    updateUsername,
    clearAvatarError,
    clearUsernameError,
  };

  return (
    <ProfileContext.Provider value={value}>
      {props.children}
    </ProfileContext.Provider>
  );
}

export function useProfile() {
  return useContext(ProfileContext);
}