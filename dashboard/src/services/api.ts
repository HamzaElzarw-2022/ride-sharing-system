import axios from 'axios';
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
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
