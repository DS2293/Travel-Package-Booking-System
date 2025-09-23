import { createContext, useContext, useState, useEffect } from 'react';
import { userService } from '../services';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Check if user is logged in from localStorage
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      const user = JSON.parse(savedUser);
      setCurrentUser(user);
      setIsAuthenticated(true);
    }
  }, []);

  const login = async (email, password) => {
    try {
      console.log('🔐 Attempting login with:', { email });
      const result = await userService.login({ email, password });
      console.log('📥 Login response:', result);
      
      if (result.success && result.data) {
        // Backend returns AuthResponse with token, user, message
        const authData = result.data;
        console.log('✅ Login successful, auth data:', authData);
        
        // Store user info with token
        const userData = {
          ...authData.user,
          token: authData.token
        };
        
        setCurrentUser(userData);
        setIsAuthenticated(true);
        localStorage.setItem('currentUser', JSON.stringify(userData));
        localStorage.setItem('authToken', authData.token);
        
        return { success: true, user: userData };
      } else {
        console.log('❌ Login failed:', result.error);
        return { success: false, message: result.error || 'Invalid email or password' };
      }
    } catch (error) {
      console.error('💥 Login error:', error);
      return { success: false, message: 'Login failed. Please try again.' };
    }
  };

  const register = async (userData) => {
    try {
      console.log('🚀 Starting registration with:', userData);
      
      // Use the register endpoint instead of createUser
      const result = await userService.register({
        name: userData.name,
        email: userData.email,
        password: userData.password,
        role: userData.role,
        contactNumber: userData.contactNumber || ''
      });
      
      console.log('📥 Registration response:', result);
      
      if (result.success && result.data) {
        // Backend returns AuthResponse with token, user, message
        const authData = result.data;
        console.log('✅ Registration successful, auth data:', authData);
        
        // Auto-login after successful registration for customers
        if (userData.role === 'customer' && authData.token) {
          const userDataWithToken = {
            ...authData.user,
            token: authData.token
          };
          
          setCurrentUser(userDataWithToken);
          setIsAuthenticated(true);
          localStorage.setItem('currentUser', JSON.stringify(userDataWithToken));
          localStorage.setItem('authToken', authData.token);
          
          return { success: true, user: userDataWithToken, message: authData.message };
        } else {
          // Agent registration - no auto-login
          return { success: true, user: authData.user, message: authData.message || 'Registration successful' };
        }
      } else {
        console.log('❌ Registration failed:', result.error);
        return { success: false, message: result.error || 'Registration failed. Please try again.' };
      }
    } catch (error) {
      console.error('💥 Registration error:', error);
      return { success: false, message: 'Registration failed. Please try again.' };
    }
  };

  const logout = async () => {
    try {
      // Call API logout endpoint if available
      // await userService.logout();
    } catch (error) {
      console.error('Logout API call failed:', error);
    }

    setCurrentUser(null);
    setIsAuthenticated(false);
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
  };

  const updateUser = async (updatedUser) => {
    try {
      // Update the context and localStorage with the new user data
      const userDataWithToken = {
        ...updatedUser,
        token: currentUser?.token // Preserve the existing token
      };
      
      setCurrentUser(userDataWithToken);
      localStorage.setItem('currentUser', JSON.stringify(userDataWithToken));
      return { success: true };
    } catch (error) {
      console.error('Error updating user context:', error);
      return { success: false, message: 'Failed to update user information' };
    }
  };

  // Function to refresh user data from database
  const refreshUserData = async () => {
    try {
      const result = await userService.getCurrentUser();
      if (result.success && result.data) {
        const userDataWithToken = {
          ...result.data,
          token: currentUser?.token // Preserve the existing token
        };
        
        setCurrentUser(userDataWithToken);
        localStorage.setItem('currentUser', JSON.stringify(userDataWithToken));
        return { success: true, data: result.data };
      } else {
        return { success: false, message: result.error || 'Failed to refresh user data' };
      }
    } catch (error) {
      console.error('Error refreshing user data:', error);
      return { success: false, message: 'Failed to refresh user data' };
    }
  };

  const value = {
    currentUser,
    isAuthenticated,
    login,
    register,
    logout,
    updateUser,
    refreshUserData
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
