import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import {
    Menu, Search, ShoppingBag, ArrowRight, Star, Flower2,
    Package, Clock, Check, Plus, Mail, Instagram, Facebook,
    Linkedin, X, LogOut, User, Settings, ChevronDown, Leaf, Store
} from 'lucide-react';
import KpiCard from '../components/KpiCard';
import ProductCard from '../components/ProductCard';
import Grainient from '../components/Grainient';
import { productsAPI } from '../services/api';

const moodFilters = [
    { label: 'All', value: '' },
    { label: 'Romance', value: 'romance' },
    { label: 'Apology', value: 'apology' },
    { label: 'Celebration', value: 'celebration' },
    { label: 'Sympathy', value: 'sympathy' },
    { label: 'Friendship', value: 'friendship' },
    { label: 'Just Because', value: 'just because' },
];

export default function Dashboard() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const [showProfileMenu, setShowProfileMenu] = useState(false);
    const [scrolled, setScrolled] = useState(false);
    const [products, setProducts] = useState([]);
    const [selectedMood, setSelectedMood] = useState(searchParams.get('mood') || '');
    const [productsLoading, setProductsLoading] = useState(true);
    const [productsError, setProductsError] = useState('');

    // Artisan Shop States
    const [artisanTab, setArtisanTab] = useState('overview');
    const [orderTab, setOrderTab] = useState('all');
    const [productTab, setProductTab] = useState('live');

    // Scroll effect for header
    useEffect(() => {
        const handleScroll = () => {
            setScrolled(window.scrollY > 20);
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    useEffect(() => {
        const fetchProducts = async () => {
            setProductsLoading(true);
            setProductsError('');

            try {
                const response = await productsAPI.getProducts(selectedMood);
                setProducts(response.data.data || []);
            } catch (err) {
                setProductsError(err.response?.data?.message || err.message || 'Unable to load products');
            } finally {
                setProductsLoading(false);
            }
        };

        fetchProducts();
    }, [selectedMood]);

    useEffect(() => {
        const mood = searchParams.get('mood') || '';
        setSelectedMood(mood);
    }, [searchParams]);

    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';
    const displayName = user?.name || 'Guest';
    const initials = displayName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const handleMoodSelect = (mood) => {
        setSelectedMood(mood);
        if (mood) {
            setSearchParams({ mood });
        } else {
            setSearchParams({});
        }
    };

    return (
        <div className="antialiased selection:bg-stone-200 selection:text-stone-900 bg-[#FDFCF8] min-h-screen">

            {/* ─── NAVIGATION ─── */}
            <header className={`fixed top-0 w-full h-20 z-50 border-b ${scrolled ? 'bg-[#FDFCF8]/95 backdrop-blur-md border-stone-300 shadow-md' : 'bg-transparent border-transparent shadow-none'}`}>
                <div className="max-w-7xl mx-auto px-6 h-full flex items-center justify-between">
                    {/* Mobile Menu Trigger */}
                    <button className="lg:hidden text-stone-800">
                        <Menu size={24} strokeWidth={1.5} />
                    </button>

                    {/* Logo */}
                    <Link to="/dashboard" className="text-2xl font-serif tracking-tight text-stone-900 z-50 relative">
                        Petal
                    </Link>

                    {/* Desktop Nav */}
                    <nav className="hidden lg:flex gap-8 items-center absolute left-1/2 transform -translate-x-1/2">
                        {isArtisan ? (
                            <>
                                <Link to="/seller-education" className="text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors">Seller Education</Link>
                                <button className="text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors">Marketing Centre</button>
                                <button className="text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors">Help Centre</button>
                            </>
                        ) : (
                            <>
                                <button className="text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors">New Arrivals</button>
                                <Link to="/browse" className="text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors">Browse</Link>
                                <Link to="/shop-by-mood" className="text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors">Shop by Mood</Link>
                                <button className="text-sm font-medium text-stone-600 hover:text-stone-900 transition-colors">Daily Discover</button>
                            </>
                        )}
                    </nav>

                    {/* Actions */}
                    <div className="flex items-center gap-5">
                        <button className="text-stone-800 hover:text-stone-600 transition-colors hidden sm:block">
                            <Search size={20} strokeWidth={1.5} />
                        </button>

                        {!isArtisan && (
                            <Link to="/cart" className="text-stone-800 hover:text-stone-600 transition-colors relative">
                                <ShoppingBag size={20} strokeWidth={1.5} />
                                <span className="absolute -top-1 -right-1 flex h-2 w-2">
                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-stone-400 opacity-75"></span>
                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-stone-800"></span>
                                </span>
                            </Link>
                        )}

                        {/* Profile Dropdown */}
                        <div className="relative border-l border-stone-200 pl-5 ml-1">
                            <button
                                onClick={() => setShowProfileMenu(!showProfileMenu)}
                                className="flex items-center gap-2 group"
                            >
                                <div className="w-8 h-8 rounded-full bg-stone-100 border border-stone-200 text-stone-700 flex items-center justify-center text-xs font-serif italic group-hover:bg-stone-200 transition-colors">
                                    {initials}
                                </div>
                            </button>
                            {showProfileMenu && (
                                <>
                                    <div className="fixed inset-0 z-40" onClick={() => setShowProfileMenu(false)} />
                                    <div className="absolute right-0 mt-4 w-56 bg-white border border-stone-100 shadow-xl z-50 py-2 animate-fadeIn rounded-sm">
                                        <div className="px-5 py-3 border-b border-stone-100">
                                            <p className="text-sm font-serif text-stone-900">{displayName}</p>
                                            <p className="text-xs text-stone-500 mt-0.5">{user?.email}</p>
                                        </div>
                                        <div className="py-1">
                                            {isArtisan && (
                                                <>
                                                    <Link to="/seller-centre" className="flex items-center gap-3 w-full px-5 py-2.5 text-sm text-stone-600 hover:bg-stone-50 hover:text-stone-900 transition-colors">
                                                        <Store size={16} /> Seller Centre
                                                    </Link>
                                                    <Link to="/seller-centre?tab=shopInfo" className="flex items-center gap-3 w-full px-5 py-2.5 text-sm text-stone-600 hover:bg-stone-50 hover:text-stone-900 transition-colors">
                                                        <Settings size={16} /> Shop Information
                                                    </Link>
                                                </>
                                            )}
                                            <Link to="/profile" className="flex items-center gap-3 w-full px-5 py-2.5 text-sm text-stone-600 hover:bg-stone-50 hover:text-stone-900 transition-colors">
                                                <User size={16} /> Profile
                                            </Link>
                                            {!isArtisan && (
                                                <button className="flex items-center gap-3 w-full px-5 py-2.5 text-sm text-stone-600 hover:bg-stone-50 hover:text-stone-900 transition-colors">
                                                    <Settings size={16} /> Settings
                                                </button>
                                            )}
                                        </div>
                                        <div className="border-t border-stone-100 pt-1">
                                            <button
                                                onClick={handleLogout}
                                                className="flex items-center gap-3 w-full px-5 py-2.5 text-sm text-red-800 hover:bg-red-50 transition-colors"
                                            >
                                                <LogOut size={16} /> Sign Out
                                            </button>
                                        </div>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </header>

            <main className="pt-20">
                {/* ─── HERO SECTION ─── */}
                <section className="relative min-h-[90vh] flex items-center justify-center overflow-hidden">
                    <div className="absolute inset-0 z-0">
                        <Grainient
                            color1="#F9C7C7"
                            color2="#C98578"
                            color3="#7C4A44"
                            timeSpeed={0.18}
                            colorBalance={-0.04}
                            warpStrength={1.85}
                            warpFrequency={6.2}
                            warpSpeed={1.25}
                            warpAmplitude={34}
                            blendAngle={-24}
                            blendSoftness={0.14}
                            rotationAmount={380}
                            noiseScale={1.7}
                            grainAmount={0.1}
                            grainScale={2.4}
                            grainAnimated={false}
                            contrast={1.22}
                            gamma={1.0}
                            saturation={1.05}
                            centerX={-0.08}
                            centerY={0.02}
                            zoom={0.82}
                        />
                        <div className="absolute inset-0 bg-[#FDFCF8]/8"></div>
                        <div className="absolute inset-0 bg-gradient-to-t from-[#FDFCF8] via-[#FDFCF8]/10 to-transparent"></div>
                    </div>

                    <div className="relative z-10 text-center max-w-4xl px-6 mt-20">
                        <span className="block text-xs uppercase tracking-[0.2em] text-stone-600 mb-4 animate-fadeIn">Timeless Collection</span>
                        <h1 className="text-5xl md:text-7xl lg:text-8xl font-serif font-light text-stone-900 leading-[0.9] tracking-tight mb-8">
                            The art of <br />
                            <span className="italic text-stone-600">nature preserved</span>
                        </h1>
                        <p className="text-base md:text-lg text-stone-700 max-w-lg mx-auto mb-10 font-light leading-relaxed">
                            Sustainable floral creations, sculpted by time. A raw and poetic aesthetic to elevate your interior without ever fading away.
                        </p>
                        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
                            <a href="#collection" className="group relative px-8 py-3 bg-stone-900 text-stone-50 text-sm font-medium overflow-hidden rounded-sm transition-all hover:bg-stone-800 hover:shadow-lg">
                                <span className="relative z-10 flex items-center gap-2">
                                    Explore Collection
                                    <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform" />
                                </span>
                            </a>
                        </div>
                    </div>
                </section>

                {/* ─── MARQUEE ─── */}
                <div className="border-y border-stone-200 py-4 bg-white overflow-hidden">
                    <div className="flex whitespace-nowrap gap-12 animate-marquee items-center opacity-60 hover:opacity-100 transition-opacity">
                        {[...Array(2)].map((_, i) => (
                            <div key={i} className="flex gap-12">
                                <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-stone-500"><Star size={12} /> Handmade in Cebu, Philippines</div>
                                <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-stone-500"><Flower2 size={12} /> 100% Natural Flowers</div>
                                <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-stone-500"><Package size={12} /> Zero Plastic Packaging</div>
                                <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-stone-500"><Clock size={12} /> Lasts 2+ Years</div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* ─── COLLECTION GRID ─── */}
                <section id="collection" className="max-w-7xl mx-auto px-6 py-24">
                    <div className="flex flex-col md:flex-row justify-between items-end mb-12 gap-6">
                        <div>
                            <h2 className="text-3xl md:text-4xl font-serif text-stone-900 tracking-tight mb-2">Seasonal Selection</h2>
                            <p className="text-stone-500 text-sm font-light">Shop by mood and local artisan availability</p>
                        </div>

                        <div className="flex flex-wrap justify-start md:justify-end gap-2 max-w-2xl">
                            {moodFilters.map((mood) => (
                                <button
                                    key={mood.value || 'all'}
                                    type="button"
                                    onClick={() => handleMoodSelect(mood.value)}
                                    className={`h-10 px-4 border rounded-sm text-xs font-medium uppercase tracking-wide transition-colors ${selectedMood === mood.value
                                        ? 'bg-stone-900 border-stone-900 text-white'
                                        : 'bg-white/70 border-stone-200 text-stone-600 hover:border-stone-500 hover:text-stone-900'
                                        }`}
                                >
                                    {mood.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {productsError && (
                        <div className="mb-10 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
                            {productsError}
                        </div>
                    )}

                    {productsLoading ? (
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

                    <div className="mt-20 text-center">
                        <button className="inline-flex items-center gap-2 text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors border-b border-stone-300 hover:border-stone-900 pb-1">
                            View Full Collection
                        </button>
                    </div>
                </section>

                {/* ─── FORGET-ME-NOT PREVIEW ─── */}
                <section className="bg-stone-900 text-stone-100 py-24 overflow-hidden">
                    <div className="max-w-7xl mx-auto px-6">
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
                            <div className="order-2 lg:order-1 relative">
                                <div className="absolute -top-10 -left-10 w-32 h-32 rounded-full border border-stone-700/50"></div>
                                <div className="relative z-10">
                                    <h2 className="text-4xl md:text-5xl font-serif leading-tight mb-6">
                                        Never forget <br />
                                        <span className="italic text-stone-400">a special moment.</span>
                                    </h2>
                                    <p className="text-stone-400 text-base leading-relaxed mb-8 font-light">
                                        Life gets busy, but love shouldn't wait. With our <strong>Forget-Me-Not</strong> service, schedule recurring deliveries for anniversaries and birthdays. We'll remind you 3 days before, so you're always the hero.
                                    </p>



                                    <Link to="/profile" className="text-sm text-white font-medium hover:text-stone-300 flex items-center gap-2 group mt-8">
                                        Manage your dates <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform" />
                                    </Link>
                                </div>
                            </div>

                            {/* Right Column: Feature Showcase */}
                            {/* Right Column: Feature Showcase */}
                            <div className="order-1 lg:order-2 relative h-[400px] w-full flex items-center justify-center p-8">
                                {/* Decorative elements - subtle glow instead of box */}
                                <div className="absolute inset-0 flex items-center justify-center opacity-30 pointer-events-none">
                                    <div className="w-64 h-64 bg-stone-700/30 rounded-full blur-3xl"></div>
                                </div>

                                {/* The Card */}
                                <div className="bg-stone-800 border border-stone-700 p-8 rounded-sm max-w-md w-full shadow-2xl relative z-10 transform transition-transform hover:scale-105 duration-500">
                                    <div className="flex items-center gap-5 mb-6">
                                        <div className="bg-stone-900 p-3 rounded-full text-stone-300 border border-stone-600">
                                            <Leaf size={24} />
                                        </div>
                                        <div>
                                            <p className="font-serif text-xl text-white">Wife's Birthday</p>
                                            <p className="text-xs text-stone-400 uppercase tracking-wider font-medium">February 14 • Yearly</p>
                                        </div>
                                        <div className="ml-auto">
                                            <span className="text-[10px] uppercase tracking-wider text-green-400 bg-green-900/20 px-3 py-1.5 rounded-sm border border-green-900/30 shadow-sm font-bold">Active</span>
                                        </div>
                                    </div>

                                    <div className="space-y-4 mb-6">
                                        <div className="h-2 w-full bg-stone-900 rounded-full overflow-hidden">
                                            <div className="h-full bg-stone-500 w-3/4"></div>
                                        </div>
                                        <div className="flex justify-between text-xs text-stone-500 font-medium tracking-wide">
                                            <span>Reminder sent</span>
                                            <span className="text-stone-300">3 days left</span>
                                        </div>
                                    </div>

                                    <p className="text-sm text-stone-400 italic border-l-2 border-stone-600 pl-4 py-1">"Automated order created. Pending approval."</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* ─── NEWSLETTER ─── */}
                <section className="py-24 border-b border-stone-200">
                    <div className="max-w-xl mx-auto px-6 text-center">
                        <Mail className="mx-auto mb-6 text-stone-400" size={24} strokeWidth={1.5} />
                        <h3 className="text-2xl font-serif text-stone-900 mb-2">Floral Journal</h3>
                        <p className="text-sm text-stone-500 mb-8">Decor inspiration and access to private sales.</p>

                        <form className="flex flex-col sm:flex-row gap-3">
                            <input type="email" placeholder="your@email.com" className="flex-1 appearance-none bg-stone-50 border border-stone-200 rounded-sm px-4 py-3 text-sm text-stone-900 placeholder:text-stone-400 focus:outline-none focus:border-stone-400 focus:ring-1 focus:ring-stone-400 transition-all" />
                            <button type="button" className="px-6 py-3 bg-stone-900 text-white text-sm font-medium rounded-sm hover:bg-stone-800 transition-colors">
                                Subscribe
                            </button>
                        </form>
                        <p className="text-[10px] text-stone-400 mt-4">We respect your secret garden. No spam.</p>
                    </div>
                </section>
            </main>

            {/* ─── FOOTER ─── */}
            <footer className="bg-[#FDFCF8] pt-16 pb-8">
                <div className="max-w-7xl mx-auto px-6 grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-10 mb-16">
                    <div className="col-span-2 lg:col-span-2">
                        <a href="#" className="text-lg tracking-tighter uppercase font-medium text-stone-900 block mb-6">
                            Petal
                        </a>
                        <p className="text-sm text-stone-500 max-w-xs leading-relaxed mb-6">
                            House of permanent floral composition. For interior design that lasts.
                        </p>
                        <div className="flex gap-4">
                            <a href="#" className="text-stone-400 hover:text-stone-900 transition-colors"><Instagram size={20} /></a>
                            <a href="#" className="text-stone-400 hover:text-stone-900 transition-colors"><Facebook size={20} /></a>
                            <a href="#" className="text-stone-400 hover:text-stone-900 transition-colors"><Linkedin size={20} /></a>
                        </div>
                    </div>

                    <div>
                        <h4 className="text-xs font-semibold uppercase tracking-wider text-stone-900 mb-4">Shop</h4>
                        <ul className="space-y-3">
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Bouquets</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Wreaths</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Vases</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Gift Cards</a></li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="text-xs font-semibold uppercase tracking-wider text-stone-900 mb-4">Maison</h4>
                        <ul className="space-y-3">
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">About</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">The Workshop</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Sustainability</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Press</a></li>
                        </ul>
                    </div>

                    <div>
                        <h4 class="text-xs font-semibold uppercase tracking-wider text-stone-900 mb-4">Help</h4>
                        <ul class="space-y-3">
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Shipping & Returns</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">FAQ</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Care Guide</a></li>
                            <li><a href="#" className="text-sm text-stone-500 hover:text-stone-900 transition-colors">Contact</a></li>
                        </ul>
                    </div>
                </div>

                <div className="max-w-7xl mx-auto px-6 pt-8 border-t border-stone-200 flex flex-col md:flex-row justify-between items-center gap-4">
                    <p className="text-xs text-stone-400">© 2026 Petal. All rights reserved.</p>
                    <div className="flex gap-6">
                        <a href="#" className="text-xs text-stone-400 hover:text-stone-600">Privacy</a>
                        <a href="#" className="text-xs text-stone-400 hover:text-stone-600">Terms</a>
                    </div>
                </div>
            </footer>
        </div >
    );
}
