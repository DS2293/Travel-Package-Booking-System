import { Link, useNavigate } from 'react-router-dom';
import { useState, useRef, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import '../styles/Header.css';

const Header = () => {
  const { currentUser, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Debug log to see the actual user data structure
  useEffect(() => {
    if (currentUser) {
      console.log('📊 Header - Current user data:', currentUser);
    }
  }, [currentUser]);

  const getUserName = () => {
    if (!currentUser) return 'Guest';
    // Handle both lowercase and uppercase field names for compatibility
    return currentUser.name || currentUser.Name || currentUser.email || 'User';
  };

  const getUserRole = () => {
    if (!currentUser) return 'guest';
    // Handle both lowercase and uppercase field names for compatibility
    return currentUser.role || currentUser.Role || 'customer';
  };

  const getUserApprovalStatus = () => {
    if (!currentUser) return null;
    // Handle both lowercase and uppercase field names for compatibility
    return currentUser.approval || currentUser.Approval;
  };

  const isAgentPendingApproval = () => {
    return getUserRole() === 'agent' && getUserApprovalStatus() === 'pending';
  };

  const handleLogout = () => {
    logout();
    navigate('/');
    setIsDropdownOpen(false);
  };

  const getDashboardLink = () => {
    if (!currentUser) return null;
    
    // Don't allow dashboard access for pending agents
    if (isAgentPendingApproval()) return null;
    
    // Use helper function to get role
    const userRole = getUserRole();
    switch (userRole) {
      case 'admin':
        return '/admin-dashboard';
      case 'agent':
        return '/agent-dashboard';
      case 'customer':
        return '/user-dashboard';
      default:
        return '/user-dashboard';
    }
  };

  const getDashboardLabel = () => {
    if (!currentUser) return 'Dashboard';
    
    // Use helper function to get role
    const userRole = getUserRole();
    switch (userRole) {
      case 'admin':
        return 'Admin Dashboard';
      case 'agent':
        return 'Agent Dashboard';
      case 'customer':
        return 'My Dashboard';
      default:
        return 'Dashboard';
    }
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };

    if (isDropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isDropdownOpen]);

  return (
    <header className="header">
      <div className="container">
        <div className="header-content">
          <div className="header-left">
            <Link to="/" className="logo">
              <img src="/src/assets/logo.png" alt="TravelEase" className="logo-image" />
              <span className="logo-text">TravelEase</span>
            </Link>
          </div>

          <nav className="nav">
            <Link to="/packages" className="nav-link">Packages</Link>
            <Link to="/reviews" className="nav-link">Reviews</Link>
            <Link to="/assistance" className="nav-link">Assistance</Link>
            
            {isAuthenticated ? (
              <div className="user-menu" ref={dropdownRef}>
                <button 
                  className="user-menu-trigger"
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                >
                  <span className="user-name">
                    {getUserName()}
                  </span>
                  <span className="user-role">
                    ({getUserRole()}{isAgentPendingApproval() ? ' - Pending Approval' : ''})
                  </span>
                  <span className={`dropdown-arrow ${isDropdownOpen ? 'open' : ''}`}>
                    ▼
                  </span>
                </button>
                
                {isDropdownOpen && (
                  <div className="dropdown-menu">
                    {getDashboardLink() && (
                      <Link 
                        to={getDashboardLink()}
                        className="dropdown-item"
                        onClick={() => setIsDropdownOpen(false)}
                      >
                        📊 {getDashboardLabel()}
                      </Link>
                    )}
                    {isAgentPendingApproval() && (
                      <div className="dropdown-item disabled">
                        ⏳ Dashboard (Pending Approval)
                      </div>
                    )}
                    <Link 
                      to="/profile"
                      className="dropdown-item"
                      onClick={() => setIsDropdownOpen(false)}
                    >
                      👤 Profile
                    </Link>
                    <button 
                      onClick={handleLogout}
                      className="dropdown-item logout-btn"
                    >
                      🚪 Logout
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="auth-buttons">
                <Link to="/signin" className="btn btn-secondary">Sign In</Link>
                <Link to="/register" className="btn btn-primary">Sign Up</Link>
              </div>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
};

export default Header; 