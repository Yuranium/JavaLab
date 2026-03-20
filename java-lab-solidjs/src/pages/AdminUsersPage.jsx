import { createSignal } from 'solid-js';
import { useUsers } from '../context/UsersContext';
import AdminLayout from '../components/Admin/AdminLayout/AdminLayout';
import UserList from '../components/Admin/UserList/UserList';
import BlockUserModal from '../components/Admin/BlockUserModal/BlockUserModal';
import './AdminUsersPage.css';
import { Show } from 'solid-js';

export default function AdminUsersPage() {
  const { blockUser } = useUsers();
  const [visibleCount, setVisibleCount] = createSignal(1);
  const [selectedUser, setSelectedUser] = createSignal(null);
  const [isModalOpen, setIsModalOpen] = createSignal(false);

  const handleLoadMore = () => {
    setVisibleCount((prev) => prev + 1);
  };

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
        <UserList
          visibleCount={visibleCount()}
          onLoadMore={handleLoadMore}
          onBlockClick={handleBlockClick}
        />

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
