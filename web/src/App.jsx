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

const BUYER_ROLES = ['BUYER'];
const SELLER_ROLES = ['FLORIST'];

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
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <Profile />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/purchase-history"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <PurchaseHistory />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/orders/:id"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <OrderDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/orders/:id/shipping"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <ShippingInformation />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/browse"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <Browse />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/cart"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <Cart />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/checkout"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <Checkout />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/checkout/confirmation"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <CheckoutConfirmation />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/products/:id"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <ProductDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/shop-by-mood"
                        element={
                            <ProtectedRoute roles={BUYER_ROLES}>
                                <ShopByMood />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-education"
                        element={
                            <ProtectedRoute roles={SELLER_ROLES}>
                                <SellerEducation />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-centre"
                        element={
                            <ProtectedRoute roles={SELLER_ROLES}>
                                <SellerCentre />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-orders/:id"
                        element={
                            <ProtectedRoute roles={SELLER_ROLES}>
                                <SellerOrderDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/seller-onboarding"
                        element={
                            <ProtectedRoute roles={SELLER_ROLES}>
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
