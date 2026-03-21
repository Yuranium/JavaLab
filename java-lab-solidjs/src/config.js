export const config = {
  backendUrl: import.meta.env.BACKEND_URL,
  authUrl: import.meta.env.AUTH_URL,
  realm: import.meta.env.REALM,
  grantType: import.meta.env.GRANT_TYPE,
  clientId: import.meta.env.CLIENT_ID,
};

export const getAuthUrl = () => `${config.authUrl}/realms/${config.realm}/protocol/openid-connect/token`;