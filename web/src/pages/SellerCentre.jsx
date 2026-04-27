import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { productAPI, floristAPI } from '../services/api';
import {
    Leaf, LogOut, Package, User, Settings,
    Flower2, Edit2, Plus, Search, ArrowRight,
    ChevronDown, Store, AlertCircle, X, Check,
    Trash2, ToggleLeft, ToggleRight, RefreshCw, Tag
} from 'lucide-react';
import KpiCard from '../components/KpiCard';

// ─── Mood tag options matching the backend enum ───
const MOOD_OPTIONS = [
    { value: 'ROMANCE', label: 'Romance', emoji: '💕' },
    { value: 'APOLOGY', label: 'Apology', emoji: '🙏' },
    { value: 'CELEBRATION', label: 'Celebration', emoji: '🎉' },
    { value: 'SYMPATHY', label: 'Sympathy', emoji: '🕊️' },
    { value: 'FRIENDSHIP', label: 'Friendship', emoji: '🤝' },
    { value: 'JUST_BECAUSE', label: 'Just Because', emoji: '🌸' },
];

// Default placeholder images for products
const PLACEHOLDER_IMAGES = [
    'https://images.unsplash.com/photo-1487530811176-3780de880c2d?w=400&h=400&fit=crop',
    'https://images.unsplash.com/photo-1490750967868-88aa4f44baee?w=400&h=400&fit=crop',
    'https://images.unsplash.com/photo-1561181286-d3fee7d55364?w=400&h=400&fit=crop',
    'https://images.unsplash.com/photo-1606041008023-472dfb5e530f?w=400&h=400&fit=crop',
    'https://images.unsplash.com/photo-1563241527-3004b7be0ffd?w=400&h=400&fit=crop',
    'https://images.unsplash.com/photo-1455659817273-f96807779a8a?w=400&h=400&fit=crop',
];

export default function SellerCentre() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Tab state from URL param
    const searchParams = new URLSearchParams(location.search);
    const initialTab = searchParams.get('tab') || 'overview';

    // ─── STATE ───
    const [artisanTab, setArtisanTab] = useState(initialTab);
    const [productTab, setProductTab] = useState('all');
    const [isEditingShop, setIsEditingShop] = useState(false);

    // Data from API
    const [products, setProducts] = useState([]);
    const [floristProfile, setFloristProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [actionLoading, setActionLoading] = useState(false);

    // Shop form state
    const [shopForm, setShopForm] = useState({
        storeName: '',
        bio: '',
        street: '',
        city: '',
        zipCode: '',
    });

    // Product modal state
    const [showProductModal, setShowProductModal] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [productForm, setProductForm] = useState({
        name: '',
        description: '',
        price: '',
        imageUrl: '',
        moodTags: [],
    });
    const [formErrors, setFormErrors] = useState({});

    // Toast Notification
    const [toast, setToast] = useState(null);

    // Delete confirmation
    const [deleteConfirm, setDeleteConfirm] = useState(null);

    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';
    const initials = (floristProfile?.storeName || user?.name || 'P')
        .split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

    // ─── TOAST HELPER ───
    const showToast = (message, type = 'success') => {
        setToast({ message, type });
        setTimeout(() => setToast(null), 4000);
    };

    // ─── FETCH DATA ───
    const fetchProducts = useCallback(async () => {
        try {
            const res = await productAPI.getMine();
            setProducts(res.data.data || []);
        } catch (err) {
            console.error('Failed to fetch products:', err);
        }
    }, []);

    const fetchProfile = useCallback(async () => {
        try {
            const res = await floristAPI.getProfile();
            const profile = res.data.data;
            setFloristProfile(profile);
            setShopForm({
                storeName: profile.storeName || '',
                bio: profile.bio || '',
                street: profile.street || '',
                city: profile.city || '',
                zipCode: profile.zipCode || '',
            });
        } catch (err) {
            console.error('Failed to fetch profile:', err);
        }
    }, []);

    useEffect(() => {
        const loadData = async () => {
            setLoading(true);
            await Promise.all([fetchProducts(), fetchProfile()]);
            setLoading(false);
        };
        if (isArtisan) loadData();
    }, [isArtisan, fetchProducts, fetchProfile]);

    // ─── HANDLERS ───
    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    // Protect route
    if (user && !isArtisan) {
        navigate('/dashboard');
        return null;
    }

    // ─── SHOP PROFILE SAVE ───
    const handleSaveShopInfo = async () => {
        if (!shopForm.storeName.trim()) {
            showToast('Store name is required', 'error');
            return;
        }
        setActionLoading(true);
        try {
            const res = await floristAPI.updateProfile(shopForm);
            setFloristProfile(res.data.data);
            setIsEditingShop(false);
            showToast('Shop information saved successfully!');
        } catch (err) {
            showToast(err.response?.data?.message || 'Failed to save shop information', 'error');
        } finally {
            setActionLoading(false);
        }
    };

    // ─── PRODUCT FORM VALIDATION ───
    const validateProductForm = () => {
        const errors = {};
        if (!productForm.name.trim()) errors.name = 'Product name is required';
        if (!productForm.price || parseFloat(productForm.price) <= 0) errors.price = 'Valid price is required';
        if (productForm.moodTags.length === 0) errors.moodTags = 'Select at least one mood tag';
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // ─── OPEN ADD PRODUCT MODAL ───
    const openAddModal = () => {
        setEditingProduct(null);
        setProductForm({ name: '', description: '', price: '', imageUrl: '', moodTags: [] });
        setFormErrors({});
        setShowProductModal(true);
    };

    // ─── OPEN EDIT PRODUCT MODAL ───
    const openEditModal = (product) => {
        setEditingProduct(product);
        setProductForm({
            name: product.name,
            description: product.description || '',
            price: product.price.toString(),
            imageUrl: product.imageUrl || '',
            moodTags: Array.from(product.moodTags || []),
        });
        setFormErrors({});
        setShowProductModal(true);
    };

    // ─── SAVE PRODUCT (CREATE OR UPDATE) ───
    const handleSaveProduct = async () => {
        if (!validateProductForm()) return;

        setActionLoading(true);
        const payload = {
            name: productForm.name.trim(),
            description: productForm.description.trim(),
            price: parseFloat(productForm.price),
            imageUrl: productForm.imageUrl.trim() ||
                PLACEHOLDER_IMAGES[Math.floor(Math.random() * PLACEHOLDER_IMAGES.length)],
            moodTags: productForm.moodTags,
        };

        try {
            if (editingProduct) {
                await productAPI.update(editingProduct.id, payload);
                showToast('Product updated successfully!');
            } else {
                await productAPI.create(payload);
                showToast('Product created successfully!');
            }
            setShowProductModal(false);
            await fetchProducts();
        } catch (err) {
            const msg = err.response?.data?.message || 'Failed to save product';
            showToast(msg, 'error');
        } finally {
            setActionLoading(false);
        }
    };

    // ─── TOGGLE STOCK ───
    const handleToggleStock = async (product) => {
        try {
            await productAPI.toggleStock(product.id, !product.inStock);
            showToast(`"${product.name}" marked as ${!product.inStock ? 'In Stock' : 'Out of Stock'}`);
            await fetchProducts();
        } catch (err) {
            showToast('Failed to update stock status', 'error');
        }
    };

    // ─── DELETE PRODUCT ───
    const handleDeleteProduct = async (productId) => {
        try {
            await productAPI.delete(productId);
            showToast('Product deleted successfully!');
            setDeleteConfirm(null);
            await fetchProducts();
        } catch (err) {
            showToast('Failed to delete product', 'error');
        }
    };

    // ─── MOOD TAG TOGGLE ───
    const toggleMoodTag = (tag) => {
        setProductForm(prev => ({
            ...prev,
            moodTags: prev.moodTags.includes(tag)
                ? prev.moodTags.filter(t => t !== tag)
                : [...prev.moodTags, tag],
        }));
    };

    // ─── FILTERED PRODUCTS ───
    const filteredProducts = products.filter(p => {
        if (productTab === 'all') return true;
        if (productTab === 'live') return p.inStock;
        if (productTab === 'sold out') return !p.inStock;
        return true;
    });

    const liveCount = products.filter(p => p.inStock).length;
    const soldOutCount = products.filter(p => !p.inStock).length;

    // ─── LOADING STATE ───
    if (loading) {
        return (
            <div className="min-h-screen bg-[#FDFCF8] flex items-center justify-center">
                <div className="text-center">
                    <RefreshCw className="animate-spin text-stone-400 mx-auto mb-4" size={32} />
                    <p className="text-stone-500 text-sm">Loading your dashboard...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900 font-sans">

            {/* ─── TOAST NOTIFICATION ─── */}
            {toast && (
                <div className={`fixed top-6 right-6 z-[100] px-5 py-3 rounded-sm shadow-xl border text-sm font-medium flex items-center gap-3 animate-fadeIn ${
                    toast.type === 'error'
                        ? 'bg-red-50 border-red-200 text-red-800'
                        : 'bg-green-50 border-green-200 text-green-800'
                }`}>
                    {toast.type === 'error' ? <AlertCircle size={16} /> : <Check size={16} />}
                    {toast.message}
                    <button onClick={() => setToast(null)} className="ml-2 opacity-60 hover:opacity-100">
                        <X size={14} />
                    </button>
                </div>
            )}

            {/* ─── NAVIGATION ─── */}
            <nav className="fixed top-0 w-full z-50 bg-[#FDFCF8]/90 backdrop-blur-md border-b border-stone-200 py-4">
                <div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                        <Link to="/dashboard" className="text-xl font-serif tracking-tight text-stone-900">
                            Petal
                        </Link>
                        <span className="text-stone-300">|</span>
                        <span className="text-sm font-medium text-stone-600">Seller Centre</span>
                    </div>

                    <div className="flex items-center gap-6">
                        <Link to="/seller-education" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Seller Education
                        </Link>
                        <Link to="/dashboard" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Return to Shop
                        </Link>
                        <button onClick={handleLogout} className="text-sm font-medium text-stone-900 hover:text-red-700 transition-colors">
                            Sign Out
                        </button>
                    </div>
                </div>
            </nav>

            <main className="pt-32 pb-24 max-w-7xl mx-auto px-6 min-h-[80vh]">
                <div className="mb-8 flex justify-between items-end">
                    <div>
                        <h1 className="text-3xl font-serif text-stone-900">Seller Centre</h1>
                        <p className="text-stone-500 text-sm mt-1">Manage your shop, products, and orders.</p>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                    {/* ─── SIDEBAR ─── */}
                    <aside className="lg:col-span-3">
                        <div className="bg-white border border-stone-200 rounded-sm p-4 sticky top-24">
                            <div className="mb-6">
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Dashboard</h3>
                                <ul className="space-y-1">
                                    <li>
                                        <button onClick={() => setArtisanTab('overview')} className={`w-full text-left px-3 py-2 text-sm rounded-sm transition-colors ${artisanTab === 'overview' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}>Overview</button>
                                    </li>
                                </ul>
                            </div>
                            <div className="mb-6">
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Products</h3>
                                <ul className="space-y-1">
                                    <li><button onClick={() => setArtisanTab('products')} className={`w-full text-left px-3 py-2 text-sm rounded-sm transition-colors ${artisanTab === 'products' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}>My Products</button></li>
                                    <li><button onClick={() => { setArtisanTab('products'); openAddModal(); }} className="w-full text-left px-3 py-2 text-sm text-stone-600 hover:bg-stone-50 rounded-sm transition-colors">Add New Product</button></li>
                                </ul>
                            </div>
                            <div className="mb-6">
                                <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-3 px-3">Shop</h3>
                                <ul className="space-y-1">
                                    <li><button onClick={() => setArtisanTab('shopInfo')} className={`w-full text-left px-3 py-2 text-sm rounded-sm transition-colors ${artisanTab === 'shopInfo' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}>Shop Information</button></li>
                                </ul>
                            </div>
                        </div>
                    </aside>

                    {/* ─── MAIN CONTENT ─── */}
                    <div className="lg:col-span-9 space-y-6">

                        {/* ─── OVERVIEW TAB ─── */}
                        {artisanTab === 'overview' && (
                            <>
                                {/* To Do List */}
                                <div className="bg-white border border-stone-200 rounded-sm p-6">
                                    <h3 className="text-lg font-serif text-stone-900 mb-6 font-medium">To Do List</h3>
                                    <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
                                        <div className="text-center group cursor-pointer" onClick={() => setArtisanTab('products')}>
                                            <p className="text-2xl font-light text-stone-900 group-hover:text-amber-600 transition-colors">{products.length}</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">Total Products</p>
                                        </div>
                                        <div className="text-center group cursor-pointer border-l border-stone-100" onClick={() => { setArtisanTab('products'); setProductTab('live'); }}>
                                            <p className="text-2xl font-light text-green-700 group-hover:text-green-500 transition-colors">{liveCount}</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">Live</p>
                                        </div>
                                        <div className="text-center group cursor-pointer border-l border-stone-100" onClick={() => { setArtisanTab('products'); setProductTab('sold out'); }}>
                                            <p className="text-2xl font-light text-red-600 group-hover:text-red-400 transition-colors">{soldOutCount}</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">Sold Out</p>
                                        </div>
                                        <div className="text-center border-l border-stone-100">
                                            <p className="text-2xl font-light text-stone-900">{products.reduce((sum, p) => sum + (p.moodTags?.length || 0), 0)}</p>
                                            <p className="text-xs text-stone-500 mt-1 uppercase tracking-wider">Mood Tags</p>
                                        </div>
                                    </div>
                                </div>

                                {/* Business Insights */}
                                <div className="bg-white border border-stone-200 rounded-sm p-6">
                                    <div className="flex justify-between items-center mb-6">
                                        <h3 className="text-lg font-serif text-stone-900 font-medium">Business Insights</h3>
                                        <button onClick={() => setArtisanTab('products')} className="text-xs text-stone-500 hover:text-stone-900 flex items-center gap-1 transition-colors">View Products <ArrowRight size={14} /></button>
                                    </div>
                                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                                        <KpiCard title="Total Products" value={products.length.toString()} />
                                        <KpiCard title="In Stock" value={liveCount.toString()} />
                                        <KpiCard title="Out of Stock" value={soldOutCount.toString()} />
                                    </div>
                                </div>

                                {/* Quick Actions */}
                                <div className="bg-white border border-stone-200 rounded-sm p-6">
                                    <h3 className="text-lg font-serif text-stone-900 mb-6 font-medium">Quick Actions</h3>
                                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                        <button onClick={openAddModal} className="flex items-center gap-3 p-4 border border-stone-200 rounded-sm hover:bg-stone-50 hover:border-stone-300 transition-all group">
                                            <div className="w-10 h-10 bg-stone-100 rounded-full flex items-center justify-center group-hover:bg-stone-900 transition-colors">
                                                <Plus size={18} className="text-stone-600 group-hover:text-white transition-colors" />
                                            </div>
                                            <div className="text-left">
                                                <p className="text-sm font-medium text-stone-900">Add Product</p>
                                                <p className="text-xs text-stone-500">List a new arrangement</p>
                                            </div>
                                        </button>
                                        <button onClick={() => setArtisanTab('shopInfo')} className="flex items-center gap-3 p-4 border border-stone-200 rounded-sm hover:bg-stone-50 hover:border-stone-300 transition-all group">
                                            <div className="w-10 h-10 bg-stone-100 rounded-full flex items-center justify-center group-hover:bg-stone-900 transition-colors">
                                                <Store size={18} className="text-stone-600 group-hover:text-white transition-colors" />
                                            </div>
                                            <div className="text-left">
                                                <p className="text-sm font-medium text-stone-900">Edit Store</p>
                                                <p className="text-xs text-stone-500">Update shop details</p>
                                            </div>
                                        </button>
                                        <button onClick={fetchProducts} className="flex items-center gap-3 p-4 border border-stone-200 rounded-sm hover:bg-stone-50 hover:border-stone-300 transition-all group">
                                            <div className="w-10 h-10 bg-stone-100 rounded-full flex items-center justify-center group-hover:bg-stone-900 transition-colors">
                                                <RefreshCw size={18} className="text-stone-600 group-hover:text-white transition-colors" />
                                            </div>
                                            <div className="text-left">
                                                <p className="text-sm font-medium text-stone-900">Refresh Data</p>
                                                <p className="text-xs text-stone-500">Sync latest products</p>
                                            </div>
                                        </button>
                                    </div>
                                </div>
                            </>
                        )}

                        {/* ─── PRODUCTS TAB ─── */}
                        {artisanTab === 'products' && (
                            <div className="bg-white border border-stone-200 rounded-sm">
                                <div className="border-b border-stone-200 flex flex-col sm:flex-row justify-between sm:items-center pr-6 gap-4 sm:gap-0">
                                    <nav className="flex px-2 overflow-x-auto" aria-label="Tabs">
                                        {[
                                            { key: 'all', label: 'All', count: products.length },
                                            { key: 'live', label: 'Live', count: liveCount },
                                            { key: 'sold out', label: 'Sold Out', count: soldOutCount },
                                        ].map((tab) => (
                                            <button
                                                key={tab.key}
                                                onClick={() => setProductTab(tab.key)}
                                                className={`whitespace-nowrap py-4 px-6 text-sm font-medium border-b-2 transition-colors ${productTab === tab.key
                                                    ? 'border-stone-900 text-stone-900'
                                                    : 'border-transparent text-stone-500 hover:text-stone-700 hover:border-stone-300'
                                                    }`}
                                            >
                                                {tab.label} ({tab.count})
                                            </button>
                                        ))}
                                    </nav>
                                    <div className="px-6 pb-4 sm:p-0 sm:pl-4 self-start sm:self-center">
                                        <button onClick={openAddModal} className="flex items-center gap-2 bg-stone-900 text-white px-4 py-2 rounded-sm text-sm font-medium hover:bg-stone-800 transition-colors whitespace-nowrap">
                                            <Plus size={16} /> Add New Product
                                        </button>
                                    </div>
                                </div>
                                <div className="p-6">
                                    {filteredProducts.length === 0 ? (
                                        <div className="text-center py-16">
                                            <Flower2 className="mx-auto text-stone-300 mb-4" size={48} strokeWidth={1} />
                                            <h4 className="text-lg font-serif text-stone-700 mb-2">No products yet</h4>
                                            <p className="text-sm text-stone-500 mb-6">Start by adding your first floral arrangement</p>
                                            <button onClick={openAddModal} className="inline-flex items-center gap-2 bg-stone-900 text-white px-5 py-2.5 rounded-sm text-sm font-medium hover:bg-stone-800 transition-colors">
                                                <Plus size={16} /> Add Your First Product
                                            </button>
                                        </div>
                                    ) : (
                                        <div className="overflow-x-auto">
                                            <table className="w-full text-sm text-left">
                                                <thead className="text-xs text-stone-500 uppercase bg-stone-50 border-y border-stone-200">
                                                    <tr>
                                                        <th className="px-4 py-3 font-medium">Product Name</th>
                                                        <th className="px-4 py-3 font-medium text-center">Price</th>
                                                        <th className="px-4 py-3 font-medium text-center">Mood Tags</th>
                                                        <th className="px-4 py-3 font-medium text-center">Status</th>
                                                        <th className="px-4 py-3 font-medium text-center">Actions</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {filteredProducts.map((product) => (
                                                        <tr key={product.id} className="border-b border-stone-100 hover:bg-stone-50/50 transition-colors">
                                                            <td className="px-4 py-4 min-w-[250px]">
                                                                <div className="flex gap-3 items-start">
                                                                    <img
                                                                        src={product.imageUrl || PLACEHOLDER_IMAGES[0]}
                                                                        className="w-12 h-12 object-cover border border-stone-200 rounded-sm flex-shrink-0"
                                                                        alt={product.name}
                                                                        onError={(e) => { e.target.src = PLACEHOLDER_IMAGES[0]; }}
                                                                    />
                                                                    <div>
                                                                        <p className="text-stone-900 font-medium line-clamp-2">{product.name}</p>
                                                                        <p className="text-stone-500 text-xs mt-1 line-clamp-1">{product.description || 'No description'}</p>
                                                                    </div>
                                                                </div>
                                                            </td>
                                                            <td className="px-4 py-4 text-center font-medium text-stone-900 whitespace-nowrap">
                                                                ₱{parseFloat(product.price).toLocaleString('en-PH', { minimumFractionDigits: 2 })}
                                                            </td>
                                                            <td className="px-4 py-4 text-center">
                                                                <div className="flex flex-wrap gap-1 justify-center">
                                                                    {(product.moodTags || []).map(tag => {
                                                                        const mood = MOOD_OPTIONS.find(m => m.value === tag);
                                                                        return (
                                                                            <span key={tag} className="text-[10px] bg-stone-100 text-stone-600 px-2 py-0.5 rounded-sm border border-stone-200" title={mood?.label || tag}>
                                                                                {mood?.emoji || '🌿'} {mood?.label || tag}
                                                                            </span>
                                                                        );
                                                                    })}
                                                                </div>
                                                            </td>
                                                            <td className="px-4 py-4 text-center">
                                                                <button
                                                                    onClick={() => handleToggleStock(product)}
                                                                    className="inline-flex items-center gap-1.5 cursor-pointer group"
                                                                    title={product.inStock ? 'Click to mark Out of Stock' : 'Click to mark In Stock'}
                                                                >
                                                                    {product.inStock ? (
                                                                        <>
                                                                            <ToggleRight size={20} className="text-green-600 group-hover:text-green-800" />
                                                                            <span className="text-[10px] uppercase font-bold text-green-700 bg-green-100 px-2 py-0.5 rounded-sm">In Stock</span>
                                                                        </>
                                                                    ) : (
                                                                        <>
                                                                            <ToggleLeft size={20} className="text-stone-400 group-hover:text-stone-600" />
                                                                            <span className="text-[10px] uppercase font-bold text-red-700 bg-red-100 px-2 py-0.5 rounded-sm">Sold Out</span>
                                                                        </>
                                                                    )}
                                                                </button>
                                                            </td>
                                                            <td className="px-4 py-4 text-center">
                                                                <div className="flex items-center justify-center gap-3">
                                                                    <button
                                                                        onClick={() => openEditModal(product)}
                                                                        className="text-sm text-stone-600 hover:text-stone-900 transition-colors font-medium"
                                                                    >
                                                                        Edit
                                                                    </button>
                                                                    <button
                                                                        onClick={() => setDeleteConfirm(product)}
                                                                        className="text-sm text-red-400 hover:text-red-700 transition-colors"
                                                                    >
                                                                        <Trash2 size={15} />
                                                                    </button>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}

                        {/* ─── SHOP INFO TAB ─── */}
                        {artisanTab === 'shopInfo' && (
                            <div className="bg-white border border-stone-200 rounded-sm p-6 lg:p-8">
                                <div className="flex items-center justify-between mb-8 pb-4 border-b border-stone-100">
                                    <div>
                                        <h3 className="text-xl font-serif text-stone-900">Shop Information</h3>
                                        <p className="text-stone-500 text-sm mt-1">Manage your studio's public-facing profile and branding.</p>
                                    </div>
                                    <button
                                        onClick={() => {
                                            if (isEditingShop) {
                                                // Reset form on cancel
                                                setShopForm({
                                                    storeName: floristProfile?.storeName || '',
                                                    bio: floristProfile?.bio || '',
                                                    street: floristProfile?.street || '',
                                                    city: floristProfile?.city || '',
                                                    zipCode: floristProfile?.zipCode || '',
                                                });
                                            }
                                            setIsEditingShop(!isEditingShop);
                                        }}
                                        className="text-xs uppercase tracking-widest text-stone-500 hover:text-stone-900 flex items-center gap-2 transition-colors border border-stone-200 px-4 py-2 rounded-sm hover:bg-stone-50"
                                    >
                                        <Edit2 size={14} /> {isEditingShop ? 'Cancel' : 'Edit Profile'}
                                    </button>
                                </div>

                                <div className="flex flex-col md:flex-row gap-10">
                                    <div className="flex-1 space-y-6">
                                        <div className="space-y-2">
                                            <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Store Name</label>
                                            {isEditingShop ? (
                                                <input
                                                    type="text"
                                                    value={shopForm.storeName}
                                                    onChange={(e) => setShopForm({ ...shopForm, storeName: e.target.value })}
                                                    className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                                    placeholder="Your store name"
                                                />
                                            ) : (
                                                <p className="text-stone-800 text-lg font-medium">{floristProfile?.storeName || 'Not set'}</p>
                                            )}
                                        </div>

                                        <div className="space-y-2">
                                            <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Studio Bio</label>
                                            {isEditingShop ? (
                                                <textarea
                                                    rows={4}
                                                    value={shopForm.bio}
                                                    onChange={(e) => setShopForm({ ...shopForm, bio: e.target.value })}
                                                    className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                                    placeholder="Tell customers about your studio..."
                                                />
                                            ) : (
                                                <p className="text-stone-600 font-light leading-relaxed">{floristProfile?.bio || 'No bio set'}</p>
                                            )}
                                        </div>

                                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                            <div className="space-y-2">
                                                <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">Street</label>
                                                {isEditingShop ? (
                                                    <input
                                                        type="text"
                                                        value={shopForm.street}
                                                        onChange={(e) => setShopForm({ ...shopForm, street: e.target.value })}
                                                        className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-sm text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                                        placeholder="Street address"
                                                    />
                                                ) : (
                                                    <p className="text-stone-600 text-sm">{floristProfile?.street || '—'}</p>
                                                )}
                                            </div>
                                            <div className="space-y-2">
                                                <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">City</label>
                                                {isEditingShop ? (
                                                    <input
                                                        type="text"
                                                        value={shopForm.city}
                                                        onChange={(e) => setShopForm({ ...shopForm, city: e.target.value })}
                                                        className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-sm text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                                        placeholder="City"
                                                    />
                                                ) : (
                                                    <p className="text-stone-600 text-sm">{floristProfile?.city || '—'}</p>
                                                )}
                                            </div>
                                            <div className="space-y-2">
                                                <label className="text-xs uppercase tracking-wider text-stone-400 font-medium">ZIP Code</label>
                                                {isEditingShop ? (
                                                    <input
                                                        type="text"
                                                        value={shopForm.zipCode}
                                                        onChange={(e) => setShopForm({ ...shopForm, zipCode: e.target.value })}
                                                        className="w-full bg-stone-50 border border-stone-200 px-3 py-2 text-sm text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                                        placeholder="ZIP"
                                                    />
                                                ) : (
                                                    <p className="text-stone-600 text-sm">{floristProfile?.zipCode || '—'}</p>
                                                )}
                                            </div>
                                        </div>

                                        {isEditingShop && (
                                            <div className="pt-4 flex justify-end">
                                                <button
                                                    onClick={handleSaveShopInfo}
                                                    disabled={actionLoading}
                                                    className="bg-stone-900 text-white px-6 py-2.5 text-sm font-medium hover:bg-stone-800 transition-colors rounded-sm disabled:opacity-50 flex items-center gap-2"
                                                >
                                                    {actionLoading && <RefreshCw className="animate-spin" size={14} />}
                                                    Save Shop Information
                                                </button>
                                            </div>
                                        )}
                                    </div>

                                    {/* Avatar / Logo Section */}
                                    <div className="md:w-64 space-y-6 md:border-l md:border-stone-100 md:pl-10">
                                        <div className="space-y-4 text-center">
                                            <div className="w-32 h-32 mx-auto bg-stone-50 rounded-full flex items-center justify-center text-4xl font-serif italic text-stone-700 border-2 border-dashed border-stone-200 relative group overflow-hidden">
                                                <span>{initials}</span>
                                            </div>
                                            <div>
                                                <p className="text-sm font-medium text-stone-900">Studio Mark / Logo</p>
                                                <p className="text-xs text-stone-500 mt-1 px-4 leading-relaxed">JPEG, PNG. Recommended 500x500px.</p>
                                            </div>
                                        </div>

                                        {/* Owner Info */}
                                        <div className="border-t border-stone-100 pt-4 space-y-2">
                                            <p className="text-xs uppercase tracking-wider text-stone-400 font-medium">Owner</p>
                                            <p className="text-sm text-stone-800 font-medium">{floristProfile?.ownerName}</p>
                                            <p className="text-xs text-stone-500">{floristProfile?.ownerEmail}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                    </div>
                </div>
            </main>

            {/* ─── PRODUCT MODAL ─── */}
            {showProductModal && (
                <>
                    <div className="fixed inset-0 bg-black/40 z-50 backdrop-blur-sm" onClick={() => setShowProductModal(false)} />
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                        <div className="bg-white border border-stone-200 rounded-sm w-full max-w-lg max-h-[90vh] overflow-y-auto shadow-2xl animate-fadeIn" onClick={(e) => e.stopPropagation()}>
                            {/* Modal Header */}
                            <div className="flex items-center justify-between px-6 py-4 border-b border-stone-100">
                                <h3 className="text-lg font-serif text-stone-900">
                                    {editingProduct ? 'Edit Product' : 'Add New Product'}
                                </h3>
                                <button onClick={() => setShowProductModal(false)} className="text-stone-400 hover:text-stone-900 transition-colors">
                                    <X size={20} />
                                </button>
                            </div>

                            {/* Modal Body */}
                            <div className="px-6 py-5 space-y-5">
                                {/* Product Name */}
                                <div className="space-y-1.5">
                                    <label className="text-xs uppercase tracking-wider text-stone-500 font-medium">Product Name *</label>
                                    <input
                                        type="text"
                                        value={productForm.name}
                                        onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
                                        className={`w-full bg-stone-50 border ${formErrors.name ? 'border-red-300' : 'border-stone-200'} px-3 py-2.5 text-sm text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm`}
                                        placeholder="e.g., Romantic Rose Bouquet"
                                    />
                                    {formErrors.name && <p className="text-xs text-red-500">{formErrors.name}</p>}
                                </div>

                                {/* Description */}
                                <div className="space-y-1.5">
                                    <label className="text-xs uppercase tracking-wider text-stone-500 font-medium">Description</label>
                                    <textarea
                                        rows={3}
                                        value={productForm.description}
                                        onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
                                        className="w-full bg-stone-50 border border-stone-200 px-3 py-2.5 text-sm text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                        placeholder="Describe your floral arrangement..."
                                    />
                                </div>

                                {/* Price */}
                                <div className="space-y-1.5">
                                    <label className="text-xs uppercase tracking-wider text-stone-500 font-medium">Price (₱) *</label>
                                    <input
                                        type="number"
                                        min="0"
                                        step="0.01"
                                        value={productForm.price}
                                        onChange={(e) => setProductForm({ ...productForm, price: e.target.value })}
                                        className={`w-full bg-stone-50 border ${formErrors.price ? 'border-red-300' : 'border-stone-200'} px-3 py-2.5 text-sm text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm`}
                                        placeholder="0.00"
                                    />
                                    {formErrors.price && <p className="text-xs text-red-500">{formErrors.price}</p>}
                                </div>

                                {/* Image URL */}
                                <div className="space-y-1.5">
                                    <label className="text-xs uppercase tracking-wider text-stone-500 font-medium">Image URL</label>
                                    <input
                                        type="url"
                                        value={productForm.imageUrl}
                                        onChange={(e) => setProductForm({ ...productForm, imageUrl: e.target.value })}
                                        className="w-full bg-stone-50 border border-stone-200 px-3 py-2.5 text-sm text-stone-900 outline-none focus:border-stone-900 transition-colors rounded-sm"
                                        placeholder="https://example.com/image.jpg (optional)"
                                    />
                                    <p className="text-[10px] text-stone-400">Leave blank for a placeholder image</p>
                                </div>

                                {/* Mood Tags */}
                                <div className="space-y-2">
                                    <label className="text-xs uppercase tracking-wider text-stone-500 font-medium flex items-center gap-2">
                                        <Tag size={12} /> Mood Tags *
                                    </label>
                                    <div className="flex flex-wrap gap-2">
                                        {MOOD_OPTIONS.map((mood) => (
                                            <button
                                                key={mood.value}
                                                type="button"
                                                onClick={() => toggleMoodTag(mood.value)}
                                                className={`px-3 py-1.5 text-xs font-medium rounded-sm border transition-all ${
                                                    productForm.moodTags.includes(mood.value)
                                                        ? 'bg-stone-900 text-white border-stone-900'
                                                        : 'bg-white text-stone-600 border-stone-200 hover:bg-stone-50 hover:border-stone-300'
                                                }`}
                                            >
                                                {mood.emoji} {mood.label}
                                            </button>
                                        ))}
                                    </div>
                                    {formErrors.moodTags && <p className="text-xs text-red-500">{formErrors.moodTags}</p>}
                                </div>
                            </div>

                            {/* Modal Footer */}
                            <div className="px-6 py-4 border-t border-stone-100 flex justify-end gap-3">
                                <button
                                    onClick={() => setShowProductModal(false)}
                                    className="px-5 py-2.5 text-sm font-medium text-stone-600 border border-stone-200 rounded-sm hover:bg-stone-50 transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={handleSaveProduct}
                                    disabled={actionLoading}
                                    className="px-5 py-2.5 text-sm font-medium bg-stone-900 text-white rounded-sm hover:bg-stone-800 transition-colors disabled:opacity-50 flex items-center gap-2"
                                >
                                    {actionLoading && <RefreshCw className="animate-spin" size={14} />}
                                    {editingProduct ? 'Save Changes' : 'Create Product'}
                                </button>
                            </div>
                        </div>
                    </div>
                </>
            )}

            {/* ─── DELETE CONFIRMATION MODAL ─── */}
            {deleteConfirm && (
                <>
                    <div className="fixed inset-0 bg-black/40 z-50 backdrop-blur-sm" onClick={() => setDeleteConfirm(null)} />
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                        <div className="bg-white border border-stone-200 rounded-sm w-full max-w-sm shadow-2xl animate-fadeIn p-6" onClick={(e) => e.stopPropagation()}>
                            <div className="text-center mb-6">
                                <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                    <Trash2 size={20} className="text-red-600" />
                                </div>
                                <h4 className="text-lg font-serif text-stone-900 mb-2">Delete Product?</h4>
                                <p className="text-sm text-stone-500">
                                    Are you sure you want to delete <strong>"{deleteConfirm.name}"</strong>? This action cannot be undone.
                                </p>
                            </div>
                            <div className="flex gap-3">
                                <button
                                    onClick={() => setDeleteConfirm(null)}
                                    className="flex-1 px-4 py-2.5 text-sm font-medium text-stone-600 border border-stone-200 rounded-sm hover:bg-stone-50 transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={() => handleDeleteProduct(deleteConfirm.id)}
                                    className="flex-1 px-4 py-2.5 text-sm font-medium bg-red-600 text-white rounded-sm hover:bg-red-700 transition-colors"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
}
