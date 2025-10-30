import api from './api';

const VISITOR_TIMESTAMP_KEY = 'visitor_timestamp';

export const visitorService = {
  incrementVisitorCount: async () => {
    const lastVisited = sessionStorage.getItem(VISITOR_TIMESTAMP_KEY);
    const now = new Date().getTime();
    const threeHours = 3 * 60 * 60 * 1000;

    if (!lastVisited || now - parseInt(lastVisited, 10) > threeHours) {
      try {
        await api.post('/api/account/visitors');
        sessionStorage.setItem(VISITOR_TIMESTAMP_KEY, now.toString());
      } catch (error) {
        console.error('Failed to increment visitor count:', error);
      }
    }
  },
};
