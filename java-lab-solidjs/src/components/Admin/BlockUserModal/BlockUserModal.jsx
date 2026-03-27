import { createSignal, createEffect, createMemo, Show, For, onCleanup } from 'solid-js';
import { Portal } from 'solid-js/web';
import { config } from '../../../config';
import axios from 'axios';
import './BlockUserModal.css';

const BLOCK_DURATIONS = [
  { value: '30minutes', label: '30 минут' },
  { value: '1day', label: '1 день' },
  { value: '3days', label: '3 дня' },
];

const MODES = {
  block: {
    title: 'Заблокировать пользователя',
    submitLabel: 'Заблокировать пользователя',
    submittingLabel: 'Блокировка...',
    buttonClass: 'block-user-modal-btn--block',
  },
  unblock: {
    title: 'Разблокировать пользователя',
    submitLabel: 'Разблокировать пользователя',
    submittingLabel: 'Разблокировка...',
    buttonClass: 'block-user-modal-btn--unblock',
  },
};

const VALIDATION_ERRORS = {
  PAST_DATE: 'Выбрана прошедшая дата',
  SAME_DATETIME: 'Дата начала и окончания не могут совпадать',
  END_BEFORE_START: 'Дата окончания должна быть позже даты начала',
  PAST_UNLOCK_TIME: 'Выбранное время уже прошло',
};

export default function UserActionModal(props) {
  const mode = props.mode === 'unblock' ? 'unblock' : 'block';
  const modeConfig = MODES[mode];

  const [reason, setReason] = createSignal('');
  const [duration, setDuration] = createSignal('');
  const [startLockDate, setStartLockDate] = createSignal('');
  const [startLockTime, setStartLockTime] = createSignal('');
  const [endLockDate, setEndLockDate] = createSignal('');
  const [endLockTime, setEndLockTime] = createSignal('');
  const [unlockTimeDate, setUnlockTimeDate] = createSignal('');
  const [unlockTimeTime, setUnlockTimeTime] = createSignal('');
  const [isSubmitting, setIsSubmitting] = createSignal(false);
  const [error, setError] = createSignal('');
  const [validationErrors, setValidationErrors] = createSignal({
    startLock: '',
    endLock: '',
    unlockTime: '',
  });

  const combineDateTime = (date, time) => {
    if (!date || date.length === 0) return '';
    if (!time || time.length === 0) return date; // Только дата
    return `${date}T${time}`;
  };

  const isPastDate = (dateTimeString) => {
    if (!dateTimeString || dateTimeString.length === 0) return false;
    const inputDate = new Date(dateTimeString);
    const now = new Date();
    now.setSeconds(0, 0);
    now.setMilliseconds(0);
    inputDate.setSeconds(0, 0);
    inputDate.setMilliseconds(0);
    return inputDate < now;
  };

  const areDatesEqual = (date1, date2) => {
    if (!date1 || !date2 || date1.length === 0 || date2.length === 0) return false;
    const d1 = new Date(date1);
    const d2 = new Date(date2);
    d1.setSeconds(0, 0);
    d2.setSeconds(0, 0);
    return d1.getTime() === d2.getTime();
  };

  const validateCustomRange = () => {
    const errors = { startLock: '', endLock: '' };
    const startLockCombined = combineDateTime(startLockDate(), startLockTime());
    const endLockCombined = combineDateTime(endLockDate(), endLockTime());
    const hasStartLock = startLockDate().length > 0;
    const hasEndLock = endLockDate().length > 0;

    if (hasStartLock && isPastDate(startLockCombined)) {
      errors.startLock = VALIDATION_ERRORS.PAST_DATE;
    }

    if (hasEndLock && !errors.endLock && isPastDate(endLockCombined)) {
      errors.endLock = VALIDATION_ERRORS.PAST_DATE;
    }

    if (hasStartLock && hasEndLock && !errors.startLock && !errors.endLock) {
      if (startLockDate() === endLockDate()) {
        if (!startLockTime() && !endLockTime()) {
          errors.endLock = VALIDATION_ERRORS.SAME_DATETIME;
        } else if (startLockTime() && endLockTime()) {
          if (areDatesEqual(startLockCombined, endLockCombined)) {
            errors.endLock = VALIDATION_ERRORS.SAME_DATETIME;
          }
        }
      }
    }

    if (hasStartLock && hasEndLock && !errors.startLock && !errors.endLock) {
      const startDate = new Date(startLockCombined || new Date());
      const endDate = new Date(endLockCombined || new Date());
      if (endDate < startDate) {
        errors.endLock = VALIDATION_ERRORS.END_BEFORE_START;
      }
    }

    setValidationErrors(errors);
    return errors;
  };

  const validateUnlockTime = () => {
    const errors = { startLock: '', endLock: '', unlockTime: '' };
    const unlockTimeCombined = combineDateTime(unlockTimeDate(), unlockTimeTime());
    const hasUnlockTime = unlockTimeDate().length > 0;

    if (hasUnlockTime && isPastDate(unlockTimeCombined)) {
      errors.unlockTime = VALIDATION_ERRORS.PAST_UNLOCK_TIME;
    }

    setValidationErrors(errors);
    return errors;
  };

  createEffect(() => {
    if (mode === 'block') {
      const hasCustomRange = startLockDate().length > 0 || endLockDate().length > 0;
      if (hasCustomRange) {
        validateCustomRange();
      } else {
        setValidationErrors({ startLock: '', endLock: '', unlockTime: '' });
      }
    } else {
      const hasUnlockTime = unlockTimeDate().length > 0;
      if (hasUnlockTime) {
        validateUnlockTime();
      } else {
        setValidationErrors({ startLock: '', endLock: '', unlockTime: '' });
      }
    }
  });

  const isFormValid = createMemo(() => {
    if (mode === 'block') {
      const hasReason = reason().trim().length > 0;
      const hasDuration = duration().length > 0;
      const hasStartLock = startLockDate().length > 0;
      const hasEndLock = endLockDate().length > 0;
      const hasCustomRange = hasStartLock || hasEndLock;

      if (hasDuration) {
        return hasReason;
      }

      if (hasCustomRange) {
        const errors = validationErrors();
        const hasErrors = errors.startLock || errors.endLock;
        return hasReason && !hasErrors;
      }

      return hasReason;
    }

    const hasUnlockTime = unlockTimeDate().length > 0;
    if (hasUnlockTime) {
      const errors = validationErrors();
      return !errors.unlockTime;
    }
    return true;
  });

  createEffect(() => {
    if (props.isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  });

  onCleanup(() => {
    document.body.style.overflow = '';
  });

  const calculateEndLock = (start, durationValue) => {
    const startDate = start ? new Date(start) : new Date();
    const endDate = new Date(startDate);

    switch (durationValue) {
      case '30minutes':
        endDate.setMinutes(endDate.getMinutes() + 30);
        break;
      case '1day':
        endDate.setDate(endDate.getDate() + 1);
        break;
      case '3days':
        endDate.setDate(endDate.getDate() + 3);
        break;
      default:
        endDate.setDate(endDate.getDate() + 1);
    }

    return endDate.toISOString();
  };

  const formatToUTCTimestamp = (date, time) => {
    if (!date || date.length === 0) return null;

    const [year, month, day] = date.split('-').map(Number);
    
    if (!time || time.length === 0) {
      const localDate = new Date(year, month - 1, day, 0, 0, 0, 0);
      return localDate.toISOString();
    } else {
      const [hours, minutes] = time.split(':').map(Number);
      const localDate = new Date(year, month - 1, day, hours, minutes, 0, 0);
      return localDate.toISOString();
    }
  };

  const formatDateTimeToLocale = (date, time) => {
    if (!date) return '';
    const dateTimeString = time ? `${date}T${time}` : `${date}T00:00`;
    const dateTime = new Date(dateTimeString);
    return dateTime.toLocaleDateString('ru-RU', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const blockDurationHint = createMemo(() => {
    if (duration().length > 0) {
      const selected = BLOCK_DURATIONS.find(d => d.value === duration());
      if (selected) {
        return `Блокировка на ${selected.label.toLowerCase()}`;
      }
    }

    const hasStartLock = startLockDate().length > 0;
    const hasEndLock = endLockDate().length > 0;

    if (hasStartLock && hasEndLock) {
      return `Блокировка с ${formatDateTimeToLocale(startLockDate(), startLockTime())} по ${formatDateTimeToLocale(endLockDate(), endLockTime())}`;
    } else if (hasStartLock && !hasEndLock) {
      return `Блокировка с ${formatDateTimeToLocale(startLockDate(), startLockTime())}, без ограничения по сроку`;
    } else if (!hasStartLock && hasEndLock) {
      return `Блокировка до ${formatDateTimeToLocale(endLockDate(), endLockTime())}`;
    } else {
      return 'Блокировка без ограничения по сроку';
    }
  });

  const unlockHint = createMemo(() => {
    if (unlockTimeDate().length > 0) {
      return `Разблокировка ${formatDateTimeToLocale(unlockTimeDate(), unlockTimeTime())}`;
    }
    return 'Разблокировка произойдёт немедленно';
  });

  const handleSubmit = async () => {
    if (!isFormValid()) return;

    setIsSubmitting(true);
    setError('');

    let startLockUTC = null;
    let endLockUTC = null;
    let unlockTimeUTC = null;

    try {
      if (mode === 'block') {
        const hasStartLock = startLockDate().length > 0;
        const hasEndLock = endLockDate().length > 0;

        if (hasStartLock || hasEndLock) {
          if (hasStartLock && hasEndLock) {
            startLockUTC = formatToUTCTimestamp(startLockDate(), startLockTime());
            endLockUTC = formatToUTCTimestamp(endLockDate(), endLockTime());
          } else if (hasStartLock && !hasEndLock) {
            startLockUTC = formatToUTCTimestamp(startLockDate(), startLockTime());
            endLockUTC = null;
          } else if (!hasStartLock && hasEndLock) {
            startLockUTC = null;
            endLockUTC = formatToUTCTimestamp(endLockDate(), endLockTime());
          }
        } else if (duration().length > 0) {
          const now = new Date();
          startLockUTC = now.toISOString();
          endLockUTC = calculateEndLock(now, duration());
        } else {
          startLockUTC = null;
          endLockUTC = null;
        }

        const payload = {
          startLock: startLockUTC,
          endLock: endLockUTC,
          message: reason(),
        };

        await axios.post(
          `${config.backendUrl}/api/v1/user/access/${props.user.id}/lock`,
          payload,
          {
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${props.accessToken}`,
            },
          }
        );
      } else {
        const hasUnlockTime = unlockTimeDate().length > 0;
        unlockTimeUTC = hasUnlockTime 
          ? formatToUTCTimestamp(unlockTimeDate(), unlockTimeTime()) 
          : null;

        const payload = {
          unlockTime: unlockTimeUTC,
        };

        await axios.post(
          `${config.backendUrl}/api/v1/user/access/${props.user.id}/unlock`,
          payload,
          {
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${props.accessToken}`,
            },
          }
        );
      }

      if (props.onSuccess) {
        if (mode === 'block') {
          props.onSuccess(props.user.id, 'block', { startLock: startLockUTC, endLock: endLockUTC });
        } else {
          props.onSuccess(props.user.id, 'unblock', { unlockTime: unlockTimeUTC });
        }
      }

      handleClose();
    } catch (err) {
      if (err.response) {
        const status = err.response.status;
        if (status >= 400 && status < 500) {
          setError(err.response.data?.message || `Ошибка при ${mode === 'block' ? 'блокировке' : 'разблокировке'} (4xx)`);
        } else if (status >= 500) {
          setError('Ошибка сервера. Попробуйте позже (5xx)');
        }
      } else if (err.request) {
        setError('Нет ответа от сервера. Проверьте подключение к сети');
      } else {
        setError(err.message || `Ошибка при ${mode === 'block' ? 'блокировке' : 'разблокировке'}`);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    setReason('');
    setDuration('');
    setStartLockDate('');
    setStartLockTime('');
    setEndLockDate('');
    setEndLockTime('');
    setUnlockTimeDate('');
    setUnlockTimeTime('');
    setError('');
    setValidationErrors({ startLock: '', endLock: '', unlockTime: '' });
    setIsSubmitting(false);
    props.onClose();
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Escape') {
      handleClose();
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) {
      handleClose();
    }
  };

  if (!props.user) return null;

  return (
    <Portal>
      <div
        class="block-user-modal-backdrop"
        classList={{ visible: props.isOpen }}
        onClick={handleBackdropClick}
        onKeyDown={handleKeyDown}
        tabindex="-1"
      >
        <div class="block-user-modal" classList={{ visible: props.isOpen }}>
          <div class="block-user-modal-header">
            <h2 class="block-user-modal-title">{modeConfig.title}</h2>
            <button class="block-user-modal-close" onClick={handleClose} disabled={isSubmitting()}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <div class="block-user-modal-body">
            <div class="block-user-modal-user-info">
              <span class="block-user-modal-username">@{props.user.username}</span>
            </div>

            <div class="block-user-modal-form">
              <Show when={mode === 'block'}>
                <div class="block-user-modal-field">
                  <label class="block-user-modal-label" for="reason">
                    Причина блокировки <span class="necessary-action">*</span>
                  </label>
                  <textarea
                    id="reason"
                    class="block-user-modal-textarea"
                    placeholder="Укажите причину блокировки..."
                    value={reason()}
                    onInput={(e) => setReason(e.target.value)}
                    rows="4"
                    disabled={isSubmitting()}
                  />
                </div>

                <div class="block-user-modal-field">
                  <label class="block-user-modal-label">Срок блокировки</label>

                  <div class="block-user-modal-options">
                    <select
                      class="block-user-modal-select"
                      value={duration()}
                      onChange={(e) => {
                        const value = e.target.value;
                        setDuration(value);
                        if (value) {
                          setStartLockDate('');
                          setStartLockTime('');
                          setEndLockDate('');
                          setEndLockTime('');
                        }
                      }}
                      disabled={isSubmitting()}
                    >
                      <option value="" disabled>Выберите срок</option>
                      <For each={BLOCK_DURATIONS}>
                        {(option) => (
                          <option value={option.value}>{option.label}</option>
                        )}
                      </For>
                    </select>

                    <span class="block-user-modal-divider">или укажите кастомный диапазон</span>

                    <div class="block-user-modal-custom-interval">
                      <div class="block-user-modal-row">
                        <div class="block-user-modal-field block-user-modal-field--date">
                          <label class="block-user-modal-label block-user-modal-label--small">
                            Дата начала
                          </label>
                          <input
                            type="date"
                            class="block-user-modal-input"
                            classList={{ 'block-user-modal-input--error': validationErrors().startLock }}
                            value={startLockDate()}
                            onInput={(e) => {
                              setStartLockDate(e.target.value);
                              setDuration('');
                            }}
                            disabled={isSubmitting()}
                          />
                        </div>
                        <div class="block-user-modal-field block-user-modal-field--time">
                          <label class="block-user-modal-label block-user-modal-label--small">
                            Время начала
                          </label>
                          <input
                            type="time"
                            class="block-user-modal-input"
                            classList={{ 'block-user-modal-input--error': validationErrors().startLock }}
                            value={startLockTime()}
                            onInput={(e) => {
                              setStartLockTime(e.target.value);
                              setDuration('');
                            }}
                            disabled={isSubmitting()}
                          />
                        </div>
                      </div>
                      <Show when={validationErrors().startLock}>
                        <span class="block-user-modal-error-text">{validationErrors().startLock}</span>
                      </Show>

                      <div class="block-user-modal-row">
                        <div class="block-user-modal-field block-user-modal-field--date">
                          <label class="block-user-modal-label block-user-modal-label--small">
                            Дата окончания
                          </label>
                          <input
                            type="date"
                            class="block-user-modal-input"
                            classList={{ 'block-user-modal-input--error': validationErrors().endLock }}
                            value={endLockDate()}
                            onInput={(e) => {
                              setEndLockDate(e.target.value);
                              setDuration('');
                            }}
                            disabled={isSubmitting()}
                          />
                        </div>
                        <div class="block-user-modal-field block-user-modal-field--time">
                          <label class="block-user-modal-label block-user-modal-label--small">
                            Время окончания
                          </label>
                          <input
                            type="time"
                            class="block-user-modal-input"
                            classList={{ 'block-user-modal-input--error': validationErrors().endLock }}
                            value={endLockTime()}
                            onInput={(e) => {
                              setEndLockTime(e.target.value);
                              setDuration('');
                            }}
                            disabled={isSubmitting()}
                          />
                        </div>
                      </div>
                      <Show when={validationErrors().endLock}>
                        <span class="block-user-modal-error-text">{validationErrors().endLock}</span>
                      </Show>
                    </div>

                    <Show when={blockDurationHint()}>
                      <span class="block-user-modal-hint">{blockDurationHint()}</span>
                    </Show>
                  </div>
                </div>
              </Show>

              <Show when={mode === 'unblock'}>
                <div class="block-user-modal-field">
                  <label class="block-user-modal-label" for="unlockTime">
                    Время разблокировки
                  </label>
                  <div class="block-user-modal-row">
                    <div class="block-user-modal-field block-user-modal-field--date">
                      <label class="block-user-modal-label block-user-modal-label--small">
                        Дата
                      </label>
                      <input
                        type="date"
                        id="unlockTime"
                        class="block-user-modal-input"
                        classList={{ 'block-user-modal-input--error': validationErrors().unlockTime }}
                        value={unlockTimeDate()}
                        onInput={(e) => {
                          setUnlockTimeDate(e.target.value);
                        }}
                        disabled={isSubmitting()}
                      />
                    </div>
                    <div class="block-user-modal-field block-user-modal-field--time">
                      <label class="block-user-modal-label block-user-modal-label--small">
                        Время
                      </label>
                      <input
                        type="time"
                        class="block-user-modal-input"
                        classList={{ 'block-user-modal-input--error': validationErrors().unlockTime }}
                        value={unlockTimeTime()}
                        onInput={(e) => {
                          setUnlockTimeTime(e.target.value);
                        }}
                        disabled={isSubmitting()}
                      />
                    </div>
                  </div>
                  <Show when={validationErrors().unlockTime}>
                    <span class="block-user-modal-error-text">{validationErrors().unlockTime}</span>
                  </Show>
                  <Show when={!validationErrors().unlockTime}>
                    <span class="block-user-modal-hint">
                      {unlockHint()}
                    </span>
                  </Show>
                </div>
              </Show>

              <Show when={error()}>
                <div class="block-user-modal-error">
                  <svg class="block-user-modal-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  <span class="block-user-modal-error-message">{error()}</span>
                </div>
              </Show>
            </div>
          </div>

          <div class="block-user-modal-footer">
            <button
              class="block-user-modal-btn block-user-modal-btn--cancel"
              onClick={handleClose}
              disabled={isSubmitting()}
            >
              Отменить
            </button>
            <button
              class={`block-user-modal-btn ${modeConfig.buttonClass}`}
              onClick={handleSubmit}
              disabled={!isFormValid() || isSubmitting()}
            >
              {isSubmitting() ? (
                <span class="block-user-modal-btn-loading">
                  <svg class="block-user-modal-spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10" stroke-opacity="0.25"/>
                    <path d="M12 2a10 10 0 0 1 10 10" stroke-linecap="round"/>
                  </svg>
                  {modeConfig.submittingLabel}
                </span>
              ) : (
                modeConfig.submitLabel
              )}
            </button>
          </div>
        </div>
      </div>
    </Portal>
  );
}
