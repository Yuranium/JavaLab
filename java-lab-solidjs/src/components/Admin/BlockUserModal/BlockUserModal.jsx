import { createSignal, createEffect, onCleanup, For, createMemo } from 'solid-js';
import { Portal } from 'solid-js/web';
import './BlockUserModal.css';

const BLOCK_DURATIONS = [
  { value: '1day', label: 'На 1 день' },
  { value: '3days', label: 'На 3 дня' },
  { value: '1month', label: 'На 1 месяц' },
  { value: 'permanent', label: 'Бессрочно' },
];

export default function BlockUserModal(props) {
  const [reason, setReason] = createSignal('');
  const [duration, setDuration] = createSignal('');

  const isFormValid = createMemo(() => {
    return reason().trim().length > 0 && duration().length > 0;
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

  const handleSubmit = () => {
    if (isFormValid()) {
      props.onConfirm({
        userId: props.user.id,
        reason: reason().trim(),
        duration: duration(),
      });
      handleClose();
    }
  };

  const handleClose = () => {
    setReason('');
    setDuration('');
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
            <h2 class="block-user-modal-title">Заблокировать пользователя</h2>
            <button class="block-user-modal-close" onClick={handleClose}>
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
                />
              </div>

              <div class="block-user-modal-field">
                <label class="block-user-modal-label" for="duration">
                  Срок блокировки <span class="necessary-action">*</span>
                </label>
                <select
                  id="duration"
                  class="block-user-modal-select"
                  value={duration()}
                  onChange={(e) => setDuration(e.target.value)}
                >
                  <option value="" disabled>Выберите срок</option>
                  <For each={BLOCK_DURATIONS}>
                    {(option) => (
                      <option value={option.value}>{option.label}</option>
                    )}
                  </For>
                </select>
              </div>
            </div>
          </div>

          <div class="block-user-modal-footer">
            <button
              class="block-user-modal-btn block-user-modal-btn--cancel"
              onClick={handleClose}
            >
              Отменить
            </button>
            <button
              class="block-user-modal-btn block-user-modal-btn--block"
              onClick={handleSubmit}
              disabled={!isFormValid()}
            >
              Заблокировать пользователя
            </button>
          </div>
        </div>
      </div>
    </Portal>
  );
}