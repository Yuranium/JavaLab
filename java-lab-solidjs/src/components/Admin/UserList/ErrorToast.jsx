import { Show } from 'solid-js';
import { useUsers } from '../../../context/UsersContext';
import './ErrorToast.css';

export default function ErrorToast() {
  const { error } = useUsers();

  return (
    <Show when={error()}>
      <div class="error-toast">
        {error()}
      </div>
    </Show>
  );
}