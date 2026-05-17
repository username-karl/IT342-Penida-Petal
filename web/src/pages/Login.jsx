import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { Leaf } from 'lucide-react';
import FormInput from '../components/FormInput';
import PrimaryButton from '../components/PrimaryButton';

export default function Login() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await login(email, password);
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Failed to login');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#FDFCF8] flex selection:bg-stone-200 selection:text-stone-900 font-sans">
            {/* Left Column: Form */}
            <div className="w-full lg:w-1/2 flex flex-col justify-center p-8 md:p-20 relative">
                <div className="max-w-md w-full mx-auto">
                    <div className="mb-12">
                        <Link to="/" className="inline-flex items-center gap-2 mb-6 group">
                            <Leaf className="w-5 h-5 text-stone-400 group-hover:text-stone-600 transition-colors" />
                            <span className="text-sm uppercase tracking-widest text-stone-500 group-hover:text-stone-900 transition-colors">Petal</span>
                        </Link>
                        <h1 className="text-4xl md:text-5xl font-serif text-stone-900 mb-4 leading-tight">
                            Welcome Back
                        </h1>
                        <p className="text-stone-500 font-light">
                            Sign in to access your curated floral collection.
                        </p>
                    </div>

                    {error && (
                        <div className="mb-6 p-4 bg-red-50 border border-red-100 text-red-600 text-sm">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <FormInput
                            label="Email Address"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="name@example.com"
                            required
                        />

                        <FormInput
                            label="Password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            required
                            extraLabel={
                                <a href="#" className="text-xs text-stone-400 hover:text-stone-900 transition-colors">Forgot password?</a>
                            }
                        />

                        <PrimaryButton type="submit" loading={loading} loadingText="Signing in...">
                            Sign In
                        </PrimaryButton>
                    </form>

                    <div className="mt-12 pt-8 border-t border-stone-100 text-center">
                        <p className="text-stone-500 text-sm">
                            New to Petal?{' '}
                            <Link to="/register" className="text-stone-900 font-medium hover:underline decoration-stone-300 underline-offset-4">
                                Create an account
                            </Link>
                        </p>
                    </div>
                </div>

                <div className="absolute bottom-8 left-8 md:left-20 text-[10px] text-stone-400 uppercase tracking-widest">
                    © 2026 Petal. Paris.
                </div>
            </div>

            {/* Right Column: Image */}
            <div className="hidden lg:block w-1/2 bg-stone-100 relative overflow-hidden">
                <img
                    src="/images/login_background_1771726810843.png"
                    alt="Dried Flowers"
                    className="absolute inset-0 w-full h-full object-cover grayscale-[20%] opacity-90"
                />
                <div className="absolute inset-0 bg-stone-900/10 mix-blend-multiply"></div>
                <div className="absolute bottom-12 left-12 text-white p-8 max-w-sm">
                    <p className="font-serif text-3xl italic mb-4">"The details are not the details. They make the design."</p>
                    <p className="text-sm uppercase tracking-widest opacity-80">— Charles Eames</p>
                </div>
            </div>
        </div>
    );
}
