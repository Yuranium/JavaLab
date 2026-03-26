import { createSignal, onMount } from 'solid-js';
import { useUsers } from '../context/UsersContext';
import { useAuth } from '../context/AuthContext';
import AdminLayout from '../components/Admin/AdminLayout/AdminLayout';
import UserList from '../components/Admin/UserList/UserList';
import UserActionModal from '../components/Admin/BlockUserModal/BlockUserModal';
import './AdminUsersPage.css';
import { Show } from 'solid-js';

export default function AdminUsersPage() {
  const { loadUsers } = useUsers();
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

  const handleSuccess = async (userId) => {
    await loadUsers(0, false);
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
