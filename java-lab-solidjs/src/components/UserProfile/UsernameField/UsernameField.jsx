import { createSignal, createEffect, Show } from 'solid-js';
import './UsernameField.css';

export default function UsernameField(props) {
  const [isEditing, setIsEditing] = createSignal(false);
  const [inputValue, setInputValue] = createSignal(props.username || '');

  createEffect(() => {
    setInputValue(props.username || '');
  });

  const handleSubmit = async () => {
    const newValue = inputValue().trim();

    if (!newValue) {
      setIsEditing(false);
      return;
    }

    if (newValue === props.username) {
      setIsEditing(false);
      return;
    }

    try {
      await props.onUsernameUpdate(newValue);
      setIsEditing(false);
    } catch (err) {
      setInputValue(props.username || '');
      setIsEditing(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleSubmit();
    } else if (e.key === 'Escape') {
      setInputValue(props.username || '');
      setIsEditing(false);
    }
  };

  const handleInput = (e) => {
    let value = e.target.value;
    if (value.startsWith('@')) {
      value = value.slice(1);
    }
    setInputValue(value);
    if (props.clearUsernameError && props.usernameError) {
      props.clearUsernameError();
    }
  };

  return (
    <div class="username-field">
      <label class="username-label">Имя пользователя</label>
      {!isEditing() ? (
        <div
          class="username-value"
          title="Изменить юзернейм"
          onClick={() => {
            if (props.clearUsernameError) {
              props.clearUsernameError();
            }
            setIsEditing(true);
          }}
        >
          <span class="username-prefix">@</span>
          <span class="username-text">{props.username || '—'}</span>
          <svg class="username-edit-icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
          </svg>
        </div>
      ) : (
        <div class="username-editable">
          <div class="username-input-wrapper">
            <span class="username-input-prefix">@</span>
            <input
              type="text"
              value={inputValue()}
              onInput={handleInput}
              onBlur={handleSubmit}
              onKeyDown={handleKeyDown}
              class="username-input"
              placeholder="Введите username"
              autofocus
              disabled={props.isUpdatingUsername}
            />
          </div>
          <Show when={props.isUpdatingUsername}>
            <span class="username-loading">Сохранение...</span>
          </Show>
        </div>
      )}
      <Show when={props.usernameError}>
        <div class="username-error">{props.usernameError}</div>
      </Show>
    </div>
  );
}
