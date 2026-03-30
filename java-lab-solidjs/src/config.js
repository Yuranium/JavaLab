export const config = {
  backendUrl: import.meta.env.BACKEND_URL,
  authUrl: import.meta.env.AUTH_URL,
  realm: import.meta.env.REALM,
  grantType: import.meta.env.GRANT_TYPE,
  clientId: import.meta.env.CLIENT_ID,
  s3Url: import.meta.env.S3_URL,
  s3Bucket: import.meta.env.S3_BUCKET,
  redirectUri: import.meta.env.REDIRECT_URI || 'http://localhost:3000/callback',
};

export const oauthBrokers = {
  google: 'google',
  github: 'github',
  yandex: 'yandex',
  vk: 'vk',
};

export const getAuthUrl = () => `${config.authUrl}/realms/${config.realm}/protocol/openid-connect/token`;

export const getOAuthBrokerUrl = (broker) => {
  const params = new URLSearchParams({
    client_id: config.clientId,
    redirect_uri: config.redirectUri,
    response_type: 'code',
    scope: 'openid',
  });
  return `${config.authUrl}/realms/${config.realm}/broker/${broker}/login?${params.toString()}`;
};

export const getS3Url = (path = '') => {
  const basePath = `${config.s3Url}/${config.s3Bucket}`;
  return path ? `${basePath}/${path}` : basePath;
};