import { For, createMemo } from 'solid-js';
import './TaskTabs.css';

export default function TaskTabs(props) {
  const isAuthenticated = () => props.isAuthenticated || false;

  const tabs = createMemo(() => {
    const base = [{ id: 'description', label: 'Описание' }];
    if (isAuthenticated()) {
      base.push({ id: 'ai', label: 'AI' });
      base.push({ id: 'solutions', label: 'Мои решения' });
    }
    return base;
  });

  return (
    <div class="task-tabs">
      <div class="task-tabs-list">
        <For each={tabs()}>
          {(tab) => (
            <button
              class={`task-tab-btn ${props.activeTab === tab.id ? 'task-tab-btn--active' : ''}`}
              onClick={() => props.onTabChange(tab.id)}
            >
              {tab.label}
            </button>
          )}
        </For>
      </div>
    </div>
  );
}
