import { createContext, useContext, createSignal } from 'solid-js';

const LoginContext = createContext();

const initialFormState = {
  username: '',
  password: '',
};

const initialErrorsState = {
  username: '',
  password: '',
  general: '',
};

export function LoginProvider(props) {
  const [formData, setFormData] = createSignal({ ...initialFormState });
  const [errors, setErrors] = createSignal({ ...initialErrorsState });
  const [isSubmitting, setIsSubmitting] = createSignal(false);

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

  const validateForm = () => {
    const currentFormData = formData();
    const newErrors = { ...initialErrorsState };
    let isValid = true;

    if (!currentFormData.username || currentFormData.username.trim() === '') {
      newErrors.username = 'Имя пользователя обязательно';
      isValid = false;
    }

    if (!currentFormData.password || currentFormData.password.trim() === '') {
      newErrors.password = 'Пароль обязателен';
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  const submitForm = async (auth) => {
    if (!validateForm()) {
      return false;
    }

    setIsSubmitting(true);
    setErrors(prev => ({ ...prev, general: '' }));

    try {
      const { username, password } = formData();
      
      const result = await auth.login(username, password);

      if (result.success) {
        setFormData({ ...initialFormState });
        setErrors({ ...initialErrorsState });
        return true;
      } else {
        setErrors(prev => ({
          ...prev,
          general: result.error || 'Ошибка при входе',
        }));
        return false;
      }
    } catch (error) {
      console.error('Ошибка при входе:', error);
      setErrors(prev => ({
        ...prev,
        general: 'Ошибка при входе. Попробуйте позже.',
      }));
      return false;
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setFormData({ ...initialFormState });
    setErrors({ ...initialErrorsState });
  };

  const value = {
    formData,
    errors,
    isSubmitting,
    updateField,
    submitForm,
    resetForm,
    validateForm,
  };

  return (
    <LoginContext.Provider value={value}>
      {props.children}
    </LoginContext.Provider>
  );
}

export function useLogin() {
  return useContext(LoginContext);
}