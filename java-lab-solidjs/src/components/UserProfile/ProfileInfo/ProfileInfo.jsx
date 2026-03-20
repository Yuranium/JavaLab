import { createSignal } from 'solid-js';
import './ProfileInfo.css';

export default function ProfileInfo(props) {
  const [isEditingFirstName, setIsEditingFirstName] = createSignal(false);
  const [isEditingLastName, setIsEditingLastName] = createSignal(false);
  const [firstNameValue, setFirstNameValue] = createSignal(props.firstName || '');
  const [lastNameValue, setLastNameValue] = createSignal(props.lastName || '');

  const handleFirstNameSubmit = () => {
    setIsEditingFirstName(false);
    if (props.onNameUpdate) {
      props.onNameUpdate('firstName', firstNameValue());
    }
  };

  const handleLastNameSubmit = () => {
    setIsEditingLastName(false);
    if (props.onNameUpdate) {
      props.onNameUpdate('lastName', lastNameValue());
    }
  };

  const handleKeyDown = (e, submitFn) => {
    if (e.key === 'Enter') {
      submitFn();
    } else if (e.key === 'Escape') {
      setFirstNameValue(props.firstName || '');
      setLastNameValue(props.lastName || '');
      setIsEditingFirstName(false);
      setIsEditingLastName(false);
    }
  };

  return (
    <div class="profile-info">
      <div class="profile-info-section">
        <h3 class="profile-info-title">Личная информация</h3>

        <div class="profile-info-grid">
          <div class="profile-info-field">
            <label class="profile-info-label">Имя</label>
            {isEditingFirstName() ? (
              <div class="profile-info-editable">
                <input
                  type="text"
                  value={firstNameValue()}
                  onInput={(e) => setFirstNameValue(e.target.value)}
                  onBlur={handleFirstNameSubmit}
                  onKeyDown={(e) => handleKeyDown(e, handleFirstNameSubmit)}
                  class="profile-info-input"
                  placeholder="Введите имя"
                  autofocus
                />
              </div>
            ) : (
              <div
                class="profile-info-value profile-info-value--clickable"
                onClick={() => setIsEditingFirstName(true)}
              >
                {props.firstName || 'Не указано'}
                <svg class="profile-info-edit-icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
              </div>
            )}
          </div>

          <div class="profile-info-field">
            <label class="profile-info-label">Фамилия</label>
            {isEditingLastName() ? (
              <div class="profile-info-editable">
                <input
                  type="text"
                  value={lastNameValue()}
                  onInput={(e) => setLastNameValue(e.target.value)}
                  onBlur={handleLastNameSubmit}
                  onKeyDown={(e) => handleKeyDown(e, handleLastNameSubmit)}
                  class="profile-info-input"
                  placeholder="Введите фамилию"
                  autofocus
                />
              </div>
            ) : (
              <div
                class="profile-info-value profile-info-value--clickable"
                onClick={() => setIsEditingLastName(true)}
              >
                {props.lastName || 'Не указано'}
                <svg class="profile-info-edit-icon" viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
              </div>
            )}
          </div>

          <div class="profile-info-field">
            <label class="profile-info-label">Email</label>
            <div class="profile-info-value">{props.email}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
