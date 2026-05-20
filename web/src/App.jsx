import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Browse from './pages/Browse';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import CheckoutConfirmation from './pages/CheckoutConfirmation';
import ProductDetail from './pages/ProductDetail';
import Profile from './pages/Profile';
import PurchaseHistory from './pages/PurchaseHistory';
import OrderDetail from './pages/OrderDetail';
import ShippingInformation from './pages/ShippingInformation';
import ShopByMood from './pages/ShopByMood';
import SellerCentre from './pages/SellerCentre';
import SellerEducation from './pages/SellerEducation';
import SellerOrderDetail from './pages/SellerOrderDetail';
import SellerOnboarding from './pages/SellerOnboarding';
import './index.css';

export default function App() {
    return (
        <Router>
            <AuthProvider>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <Dashboard />
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
                    <Route
                        path="/purchase-history"
                        element={
                            <ProtectedRoute>
                                <PurchaseHistory />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/orders/:id"
                        element={
                            <ProtectedRoute>
                                <OrderDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/orders/:id/shipping"
                        element={
                            <ProtectedRoute>
                                <ShippingInformation />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/browse"
                        element={
                            <ProtectedRoute>
                                <Browse />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/cart"
                        element={
                            <ProtectedRoute>
                                <Cart />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/checkout"
                        element={
                            <ProtectedRoute>
                                <Checkout />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/checkout/confirmation"
                        element={
                            <ProtectedRoute>
                                <CheckoutConfirmation />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/products/:id"
                        element={
                            <ProtectedRoute>
                                <ProductDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/shop-by-mood"
                        element={
                            <ProtectedRoute>
                                <ShopByMood />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-education"
                        element={
                            <ProtectedRoute>
                                <SellerEducation />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-centre"
                        element={
                            <ProtectedRoute>
                                <SellerCentre />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-orders/:id"
                        element={
                            <ProtectedRoute>
                                <SellerOrderDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-onboarding"
                        element={
                            <ProtectedRoute>
                                <SellerOnboarding />
                            </ProtectedRoute>
                        }
                    />
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </AuthProvider>
        </Router>
    );
}
