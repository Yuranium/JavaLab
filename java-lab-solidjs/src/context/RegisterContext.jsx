import { createContext, useContext, createSignal, createEffect } from 'solid-js';
import { config } from '../config';
import axios from 'axios';

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
  const [isVerificationSent, setIsVerificationSent] = createSignal(false);
  const [verificationCode, setVerificationCode] = createSignal('');
  const [verificationError, setVerificationError] = createSignal('');
  const [isVerifying, setIsVerifying] = createSignal(false);
  const [resendTimer, setResendTimer] = createSignal(60);
  const [userId, setUserId] = createSignal(null);
  const [userCredentials, setUserCredentials] = createSignal({ username: '', password: '' });
  let wasVerificationSent = false;
  let timerInterval = null;

  const initTimezone = () => {
    const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    setFormData(prev => ({ ...prev, timezone }));
  };

  createEffect(() => {
    const isSent = isVerificationSent();
    
    if (isSent && !wasVerificationSent) {
      wasVerificationSent = true;
      
      if (timerInterval) {
        clearInterval(timerInterval);
      }
      
      timerInterval = setInterval(() => {
        setResendTimer(prev => {
          if (prev <= 1) {
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } 
    else if (!isSent && wasVerificationSent) {
      wasVerificationSent = false;
      
      if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
      }
      setResendTimer(60);
    }
  });

  const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const isOnlySpaces = (value) => value && /^\s+$/.test(value);

  const clearError = (field) => {
    setErrors(prev => ({ ...prev, [field]: '' }));
  };

  const updateField = (field, value) => {
    if (field === 'username' && value) {
      value = value.replace(/^@+/, '');
    }
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
      const currentFormData = formData();
            
      const formDataToSend = new FormData();
      formDataToSend.append('username', currentFormData.username);
      formDataToSend.append('name', currentFormData.firstName);
      formDataToSend.append('lastName', currentFormData.lastName);
      formDataToSend.append('password', currentFormData.password);
      formDataToSend.append('email', currentFormData.email);
      formDataToSend.append('notifyEnabled', currentFormData.notificationsEnabled.toString());
      formDataToSend.append('timezone', currentFormData.timezone);
      
      if (currentFormData.avatar) {
        formDataToSend.append('avatar', currentFormData.avatar);
      }

      console.log('Отправка данных на сервер:', {
        username: currentFormData.username,
        name: currentFormData.firstName,
        lastName: currentFormData.lastName,
        email: currentFormData.email,
        notifyEnabled: currentFormData.notificationsEnabled,
        timezone: currentFormData.timezone,
        avatar: currentFormData.avatar?.name ?? null,
      });

      const response = await axios.post(`${config.backendUrl}/api/v1/user`, formDataToSend);

      if (response.status >= 400) {
        throw new Error(response.data?.message || 'Ошибка при регистрации');
      }

      const result = response.data;
      console.log('Ответ от сервера:', result);

      const username = result.username || currentFormData.username;
      
      localStorage.setItem('username', username);
      setUserId(username); // setUserId для обратной совместимости, но сохраняем username

      setUserCredentials({
        username: currentFormData.username,
        password: currentFormData.password,
      });

      setIsSuccess(true);
      setIsVerificationSent(true);
      setFormData(prev => ({ ...initialFormState, timezone: prev.timezone }));
      setErrors({ ...initialErrorsState });
      initTimezone();

      return true;
    } catch (error) {
      console.error('Ошибка при регистрации:', error);
      
      if (error.response?.status === 409) {
        const errorMessage = error.response.data?.message || 'Данный username или email уже используется';
        setErrors(prev => ({
          ...prev,
          general: errorMessage
        }));
      } else {
        setErrors(prev => ({
          ...prev,
          general: error.message || 'Ошибка при регистрации. Попробуйте позже.'
        }));
      }
      return false;
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setFormData({ ...initialFormState });
    setErrors({ ...initialErrorsState });
    setIsSuccess(false);
    setIsVerificationSent(false);
    setVerificationCode('');
    setVerificationError('');
    setIsVerifying(false);
    setResendTimer(60);
    setUserCredentials({ username: '', password: '' });
    initTimezone();
  };

  const getUserCredentials = () => {
    return userCredentials();
  };

  const submitVerificationCode = async (code) => {
    const credentials = userCredentials();
    const username = credentials?.username;

    if (!username) {
      setVerificationError('Пользователь не найден');
      return false;
    }

    setIsVerifying(true);
    setVerificationError('');

    try {
      const response = await axios.post(
        `${config.backendUrl}/api/v1/auth/${username}/verify-account?code=${code}`
      );

      if (response.status >= 400) {
        throw new Error(response.data?.message || 'Неверный код подтверждения');
      }

      const result = response.data;
      console.log('Ответ от сервера:', result);

      return true;
    } catch (error) {
      console.error('Ошибка при проверке кода:', error);
      setVerificationError(error.message || 'Ошибка при проверке кода. Попробуйте позже.');
      return false;
    } finally {
      setIsVerifying(false);
    }
  };

  const resendVerificationCode = async () => {
    if (resendTimer() > 0) {
      return false;
    }

    const credentials = userCredentials();
    const username = credentials?.username;

    if (!username) {
      setVerificationError('Пользователь не найден');
      return false;
    }

    try {
      console.log('Повторная отправка кода подтверждения:', {
        username: username,
      });

      const response = await axios.post(
        `${config.backendUrl}/api/v1/auth/${username}/resend-verification`
      );

      if (response.status >= 400) {
        throw new Error(response.data?.message || 'Ошибка при отправке кода');
      }

      setResendTimer(60);
      setVerificationError('');
      return true;
    } catch (error) {
      console.error('Ошибка при повторной отправке кода:', error);
      setVerificationError(error.message || 'Ошибка при отправке кода. Попробуйте позже.');
      return false;
    }
  };

  const updateVerificationCode = (code) => {
    setVerificationCode(code);
    setVerificationError('');
  };

  const resetVerification = () => {
    setIsVerificationSent(false);
    setVerificationCode('');
    setVerificationError('');
    setIsVerifying(false);
    setResendTimer(60);
  };

  const value = {
    formData,
    errors,
    isSubmitting,
    isSuccess,
    isVerificationSent,
    verificationCode,
    verificationError,
    isVerifying,
    resendTimer,
    userId,
    userCredentials,
    updateField,
    updateAvatar,
    toggleNotifications,
    submitForm,
    resetForm,
    validateForm,
    initTimezone,
    submitVerificationCode,
    resendVerificationCode,
    updateVerificationCode,
    resetVerification,
    getUserCredentials,
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
