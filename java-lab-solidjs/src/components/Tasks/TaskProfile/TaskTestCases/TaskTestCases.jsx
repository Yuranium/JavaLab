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
                  <div class={`task-test-case ${testCase.status === 'PASSED' ? 'passed' : testCase.status === 'FAILED' ? 'failed' : ''}`}>
                    <div class="task-test-case-header">
                      <div style={{ display: 'flex', gap: '8px', 'align-items': 'center' }}>
                        <span class="task-test-case-number">Тест #{index() + 1}</span>
                        <Show when={testCase.isHidden && isAdmin()}>
                          <span class="task-test-case-badge-hidden">Скрытый</span>
                        </Show>
                      </div>

                      <div>
                        <Show when={testCase.status}>
                          <span class={`task-test-case-status ${testCase.status === 'PASSED' ? 'passed' : testCase.status === 'FAILED' ? 'failed' : 'pending'}`}>
                            {testCase.status}
                          </span>
                        </Show>
                      </div>
                    </div>
                <div class="task-test-case-content">
                  <div class="task-test-case-section">
                    <span class="task-test-case-label">Входные данные:</span>
                    <pre class="task-test-case-input">{testCase.input ?? testCase.inputData ?? ''}</pre>
                  </div>

                  <Show when={testCase.status || testCase.output !== undefined || testCase.executionDuration !== undefined}>
                    <div class="task-test-case-section">
                      <span class="task-test-case-label">Результат выполнения:</span>
                      <pre class="task-test-case-output">{testCase.output ?? ''}</pre>
                      <Show when={testCase.error}>
                        <pre class="task-test-case-error">{testCase.error}</pre>
                      </Show>
                    </div>

                    <div class="task-test-case-section">
                      <span class="task-test-case-label">Ожидаемый результат:</span>
                      <pre class="task-test-case-output">{testCase.expectedOutput ?? testCase.exceptedOutput ?? ''}</pre>
                    </div>

                    <Show when={testCase.executionDuration !== undefined && testCase.executionDuration !== null}>
                      <div class="task-test-case-section">
                        <span class="task-test-case-label">Время выполнения:</span>
                        <div class="task-test-case-duration">{testCase.executionDuration} ms</div>
                      </div>
                    </Show>
                  </Show>

                  <Show when={!(testCase.status || testCase.output !== undefined || testCase.executionDuration !== undefined) && (testCase.expectedOutput ?? testCase.exceptedOutput)}>
                    <div class="task-test-case-section">
                      <span class="task-test-case-label">Ожидаемый результат:</span>
                      <pre class="task-test-case-output">{testCase.expectedOutput ?? testCase.exceptedOutput ?? ''}</pre>
                    </div>
                  </Show>
                </div>
              </div>
            )}
          </For>
        </div>
      </Show>
    </div>
  );
}
