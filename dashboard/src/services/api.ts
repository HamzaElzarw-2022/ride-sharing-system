import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  // No auth headers; endpoints are public
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    console.error('[API ERROR]', error?.response || error);
    return Promise.reject(error);
  }
);

export default api;
