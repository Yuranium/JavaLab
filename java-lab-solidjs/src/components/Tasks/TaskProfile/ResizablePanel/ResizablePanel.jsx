import { createSignal, children, onCleanup } from 'solid-js';
import './ResizablePanel.css';

export default function ResizablePanel(props) {
  const [width, setWidth] = createSignal(props.initialWidth || 50);
  let containerRef;
  let isDragging = false;

  const handleMouseDown = (e) => {
    e.preventDefault();
    isDragging = true;
    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
  };

  const handleMouseMove = (e) => {
    if (!isDragging || !containerRef) return;
    const containerRect = containerRef.parentElement.getBoundingClientRect();
    const newWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100;
    const clampedWidth = Math.min(Math.max(newWidth, props.minWidth || 20), props.maxWidth || 80);
    setWidth(clampedWidth);
  };

  const handleMouseUp = () => {
    isDragging = false;
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
  };

  onCleanup(() => {
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
  });

  const content = children(() => props.children);

  return (
    <div class="resizable-panel" ref={containerRef} style={{ width: `${width()}%` }}>
      <div class="resizable-panel-content">
        {content()}
      </div>
      <div
        class="resizable-panel-handle"
        onMouseDown={handleMouseDown}
      >
        <div class="resizable-panel-handle-line"></div>
      </div>
    </div>
  );
}
