import { createSignal, createEffect, Show } from 'solid-js';
import { useParams, useNavigate } from '@solidjs/router';
import axios from 'axios';
import { config } from '../config';
import { useAuth } from '../context/AuthContext';
import ResizablePanel from '../components/Tasks/TaskProfile/ResizablePanel/ResizablePanel';
import TaskTabs from '../components/Tasks/TaskProfile/TaskTabs/TaskTabs';
import TaskDescription from '../components/Tasks/TaskProfile/TaskDescription/TaskDescription';
import TaskEditForm from '../components/Tasks/TaskProfile/TaskEditForm/TaskEditForm';
import TaskTestCases from '../components/Tasks/TaskProfile/TaskTestCases/TaskTestCases';
import TaskCodeEditor from '../components/Tasks/TaskProfile/TaskCodeEditor/TaskCodeEditor';
import TaskAttempts from '../components/Tasks/TaskProfile/TaskAttempts/TaskAttempts';
import './TaskProfilePage.css';

export default function TaskProfilePage() {
  const params = useParams();
  const navigate = useNavigate();
  const auth = useAuth();

  const [task, setTask] = createSignal(null);
  const [isLoading, setIsLoading] = createSignal(true);
  const [error, setError] = createSignal('');
  const [activeTab, setActiveTab] = createSignal('description');
  const [liveTestCases, setLiveTestCases] = createSignal([]);
  const [taskResultStatus, setTaskResultStatus] = createSignal(null);
  const [taskError, setTaskError] = createSignal(null);

  const [isEditMode, setIsEditMode] = createSignal(false);
  const [editTask, setEditTask] = createSignal(null);

  const isAdmin = () => auth.hasRole(auth.ROLES.ADMIN);

  const loadTask = async () => {
    setIsLoading(true);
    setError('');

    try {
      let data;
      if (isAdmin()) {
        const token = localStorage.getItem('access_token');
        const response = await axios.get(`${config.backendUrl}/api/v1/task/${params.id}/admin`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        data = response.data;
      } else {
        const response = await fetch(`${config.backendUrl}/api/v1/task/${params.id}`);
        if (!response.ok) {
          const errorData = await response.json().catch(() => null);
          throw new Error(
            errorData?.message || `Ошибка загрузки: ${response.status} ${response.statusText}`
          );
        }
        data = await response.json();
      }
      setTask(data);
      setLiveTestCases(data.testCases || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Неизвестная ошибка');
    } finally {
      setIsLoading(false);
    }
  };

  const handleExecutionMessage = (msg) => {
    if (!msg || !msg.type) return;
    if (msg.type === 'TEST_RESULT') {
      const payload = msg.payload || {};
      const number = payload.testNumber;
      if (!number) return;
      const prev = liveTestCases() || [];
      const copy = prev.map(tc => ({ ...tc }));
      const idx = number - 1;
      if (idx >= 0 && idx < copy.length) {
        copy[idx] = {
          ...copy[idx],
          status: payload.status,
          output: payload.output,
          expectedOutput: payload.exceptedOutput ?? payload.expectedOutput ?? copy[idx].expectedOutput,
          error: payload.error,
          executionDuration: payload.executionDuration
        };
        setLiveTestCases(copy);
      }
    } else if (msg.type === 'FINAL_RESULT') {
      const payload = msg.payload || {};
      if (payload.status) {
        setTaskResultStatus(payload.status);
      }
      if (payload.error) setTaskError(payload.error);
      else setTaskError(null);
      if (payload.testCases && Array.isArray(payload.testCases)) {
        const existing = liveTestCases() || [];
        const mapped = payload.testCases.map((tc, idx) => ({
          input: tc.input ?? tc.inputData ?? (existing[idx] && (existing[idx].input ?? existing[idx].inputData)) ?? '',
          expectedOutput: tc.exceptedOutput ?? tc.expectedOutput ?? (existing[idx] && existing[idx].expectedOutput) ?? '',
          status: tc.status,
          output: tc.output,
          error: tc.error,
          executionDuration: tc.executionDuration,
          isHidden: tc.isHidden || false
        }));
        setLiveTestCases(mapped);
      }
    }
  };

  createEffect(() => {
    if (params.id) {
      loadTask();
    }
  });

  const handleToggleEditMode = (enabled) => {
    if (!enabled) {
      setIsEditMode(false);
      setEditTask(null);
      return;
    }
    setEditTask(task());
    setIsEditMode(true);
  };

  const handleEditSuccess = () => {
    setIsEditMode(false);
    setEditTask(null);
    loadTask();
  };

  const handleBack = () => {
    navigate('/tasks');
  };

  return (
    <div class="task-profile-page">
      <div class="task-profile-header">
        <button class="task-profile-back-btn" onClick={handleBack}>
          <svg class="task-profile-back-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 12H5" />
            <path d="M12 19l-7-7 7-7" />
          </svg>
          Назад к задачам
        </button>
      </div>

      <Show when={error()}>
        <div class="task-profile-error">
          <svg class="task-profile-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <span class="task-profile-error-text">{error()}</span>
        </div>
      </Show>

      <Show
        when={!isLoading()}
        fallback={
          <div class="task-profile-loading">
            <div class="task-profile-spinner"></div>
          </div>
        }
      >
        <Show when={task()}>
          <div class="task-profile-content">
            <div class="task-profile-panels">
              <ResizablePanel initialWidth={50} minWidth={30} maxWidth={70}>
                <TaskTabs
                  activeTab={activeTab()}
                  onTabChange={setActiveTab}
                  isAuthenticated={auth.isAuthenticated()}
                />

                <Show when={activeTab() === 'description'}>
                  <Show when={isAdmin()}>
                    <div class="task-profile-edit-bar">
                      <label class="task-profile-toggle">
                        <span class="task-profile-toggle-text">
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                          </svg>
                          Режим редактирования
                        </span>
                        <span class="task-profile-switch">
                          <input
                            type="checkbox"
                            class="task-profile-switch-input"
                            checked={isEditMode()}
                            onChange={(e) => handleToggleEditMode(e.target.checked)}
                            />
                          <span class="task-profile-switch-track">
                            <span class="task-profile-switch-thumb" />
                          </span>
                        </span>
                      </label>
                    </div>
                  </Show>

                  <Show when={isEditMode() && editTask()}>
                    <TaskEditForm
                      task={editTask()}
                      taskId={params.id}
                      onSuccess={handleEditSuccess}
                    />
                  </Show>

                  <Show when={!isEditMode()}>
                    <TaskDescription task={task()} />
                  </Show>
                </Show>

                <Show when={activeTab() === 'ai' && auth.isAuthenticated()}>
                  <div class="task-profile-placeholder">
                    <svg class="task-profile-placeholder-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M12 2a4 4 0 0 1 4 4c0 1.95-1.4 3.58-3.25 3.93" />
                      <path d="M8.56 6.22A4 4 0 0 1 12 2" />
                      <path d="M12 18a8 8 0 0 0 8-8" />
                      <path d="M12 18a8 8 0 0 1-8-8" />
                      <path d="M12 18v4" />
                      <circle cx="12" cy="10" r="2" />
                    </svg>
                    <p>AI-помощник скоро будет доступен</p>
                    <p class="task-profile-placeholder-subtext">
                      В будущем здесь появится возможность получать помощь от AI
                    </p>
                  </div>
                </Show>

                <Show when={activeTab() === 'solutions' && auth.isAuthenticated()}>
                  <div class="task-profile-placeholder">
                    <TaskAttempts taskId={params.id} />
                  </div>
                </Show>
              </ResizablePanel>

              <ResizablePanel initialWidth={50} minWidth={30} maxWidth={70}>
                <div class="task-profile-right-panel">
                  <div class="task-profile-code-section">
                    <TaskCodeEditor
                      starterCode={task()?.starterCode}
                      onExecutionMessage={handleExecutionMessage}
                    />
                  </div>
                    <div class="task-profile-testcases-section">
                      <TaskTestCases
                        testCases={liveTestCases() || []}
                        isAdmin={isAdmin()}
                        taskStatus={taskResultStatus()}
                        error={taskError()}
                      />
                    </div>
                </div>
              </ResizablePanel>
            </div>
          </div>
        </Show>
      </Show>
    </div>
  );
}
