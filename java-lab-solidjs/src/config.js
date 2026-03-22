export const config = {
  backendUrl: import.meta.env.BACKEND_URL,
  authUrl: import.meta.env.AUTH_URL,
  realm: import.meta.env.REALM,
  grantType: import.meta.env.GRANT_TYPE,
  clientId: import.meta.env.CLIENT_ID,
  s3Url: import.meta.env.S3_URL,
  s3Bucket: import.meta.env.S3_BUCKET,
};

export const getAuthUrl = () => `${config.authUrl}/realms/${config.realm}/protocol/openid-connect/token`;

export const getS3Url = (path = '') => {
  const basePath = `${config.s3Url}/${config.s3Bucket}`;
  return path ? `${basePath}/${path}` : basePath;
};