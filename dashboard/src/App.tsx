import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Rider from './pages/Rider';
import { AuthProvider } from './context/AuthContext';
import { visitorService } from './services/visitorService';

function App() {
  useEffect(() => {
    visitorService.incrementVisitorCount();
  }, []);

  return (
    <Router>
      <AuthProvider>
        <Layout>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/rider" element={<Rider />} />
          </Routes>
        </Layout>
      </AuthProvider>
    </Router>
  );
}

export default App;
