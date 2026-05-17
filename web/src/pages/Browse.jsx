import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
    ChevronDown, Filter, Leaf, Package, Search, ShieldCheck,
    ShoppingBag, SlidersHorizontal, Sparkles, Star, Truck
} from 'lucide-react';
import ProductCard from '../components/ProductCard';
import { productsAPI } from '../services/api';

const moods = ['romance', 'apology', 'celebration', 'sympathy', 'friendship', 'just because'];

const sortProducts = (products, sortBy) => {
    const nextProducts = [...products];

    if (sortBy === 'price-low') {
        return nextProducts.sort((a, b) => Number(a.price) - Number(b.price));
    }

    if (sortBy === 'price-high') {
        return nextProducts.sort((a, b) => Number(b.price) - Number(a.price));
    }

    if (sortBy === 'name') {
        return nextProducts.sort((a, b) => a.name.localeCompare(b.name));
    }

    return nextProducts;
};

export default function Browse() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [query, setQuery] = useState('');
    const [selectedMood, setSelectedMood] = useState('');
    const [inStockOnly, setInStockOnly] = useState(false);
    const [sortBy, setSortBy] = useState('featured');

    useEffect(() => {
        const fetchProducts = async () => {
            setLoading(true);
            setError('');

            try {
                const response = await productsAPI.getProducts();
                setProducts(response.data.data || []);
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load products');
            } finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, []);

    const filteredProducts = useMemo(() => {
        const normalizedQuery = query.trim().toLowerCase();

        const filtered = products.filter((product) => {
            const matchesSearch = !normalizedQuery
                || product.name?.toLowerCase().includes(normalizedQuery)
                || product.description?.toLowerCase().includes(normalizedQuery)
                || product.floristName?.toLowerCase().includes(normalizedQuery);
            const matchesMood = !selectedMood || product.moodTags?.includes(selectedMood);
            const matchesStock = !inStockOnly || product.inStock;

            return matchesSearch && matchesMood && matchesStock;
        });

        return sortProducts(filtered, sortBy);
    }, [products, query, selectedMood, inStockOnly, sortBy]);

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900">
            <header className="sticky top-0 z-50 border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between gap-6">
                    <Link to="/dashboard" className="text-2xl font-serif tracking-tight">Petal</Link>
                    <div className="hidden md:flex flex-1 max-w-2xl h-11 border border-stone-300 bg-white">
                        <input
                            value={query}
                            onChange={(event) => setQuery(event.target.value)}
                            placeholder="Search bouquets, florists, or moods"
                            className="flex-1 bg-transparent px-4 text-sm outline-none"
                        />
                        <button type="button" className="w-12 flex items-center justify-center bg-stone-900 text-white">
                            <Search size={18} strokeWidth={1.5} />
                        </button>
                    </div>
                    <div className="flex items-center gap-5">
                        <Link to="/dashboard" className="text-sm text-stone-600 hover:text-stone-900">Home</Link>
                        <Link to="/cart" className="relative inline-flex h-10 w-10 items-center justify-center border border-stone-200 bg-white/70 text-stone-800 hover:border-stone-900" aria-label="View basket">
                            <ShoppingBag size={19} strokeWidth={1.5} />
                        </Link>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-6 py-8">
                <section className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-5 mb-8">
                    <div className="bg-stone-900 text-white p-8 md:p-10 overflow-hidden relative">
                        <div className="relative z-10 max-w-2xl">
                            <div className="inline-flex items-center gap-2 text-xs uppercase tracking-[0.22em] text-stone-300 mb-5">
                                <Sparkles size={14} strokeWidth={1.5} />
                                Browse Petal Market
                            </div>
                            <h1 className="text-5xl md:text-6xl font-serif font-light leading-[0.9] mb-5">
                                Local floral finds, arranged for gifting.
                            </h1>
                            <p className="text-stone-300 leading-relaxed">
                                Browse all available arrangements, compare prices, filter by emotion, and discover Cebu-based florist creations in one marketplace view.
                            </p>
                        </div>
                        <div className="absolute -right-20 -bottom-24 w-72 h-72 rounded-full border border-white/10" />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div className="bg-white border border-stone-200 p-5">
                            <Truck size={20} strokeWidth={1.5} className="text-stone-500 mb-4" />
                            <p className="font-serif text-2xl">Cebu delivery</p>
                            <p className="text-xs text-stone-500 mt-1">AM/PM slots</p>
                        </div>
                        <div className="bg-white border border-stone-200 p-5">
                            <ShieldCheck size={20} strokeWidth={1.5} className="text-stone-500 mb-4" />
                            <p className="font-serif text-2xl">Mock payment</p>
                            <p className="text-xs text-stone-500 mt-1">No real charge</p>
                        </div>
                        <div className="bg-white border border-stone-200 p-5">
                            <Package size={20} strokeWidth={1.5} className="text-stone-500 mb-4" />
                            <p className="font-serif text-2xl">Fresh stock</p>
                            <p className="text-xs text-stone-500 mt-1">Live availability</p>
                        </div>
                        <div className="bg-white border border-stone-200 p-5">
                            <Star size={20} strokeWidth={1.5} className="text-stone-500 mb-4" />
                            <p className="font-serif text-2xl">Mood picks</p>
                            <p className="text-xs text-stone-500 mt-1">Gift intent first</p>
                        </div>
                    </div>
                </section>

                <section className="grid grid-cols-1 lg:grid-cols-[250px_1fr] gap-8">
                    <aside className="space-y-4">
                        <div className="bg-white border border-stone-200 p-5">
                            <div className="flex items-center gap-2 mb-5">
                                <Filter size={17} strokeWidth={1.5} />
                                <p className="text-sm font-semibold">Filters</p>
                            </div>

                            <div className="md:hidden mb-5">
                                <label className="text-xs uppercase tracking-wider text-stone-500 mb-2 block">Search</label>
                                <input
                                    value={query}
                                    onChange={(event) => setQuery(event.target.value)}
                                    placeholder="Search products"
                                    className="w-full h-10 border border-stone-200 px-3 text-sm outline-none focus:border-stone-900"
                                />
                            </div>

                            <div className="border-t border-stone-100 pt-5">
                                <p className="text-xs uppercase tracking-wider text-stone-500 mb-3">Mood</p>
                                <div className="space-y-2">
                                    <button
                                        type="button"
                                        onClick={() => setSelectedMood('')}
                                        className={`w-full text-left text-sm px-3 py-2 border ${selectedMood === '' ? 'border-stone-900 bg-stone-900 text-white' : 'border-stone-200 text-stone-600 hover:border-stone-500'}`}
                                    >
                                        All moods
                                    </button>
                                    {moods.map((mood) => (
                                        <button
                                            key={mood}
                                            type="button"
                                            onClick={() => setSelectedMood(mood)}
                                            className={`w-full text-left text-sm px-3 py-2 border capitalize ${selectedMood === mood ? 'border-stone-900 bg-stone-900 text-white' : 'border-stone-200 text-stone-600 hover:border-stone-500'}`}
                                        >
                                            {mood}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <label className="mt-5 pt-5 border-t border-stone-100 flex items-center gap-3 text-sm text-stone-600 cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={inStockOnly}
                                    onChange={(event) => setInStockOnly(event.target.checked)}
                                    className="h-4 w-4 accent-stone-900"
                                />
                                In stock only
                            </label>
                        </div>
                    </aside>

                    <section>
                        <div className="bg-white border border-stone-200 px-4 py-3 mb-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
                            <div>
                                <p className="text-sm font-medium text-stone-900">{filteredProducts.length} products found</p>
                                <p className="text-xs text-stone-500">Compare local arrangements before checkout.</p>
                            </div>
                            <div className="flex items-center gap-2">
                                <SlidersHorizontal size={16} strokeWidth={1.5} className="text-stone-500" />
                                <select
                                    value={sortBy}
                                    onChange={(event) => setSortBy(event.target.value)}
                                    className="h-10 border border-stone-200 bg-white px-3 text-sm outline-none focus:border-stone-900"
                                >
                                    <option value="featured">Featured</option>
                                    <option value="price-low">Price: Low to High</option>
                                    <option value="price-high">Price: High to Low</option>
                                    <option value="name">Name</option>
                                </select>
                            </div>
                        </div>

                        {error && (
                            <div className="mb-6 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
                                {error}
                            </div>
                        )}

                        {loading ? (
                            <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-x-4 gap-y-8">
                                {[...Array(8)].map((_, index) => (
                                    <div key={index} className="animate-pulse">
                                        <div className="aspect-[4/5] bg-stone-100 mb-3 rounded-sm" />
                                        <div className="h-3 w-20 bg-stone-100 mb-3" />
                                        <div className="h-5 w-32 bg-stone-100 mb-2" />
                                        <div className="h-3 w-24 bg-stone-100" />
                                    </div>
                                ))}
                            </div>
                        ) : filteredProducts.length > 0 ? (
                            <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-x-4 gap-y-8">
                                {filteredProducts.map((product) => (
                                    <ProductCard key={product.id} product={product} />
                                ))}
                            </div>
                        ) : (
                            <div className="border border-stone-200 bg-white/70 px-6 py-12 text-center">
                                <Leaf size={24} strokeWidth={1.5} className="mx-auto text-stone-400 mb-4" />
                                <p className="font-serif text-2xl text-stone-900 mb-2">No products found</p>
                                <p className="text-sm text-stone-500">Try another mood, search term, or stock filter.</p>
                            </div>
                        )}
                    </section>
                </section>
            </main>
        </div>
    );
}
