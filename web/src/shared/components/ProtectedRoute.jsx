import { Navigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/context/AuthContext';

export default function ProtectedRoute({ children }) {
    const { user, loading } = useAuth();

    if (loading) {
        return (
            <div className="h-screen w-full flex items-center justify-center bg-[#F9F9F8]">
                <div className="flex flex-col items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center border border-rose-100">
                        <iconify-icon icon="solar:leaf-linear" width="20" className="animate-spin"></iconify-icon>
                    </div>
                    <span className="text-sm text-stone-500 font-medium">Loading...</span>
                </div>
            </div>
        );
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    return children;
}
