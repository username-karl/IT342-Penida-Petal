import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, ArrowRight, CheckCircle2, Leaf, Package, Store, Truck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { floristAPI, productsAPI } from '../services/api';

const initialShop = {
    storeName: '',
    bio: '',
    city: 'Cebu, Philippines',
    logoUrl: '',
};

const initialDelivery = {
    deliveryCoverage: 'Cebu City, Mandaue, Lapu-Lapu',
    maxDailyCapacity: 12,
    timeSlots: 'AM,PM',
    prepLeadTimeHours: 24,
};

const initialProduct = {
    name: '',
    description: '',
    price: '',
    moodTags: ['celebration'],
    imageUrl: '/images/product_aurora_hydrangea_1771726583839.png',
    inStock: true,
};

const moods = ['romance', 'celebration', 'gratitude', 'sympathy', 'apology', 'calm'];

export default function SellerOnboarding() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [step, setStep] = useState(0);
    const [shop, setShop] = useState(initialShop);
    const [delivery, setDelivery] = useState(initialDelivery);
    const [product, setProduct] = useState(initialProduct);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';

    useEffect(() => {
        if (user && !isArtisan) {
            navigate('/dashboard');
        }
    }, [isArtisan, navigate, user]);

    useEffect(() => {
        const loadProfile = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await floristAPI.getProfile();
                const profile = response.data.data;
                setShop({
                    storeName: profile.storeName || (user?.name ? `${user.name}'s Studio` : ''),
                    bio: profile.bio || initialShop.bio,
                    city: profile.city || initialShop.city,
                    logoUrl: profile.logoUrl || '',
                });
                setDelivery({
                    deliveryCoverage: profile.deliveryCoverage || initialDelivery.deliveryCoverage,
                    maxDailyCapacity: profile.maxDailyCapacity || initialDelivery.maxDailyCapacity,
                    timeSlots: profile.timeSlots || initialDelivery.timeSlots,
                    prepLeadTimeHours: profile.prepLeadTimeHours || initialDelivery.prepLeadTimeHours,
                });

                if (profile.onboardingComplete) {
                    navigate('/seller-centre');
                }
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load seller setup');
            } finally {
                setLoading(false);
            }
        };

        if (isArtisan) {
            loadProfile();
        }
    }, [isArtisan, navigate, user?.name]);

    const completion = useMemo(() => Math.round(((step + 1) / 3) * 100), [step]);

    const saveProfile = async (complete = false) => {
        return floristAPI.updateProfile({
            ...shop,
            ...delivery,
            maxDailyCapacity: Number(delivery.maxDailyCapacity),
            prepLeadTimeHours: Number(delivery.prepLeadTimeHours),
            onboardingComplete: complete,
        });
    };

    const nextStep = async () => {
        setSaving(true);
        setError('');
        try {
            await saveProfile(false);
            setStep((current) => Math.min(current + 1, 2));
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Unable to save setup');
        } finally {
            setSaving(false);
        }
    };

    const finishSetup = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError('');
        try {
            await saveProfile(true);
            if (product.name.trim() && product.description.trim() && product.price) {
                await productsAPI.createProduct({
                    ...product,
                    price: Number(product.price),
                    floristName: shop.storeName,
                });
            }
            navigate('/seller-centre');
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Unable to finish seller setup');
        } finally {
            setSaving(false);
        }
    };

    const toggleMood = (mood) => {
        setProduct((current) => {
            const next = current.moodTags.includes(mood)
                ? current.moodTags.filter((tag) => tag !== mood)
                : [...current.moodTags, mood];
            return { ...current, moodTags: next.length ? next : [mood] };
        });
    };

    if (!user || !isArtisan) return null;

    return (
        <div className="min-h-screen bg-[#F7F3EC] text-stone-900">
            <header className="border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-xl">
                <div className="max-w-6xl mx-auto h-16 px-4 sm:px-6 flex items-center justify-between">
                    <Link to="/dashboard" className="inline-flex items-center gap-2 text-xl font-serif">
                        <Leaf size={18} strokeWidth={1.5} /> Petal
                    </Link>
                    <Link to="/seller-centre" className="text-sm text-stone-500 hover:text-stone-900">
                        Skip to Seller Centre
                    </Link>
                </div>
            </header>

            <main className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
                <div className="grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-6">
                    <aside className="bg-stone-950 text-white p-6 self-start">
                        <p className="text-xs uppercase tracking-[0.24em] text-stone-400">Seller Setup</p>
                        <h1 className="mt-4 font-serif text-4xl leading-tight">Open your Petal shop.</h1>
                        <p className="mt-4 text-sm text-stone-300 leading-relaxed">
                            Complete the essentials customers need before they trust a florist: identity, delivery rules, and first listing.
                        </p>
                        <div className="mt-8">
                            <div className="flex justify-between text-sm mb-2">
                                <span className="text-stone-400">Progress</span>
                                <span>{completion}%</span>
                            </div>
                            <div className="h-2 bg-white/10">
                                <div className="h-full bg-white transition-all" style={{ width: `${completion}%` }} />
                            </div>
                        </div>
                        <div className="mt-8 space-y-3">
                            {[
                                ['Shop profile', Store],
                                ['Delivery setup', Truck],
                                ['First listing', Package],
                            ].map(([label, Icon], index) => (
                                <div key={label} className={`flex items-center gap-3 text-sm ${step === index ? 'text-white' : 'text-stone-500'}`}>
                                    <div className={`h-8 w-8 border flex items-center justify-center ${step >= index ? 'border-white bg-white text-stone-950' : 'border-white/20'}`}>
                                        {step > index ? <CheckCircle2 size={16} /> : <Icon size={15} />}
                                    </div>
                                    {label}
                                </div>
                            ))}
                        </div>
                    </aside>

                    <section className="bg-white border border-stone-200">
                        {loading ? (
                            <div className="p-8 space-y-4">
                                <div className="h-8 w-64 bg-stone-100 animate-pulse" />
                                <div className="h-12 bg-stone-100 animate-pulse" />
                                <div className="h-32 bg-stone-100 animate-pulse" />
                            </div>
                        ) : (
                            <form onSubmit={finishSetup}>
                                <div className="p-6 sm:p-8 border-b border-stone-100">
                                    <p className="text-xs uppercase tracking-[0.2em] text-stone-400">Step {step + 1} of 3</p>
                                    <h2 className="mt-3 font-serif text-3xl">
                                        {step === 0 && 'Tell customers who you are'}
                                        {step === 1 && 'Set your delivery rules'}
                                        {step === 2 && 'Create your first listing'}
                                    </h2>
                                </div>

                                {error && <div className="mx-6 sm:mx-8 mt-6 p-4 bg-red-50 border border-red-100 text-red-700 text-sm">{error}</div>}

                                <div className="p-6 sm:p-8 space-y-5">
                                    {step === 0 && (
                                        <>
                                            <label className="block">
                                                <span className="text-sm font-medium">Store name</span>
                                                <input required value={shop.storeName} onChange={(event) => setShop({ ...shop, storeName: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                            </label>
                                            <label className="block">
                                                <span className="text-sm font-medium">Studio bio</span>
                                                <textarea required rows={5} value={shop.bio} onChange={(event) => setShop({ ...shop, bio: event.target.value })} className="mt-2 w-full border border-stone-200 bg-stone-50 p-3 outline-none focus:border-stone-500" />
                                            </label>
                                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                <label className="block">
                                                    <span className="text-sm font-medium">City</span>
                                                    <input required value={shop.city} onChange={(event) => setShop({ ...shop, city: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                                </label>
                                                <label className="block">
                                                    <span className="text-sm font-medium">Logo URL</span>
                                                    <input value={shop.logoUrl} onChange={(event) => setShop({ ...shop, logoUrl: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                                </label>
                                            </div>
                                        </>
                                    )}

                                    {step === 1 && (
                                        <>
                                            <label className="block">
                                                <span className="text-sm font-medium">Delivery coverage</span>
                                                <input required value={delivery.deliveryCoverage} onChange={(event) => setDelivery({ ...delivery, deliveryCoverage: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                            </label>
                                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                                <label className="block">
                                                    <span className="text-sm font-medium">Daily capacity</span>
                                                    <input required type="number" min="1" value={delivery.maxDailyCapacity} onChange={(event) => setDelivery({ ...delivery, maxDailyCapacity: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                                </label>
                                                <label className="block">
                                                    <span className="text-sm font-medium">Prep lead time</span>
                                                    <input required type="number" min="1" value={delivery.prepLeadTimeHours} onChange={(event) => setDelivery({ ...delivery, prepLeadTimeHours: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                                </label>
                                                <label className="block">
                                                    <span className="text-sm font-medium">Time slots</span>
                                                    <select value={delivery.timeSlots} onChange={(event) => setDelivery({ ...delivery, timeSlots: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500">
                                                        <option value="AM,PM">AM and PM</option>
                                                        <option value="AM">AM only</option>
                                                        <option value="PM">PM only</option>
                                                    </select>
                                                </label>
                                            </div>
                                        </>
                                    )}

                                    {step === 2 && (
                                        <>
                                            <p className="text-sm text-stone-500">Add one product now, or leave this blank and create listings from Seller Centre later.</p>
                                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                <label className="block">
                                                    <span className="text-sm font-medium">Product name</span>
                                                    <input value={product.name} onChange={(event) => setProduct({ ...product, name: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                                </label>
                                                <label className="block">
                                                    <span className="text-sm font-medium">Price</span>
                                                    <input type="number" min="1" step="0.01" value={product.price} onChange={(event) => setProduct({ ...product, price: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                                </label>
                                            </div>
                                            <label className="block">
                                                <span className="text-sm font-medium">Description</span>
                                                <textarea rows={4} value={product.description} onChange={(event) => setProduct({ ...product, description: event.target.value })} className="mt-2 w-full border border-stone-200 bg-stone-50 p-3 outline-none focus:border-stone-500" />
                                            </label>
                                            <label className="block">
                                                <span className="text-sm font-medium">Image URL</span>
                                                <input value={product.imageUrl} onChange={(event) => setProduct({ ...product, imageUrl: event.target.value })} className="mt-2 h-12 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                            </label>
                                            <div>
                                                <p className="text-sm font-medium mb-2">Mood tags</p>
                                                <div className="flex flex-wrap gap-2">
                                                    {moods.map((mood) => (
                                                        <button key={mood} type="button" onClick={() => toggleMood(mood)} className={`h-9 px-3 border text-sm capitalize ${product.moodTags.includes(mood) ? 'bg-stone-900 text-white border-stone-900' : 'border-stone-200 text-stone-600 hover:border-stone-400'}`}>
                                                            {mood}
                                                        </button>
                                                    ))}
                                                </div>
                                            </div>
                                        </>
                                    )}
                                </div>

                                <div className="p-6 sm:p-8 border-t border-stone-100 flex flex-col sm:flex-row justify-between gap-3">
                                    <button type="button" onClick={() => setStep((current) => Math.max(0, current - 1))} disabled={step === 0 || saving} className="h-11 px-5 border border-stone-200 text-sm font-medium inline-flex items-center justify-center gap-2 disabled:opacity-40">
                                        <ArrowLeft size={16} /> Back
                                    </button>
                                    {step < 2 ? (
                                        <button type="button" onClick={nextStep} disabled={saving} className="h-11 px-5 bg-stone-900 text-white text-sm font-medium inline-flex items-center justify-center gap-2 disabled:opacity-60">
                                            {saving ? 'Saving...' : 'Continue'} <ArrowRight size={16} />
                                        </button>
                                    ) : (
                                        <button disabled={saving} className="h-11 px-5 bg-stone-900 text-white text-sm font-medium inline-flex items-center justify-center gap-2 disabled:opacity-60">
                                            {saving ? 'Opening shop...' : 'Open Seller Centre'} <ArrowRight size={16} />
                                        </button>
                                    )}
                                </div>
                            </form>
                        )}
                    </section>
                </div>
            </main>
        </div>
    );
}
