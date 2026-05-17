import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { Leaf, User, Package } from 'lucide-react';
import FormInput from '../components/FormInput';
import PrimaryButton from '../components/PrimaryButton';

export default function Register() {
    const { register, login } = useAuth();
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        role: 'customer'
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await register(formData.name, formData.email, formData.password, formData.role);
            await login(formData.email, formData.password);
            navigate('/dashboard');
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Failed to register');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#FDFCF8] flex selection:bg-stone-200 selection:text-stone-900 font-sans">
            {/* Left Column: Image */}
            <div className="hidden lg:block w-1/2 bg-stone-100 relative overflow-hidden">
                <img
                    src="/images/register_background_1771726826857.png"
                    alt="Floral Arrangement"
                    className="absolute inset-0 w-full h-full object-cover grayscale-[20%] opacity-90"
                />
                <div className="absolute inset-0 bg-stone-900/10 mix-blend-multiply"></div>
                <div className="absolute top-12 left-12">
                    <Link to="/" className="inline-flex items-center gap-2 group text-white">
                        <Leaf className="w-5 h-5 text-white/80 group-hover:text-white transition-colors" />
                        <span className="text-sm uppercase tracking-widest text-white/80 group-hover:text-white transition-colors">Petal</span>
                    </Link>
                </div>
                <div className="absolute bottom-12 left-12 text-white p-8 max-w-md">
                    <p className="font-serif text-3xl italic mb-4">"Flowers always make people better, happier, and more helpful; they are sunshine, food and medicine for the soul."</p>
                    <p className="text-sm uppercase tracking-widest opacity-80">— Luther Burbank</p>
                </div>
            </div>

            {/* Right Column: Form */}
            <div className="w-full lg:w-1/2 flex flex-col justify-center p-8 md:p-20 relative">
                <div className="max-w-md w-full mx-auto">
                    <div className="mb-10">
                        <div className="lg:hidden mb-8">
                            <Link to="/" className="inline-flex items-center gap-2 group">
                                <Leaf className="w-5 h-5 text-stone-400" />
                                <span className="text-sm uppercase tracking-widest text-stone-500">Petal</span>
                            </Link>
                        </div>
                        <h1 className="text-4xl md:text-5xl font-serif text-stone-900 mb-4 leading-tight">
                            Join the Garden
                        </h1>
                        <p className="text-stone-500 font-light">
                            Create an account to start your journey with timeless nature.
                        </p>
                    </div>

                    {error && (
                        <div className="mb-6 p-4 bg-red-50 border border-red-100 text-red-600 text-sm">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-5">
                        <FormInput
                            label="Full Name"
                            type="text"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            placeholder="Jardinier Parisien"
                            required
                        />

                        <FormInput
                            label="Email Address"
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            placeholder="name@example.com"
                            required
                        />

                        <FormInput
                            label="Password"
                            type="password"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            placeholder="••••••••"
                            required
                        />

                        <div className="space-y-2 pt-2">
                            <label className="text-xs uppercase tracking-wider text-stone-500 font-medium mb-2 block">I am a...</label>
                            <div className="grid grid-cols-2 gap-4">
                                <label className={`cursor-pointer border rounded-sm p-4 flex flex-col items-center gap-2 transition-all ${formData.role === 'customer' ? 'border-stone-900 bg-stone-50' : 'border-stone-200 hover:border-stone-400'}`}>
                                    <input
                                        type="radio"
                                        name="role"
                                        value="customer"
                                        checked={formData.role === 'customer'}
                                        onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                                        className="hidden"
                                    />
                                    <User size={20} strokeWidth={1.5} className={formData.role === 'customer' ? 'text-stone-900' : 'text-stone-400'} />
                                    <span className={`text-xs font-medium uppercase tracking-wide ${formData.role === 'customer' ? 'text-stone-900' : 'text-stone-500'}`}>Customer</span>
                                </label>
                                <label className={`cursor-pointer border rounded-sm p-4 flex flex-col items-center gap-2 transition-all ${formData.role === 'artisan' ? 'border-stone-900 bg-stone-50' : 'border-stone-200 hover:border-stone-400'}`}>
                                    <input
                                        type="radio"
                                        name="role"
                                        value="artisan"
                                        checked={formData.role === 'artisan'}
                                        onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                                        className="hidden"
                                    />
                                    <Package size={20} strokeWidth={1.5} className={formData.role === 'artisan' ? 'text-stone-900' : 'text-stone-400'} />
                                    <span className={`text-xs font-medium uppercase tracking-wide ${formData.role === 'artisan' ? 'text-stone-900' : 'text-stone-500'}`}>Artisan</span>
                                </label>
                            </div>
                        </div>

                        <PrimaryButton type="submit" loading={loading} loadingText="Creating Account..." className="mt-6">
                            Create Account
                        </PrimaryButton>
                    </form>

                    <div className="mt-10 pt-8 border-t border-stone-100 text-center">
                        <p className="text-stone-500 text-sm">
                            Already have an account?{' '}
                            <Link to="/login" className="text-stone-900 font-medium hover:underline decoration-stone-300 underline-offset-4">
                                Sign In
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
