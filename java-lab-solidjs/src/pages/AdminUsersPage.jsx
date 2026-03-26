import { createSignal, onMount } from 'solid-js';
import { useUsers } from '../context/UsersContext';
import { useAuth } from '../context/AuthContext';
import AdminLayout from '../components/Admin/AdminLayout/AdminLayout';
import UserList from '../components/Admin/UserList/UserList';
import UserActionModal from '../components/Admin/BlockUserModal/BlockUserModal';
import './AdminUsersPage.css';
import { Show } from 'solid-js';

export default function AdminUsersPage() {
  const { loadUsers, updateUserInList } = useUsers();
  const auth = useAuth();
  const [selectedUser, setSelectedUser] = createSignal(null);
  const [modalMode, setModalMode] = createSignal('block');
  const [isModalOpen, setIsModalOpen] = createSignal(false);

  onMount(() => {
    loadUsers(0, false);
  });

  const openBlockModal = (user) => {
    setSelectedUser(user);
    setModalMode('block');
    setIsModalOpen(true);
  };

  const openUnblockModal = (user) => {
    setSelectedUser(user);
    setModalMode('unblock');
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedUser(null);
  };

  const shouldUpdateStatusImmediately = (mode, dates) => {
    const now = new Date();

    if (mode === 'block') {
      const { startLock, endLock } = dates;
      if (startLock && new Date(startLock) <= now) {
        return true;
      }
      if (!startLock && endLock && new Date(endLock) <= now) {
        return false;
      }
      return false;
    } else {
      const { unlockTime } = dates;
      if (unlockTime === null) {
        return true;
      }
      return new Date(unlockTime) <= now;
    }
  };

  const handleSuccess = (userId, mode, dates) => {
    const user = selectedUser();
    if (user) {
      const shouldUpdate = shouldUpdateStatusImmediately(mode, dates);
      if (shouldUpdate) {
        const newActivityStatus = mode !== 'block';
        updateUserInList(userId, newActivityStatus);
      }
    }
    handleModalClose();
  };

  return (
    <AdminLayout activePage="users" title="Управление пользователями">
      <div class="admin-users-page">
        <Show when={auth.isAuthenticated()}>
          <UserList
            onBlockClick={openBlockModal}
            onUnblockClick={openUnblockModal}
          />
        </Show>

        <Show when={isModalOpen()}>
          <UserActionModal
            isOpen={isModalOpen()}
            mode={modalMode()}
            user={selectedUser()}
            accessToken={auth.accessToken()}
            onClose={handleModalClose}
            onSuccess={handleSuccess}
          />
        </Show>
      </div>
    </AdminLayout>
  );
}
