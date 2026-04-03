import { createSignal, createEffect, createMemo, Show } from 'solid-js';
import { useAuth } from '../../../../context/AuthContext';
import './TaskCodeEditor.css';

const JAVA_KEYWORDS = [
  'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char',
  'class', 'const', 'continue', 'default', 'do', 'double', 'else', 'enum',
  'extends', 'final', 'finally', 'float', 'for', 'goto', 'if', 'implements',
  'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new',
  'package', 'private', 'protected', 'public', 'return', 'short', 'static',
  'strictfp', 'super', 'switch', 'synchronized', 'this', 'throw', 'throws',
  'transient', 'try', 'void', 'volatile', 'while', 'var', 'record', 'sealed',
  'permits', 'yield'
];

const JAVA_TYPES = [
  'String', 'Integer', 'Long', 'Double', 'Float', 'Boolean', 'Character',
  'Byte', 'Short', 'Object', 'System', 'Math', 'Arrays', 'List', 'ArrayList',
  'Map', 'HashMap', 'Set', 'HashSet', 'Optional', 'Stream', 'Collectors'
];

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function highlightJavaCode(code) {
  if (!code) return '';

  let html = escapeHtml(code);

  const strings = [];
  html = html.replace(/"([^"\\]|\\.)*"/g, (match) => {
    strings.push(match);
    return `%%STRING_${strings.length - 1}%%`;
  });

  const comments = [];
  html = html.replace(/\/\/.*$/gm, (match) => {
    comments.push(match);
    return `%%COMMENT_${comments.length - 1}%%`;
  });

  html = html.replace(/\/\*[\s\S]*?\*\//g, (match) => {
    comments.push(match);
    return `%%COMMENT_${comments.length - 1}%%`;
  });

  const keywords = JAVA_KEYWORDS.join('|');
  const types = JAVA_TYPES.join('|');

  html = html.replace(new RegExp(`\\b(${keywords})\\b`, 'g'), '<span class="code-keyword">$1</span>');
  html = html.replace(new RegExp(`\\b(${types})\\b`, 'g'), '<span class="code-type">$1</span>');
  html = html.replace(/\b(\d+\.?\d*)\b/g, '<span class="code-number">$1</span>');

  html = html.replace(/%%STRING_(\d+)%%/g, (_, i) => `<span class="code-string">${strings[i]}</span>`);
  html = html.replace(/%%COMMENT_(\d+)%%/g, (_, i) => `<span class="code-comment">${comments[i]}</span>`);

  return html;
}

export default function TaskCodeEditor(props) {
  const auth = useAuth();
  const [code, setCode] = createSignal('');
  let textareaRef;
  let highlightPreRef;

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

  const handleKeyDown = (e) => {
    if (e.key === 'Tab') {
      e.preventDefault();
      const textarea = e.target;
      const start = textarea.selectionStart;
      const value = textarea.value;
      const newValue = value.substring(0, start) + '    ' + value.substring(textarea.selectionEnd);
      setCode(newValue);
      if (props.onCodeChange) {
        props.onCodeChange(newValue);
      }
      setTimeout(() => {
        textarea.selectionStart = textarea.selectionEnd = start + 4;
      }, 0);
    }

    if (e.key === 'Enter') {
      e.preventDefault();
      const textarea = e.target;
      const start = textarea.selectionStart;
      const value = textarea.value;
      const lineStart = value.lastIndexOf('\n', start - 1) + 1;
      const currentLine = value.substring(lineStart, start);
      const indentation = currentLine.match(/^(\s*)/)[1];
      const trimmedLine = currentLine.trim();
      const endsWithBrace = trimmedLine.endsWith('{');
      const insert = endsWithBrace ? '\n' + indentation + '    ' : '\n' + indentation;
      const newValue = value.substring(0, start) + insert + value.substring(textarea.selectionEnd);
      setCode(newValue);
      if (props.onCodeChange) {
        props.onCodeChange(newValue);
      }
      setTimeout(() => {
        textarea.selectionStart = textarea.selectionEnd = start + insert.length;
      }, 0);
    }
  };

  const handleCodeChange = (e) => {
    setCode(e.target.value);
    if (props.onCodeChange) {
      props.onCodeChange(e.target.value);
    }
  };

  const handleScroll = () => {
    if (textareaRef && highlightPreRef) {
      highlightPreRef.scrollTop = textareaRef.scrollTop;
      highlightPreRef.scrollLeft = textareaRef.scrollLeft;
    }
  };

  const isAuthenticated = () => auth.isAuthenticated();

  const handleSubmit = () => {
    if (props.onSubmit) {
      props.onSubmit(code());
    }
  };

  createEffect(() => {
    const currentCode = code();
    if (highlightPreRef) {
      highlightPreRef.innerHTML = highlightJavaCode(currentCode);
    }
  });

  return (
    <div class="task-code-editor">
      <div class="task-code-editor-header">
        <span class="task-code-editor-language">Java</span>
      </div>

      <div class="task-code-editor-container">
        <div class="task-code-editor-highlight">
          <pre ref={highlightPreRef}></pre>
        </div>
        <textarea
          ref={textareaRef}
          class="task-code-editor-textarea"
          value={code()}
          onInput={handleCodeChange}
          onKeyDown={handleKeyDown}
          onScroll={handleScroll}
          spellcheck="false"
          autocomplete="off"
          autocorrect="off"
          autocapitalize="off"
          readonly={!isAuthenticated()}
        />
      </div>

      <Show when={isAuthenticated()}>
        <div class="task-code-editor-actions">
          <button class="task-code-editor-submit-btn" onClick={handleSubmit}>
            Отправить решение
          </button>
        </div>
      </Show>
    </div>
  );
}
