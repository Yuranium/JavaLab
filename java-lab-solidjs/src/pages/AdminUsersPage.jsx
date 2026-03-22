import { createSignal, onMount } from 'solid-js';
import { useUsers } from '../context/UsersContext';
import { useAuth } from '../context/AuthContext';
import AdminLayout from '../components/Admin/AdminLayout/AdminLayout';
import UserList from '../components/Admin/UserList/UserList';
import BlockUserModal from '../components/Admin/BlockUserModal/BlockUserModal';
import './AdminUsersPage.css';
import { Show } from 'solid-js';

export default function AdminUsersPage() {
  const { loadUsers, blockUser } = useUsers();
  const { isAuthenticated } = useAuth();
  const [selectedUser, setSelectedUser] = createSignal(null);
  const [isModalOpen, setIsModalOpen] = createSignal(false);

  onMount(() => {
    loadUsers(0, false);
  });

  const handleBlockClick = (user) => {
    setSelectedUser(user);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedUser(null);
  };

  const handleBlockConfirm = (data) => {
    blockUser(data.userId, data.reason, data.duration);
    handleModalClose();
  };

  return (
    <AdminLayout activePage="users" title="Управление пользователями">
      <div class="admin-users-page">
        <Show when={isAuthenticated()}>
          <UserList
            onBlockClick={handleBlockClick}
          />
        </Show>

        <Show when={isModalOpen()}>
          <BlockUserModal
            isOpen={isModalOpen()}
            user={selectedUser()}
            onClose={handleModalClose}
            onConfirm={handleBlockConfirm}
          />
        </Show>
      </div>
    </AdminLayout>
  );
}
