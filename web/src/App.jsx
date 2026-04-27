import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './features/auth/context/AuthContext';
import ProtectedRoute from './shared/components/ProtectedRoute';
import LoginPage from './features/auth/pages/LoginPage';
import RegisterPage from './features/auth/pages/RegisterPage';
import DashboardPage from './features/user/pages/DashboardPage';
import ProfilePage from './features/user/pages/ProfilePage';
import SellerCentrePage from './features/seller/pages/SellerCentrePage';
import SellerEducationPage from './features/seller/pages/SellerEducationPage';
import './index.css';

export default function App() {
    return (
        <Router>
            <AuthProvider>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <DashboardPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <ProfilePage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-education"
                        element={
                            <ProtectedRoute>
                                <SellerEducationPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-centre"
                        element={
                            <ProtectedRoute>
                                <SellerCentrePage />
                            </ProtectedRoute>
                        }
                    />
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </AuthProvider>
        </Router>
    );
}
