import { createSignal, createEffect, createMemo, Show, onMount, onCleanup } from 'solid-js';
import { useParams } from '@solidjs/router';
import { useAuth } from '../../../../context/AuthContext';
import { useTheme } from '../../../../context/ThemeContext';
import './TaskCodeEditor.css';
import 'monaco-editor/min/vs/editor/editor.main.css';
import {config} from "../../../../config";


export default function TaskCodeEditor(props) {
  const auth = useAuth();
  const [code, setCode] = createSignal('');
  let containerRef;
  let editor = null;
  const { theme } = useTheme();

  const starterCodeValue = createMemo(() => {
    const sc = props.starterCode;
    if (sc && sc.code !== undefined && sc.code !== null) {
      return sc.code;
    }
    return '';
  });

  createEffect(() => {
    const val = starterCodeValue();
    setCode(val);
  });



  const isAuthenticated = () => auth.isAuthenticated();
  const params = useParams();
  const isGuest = () => {
    const u = auth.user && auth.user();
    if (!u) return true;
    const roles = u.roles || [];
    return roles.includes(auth.ROLES.GUEST);
  };

  const [execStatus, setExecStatus] = createSignal(null);
  let ws = null;

  const connectWebSocket = (codeVal) => {
    try {
      try { if (ws) ws.close(); } catch (e) {}

      const token = localStorage.getItem('access_token');
      const url = `${config.backendUrlWs}/api/v1/execution/ws?token=${encodeURIComponent(token)}`;
      ws = new WebSocket(url);

      setExecStatus({ type: 'INFO', message: 'Подключение...' });

      ws.onopen = () => {
        setExecStatus({ type: 'INFO', message: 'Соединение установлено' });
        try {
          const taskIdNum = params && params.id ? Number(params.id) : null;
          const payload = {
            taskId: taskIdNum,
            code: codeVal
          };
          ws.send(JSON.stringify(payload));
        } catch (e) {}
      };

      ws.onmessage = (ev) => {
        try {
          const msg = JSON.parse(ev.data);
          if (props.onExecutionMessage) props.onExecutionMessage(msg);

          let display = '';
          if (msg.type === 'INFO') display = msg.payload?.message || '';
          else if (msg.type === 'TEST_RESULT') display = `Тест #${msg.payload?.testNumber}: ${msg.payload?.status}`;
          else if (msg.type === 'FINAL_RESULT') display = `Итог: ${msg.payload?.status || ''}`;
          setExecStatus({ type: msg.type, message: display });

          if (msg.type === 'FINAL_RESULT') {
            try { ws.close(); } catch (e) {}
            ws = null;
          }
        } catch (e) {
          console.error('ws message parse error', e);
        }
      };

      ws.onclose = () => {
        setExecStatus({ type: 'INFO', message: 'Соединение закрыто' });
      };
      ws.onerror = (e) => {
        setExecStatus({ type: 'INFO', message: 'Ошибка соединения' });
      };

      onCleanup(() => {
        try { if (ws) ws.close(); } catch (e) {}
        ws = null;
      });
    } catch (e) {
      console.error('ws connect failed', e);
      setExecStatus({ type: 'INFO', message: 'Не удалось установить соединение' });
    }
  };

  const handleSubmit = () => {
    const val = editor ? editor.getValue() : code();
    connectWebSocket(val);
    if (props.onSubmit) props.onSubmit(val);
  };

  onMount(async () => {
    try {
      const monaco = await import('monaco-editor/esm/vs/editor/editor.main');
      try { window.monaco = monaco; } catch (e) { /* ignore */ }

      const computed = getComputedStyle(document.documentElement);
      const codeBg = (computed.getPropertyValue('--code-background') || '').trim() || '#ffffff';
      const codeFg = (computed.getPropertyValue('--text-primary') || '').trim() || '#000000';

      try {
        monaco.editor.defineTheme('javalab-dark', {
          base: 'vs-dark',
          inherit: true,
          rules: [],
          colors: {
            'editor.background': codeBg,
            'editor.foreground': codeFg
          }
        });

        monaco.editor.defineTheme('javalab-light', {
          base: 'vs',
          inherit: true,
          rules: [],
          colors: {
            'editor.background': codeBg,
            'editor.foreground': codeFg
          }
        });
      } catch (e) {
      }
      editor = monaco.editor.create(containerRef, {
        value: code(),
        language: 'java 23',
        theme: theme() === 'dark' ? 'javalab-dark' : 'javalab-light',
        automaticLayout: true,
        minimap: { enabled: false },
        lineNumbers: 'on',
        fontFamily: "'Fira Code', 'Consolas', 'Monaco', monospace",
        fontSize: 14,
        tabSize: 4,
        readOnly: false
      });

      try {
        const model = editor.getModel();
        if (model) monaco.editor.setModelLanguage(model, 'java');
      } catch (e) {
      }

      const disposable = editor.onDidChangeModelContent(() => {
        const val = editor.getValue();
        setCode(val);
        if (props.onCodeChange) props.onCodeChange(val);
      });

      editor.updateOptions({ readOnly: !isAuthenticated() });

      try {
        containerRef.style.background = codeBg;
      } catch (e) {
      }

      onCleanup(() => {
        disposable.dispose();
        try { editor.dispose(); } catch (e) {}
        editor = null;
      });
    } catch (e) {
      console.error('Failed to load monaco editor', e);
    }
  });

  createEffect(() => {
    const val = starterCodeValue();
    setCode(val);
    if (editor && editor.getValue() !== val) {
      editor.setValue(val);
    }
  });

  createEffect(() => {
    if (editor) {
      editor.updateOptions({ readOnly: !isAuthenticated() });
    }
  });

  createEffect(() => {
    try {
      if (!window.monaco) return;
      const mon = window.monaco;
      const themeName = theme() === 'dark' ? 'javalab-dark' : 'javalab-light';
      mon.editor.setTheme(themeName);
      const computed = getComputedStyle(document.documentElement);
      const codeBg = (computed.getPropertyValue('--code-background') || '').trim();
      if (containerRef) containerRef.style.background = codeBg;
    } catch (e) {}
  });

  return (
    <div class="task-code-editor">
        <div class="task-code-editor-header">
          <span class="task-code-editor-language">Java 23</span>
        </div>

      <div class="task-code-editor-container">
        <div ref={containerRef} class="monaco-container" />
      </div>

        <div class="task-code-editor-actions">
          <Show when={!isGuest()} fallback={
            <button class="task-code-editor-submit-btn task-code-editor-login-btn" onClick={() => { window.location.href = '/login'; }}>
              Войти
            </button>
          }>
            <div style={{ display: 'flex', 'align-items': 'center', gap: '12px' }}>
              <button class="task-code-editor-submit-btn" onClick={handleSubmit}>
                Отправить решение
              </button>

              <div class="execution-status" role="status" aria-live="polite">
                <span class="execution-status-text">{execStatus()?.message}</span>
              </div>
            </div>
          </Show>
        </div>
    </div>
  );
}
