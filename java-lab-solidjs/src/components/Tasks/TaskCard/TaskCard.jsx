import { Show } from 'solid-js';
import { A } from '@solidjs/router';
import './TaskCard.css';

const difficultyColors = {
  EASY: { border: 'var(--difficulty-easy)', bg: 'var(--difficulty-easy-light)', text: 'var(--difficulty-easy)' },
  MEDIUM: { border: 'var(--difficulty-medium)', bg: 'var(--difficulty-medium-light)', text: 'var(--difficulty-medium)' },
  HARD: { border: 'var(--difficulty-hard)', bg: 'var(--difficulty-hard-light)', text: 'var(--difficulty-hard)' },
};

function formatDateToLocal(timestamp) {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function TaskCard(props) {
  const task = () => props.task;

  return (
    <A href={`/tasks/${task().idTask}`} class="task-card">
      <div class="task-card-header">
        <span class="task-card-id">#{task().idTask}</span>
        <div class="task-card-badges">
          <span
            class="task-card-badge task-card-badge--difficulty"
            style={{
              'border-color': difficultyColors[task().difficulty].border,
              'background-color': difficultyColors[task().difficulty].bg,
              'color': difficultyColors[task().difficulty].text,
            }}
          >
            {task().difficulty}
          </span>
        </div>
      </div>

      <h3 class="task-card-title">{task().title}</h3>

      <div class="task-card-categories">
        <Show when={task().categories && task().categories.length > 0}>
          {task().categories.map((category) => (
            <div class="task-card-tooltip-container">
              <span class="task-card-category">{category.title}</span>
              <div class="task-card-tooltip">
                <div class="task-card-tooltip-arrow"></div>
                <div class="task-card-tooltip-content">
                  <strong>{category.title}</strong>
                  <p>{category.description || 'Описание категории'}</p>
                </div>
              </div>
            </div>
          ))}
        </Show>
      </div>

      <div class="task-card-dates">
        <div class="task-card-date-item">
          <span class="task-card-date-label">Создана:</span>
          <span class="task-card-date-value">{formatDateToLocal(task().createdAt)}</span>
        </div>
        <div class="task-card-date-item">
          <span class="task-card-date-label">Обновлена:</span>
          <span class="task-card-date-value">{formatDateToLocal(task().updatedAt)}</span>
        </div>
      </div>
    </A>
  );
}
