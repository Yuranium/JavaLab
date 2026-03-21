import { defineConfig, loadEnv } from 'vite';
import solidPlugin from 'vite-plugin-solid';
import devtools from 'solid-devtools/vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  
  return {
    plugins: [devtools(), solidPlugin()],
    server: {
      port: 3000,
    },
    build: {
      target: 'esnext',
    },
    define: {
      'import.meta.env.BACKEND_URL': JSON.stringify(env.VITE_BACKEND_URL),
      'import.meta.env.AUTH_URL': JSON.stringify(env.VITE_AUTH_URL),
      'import.meta.env.REALM': JSON.stringify(env.VITE_REALM),
      'import.meta.env.GRANT_TYPE': JSON.stringify(env.VITE_GRANT_TYPE),
      'import.meta.env.CLIENT_ID': JSON.stringify(env.VITE_CLIENT_ID),
      'import.meta.env.S3_URL': JSON.stringify(env.VITE_S3_URL),
      'import.meta.env.S3_BUCKET': JSON.stringify(env.VITE_S3_BUCKET),
    },
  };
});