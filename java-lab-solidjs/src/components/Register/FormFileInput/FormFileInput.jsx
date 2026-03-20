import { createSignal, createEffect, createMemo } from 'solid-js';
import './FormFileInput.css';

export default function FormFileInput(props) {
  const [preview, setPreview] = createSignal(null);
  const [fileName, setFileName] = createSignal('');
  const [isDragging, setIsDragging] = createSignal(false);
  let fileInputRef;
  let dropZoneRef;

  const valueMemo = createMemo(() => props.value);

  createEffect(() => {
    const value = valueMemo();
    if (value && value instanceof File) {
      const objectUrl = URL.createObjectURL(value);
      setPreview(objectUrl);
      setFileName(value.name);

      return () => {
        URL.revokeObjectURL(objectUrl);
      };
    } else {
      setPreview(null);
      setFileName('');
    }
  });

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    
    const file = e.dataTransfer.files[0];
    if (file) {
      props.onChange(file);
    }
  };

  const handleChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      props.onChange(file);
    }
  };

  const handleRemove = () => {
    props.onChange(null);
    if (fileInputRef) {
      fileInputRef.value = '';
    }
  };

  return (
    <div class="form-file-input-wrapper">
      <label class="form-file-input-label" for={props.name}>
        {props.label}
      </label>

      <div class="form-file-input-container">
        {preview() ? (
          <div class="form-file-preview">
            <img src={preview()} alt="Preview" class="form-file-preview-image" />
            <button
              type="button"
              class="form-file-remove-btn"
              onClick={handleRemove}
              aria-label="Удалить изображение"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
        ) : (
          <label 
            class="form-file-input" 
            for={props.name}
            ref={dropZoneRef}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            classList={{ 'form-file-input--dragging': isDragging() }}
          >
            <div class="form-file-input-content">
              <svg class="form-file-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
              <span class="form-file-input-text">
                Нажмите для загрузки или перетащите файл
              </span>
              <span class="form-file-input-hint">
                PNG, JPG, GIF до {props.maxSize} МБ
              </span>
            </div>
            <input
              type="file"
              id={props.name}
              name={props.name}
              accept={props.accept}
              onChange={handleChange}
              class="form-file-input-hidden"
              ref={(el) => (fileInputRef = el)}
            />
          </label>
        )}
      </div>

      {fileName() && !preview() && (
        <span class="form-file-name">{fileName()}</span>
      )}

      {props.error && <span class="form-file-input-error">{props.error}</span>}
    </div>
  );
}
