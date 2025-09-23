import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { DataProvider, useData } from './contexts/DataContext';
import { Toaster } from 'react-hot-toast';
import Header from './components/Header';
import Footer from './components/Footer';
import LoadingSpinner from './components/LoadingSpinner';
import Landing from './pages/Landing';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import UserDashboard from './pages/UserDashboard';
import AgentDashboard from './pages/AgentDashboard';
import AdminDashboard from './pages/AdminDashboard';
import Profile from './pages/Profile';
import Reviews from './pages/Reviews';
import Assistance from './pages/Assistance';
import Packages from './pages/Packages';
import './styles/common.css';

// Protected Route Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { currentUser, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/signin" replace />;
  }
  
  // Handle both lowercase and uppercase field names for compatibility
  const userRole = currentUser?.role || currentUser?.Role;
  
  if (allowedRoles && !allowedRoles.includes(userRole)) {
    return <Navigate to="/" replace />;
  }
  
  return children;
};

// Main App Component
const AppContent = () => {
  const { loading, error } = useData();

  return (
    <Router>
      <div className="app">
        <Header />
        <main className="main-content">
          {loading ? (
            <LoadingSpinner message="Loading data from backend services..." />
          ) : error ? (
            <div style={{ 
              display: 'flex', 
              flexDirection: 'column', 
              alignItems: 'center', 
              justifyContent: 'center', 
              minHeight: '400px',
              padding: '20px',
              textAlign: 'center'
            }}>
              <h2 style={{ color: '#e74c3c', marginBottom: '16px' }}>⚠️ Backend Services Unavailable</h2>
              <p style={{ color: '#666', marginBottom: '20px' }}>{error}</p>
              <p style={{ color: '#888', fontSize: '14px' }}>
                Please ensure backend services are running and try refreshing the page.
              </p>
              <button 
                onClick={() => window.location.reload()} 
                style={{
                  marginTop: '20px',
                  padding: '10px 20px',
                  backgroundColor: '#3498db',
                  color: 'white',
                  border: 'none',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
              >
                Refresh Page
              </button>
            </div>
          ) : (
            <Routes>
              <Route path="/" element={<Landing />} />
              <Route path="/signin" element={<SignIn />} />
              <Route path="/register" element={<SignUp />} />
              <Route path="/reviews" element={<Reviews />} />
              <Route path="/assistance" element={<Assistance />} />
              <Route path="/packages" element={<Packages />} />
              <Route 
                path="/user-dashboard" 
                element={
                  <ProtectedRoute allowedRoles={['customer']}>
                    <UserDashboard />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/agent-dashboard" 
                element={
                  <ProtectedRoute allowedRoles={['agent']}>
                    <AgentDashboard />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/admin-dashboard" 
                element={
                  <ProtectedRoute allowedRoles={['admin']}>
                    <AdminDashboard />
                  </ProtectedRoute>
                } 
              />
              <Route 
                path="/profile" 
                element={
                  <ProtectedRoute>
                    <Profile />
                  </ProtectedRoute>
                } 
              />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          )}
        </main>
        <Footer />
      </div>
    </Router>
  );
};

// App Component with Auth Provider
const App = () => {
  return (
    <AuthProvider>
      <DataProvider>
        <AppContent />
        <Toaster 
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#363636',
              color: '#fff',
            },
            success: {
              duration: 3000,
              iconTheme: {
                primary: '#4ade80',
                secondary: '#fff',
              },
            },
            error: {
              duration: 4000,
              iconTheme: {
                primary: '#ef4444',
                secondary: '#fff',
              },
            },
          }}
        />
      </DataProvider>
    </AuthProvider>
  );
};

export default App;
