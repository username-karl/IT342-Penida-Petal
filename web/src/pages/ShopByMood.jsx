import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ArrowLeft, Flower2, Leaf, ShoppingBag, Sparkles } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import { productsAPI } from '../services/api';

const moods = [
    { label: 'Romance', value: 'romance', note: 'Soft gestures, anniversaries, and quiet devotion.' },
    { label: 'Apology', value: 'apology', note: 'Gentle arrangements for making things right.' },
    { label: 'Celebration', value: 'celebration', note: 'Bright pieces for milestones and big days.' },
    { label: 'Sympathy', value: 'sympathy', note: 'Calm, respectful florals for tender moments.' },
    { label: 'Friendship', value: 'friendship', note: 'Warm gifts for the people who stay close.' },
    { label: 'Just Because', value: 'just because', note: 'Everyday flowers with no occasion required.' },
];

export default function ShopByMood() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [selectedMood, setSelectedMood] = useState(searchParams.get('mood') || 'romance');
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const mood = searchParams.get('mood') || 'romance';
        setSelectedMood(mood);
    }, [searchParams]);

    useEffect(() => {
        const fetchProducts = async () => {
            setLoading(true);
            setError('');

            try {
                const response = await productsAPI.getProducts(selectedMood);
                setProducts(response.data.data || []);
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load mood collection');
            } finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, [selectedMood]);

    const activeMood = moods.find((mood) => mood.value === selectedMood) || moods[0];

    const chooseMood = (mood) => {
        setSearchParams({ mood });
    };

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900">
            <header className="h-20 border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="max-w-7xl mx-auto px-6 h-full flex items-center justify-between">
                    <Link to="/dashboard" className="inline-flex items-center gap-2 text-2xl font-serif tracking-tight">
                        <Leaf size={20} strokeWidth={1.5} />
                        Petal
                    </Link>
                    <div className="flex items-center gap-5">
                        <Link to="/dashboard" className="hidden sm:inline-flex items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-900">
                            <ArrowLeft size={16} strokeWidth={1.5} />
                            Back home
                        </Link>
                        <Link to="/cart" className="relative inline-flex h-10 w-10 items-center justify-center border border-stone-200 bg-white/70 text-stone-800 hover:border-stone-900" aria-label="View basket">
                            <ShoppingBag size={19} strokeWidth={1.5} />
                        </Link>
                    </div>
                </div>
            </header>

            <main>
                <section className="border-b border-stone-200 bg-white/60">
                    <div className="max-w-7xl mx-auto px-6 py-16 md:py-20 grid grid-cols-1 lg:grid-cols-[1fr_420px] gap-10 items-end">
                        <div>
                            <div className="inline-flex items-center gap-2 text-xs uppercase tracking-[0.22em] text-stone-500 mb-5">
                                <Sparkles size={14} strokeWidth={1.5} />
                                Shop by Mood
                            </div>
                            <h1 className="text-5xl md:text-7xl font-serif font-light leading-[0.9] tracking-tight mb-6">
                                Find flowers by <span className="italic text-stone-500">feeling.</span>
                            </h1>
                            <p className="text-stone-600 leading-relaxed max-w-2xl">
                                Choose the emotion first. Petal curates matching local arrangements so the gift feels intentional before the card is even written.
                            </p>
                        </div>
                        <div className="border border-stone-200 bg-[#FDFCF8] p-6">
                            <Flower2 size={24} strokeWidth={1.5} className="text-stone-500 mb-4" />
                            <p className="text-3xl font-serif text-stone-900 mb-2">{activeMood.label}</p>
                            <p className="text-sm text-stone-500 leading-relaxed">{activeMood.note}</p>
                        </div>
                    </div>
                </section>

                <section className="max-w-7xl mx-auto px-6 py-12">
                    <div className="flex flex-wrap gap-2 mb-12">
                        {moods.map((mood) => (
                            <button
                                key={mood.value}
                                type="button"
                                onClick={() => chooseMood(mood.value)}
                                className={`h-11 px-4 border rounded-sm text-xs font-medium uppercase tracking-wide ${selectedMood === mood.value
                                    ? 'bg-stone-900 border-stone-900 text-white'
                                    : 'bg-white/70 border-stone-200 text-stone-600 hover:border-stone-500 hover:text-stone-900'
                                    }`}
                            >
                                {mood.label}
                            </button>
                        ))}
                    </div>

                    {error && (
                        <div className="mb-10 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
                            {error}
                        </div>
                    )}

                    {loading ? (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-y-16 gap-x-8">
                            {[...Array(6)].map((_, index) => (
                                <div key={index} className="animate-pulse">
                                    <div className="aspect-[4/5] bg-stone-100 mb-4 rounded-sm" />
                                    <div className="h-3 w-24 bg-stone-100 mb-3" />
                                    <div className="h-5 w-40 bg-stone-100 mb-2" />
                                    <div className="h-3 w-56 bg-stone-100" />
                                </div>
                            ))}
                        </div>
                    ) : products.length > 0 ? (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-y-16 gap-x-8">
                            {products.map((product) => (
                                <ProductCard key={product.id} product={product} />
                            ))}
                        </div>
                    ) : (
                        <div className="border border-stone-200 bg-white/70 px-6 py-12 text-center">
                            <p className="font-serif text-2xl text-stone-900 mb-2">No arrangements found</p>
                            <p className="text-sm text-stone-500">Try another mood to discover more local creations.</p>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}
