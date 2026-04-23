import { createSignal, createEffect, Show } from 'solid-js';
import { useParams, useNavigate } from '@solidjs/router';
import { config } from '../config';
import { useAuth } from '../context/AuthContext';
import ResizablePanel from '../components/Tasks/TaskProfile/ResizablePanel/ResizablePanel';
import TaskTabs from '../components/Tasks/TaskProfile/TaskTabs/TaskTabs';
import TaskDescription from '../components/Tasks/TaskProfile/TaskDescription/TaskDescription';
import TaskTestCases from '../components/Tasks/TaskProfile/TaskTestCases/TaskTestCases';
import TaskCodeEditor from '../components/Tasks/TaskProfile/TaskCodeEditor/TaskCodeEditor';
import './TaskProfilePage.css';

export default function TaskProfilePage() {
  const params = useParams();
  const navigate = useNavigate();
  const auth = useAuth();

  const [task, setTask] = createSignal(null);
  const [isLoading, setIsLoading] = createSignal(true);
  const [error, setError] = createSignal('');
  const [activeTab, setActiveTab] = createSignal('description');

  const isAdmin = () => auth.hasRole(auth.ROLES.ADMIN);

  const loadTask = async () => {
    setIsLoading(true);
    setError('');

    try {
      const response = await fetch(`${config.backendUrl}/api/v1/task/${params.id}`);

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(
          errorData?.message || `Ошибка загрузки: ${response.status} ${response.statusText}`
        );
      }

      const data = await response.json();
      setTask(data);
    } catch (err) {
      setError(err.message || 'Неизвестная ошибка');
    } finally {
      setIsLoading(false);
    }
  };

  createEffect(() => {
    if (params.id) {
      loadTask();
    }
  });

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
                  <TaskDescription task={task()} />
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
                    <svg class="task-profile-placeholder-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
                      <rect x="9" y="3" width="6" height="4" rx="1" />
                      <path d="M9 14l2 2 4-4" />
                    </svg>
                    <p>Мои решения</p>
                    <p class="task-profile-placeholder-subtext">
                      Здесь будут отображаться ваши решения задачи
                    </p>
                  </div>
                </Show>
              </ResizablePanel>

              <ResizablePanel initialWidth={50} minWidth={30} maxWidth={70}>
                <div class="task-profile-right-panel">
                  <div class="task-profile-code-section">
                    <TaskCodeEditor
                      starterCode={task()?.starterCode}
                    />
                  </div>

                  <Show when={activeTab() === 'description'}>
                    <div class="task-profile-testcases-section">
                      <TaskTestCases
                        testCases={task()?.testCases || []}
                        isAdmin={isAdmin()}
                      />
                    </div>
                  </Show>

                  <Show when={activeTab() !== 'description'}>
                    <div class="task-profile-placeholder task-profile-placeholder--compact">
                      <p>Переключитесь на вкладку "Описание" для просмотра тест-кейсов</p>
                    </div>
                  </Show>
                </div>
              </ResizablePanel>
            </div>
          </div>
        </Show>
      </Show>
    </div>
  );
}
