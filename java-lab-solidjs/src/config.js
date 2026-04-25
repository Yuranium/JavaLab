export const config = {
    backendUrl: import.meta.env.VITE_BACKEND_URL,
    backendUrlWs: import.meta.env.VITE_BACKEND_URL_WS,
    authUrl: import.meta.env.VITE_AUTH_URL,
    realm: import.meta.env.VITE_REALM,
    grantType: import.meta.env.VITE_GRANT_TYPE,
    clientId: import.meta.env.VITE_CLIENT_ID,
    redirectUri: import.meta.env.VITE_REDIRECT_URI,
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
        prompt: 'select_account',
        scope: 'openid',
        kc_idp_hint: broker,
    });
    return `${config.authUrl}/realms/${config.realm}/protocol/openid-connect/auth?${params.toString()}`;
};
