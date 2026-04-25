import { createSignal, For, Show, onMount } from 'solid-js';
import './TaskAttempts.css';

export default function TaskAttempts(props) {
  const [loading, setLoading] = createSignal(false);
  const [attempts, setAttempts] = createSignal([]);
  const taskId = props.taskId;
  void taskId;

  const stubData = [
    {
      isCorrect: true,
      code: `public class Solution {\n    public static void main(String[] args) {\n        System.out.println("Hello, world!");\n    }\n}\n`,
      createdAt: '2026-04-25T10:15:30.123Z'
    },
    {
      isCorrect: false,
      code: `public class Solution {\n    public static void main(String[] args) {\n        // wrong\n        for (int i = 0; i < 100; i++) { System.out.println(i); }\n    }\n}\n`,
      createdAt: '2026-04-24T18:05:00.000Z'
    }
  ];

  const fetchAttempts = async () => {
    setLoading(true);
    // simulate network delay
    setTimeout(() => {
      setAttempts(stubData);
      setLoading(false);
    }, 300);

    // const token = localStorage.getItem('access_token');
    // const url = `${config.backendUrl}/api/v1/progress/attempts?taskId=${props.taskId}`;
    // const resp = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
    // const data = await resp.json();
    // setAttempts(data);
  };

  onMount(() => {
    if (props && props.taskId) {
      console.debug('TaskAttempts mounted for taskId', props.taskId);
    }
    fetchAttempts();
  });

  const formatDate = (iso) => {
    try {
      const d = new Date(iso);
      return d.toLocaleDateString();
    } catch (e) {
      return iso;
    }
  };

  function AttemptItem(props) {
    const [expanded, setExpanded] = createSignal(false);
    return (
      <div class="attempt-card">
        <div class="attempt-status">
          <Show when={props.item.isCorrect} fallback={<svg class="status-icon status-fail" viewBox="0 0 24 24"><path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/></svg>}>
            <svg class="status-icon status-ok" viewBox="0 0 24 24"><path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/></svg>
          </Show>
        </div>

        <div class="attempt-body">
          <div class="attempt-meta">
            <span class="attempt-date">{formatDate(props.item.createdAt)}</span>
            <span class="attempt-result">{props.item.isCorrect ? 'Успешно' : 'Неудачно'}</span>
            <button class="attempt-expand-btn" onClick={() => setExpanded(!expanded())} aria-label="Показать код">
              <span class={"arrow " + (expanded() ? 'open' : '')}></span>
            </button>
          </div>

          <pre class={"attempt-code " + (expanded() ? 'expanded' : 'collapsed')}><code>{props.item.code}</code></pre>
        </div>
      </div>
    );
  }

  return (
    <div class="task-attempts">
      <div class="task-attempts-panel" role="region" aria-label="Мои решения">

        <Show when={loading()} fallback={
          <div class="task-attempts-list">
            <For each={attempts()}>
              {(item) => <AttemptItem item={item} />}
            </For>
          </div>
        }>
          <div class="task-attempts-loading">Загрузка...</div>
        </Show>
      </div>
    </div>
  );
}
