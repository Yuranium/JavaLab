import { createContext, useContext, createSignal } from 'solid-js';

const RegisterContext = createContext();

const initialFormState = {
  username: '',
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  avatar: null,
  notificationsEnabled: false,
  timezone: '',
};

const initialErrorsState = {
  username: '',
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  confirmPassword: '',
  avatar: '',
  general: '',
};

const AVATAR_MAX_SIZE = 10 * 1024 * 1024; // 10 МБ

export function RegisterProvider(props) {
  const [formData, setFormData] = createSignal({ ...initialFormState });
  const [errors, setErrors] = createSignal({ ...initialErrorsState });
  const [isSubmitting, setIsSubmitting] = createSignal(false);
  const [isSuccess, setIsSuccess] = createSignal(false);

  const initTimezone = () => {
    const offset = new Date().getTimezoneOffset();
    const hours = Math.floor(Math.abs(offset) / 60);
    const minutes = Math.abs(offset) % 60;
    const sign = offset <= 0 ? '+' : '-';
    const timezone = `UTC${sign}${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`;
    setFormData(prev => ({ ...prev, timezone }));
  };

  const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const isOnlySpaces = (value) => value && /^\s+$/.test(value);

  const clearError = (field) => {
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const updateField = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    clearError(field);
  };

  const updateAvatar = (file) => {
    if (!file) {
      setFormData(prev => ({ ...prev, avatar: null }));
      clearError('avatar');
      return;
    }

    if (!file.type.startsWith('image/')) {
      setFormData(prev => ({ ...prev, avatar: null }));
      setErrors(prev => ({ ...prev, avatar: 'Файл должен быть изображением' }));
      return;
    }

    if (file.size > AVATAR_MAX_SIZE) {
      setFormData(prev => ({ ...prev, avatar: null }));
      setErrors(prev => ({ ...prev, avatar: 'Размер файла не должен превышать 10 МБ' }));
      return;
    }

    setFormData(prev => ({ ...prev, avatar: file }));
    clearError('avatar');
  };

  const toggleNotifications = (enabled) => {
    setFormData(prev => ({ ...prev, notificationsEnabled: enabled }));
  };

  const validateForm = () => {
    const currentFormData = formData();
    const newErrors = { ...initialErrorsState };
    let isValid = true;

    if (!currentFormData.username || currentFormData.username.trim() === '') {
      newErrors.username = 'Имя пользователя обязательно';
      isValid = false;
    }

    if (isOnlySpaces(currentFormData.firstName)) {
      newErrors.firstName = 'Поле не должно состоять из пробелов';
      isValid = false;
    }

    if (isOnlySpaces(currentFormData.lastName)) {
      newErrors.lastName = 'Поле не должно состоять из пробелов';
      isValid = false;
    }

    if (!currentFormData.email || currentFormData.email.trim() === '') {
      newErrors.email = 'Email обязателен';
      isValid = false;
    } else if (!isValidEmail(currentFormData.email)) {
      newErrors.email = 'Введите корректный email';
      isValid = false;
    }

    if (!currentFormData.password) {
      newErrors.password = 'Пароль обязателен';
      isValid = false;
    } else if (currentFormData.password.length < 8) {
      newErrors.password = 'Пароль должен быть не менее 8 символов';
      isValid = false;
    }

    if (!currentFormData.confirmPassword) {
      newErrors.confirmPassword = 'Подтвердите пароль';
      isValid = false;
    } else if (currentFormData.password !== currentFormData.confirmPassword) {
      newErrors.confirmPassword = 'Пароли не совпадают';
      isValid = false;
    }

    if (currentFormData.avatar) {
      const file = currentFormData.avatar;
      if (!file.type.startsWith('image/')) {
        newErrors.avatar = 'Файл должен быть изображением';
        isValid = false;
      } else if (file.size > AVATAR_MAX_SIZE) {
        newErrors.avatar = 'Размер файла не должен превышать 10 МБ';
        isValid = false;
      }
    }

    setErrors(newErrors);
    return isValid;
  };

  const submitForm = async () => {
    if (!validateForm()) {
      return false;
    }

    setIsSubmitting(true);
    setIsSuccess(false);

    try {
      console.log('Отправка данных на сервер:', {
        ...formData(),
        avatar: formData().avatar?.name ?? null,
      });

      await new Promise(resolve => setTimeout(resolve, 1000));

      setIsSuccess(true);
      setFormData(prev => ({ ...initialFormState, timezone: prev.timezone }));
      setErrors({ ...initialErrorsState });
      initTimezone();

      return true;
    } catch (error) {
      setErrors(prev => ({ ...prev, general: 'Ошибка при регистрации. Попробуйте позже.' }));
      return false;
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setFormData({ ...initialFormState });
    setErrors({ ...initialErrorsState });
    setIsSuccess(false);
    initTimezone();
  };

  const value = {
    formData,
    errors,
    isSubmitting,
    isSuccess,
    updateField,
    updateAvatar,
    toggleNotifications,
    submitForm,
    resetForm,
    validateForm,
    initTimezone,
  };

  return (
    <RegisterContext.Provider value={value}>
      {props.children}
    </RegisterContext.Provider>
  );
}

export function useRegister() {
  return useContext(RegisterContext);
}
