import axios from 'axios';
export const API_HOST = import.meta.env.VITE_API_HOST || 'localhost:8080';

const api = axios.create({
  baseURL: `http://${API_HOST}/api`,
  // No auth headers; endpoints are public
});

export function setupInterceptors(logout: () => void) {
  api.interceptors.response.use(
    (res) => res,
    (error) => {
      console.error('[API ERROR]', error?.response || error);
      if (
        error.response &&
        (error.response.status === 401 || error.response.status === 403)
      ) {
        const requestUrl = error.request.responseURL;
        if (
          !requestUrl.includes('/auth/authenticate') &&
          !requestUrl.includes('/auth/register')
        ) {
          logout();
        }
      }
      return Promise.reject(error);
    }
  );
}

export default api;
