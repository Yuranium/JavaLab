import { onMount } from 'solid-js';
import { useNavigate } from '@solidjs/router';
import { useAuth } from '../context/AuthContext';
import { config } from '../config';
import axios from "axios";

export default function CallbackPage() {
  const navigate = useNavigate();
  const { setTokens } = useAuth();

  onMount(async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const error = urlParams.get('error');
    const state = urlParams.get('state');

    if (error) {
      console.error('OAuth ошибка:', error, urlParams.get('error_description'));
      navigate('/login');
      return;
    }

    if (!code) {
      navigate('/login');
      return;
    }

    try {
        const tokenResponse = await axios.post(
            `${config.authUrl}/realms/${config.realm}/protocol/openid-connect/token`,
            new URLSearchParams({
                grant_type: 'authorization_code',
                client_id: config.clientId,
                code: code,
                redirect_uri: config.redirectUri,
            }),
            {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
            }
        );

      const tokenData = tokenResponse.data;
      setTokens(tokenData.access_token, tokenData.refresh_token);
      navigate('/');

    } catch (err) {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      navigate('/login');
    }
  });

  return (
    <div class="callback-page">
      <div class="callback-loading">
        <div class="spinner"></div>
        <p>Завершение авторизации...</p>
      </div>
    </div>
  );
}