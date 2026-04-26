import { createSignal, createResource, For, Show } from 'solid-js';
import { createStore } from 'solid-js/store';
import axios from 'axios';
import { config } from '../../../../config';
import '../../../Tasks/CreateTask/CreateTaskModal.css';
import './TaskEditForm.css';

const DEFAULT_STARTER_CODE = `public class Main {
    public static void solve() {
        System.out.println("Hello, World!");
    }
}`;

let tcIdCounter = 5000;
const newTestCase = () => ({ id: ++tcIdCounter, input: '', expectedOutput: '', isHidden: true });

const taskToTcItems = (testCases) =>
  (testCases ?? []).map((tc) => ({
    id: ++tcIdCounter,
    input: tc.input ?? '',
    expectedOutput: tc.expectedOutput ?? '',
    isHidden: tc.isHidden ?? true,
  }));

const taskToCategories = (categories) =>
  (categories ?? []).map((c) => (typeof c === 'string' ? c : c.title));

const fetchAttributes = async () => {
  const { data } = await axios.get(`${config.backendUrl}/api/v1/task/attributes`);
  return data;
};

const TOOLBAR_ACTIONS = [
  {
    type: 'bold', title: 'Жирный (Ctrl+B)', placeholder: 'текст',
    wrap: (s) => [`**${s}**`, 2, 2],
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round">
        <path d="M6 4h8a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z" />
        <path d="M6 12h9a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z" />
      </svg>
    ),
  },
  {
    type: 'italic', title: 'Курсив (Ctrl+I)', placeholder: 'текст',
    wrap: (s) => [`*${s}*`, 1, 1],
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
        <line x1="19" y1="4" x2="10" y2="4" />
        <line x1="14" y1="20" x2="5" y2="20" />
        <line x1="15" y1="4" x2="9" y2="20" />
      </svg>
    ),
  },
  {
    type: 'code', title: 'Инлайн-код', placeholder: 'код',
    wrap: (s) => [`\`${s}\``, 1, 1],
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="16 18 22 12 16 6" />
        <polyline points="8 6 2 12 8 18" />
      </svg>
    ),
  },
  {
    type: 'codeblock', title: 'Блок кода', placeholder: 'код',
    wrap: (s) => [`\`\`\`\n${s}\n\`\`\``, 4, 4],
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <rect x="2" y="3" width="20" height="18" rx="3" />
        <path d="M8 10l-3 3 3 3M16 10l3 3-3 3M12 7l-2 10" />
      </svg>
    ),
  },
  {
    type: 'heading', title: 'Заголовок', placeholder: 'Заголовок',
    wrap: (s) => [`## ${s}`, 3, 3],
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
        <path d="M4 6h16M4 12h16M4 18h10" />
      </svg>
    ),
  },
  {
    type: 'list', title: 'Список', placeholder: 'элемент',
    wrap: (s) => [`- ${s}`, 2, 2],
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
        <line x1="9" y1="6" x2="20" y2="6" />
        <line x1="9" y1="12" x2="20" y2="12" />
        <line x1="9" y1="18" x2="20" y2="18" />
        <circle cx="4" cy="6" r="1.5" fill="currentColor" stroke="none" />
        <circle cx="4" cy="12" r="1.5" fill="currentColor" stroke="none" />
        <circle cx="4" cy="18" r="1.5" fill="currentColor" stroke="none" />
      </svg>
    ),
  },
];

export default function TaskEditForm(props) {
  const [attributes] = createResource(fetchAttributes);

  const [title, setTitle] = createSignal(props.task?.title ?? '');
  const [description, setDescription] = createSignal(props.task?.description ?? '');
  const [difficulty, setDifficulty] = createSignal(props.task?.difficulty ?? '');
  const [categories, setCategories] = createSignal(taskToCategories(props.task?.categories));
  const [starterCode, setStarterCode] = createSignal(
    props.task?.starterCode?.code ?? DEFAULT_STARTER_CODE
  );
  const [tcStore, setTcStore] = createStore({ items: taskToTcItems(props.task?.testCases) });
  const [isSubmitting, setIsSubmitting] = createSignal(false);
  const [submitError, setSubmitError] = createSignal('');
  const [fieldErrors, setFieldErrors] = createSignal({});

  let descRef;

  const applyFormat = (action) => {
    const el = descRef;
    if (!el) return;
    const start = el.selectionStart;
    const end = el.selectionEnd;
    const selected = description().substring(start, end) || action.placeholder;
    const [insert, prefixLen] = action.wrap(selected);
    setDescription(description().substring(0, start) + insert + description().substring(end));
    setTimeout(() => {
      el.focus();
      el.setSelectionRange(start + prefixLen, start + prefixLen + selected.length);
    }, 0);
  };

  const handleDescKeyDown = (e) => {
    if (!e.ctrlKey && !e.metaKey) return;
    if (e.key === 'b') { e.preventDefault(); applyFormat(TOOLBAR_ACTIONS[0]); }
    else if (e.key === 'i') { e.preventDefault(); applyFormat(TOOLBAR_ACTIONS[1]); }
  };

  const addTestCase = () => setTcStore('items', (items) => [...items, newTestCase()]);
  const removeTestCase = (id) => setTcStore('items', (items) => items.filter((tc) => tc.id !== id));
  const updateTestCase = (id, field, value) =>
    setTcStore('items', (item) => item.id === id, field, value);

  const toggleCategory = (cat) =>
    setCategories((prev) =>
      prev.includes(cat) ? prev.filter((c) => c !== cat) : [...prev, cat]
    );

  const validate = () => {
    const errors = {};
    const t = title().trim();
    if (!t) errors.title = 'Название обязательно';
    else if (t.length > 255) errors.title = 'Название не должно превышать 255 символов';
    if (!description().trim()) errors.description = 'Описание обязательно';
    if (!difficulty()) errors.difficulty = 'Выберите сложность';
    if (categories().length === 0) errors.categories = 'Выберите хотя бы одну категорию';
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    setSubmitError('');

    const token = localStorage.getItem('access_token');
    const payload = {
      title: title().trim(),
      description: description().trim(),
      difficulty: difficulty(),
      idAuthor: props.task?.idAuthor,
      starterCode: { code: starterCode(), isDefault: false },
      categories: categories(),
      testCases: tcStore.items.map(({ input, expectedOutput, isHidden }) => ({
        input: input || null,
        expectedOutput: expectedOutput || null,
        isHidden,
      })),
    };

    let succeeded = false;
    try {
      await axios.patch(`${config.backendUrl}/api/v1/task/${props.taskId}`, payload, {
        headers: { Authorization: `Bearer ${token}` },
      });
      succeeded = true;
    } catch (err) {
      const msg =
        err.response?.data?.message || err.response?.data || err.message || 'Неизвестная ошибка';
      setSubmitError(typeof msg === 'string' ? msg : `Ошибка сервера: ${err.response?.status}`);
    } finally {
      setIsSubmitting(false);
    }

    if (succeeded) props.onSuccess?.();
  };

  const resetForm = () => {
    setTitle(props.task?.title ?? '');
    setDescription(props.task?.description ?? '');
    setDifficulty(props.task?.difficulty ?? '');
    setCategories(taskToCategories(props.task?.categories));
    setStarterCode(props.task?.starterCode?.code ?? DEFAULT_STARTER_CODE);
    setTcStore('items', taskToTcItems(props.task?.testCases));
    setSubmitError('');
    setFieldErrors({});
  };

  return (
    <Show
      when={!attributes.loading && !attributes.error}
      fallback={
        <div class="ctm-attributes-state">
          <Show
            when={attributes.error}
            fallback={<span class="ctm-spinner ctm-spinner--dark" />}
          >
            <svg class="ctm-attributes-error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>Не удалось загрузить атрибуты задачи</span>
          </Show>
        </div>
      }
    >
      <form class="tef-form" onSubmit={handleSubmit} novalidate>
        {/* Title */}
        <div class="ctm-field">
          <label class="ctm-label" for="tef-title">
            Название <span class="ctm-required">*</span>
          </label>
          <input
            id="tef-title"
            class="ctm-input"
            classList={{ 'ctm-input--error': !!fieldErrors().title }}
            type="text"
            placeholder="Название задачи"
            value={title()}
            onInput={(e) => setTitle(e.target.value)}
            maxlength="255"
            disabled={isSubmitting()}
          />
          <div class="ctm-field-footer">
            <Show when={fieldErrors().title}>
              <span class="ctm-error-text">{fieldErrors().title}</span>
            </Show>
            <span class="ctm-char-count" classList={{ 'ctm-char-count--warn': title().length > 230 }}>
              {title().length} / 255
            </span>
          </div>
        </div>

        {/* Description with toolbar */}
        <div class="ctm-field">
          <label class="ctm-label" for="tef-description">
            Описание <span class="ctm-required">*</span>
          </label>
          <div class="ctm-editor-wrap" classList={{ 'ctm-editor-wrap--error': !!fieldErrors().description }}>
            <div class="ctm-toolbar">
              <For each={TOOLBAR_ACTIONS}>
                {(action) => (
                  <button
                    type="button"
                    class="ctm-toolbar-btn"
                    title={action.title}
                    onClick={() => applyFormat(action)}
                    disabled={isSubmitting()}
                    tabindex="-1"
                  >
                    {action.icon}
                  </button>
                )}
              </For>
              <span class="ctm-toolbar-hint">Markdown</span>
            </div>
            <textarea
              id="tef-description"
              class="ctm-textarea ctm-textarea--editor"
              placeholder="Подробное описание задачи..."
              value={description()}
              onInput={(e) => setDescription(e.target.value)}
              onKeyDown={handleDescKeyDown}
              rows="8"
              disabled={isSubmitting()}
              ref={descRef}
            />
          </div>
          <Show when={fieldErrors().description}>
            <span class="ctm-error-text">{fieldErrors().description}</span>
          </Show>
        </div>

        {/* Difficulty */}
        <div class="ctm-field">
          <label class="ctm-label" for="tef-difficulty">
            Сложность <span class="ctm-required">*</span>
          </label>
          <select
            id="tef-difficulty"
            class="ctm-select"
            classList={{ 'ctm-input--error': !!fieldErrors().difficulty }}
            value={difficulty()}
            onChange={(e) => setDifficulty(e.target.value)}
            disabled={isSubmitting()}
          >
            <option value="" disabled>Выберите сложность</option>
            <For each={attributes()?.difficulties ?? []}>
              {(d) => <option value={d}>{d}</option>}
            </For>
          </select>
          <Show when={fieldErrors().difficulty}>
            <span class="ctm-error-text">{fieldErrors().difficulty}</span>
          </Show>
        </div>

        {/* Categories */}
        <div class="ctm-field">
          <label class="ctm-label">
            Категории <span class="ctm-required">*</span>
          </label>
          <div class="ctm-chips">
            <For each={attributes()?.categories ?? []}>
              {(cat) => (
                <label class="ctm-chip" classList={{ 'ctm-chip--active': categories().includes(cat) }}>
                  <input
                    type="checkbox"
                    class="ctm-chip-input"
                    checked={categories().includes(cat)}
                    onChange={() => toggleCategory(cat)}
                    disabled={isSubmitting()}
                  />
                  {cat}
                </label>
              )}
            </For>
          </div>
          <Show when={fieldErrors().categories}>
            <span class="ctm-error-text">{fieldErrors().categories}</span>
          </Show>
        </div>

        {/* Starter code */}
        <div class="ctm-field">
          <label class="ctm-label" for="tef-starter-code">
            Стартовый код
          </label>
          <textarea
            id="tef-starter-code"
            class="ctm-textarea ctm-textarea--code"
            value={starterCode()}
            onInput={(e) => setStarterCode(e.target.value)}
            rows="7"
            disabled={isSubmitting()}
            spellcheck={false}
          />
          <span class="ctm-hint">Код, который увидит пользователь при открытии задачи</span>
        </div>

        {/* Test cases */}
        <div class="ctm-field">
          <div class="ctm-section-header">
            <label class="ctm-label">Тест-кейсы</label>
            <button
              type="button"
              class="ctm-add-btn"
              onClick={addTestCase}
              disabled={isSubmitting()}
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
              Добавить тест-кейс
            </button>
          </div>

          <div class="ctm-test-cases">
            <For each={tcStore.items}>
              {(tc, i) => (
                <div class="ctm-test-case">
                  <div class="ctm-test-case-head">
                    <span class="ctm-test-case-num">Тест #{i() + 1}</span>
                    <div class="ctm-test-case-controls">
                      <label class="ctm-inline-checkbox">
                        <input
                          type="checkbox"
                          checked={tc.isHidden}
                          onChange={(e) => updateTestCase(tc.id, 'isHidden', e.target.checked)}
                          disabled={isSubmitting()}
                        />
                        <span>Скрытый</span>
                      </label>
                      <Show when={tcStore.items.length > 1}>
                        <button
                          type="button"
                          class="ctm-remove-btn"
                          onClick={() => removeTestCase(tc.id)}
                          disabled={isSubmitting()}
                          title="Удалить тест-кейс"
                        >
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                            <path d="M10 11v6M14 11v6" />
                            <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                          </svg>
                        </button>
                      </Show>
                    </div>
                  </div>
                  <div class="ctm-test-case-body">
                    <div class="ctm-field">
                      <label class="ctm-label ctm-label--sm">Входные данные (stdin)</label>
                      <textarea
                        class="ctm-textarea ctm-textarea--code ctm-textarea--xs"
                        placeholder="Входные данные..."
                        value={tc.input}
                        onInput={(e) => updateTestCase(tc.id, 'input', e.target.value)}
                        rows="2"
                        disabled={isSubmitting()}
                        spellcheck={false}
                      />
                    </div>
                    <div class="ctm-field">
                      <label class="ctm-label ctm-label--sm">Ожидаемый вывод (stdout)</label>
                      <textarea
                        class="ctm-textarea ctm-textarea--code ctm-textarea--xs"
                        placeholder="Ожидаемый вывод..."
                        value={tc.expectedOutput}
                        onInput={(e) => updateTestCase(tc.id, 'expectedOutput', e.target.value)}
                        rows="2"
                        disabled={isSubmitting()}
                        spellcheck={false}
                      />
                    </div>
                  </div>
                </div>
              )}
            </For>
          </div>
        </div>

        <Show when={submitError()}>
          <div class="ctm-error-banner">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span>{submitError()}</span>
          </div>
        </Show>

        {/* Action buttons */}
        <div class="tef-actions">
          <button
            type="button"
            class="tef-btn tef-btn--reset"
            onClick={resetForm}
            disabled={isSubmitting()}
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
            </svg>
            Сбросить изменения
          </button>
          <button
            type="submit"
            class="tef-btn tef-btn--save"
            disabled={isSubmitting()}
          >
            {isSubmitting() ? (
              <span class="ctm-btn-spinner-wrap">
                <span class="ctm-spinner" />
                Сохранение...
              </span>
            ) : (
              <>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                  <polyline points="17 21 17 13 7 13 7 21" />
                  <polyline points="7 3 7 8 15 8" />
                </svg>
                Обновить задачу
              </>
            )}
          </button>
        </div>
      </form>
    </Show>
  );
}