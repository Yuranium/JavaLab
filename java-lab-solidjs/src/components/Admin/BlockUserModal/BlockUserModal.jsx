import { createSignal, createEffect, createMemo, Show, For, onCleanup } from 'solid-js';
import { Portal } from 'solid-js/web';
import { config } from '../../../config';
import axios from 'axios';
import './BlockUserModal.css';

const BLOCK_DURATIONS = [
  { value: '30minutes', label: 'На 30 минут' },
  { value: '1day', label: 'На 1 день' },
  { value: '3days', label: 'На 3 дня' },
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

export default function UserActionModal(props) {
  const mode = props.mode === 'unblock' ? 'unblock' : 'block';
  const modeConfig = MODES[mode];

  const [reason, setReason] = createSignal('');
  const [duration, setDuration] = createSignal('');
  const [startLock, setStartLock] = createSignal('');
  const [endLock, setEndLock] = createSignal('');
  const [unlockTime, setUnlockTime] = createSignal('');
  const [isSubmitting, setIsSubmitting] = createSignal(false);
  const [error, setError] = createSignal('');

  const isFormValid = createMemo(() => {
    if (mode === 'block') {
      const hasReason = reason().trim().length > 0;
      const hasDuration = duration().length > 0;
      const hasStartLock = startLock().length > 0;
      const hasEndLock = endLock().length > 0;
      const hasCustomRange = hasStartLock || hasEndLock;
      return hasReason && (hasDuration || hasCustomRange || (!hasDuration && !hasCustomRange));
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

  createEffect(() => {
    if (duration().length > 0) {
      setStartLock('');
      setEndLock('');
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

  const formatToUTCTimestamp = (dateTimeString) => {
    if (!dateTimeString) return null;
    const date = new Date(dateTimeString);
    return date.toISOString();
  };

  const handleSubmit = async () => {
    if (!isFormValid()) return;

    setIsSubmitting(true);
    setError('');

    try {
      if (mode === 'block') {
        let startLockUTC = null;
        let endLockUTC = null;

        if (duration().length > 0) {
          const now = new Date();
          startLockUTC = now.toISOString();
          endLockUTC = calculateEndLock(now, duration());
        } else {
          const hasStartLock = startLock().length > 0;
          const hasEndLock = endLock().length > 0;

          if (hasStartLock && hasEndLock) {
            startLockUTC = formatToUTCTimestamp(startLock());
            endLockUTC = formatToUTCTimestamp(endLock());
          } else if (hasStartLock && !hasEndLock) {
            startLockUTC = formatToUTCTimestamp(startLock());
            endLockUTC = null;
          } else if (!hasStartLock && hasEndLock) {
            startLockUTC = null;
            endLockUTC = formatToUTCTimestamp(endLock());
          } else {
            startLockUTC = null;
            endLockUTC = null;
          }
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
        const hasUnlockTime = unlockTime().length > 0;
        const unlockTimeUTC = hasUnlockTime ? formatToUTCTimestamp(unlockTime()) : null;

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

      handleClose();
      if (props.onSuccess) {
        props.onSuccess(props.user.id);
      }
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
    setStartLock('');
    setEndLock('');
    setUnlockTime('');
    setError('');
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
                      onChange={(e) => setDuration(e.target.value)}
                      disabled={isSubmitting() || startLock().length > 0 || endLock().length > 0}
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
                      <div class="block-user-modal-field">
                        <label class="block-user-modal-label block-user-modal-label--small">
                          Начало блокировки
                        </label>
                        <input
                          type="datetime-local"
                          class="block-user-modal-input"
                          value={startLock()}
                          onInput={(e) => setStartLock(e.target.value)}
                          disabled={isSubmitting() || duration().length > 0}
                        />
                      </div>
                      <div class="block-user-modal-field">
                        <label class="block-user-modal-label block-user-modal-label--small">
                          Окончание блокировки
                        </label>
                        <input
                          type="datetime-local"
                          class="block-user-modal-input"
                          value={endLock()}
                          onInput={(e) => setEndLock(e.target.value)}
                          disabled={isSubmitting() || duration().length > 0}
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </Show>

              <Show when={mode === 'unblock'}>
                <div class="block-user-modal-field">
                  <label class="block-user-modal-label" for="unlockTime">
                    Время разблокировки
                  </label>
                  <input
                    type="datetime-local"
                    id="unlockTime"
                    class="block-user-modal-input"
                    value={unlockTime()}
                    onInput={(e) => setUnlockTime(e.target.value)}
                    disabled={isSubmitting()}
                    placeholder="Оставьте пустым для немедленной разблокировки"
                  />
                  <span class="block-user-modal-hint">
                    Если не указано, разблокировка произойдёт немедленно
                  </span>
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
