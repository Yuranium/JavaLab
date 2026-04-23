import { For, Show } from 'solid-js';
import './TaskTestCases.css';

export default function TaskTestCases(props) {
  const testCases = () => props.testCases || [];
  const isAdmin = () => props.isAdmin || false;

  const visibleTestCases = () => {
    if (isAdmin()) {
      return testCases();
    }
    return testCases().filter(tc => !tc.isHidden);
  };

  return (
    <div class="task-test-cases">
      <h3 class="task-test-cases-title">Тест-кейсы</h3>

      <Show
        when={visibleTestCases().length > 0}
        fallback={
          <div class="task-test-cases-empty">
            <p>Тест-кейсы отсутствуют</p>
          </div>
        }
      >
        <div class="task-test-cases-list">
          <For each={visibleTestCases()}>
            {(testCase, index) => (
              <div class="task-test-case">
                <div class="task-test-case-header">
                  <span class="task-test-case-number">Тест #{index() + 1}</span>
                  <Show when={testCase.isHidden && isAdmin()}>
                    <span class="task-test-case-badge-hidden">Скрытый</span>
                  </Show>
                </div>
                <div class="task-test-case-content">
                  <div class="task-test-case-section">
                    <span class="task-test-case-label">Входные данные:</span>
                    <pre class="task-test-case-input">{testCase.input}</pre>
                  </div>
                  <div class="task-test-case-section">
                    <span class="task-test-case-label">Ожидаемый результат:</span>
                    <pre class="task-test-case-output">{testCase.expectedOutput}</pre>
                  </div>
                </div>
              </div>
            )}
          </For>
        </div>
      </Show>
    </div>
  );
}
