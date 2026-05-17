import { useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
    AlertCircle,
    ArrowRight,
    BarChart3,
    Boxes,
    CheckCircle2,
    ChevronDown,
    ClipboardList,
    Edit3,
    Eye,
    Leaf,
    LogOut,
    Package,
    Plus,
    Search,
    Settings,
    Store,
    Truck,
    Wallet,
    X,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { floristAPI, ordersAPI, productsAPI } from '../services/api';

const moodOptions = ['romance', 'celebration', 'sympathy', 'apology', 'calm', 'gratitude', 'wildflower'];

const blankListing = {
    name: '',
    description: '',
    price: '',
    moodTags: ['celebration'],
    imageUrl: '/images/product_aurora_hydrangea_1771726583839.png',
    floristName: '',
    floristLogoUrl: '',
    inStock: true,
};

const orderRows = [
    {
        id: 'PET-24018',
        buyer: 'Mikaela Santos',
        product: 'The Aurora',
        delivery: 'May 18, AM',
        total: '₱2,940',
        status: 'To Prepare',
        tone: 'amber',
    },
    {
        id: 'PET-24019',
        buyer: 'Dane Villamor',
        product: 'Kanso Vase',
        delivery: 'May 18, PM',
        total: '₱3,300',
        status: 'Ready for Rider',
        tone: 'green',
    },
    {
        id: 'PET-24020',
        buyer: 'Celine Yu',
        product: 'Winter Wreath',
        delivery: 'May 19, AM',
        total: '₱3,900',
        status: 'New',
        tone: 'rose',
    },
];

const navGroups = [
    {
        label: 'Operations',
        items: [
            { id: 'overview', label: 'Overview', icon: BarChart3 },
            { id: 'orders', label: 'Orders', icon: ClipboardList },
        ],
    },
    {
        label: 'Catalog',
        items: [
            { id: 'products', label: 'Products', icon: Boxes },
            { id: 'newProduct', label: 'Add Product', icon: Plus },
        ],
    },
    {
        label: 'Studio',
        items: [
            { id: 'shopInfo', label: 'Shop Profile', icon: Store },
            { id: 'settings', label: 'Settings', icon: Settings },
        ],
    },
];

function currency(value) {
    const number = Number(value || 0);
    return `₱${number.toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function statusClass(status) {
    if (status === 'COMPLETED' || status === 'READY_FOR_PICKUP') return 'bg-green-50 text-green-800 border-green-200';
    if (status === 'CANCELLED') return 'bg-stone-100 text-stone-500 border-stone-200';
    if (status === 'PREPARING') return 'bg-amber-50 text-amber-800 border-amber-200';
    return 'bg-rose-50 text-rose-800 border-rose-200';
}

function statusLabel(status) {
    const labels = {
        PENDING: 'New',
        PREPARING: 'Preparing',
        READY_FOR_PICKUP: 'Ready for Pickup',
        COMPLETED: 'Completed',
        CANCELLED: 'Cancelled',
    };
    return labels[status] || status;
}

function nextStatus(status) {
    if (status === 'PENDING') return 'PREPARING';
    if (status === 'PREPARING') return 'READY_FOR_PICKUP';
    if (status === 'READY_FOR_PICKUP') return 'COMPLETED';
    return null;
}

function nextStatusLabel(status) {
    const next = nextStatus(status);
    if (!next) return '';
    if (next === 'PREPARING') return 'Start Preparing';
    if (next === 'READY_FOR_PICKUP') return 'Mark Ready';
    return 'Complete';
}

function deliveryLabel(order) {
    if (!order.deliveryDate) return order.timeSlot || 'No delivery window';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'short', day: 'numeric' })}, ${order.timeSlot}`;
}

function apiErrorMessage(err, fallback) {
    const responseMessage = err.response?.data?.message;
    return responseMessage || err.message || fallback;
}

export default function SellerCentre() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const initialTab = new URLSearchParams(location.search).get('tab') || 'overview';

    const [activeTab, setActiveTab] = useState(initialTab);
    const [products, setProducts] = useState([]);
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');
    const [formData, setFormData] = useState(blankListing);
    const [editingProduct, setEditingProduct] = useState(null);
    const [saving, setSaving] = useState(false);
    const [notice, setNotice] = useState('');
    const [isEditingShop, setIsEditingShop] = useState(false);
    const [shopName, setShopName] = useState(user?.name ? `${user.name}'s Studio` : 'My Floral Studio');
    const [shopBio, setShopBio] = useState('Locally composed preserved floral pieces for thoughtful Cebu gifting, prepared with careful wrapping and delivery-ready notes.');
    const [shopCity, setShopCity] = useState('Cebu, Philippines');
    const [dailyCapacity, setDailyCapacity] = useState(12);
    const [updatingOrderId, setUpdatingOrderId] = useState(null);

    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';

    useEffect(() => {
        if (user && !isArtisan) {
            navigate('/dashboard');
        }
    }, [isArtisan, navigate, user]);

    useEffect(() => {
        const loadSellerData = async () => {
            setLoading(true);
            setError('');
            try {
                const [productsResponse, floristResponse, ordersResponse] = await Promise.all([
                    productsAPI.getSellerProducts(),
                    floristAPI.getProfile(),
                    ordersAPI.getSellerOrders(),
                ]);
                const florist = floristResponse.data.data;
                setProducts(productsResponse.data.data || []);
                setOrders(ordersResponse.data.data || []);
                setShopName(florist.storeName || (user?.name ? `${user.name}'s Studio` : 'My Floral Studio'));
                setShopBio(florist.bio || 'Locally composed preserved floral pieces for thoughtful Cebu gifting, prepared with careful wrapping and delivery-ready notes.');
                setShopCity(florist.city || 'Cebu, Philippines');
                setDailyCapacity(florist.maxDailyCapacity || 12);
            } catch (err) {
                setError(apiErrorMessage(err, 'Unable to load seller data'));
            } finally {
                setLoading(false);
            }
        };

        if (isArtisan) {
            loadSellerData();
        }
    }, [isArtisan, user?.name]);

    const filteredProducts = useMemo(() => {
        const normalized = search.trim().toLowerCase();
        if (!normalized) return products;

        return products.filter((product) =>
            product.name?.toLowerCase().includes(normalized)
            || product.description?.toLowerCase().includes(normalized)
            || product.moodTags?.some((tag) => tag.toLowerCase().includes(normalized))
        );
    }, [products, search]);

    const metrics = useMemo(() => {
        const liveCount = products.filter((product) => product.inStock).length;
        const soldOutCount = products.length - liveCount;
        const inventoryValue = products.reduce((sum, product) => sum + Number(product.price || 0), 0);
        const actionableOrders = orders.filter((order) => !['COMPLETED', 'CANCELLED'].includes(order.status));
        const today = new Date().toISOString().slice(0, 10);
        const dueToday = actionableOrders.filter((order) => order.deliveryDate === today).length;

        return [
            { label: 'Live Listings', value: liveCount, helper: `${soldOutCount} paused or sold out`, icon: Package },
            { label: 'Orders to Prepare', value: actionableOrders.length, helper: `${dueToday} deliveries due today`, icon: Truck },
            { label: 'Catalog Value', value: currency(inventoryValue), helper: 'Current listed assortment', icon: Wallet },
        ];
    }, [orders, products]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const resetForm = () => {
        setFormData({ ...blankListing, floristName: shopName });
        setEditingProduct(null);
    };

    const editProduct = (product) => {
        setEditingProduct(product);
        setFormData({
            name: product.name || '',
            description: product.description || '',
            price: product.price || '',
            moodTags: product.moodTags?.length ? product.moodTags : ['celebration'],
            imageUrl: product.imageUrl || blankListing.imageUrl,
            floristName: product.floristName || shopName,
            floristLogoUrl: product.floristLogoUrl || '',
            inStock: product.inStock,
        });
        setActiveTab('newProduct');
    };

    const toggleMood = (mood) => {
        setFormData((current) => {
            const exists = current.moodTags.includes(mood);
            const nextTags = exists
                ? current.moodTags.filter((tag) => tag !== mood)
                : [...current.moodTags, mood];

            return {
                ...current,
                moodTags: nextTags.length ? nextTags : [mood],
            };
        });
    };

    const saveProduct = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError('');
        setNotice('');

        const payload = {
            ...formData,
            price: Number(formData.price),
            floristName: formData.floristName || shopName,
        };

        try {
            const response = editingProduct
                ? await productsAPI.updateProduct(editingProduct.id, payload)
                : await productsAPI.createProduct(payload);
            const savedProduct = response.data.data;

            setProducts((current) => {
                if (editingProduct) {
                    return current.map((product) => product.id === savedProduct.id ? savedProduct : product);
                }
                return [savedProduct, ...current];
            });
            setNotice(editingProduct ? 'Product updated.' : 'Product added to your catalog.');
            resetForm();
            setActiveTab('products');
        } catch (err) {
            setError(apiErrorMessage(err, 'Unable to save product'));
        } finally {
            setSaving(false);
        }
    };

    const toggleStock = async (product) => {
        const payload = {
            name: product.name,
            description: product.description,
            price: Number(product.price),
            moodTags: product.moodTags,
            imageUrl: product.imageUrl,
            floristName: product.floristName,
            floristLogoUrl: product.floristLogoUrl,
            inStock: !product.inStock,
        };

        try {
            const response = await productsAPI.updateProduct(product.id, payload);
            const updatedProduct = response.data.data;
            setProducts((current) => current.map((item) => item.id === product.id ? updatedProduct : item));
            setNotice(updatedProduct.inStock ? 'Product is live again.' : 'Product was paused.');
        } catch (err) {
            setError(apiErrorMessage(err, 'Unable to update stock status'));
        }
    };

    const deleteProduct = async (product) => {
        try {
            await productsAPI.deleteProduct(product.id);
            setProducts((current) => current.filter((item) => item.id !== product.id));
            setNotice('Product removed from your catalog.');
        } catch (err) {
            setError(apiErrorMessage(err, 'Unable to delete product'));
        }
    };

    const saveShopProfile = async () => {
        setSaving(true);
        setError('');
        setNotice('');
        try {
            const response = await floristAPI.updateProfile({
                storeName: shopName,
                bio: shopBio,
                city: shopCity,
                maxDailyCapacity: Number(dailyCapacity),
            });
            const florist = response.data.data;
            setShopName(florist.storeName || shopName);
            setShopBio(florist.bio || shopBio);
            setShopCity(florist.city || shopCity);
            setDailyCapacity(florist.maxDailyCapacity || dailyCapacity);
            setIsEditingShop(false);
            setNotice('Shop profile saved.');
        } catch (err) {
            setError(apiErrorMessage(err, 'Unable to save shop profile'));
        } finally {
            setSaving(false);
        }
    };

    const updateOrderStatus = async (order, status) => {
        setUpdatingOrderId(order.id);
        setError('');
        setNotice('');
        try {
            const response = await ordersAPI.updateSellerOrderStatus(order.id, status);
            const updatedOrder = response.data.data;
            setOrders((current) => current.map((item) => item.id === updatedOrder.id ? updatedOrder : item));
            setNotice(`Order ${updatedOrder.orderNumber} moved to ${statusLabel(updatedOrder.status)}.`);
        } catch (err) {
            setError(apiErrorMessage(err, 'Unable to update order status'));
        } finally {
            setUpdatingOrderId(null);
        }
    };

    if (!user || !isArtisan) return null;

    return (
        <div className="min-h-screen bg-[#F7F3EC] text-stone-900 selection:bg-stone-200 selection:text-stone-900">
            <nav className="fixed top-0 w-full z-40 bg-[#FDFCF8]/95 backdrop-blur-xl border-b border-stone-200">
                <div className="max-w-7xl mx-auto h-16 px-4 sm:px-6 flex items-center justify-between">
                    <div className="flex items-center gap-4 min-w-0">
                        <Link to="/dashboard" className="text-xl font-serif tracking-tight text-stone-900">Petal</Link>
                        <span className="hidden sm:inline text-stone-300">|</span>
                        <span className="hidden sm:inline text-sm font-medium text-stone-600">Seller Centre</span>
                    </div>
                    <div className="flex items-center gap-2 sm:gap-4">
                        <Link to="/seller-education" className="hidden sm:inline-flex text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Seller Education
                        </Link>
                        <Link to="/dashboard" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Storefront
                        </Link>
                        <button onClick={handleLogout} className="h-10 w-10 inline-flex items-center justify-center text-stone-500 hover:text-stone-900 hover:bg-white border border-transparent hover:border-stone-200 transition-colors" aria-label="Sign out">
                            <LogOut size={18} strokeWidth={1.7} />
                        </button>
                    </div>
                </div>
            </nav>

            <main className="pt-24 pb-16 max-w-7xl mx-auto px-4 sm:px-6">
                <section className="mb-6 grid grid-cols-1 xl:grid-cols-[1.5fr_0.8fr] gap-4">
                    <div className="bg-stone-950 text-white p-6 sm:p-8 min-h-[220px] flex flex-col justify-between overflow-hidden relative">
                        <div className="absolute inset-y-0 right-0 w-1/2 bg-[radial-gradient(circle_at_center,rgba(236,180,150,0.22),transparent_62%)] pointer-events-none" />
                        <div className="relative">
                            <p className="text-xs uppercase tracking-[0.24em] text-stone-400 mb-4">Florist Operations</p>
                            <h1 className="font-serif text-4xl sm:text-5xl leading-tight max-w-2xl">
                                Manage listings, orders, and your Cebu studio in one place.
                            </h1>
                        </div>
                        <div className="relative mt-8 flex flex-wrap gap-3">
                            <button onClick={() => { resetForm(); setActiveTab('newProduct'); }} className="h-11 px-5 bg-white text-stone-950 text-sm font-semibold inline-flex items-center gap-2 hover:bg-stone-100 active:scale-[0.98] transition">
                                <Plus size={16} /> Add Product
                            </button>
                            <button onClick={() => setActiveTab('orders')} className="h-11 px-5 border border-white/20 text-white text-sm font-semibold inline-flex items-center gap-2 hover:bg-white/10 active:scale-[0.98] transition">
                                <Truck size={16} /> Prepare Orders
                            </button>
                        </div>
                    </div>

                    <div className="bg-white border border-stone-200 p-6">
                        <div className="flex items-start justify-between">
                            <div>
                                <p className="text-xs uppercase tracking-[0.22em] text-stone-400">Studio Health</p>
                                <h2 className="mt-3 text-2xl font-serif text-stone-900">{shopName}</h2>
                            </div>
                            <div className="h-12 w-12 rounded-full bg-stone-100 border border-stone-200 flex items-center justify-center">
                                <Leaf size={20} strokeWidth={1.6} />
                            </div>
                        </div>
                        <div className="mt-8 space-y-4">
                            <div>
                                <div className="flex justify-between text-sm mb-2">
                                    <span className="text-stone-500">Profile readiness</span>
                                    <span className="font-medium">82%</span>
                                </div>
                                <div className="h-2 bg-stone-100 overflow-hidden">
                                    <div className="h-full w-[82%] bg-stone-900" />
                                </div>
                            </div>
                            <p className="text-sm text-stone-500 leading-relaxed">
                                Add more product photos and delivery rules next to raise customer trust before checkout.
                            </p>
                        </div>
                    </div>
                </section>

                {(error || notice) && (
                    <div className={`mb-4 border px-4 py-3 text-sm flex items-center gap-2 ${error ? 'bg-red-50 border-red-200 text-red-700' : 'bg-green-50 border-green-200 text-green-800'}`}>
                        {error ? <AlertCircle size={16} /> : <CheckCircle2 size={16} />}
                        {error || notice}
                    </div>
                )}

                <div className="grid grid-cols-1 lg:grid-cols-[260px_1fr] gap-5">
                    <aside className="lg:sticky lg:top-24 self-start bg-white border border-stone-200 p-3">
                        {navGroups.map((group) => (
                            <div key={group.label} className="mb-5 last:mb-0">
                                <p className="px-3 mb-2 text-[10px] font-semibold uppercase tracking-[0.2em] text-stone-400">{group.label}</p>
                                <div className="space-y-1">
                                    {group.items.map((item) => {
                                        const Icon = item.icon;
                                        return (
                                            <button
                                                key={item.id}
                                                onClick={() => {
                                                    if (item.id === 'newProduct') resetForm();
                                                    setActiveTab(item.id);
                                                }}
                                                className={`w-full h-11 px-3 flex items-center gap-3 text-sm text-left transition-colors ${activeTab === item.id ? 'bg-stone-900 text-white' : 'text-stone-600 hover:bg-stone-50 hover:text-stone-950'}`}
                                            >
                                                <Icon size={17} strokeWidth={1.7} />
                                                {item.label}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        ))}
                    </aside>

                    <section className="min-w-0">
                        {activeTab === 'overview' && (
                            <div className="space-y-5">
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    {metrics.map((metric) => {
                                        const Icon = metric.icon;
                                        return (
                                            <article key={metric.label} className="bg-white border border-stone-200 p-5">
                                                <div className="flex items-center justify-between mb-5">
                                                    <p className="text-sm text-stone-500">{metric.label}</p>
                                                    <Icon size={18} strokeWidth={1.7} className="text-stone-400" />
                                                </div>
                                                <p className="text-3xl font-semibold tracking-tight text-stone-950">{metric.value}</p>
                                                <p className="mt-2 text-xs text-stone-500">{metric.helper}</p>
                                            </article>
                                        );
                                    })}
                                </div>

                                <div className="grid grid-cols-1 xl:grid-cols-[1.1fr_0.9fr] gap-5">
                                    <div className="bg-white border border-stone-200">
                                        <div className="p-5 border-b border-stone-100 flex items-center justify-between">
                                            <div>
                                                <h2 className="font-serif text-2xl">Order Work Queue</h2>
                                                <p className="text-sm text-stone-500 mt-1">Shopee-style actions for today's florist tasks.</p>
                                            </div>
                                            <button onClick={() => setActiveTab('orders')} className="text-sm font-medium inline-flex items-center gap-1 text-stone-700 hover:text-stone-950">
                                                View all <ArrowRight size={14} />
                                            </button>
                                        </div>
                                        <div className="divide-y divide-stone-100">
                                            {orders.slice(0, 4).map((order) => (
                                                <button key={order.id} onClick={() => setActiveTab('orders')} className="w-full p-5 text-left hover:bg-stone-50 transition-colors grid grid-cols-1 md:grid-cols-[1fr_auto] gap-3">
                                                    <div>
                                                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">{order.orderNumber}</p>
                                                        <p className="mt-2 font-medium text-stone-950">{order.itemSummary}</p>
                                                        <p className="text-sm text-stone-500">{order.buyerName} · {deliveryLabel(order)}</p>
                                                    </div>
                                                    <div className="md:text-right">
                                                        <span className={`inline-flex border px-2.5 py-1 text-xs font-medium ${statusClass(order.status)}`}>{statusLabel(order.status)}</span>
                                                        <p className="mt-2 font-semibold">{currency(order.sellerSubtotal)}</p>
                                                    </div>
                                                </button>
                                            ))}
                                            {!orders.length && (
                                                <div className="p-5 text-sm text-stone-500">
                                                    No customer orders yet. New checkout orders will appear here for fulfillment.
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    <div className="bg-white border border-stone-200 p-5">
                                        <div className="flex items-center justify-between mb-5">
                                            <div>
                                                <h2 className="font-serif text-2xl">Catalog Signals</h2>
                                                <p className="text-sm text-stone-500 mt-1">What needs attention before customers buy.</p>
                                            </div>
                                            <Eye size={18} className="text-stone-400" />
                                        </div>
                                        <div className="space-y-4">
                                            <div className="p-4 border border-amber-200 bg-amber-50">
                                                <p className="text-sm font-medium text-amber-900">Photo depth is low</p>
                                                <p className="text-sm text-amber-800/80 mt-1">Add 3 to 5 photos per listing so product detail pages feel more like a marketplace.</p>
                                            </div>
                                            <div className="p-4 border border-stone-200">
                                                <p className="text-sm font-medium text-stone-950">Mood coverage</p>
                                        <p className="text-sm text-stone-500 mt-1">{products.length ? `${products.length} products across ${new Set(products.flatMap((product) => product.moodTags || [])).size} moods.` : 'Add your first product to appear in mood shopping.'}</p>
                                            </div>
                                            <button onClick={() => setActiveTab('newProduct')} className="w-full h-11 bg-stone-900 text-white text-sm font-semibold hover:bg-stone-800 active:scale-[0.99] transition">
                                                Create a new listing
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {activeTab === 'orders' && (
                            <div className="bg-white border border-stone-200">
                                <div className="p-5 border-b border-stone-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
                                    <div>
                                        <h2 className="font-serif text-2xl">Orders</h2>
                                        <p className="text-sm text-stone-500 mt-1">Review buyer messages, delivery windows, and preparation status.</p>
                                    </div>
                                    <button className="h-10 px-4 bg-stone-900 text-white text-sm font-semibold inline-flex items-center gap-2">
                                        <Truck size={16} /> Arrange Pickup
                                    </button>
                                </div>
                                <div className="overflow-x-auto">
                                    <table className="w-full text-sm">
                                        <thead className="bg-stone-50 text-left text-xs uppercase tracking-[0.14em] text-stone-400">
                                            <tr>
                                                <th className="px-5 py-3 font-medium">Order</th>
                                                <th className="px-5 py-3 font-medium">Buyer</th>
                                                <th className="px-5 py-3 font-medium">Delivery</th>
                                                <th className="px-5 py-3 font-medium">Total</th>
                                                <th className="px-5 py-3 font-medium">Status</th>
                                                <th className="px-5 py-3 font-medium text-right">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y divide-stone-100">
                                            {orders.map((order) => (
                                                <tr key={order.id} className="hover:bg-stone-50">
                                                    <td className="px-5 py-4 font-medium text-stone-950">{order.orderNumber}<p className="text-xs font-normal text-stone-500 mt-1">{order.itemSummary}</p></td>
                                                    <td className="px-5 py-4 text-stone-600">{order.buyerName}<p className="text-xs text-stone-400 mt-1">For {order.recipientName}</p></td>
                                                    <td className="px-5 py-4 text-stone-600">{deliveryLabel(order)}</td>
                                                    <td className="px-5 py-4 font-semibold">{currency(order.sellerSubtotal)}</td>
                                                    <td className="px-5 py-4"><span className={`inline-flex border px-2.5 py-1 text-xs font-medium ${statusClass(order.status)}`}>{statusLabel(order.status)}</span></td>
                                                    <td className="px-5 py-4 text-right">
                                                        {nextStatus(order.status) ? (
                                                            <button
                                                                onClick={() => updateOrderStatus(order, nextStatus(order.status))}
                                                                disabled={updatingOrderId === order.id}
                                                                className="text-sm font-medium text-stone-700 hover:text-stone-950 disabled:text-stone-300"
                                                            >
                                                                {updatingOrderId === order.id ? 'Updating...' : nextStatusLabel(order.status)}
                                                            </button>
                                                        ) : (
                                                            <span className="text-xs text-stone-400">No action</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            ))}
                                            {!orders.length && (
                                                <tr>
                                                    <td colSpan="6" className="px-5 py-12 text-center text-stone-500">
                                                        No seller orders yet. Customer checkout orders will land here once they include your products.
                                                    </td>
                                                </tr>
                                            )}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        )}

                        {activeTab === 'products' && (
                            <div className="bg-white border border-stone-200">
                                <div className="p-5 border-b border-stone-100 flex flex-col xl:flex-row xl:items-center justify-between gap-4">
                                    <div>
                                        <h2 className="font-serif text-2xl">Products</h2>
                                        <p className="text-sm text-stone-500 mt-1">Create, pause, and tune listings that appear across Browse and Shop by Mood.</p>
                                    </div>
                                    <div className="flex flex-col sm:flex-row gap-3">
                                        <div className="relative">
                                            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-400" />
                                            <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search listings" className="h-10 w-full sm:w-64 pl-9 pr-3 border border-stone-200 bg-stone-50 text-sm outline-none focus:border-stone-400" />
                                        </div>
                                        <button onClick={() => { resetForm(); setActiveTab('newProduct'); }} className="h-10 px-4 bg-stone-900 text-white text-sm font-semibold inline-flex items-center justify-center gap-2">
                                            <Plus size={16} /> Add Product
                                        </button>
                                    </div>
                                </div>

                                {loading ? (
                                    <div className="p-5 space-y-3">
                                        {[1, 2, 3].map((item) => <div key={item} className="h-20 bg-stone-100 animate-pulse" />)}
                                    </div>
                                ) : (
                                    <div className="overflow-x-auto">
                                        <table className="w-full text-sm">
                                            <thead className="bg-stone-50 text-left text-xs uppercase tracking-[0.14em] text-stone-400">
                                                <tr>
                                                    <th className="px-5 py-3 font-medium">Product</th>
                                                    <th className="px-5 py-3 font-medium">Moods</th>
                                                    <th className="px-5 py-3 font-medium">Price</th>
                                                    <th className="px-5 py-3 font-medium">Status</th>
                                                    <th className="px-5 py-3 font-medium text-right">Actions</th>
                                                </tr>
                                            </thead>
                                            <tbody className="divide-y divide-stone-100">
                                                {filteredProducts.map((product) => (
                                                    <tr key={product.id} className="hover:bg-stone-50 align-top">
                                                        <td className="px-5 py-4 min-w-[280px]">
                                                            <div className="flex gap-3">
                                                                <img src={product.imageUrl} alt={product.name} className="w-14 h-16 object-cover bg-stone-100 border border-stone-200" />
                                                                <div>
                                                                    <p className="font-medium text-stone-950">{product.name}</p>
                                                                    <p className="text-xs text-stone-500 mt-1 line-clamp-2 max-w-sm">{product.description}</p>
                                                                    <p className="text-[11px] text-stone-400 mt-2">SKU PET-{String(product.id).padStart(4, '0')}</p>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td className="px-5 py-4 min-w-[180px]">
                                                            <div className="flex flex-wrap gap-1.5">
                                                                {(product.moodTags || []).map((tag) => <span key={tag} className="border border-stone-200 bg-white px-2 py-1 text-[11px] text-stone-600 capitalize">{tag}</span>)}
                                                            </div>
                                                        </td>
                                                        <td className="px-5 py-4 font-semibold whitespace-nowrap">{currency(product.price)}</td>
                                                        <td className="px-5 py-4">
                                                            <span className={`inline-flex border px-2.5 py-1 text-xs font-medium ${product.inStock ? 'bg-green-50 text-green-800 border-green-200' : 'bg-stone-100 text-stone-500 border-stone-200'}`}>
                                                                {product.inStock ? 'Live' : 'Paused'}
                                                            </span>
                                                        </td>
                                                        <td className="px-5 py-4 text-right">
                                                            <div className="flex items-center justify-end gap-3 whitespace-nowrap">
                                                                <button onClick={() => editProduct(product)} className="text-sm font-medium text-stone-700 hover:text-stone-950">Edit</button>
                                                                <button onClick={() => toggleStock(product)} className="text-sm text-stone-500 hover:text-stone-950">{product.inStock ? 'Pause' : 'Publish'}</button>
                                                                <button onClick={() => deleteProduct(product)} className="text-sm text-red-600 hover:text-red-800">Delete</button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                ))}
                                                {!filteredProducts.length && (
                                                    <tr>
                                                        <td colSpan="5" className="px-5 py-12 text-center text-stone-500">
                                                            No products found. Add a listing to start selling.
                                                        </td>
                                                    </tr>
                                                )}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </div>
                        )}

                        {activeTab === 'newProduct' && (
                            <form onSubmit={saveProduct} className="bg-white border border-stone-200">
                                <div className="p-5 border-b border-stone-100 flex items-center justify-between">
                                    <div>
                                        <h2 className="font-serif text-2xl">{editingProduct ? 'Edit Product' : 'Add Product'}</h2>
                                        <p className="text-sm text-stone-500 mt-1">Listings are immediately available in customer browsing once published.</p>
                                    </div>
                                    {editingProduct && (
                                        <button type="button" onClick={resetForm} className="h-10 px-3 border border-stone-200 text-sm inline-flex items-center gap-2 hover:bg-stone-50">
                                            <X size={15} /> Clear
                                        </button>
                                    )}
                                </div>

                                <div className="p-5 grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_280px] gap-6">
                                    <div className="space-y-5">
                                        <label className="block">
                                            <span className="text-sm font-medium text-stone-700">Product name</span>
                                            <input required value={formData.name} onChange={(event) => setFormData({ ...formData, name: event.target.value })} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                        </label>

                                        <label className="block">
                                            <span className="text-sm font-medium text-stone-700">Description</span>
                                            <textarea required rows={5} value={formData.description} onChange={(event) => setFormData({ ...formData, description: event.target.value })} className="mt-2 w-full border border-stone-200 bg-stone-50 p-3 outline-none focus:border-stone-500" />
                                        </label>

                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                            <label className="block">
                                                <span className="text-sm font-medium text-stone-700">Price</span>
                                                <input required type="number" min="1" step="0.01" value={formData.price} onChange={(event) => setFormData({ ...formData, price: event.target.value })} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                            </label>
                                            <label className="block">
                                                <span className="text-sm font-medium text-stone-700">Image URL</span>
                                                <input required value={formData.imageUrl} onChange={(event) => setFormData({ ...formData, imageUrl: event.target.value })} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 outline-none focus:border-stone-500" />
                                            </label>
                                        </div>

                                        <div>
                                            <p className="text-sm font-medium text-stone-700 mb-2">Mood tags</p>
                                            <div className="flex flex-wrap gap-2">
                                                {moodOptions.map((mood) => (
                                                    <button key={mood} type="button" onClick={() => toggleMood(mood)} className={`h-9 px-3 border text-sm capitalize transition-colors ${formData.moodTags.includes(mood) ? 'bg-stone-900 text-white border-stone-900' : 'bg-white text-stone-600 border-stone-200 hover:border-stone-400'}`}>
                                                        {mood}
                                                    </button>
                                                ))}
                                            </div>
                                        </div>
                                    </div>

                                    <aside className="space-y-4">
                                        <div className="border border-stone-200 p-4 max-w-[280px] xl:max-w-none">
                                            <p className="text-sm font-medium text-stone-700 mb-3">Marketplace Preview</p>
                                            <div className="w-full max-h-48 overflow-hidden bg-stone-100 border border-stone-100">
                                                <img src={formData.imageUrl || blankListing.imageUrl} alt="" className="w-full h-48 object-cover" />
                                            </div>
                                            <p className="mt-4 text-xs uppercase tracking-[0.18em] text-stone-400">{formData.floristName || shopName}</p>
                                            <p className="mt-1 font-serif text-2xl">{formData.name || 'Product name'}</p>
                                            <p className="mt-2 text-sm text-stone-500 line-clamp-3">{formData.description || 'Product description appears here.'}</p>
                                            <p className="mt-3 font-semibold">{formData.price ? currency(formData.price) : '₱0.00'}</p>
                                        </div>

                                        <label className="flex items-center justify-between gap-4 border border-stone-200 p-4">
                                            <span>
                                                <span className="block text-sm font-medium text-stone-800">Publish listing</span>
                                                <span className="block text-xs text-stone-500 mt-1">Turn off to keep this product paused.</span>
                                            </span>
                                            <input type="checkbox" checked={formData.inStock} onChange={(event) => setFormData({ ...formData, inStock: event.target.checked })} className="h-5 w-5 accent-stone-900" />
                                        </label>

                                        <button disabled={saving} className="w-full h-12 bg-stone-900 text-white text-sm font-semibold hover:bg-stone-800 disabled:opacity-60 active:scale-[0.99] transition">
                                            {saving ? 'Saving...' : editingProduct ? 'Save Changes' : 'Publish Product'}
                                        </button>
                                    </aside>
                                </div>
                            </form>
                        )}

                        {activeTab === 'shopInfo' && (
                            <div className="bg-white border border-stone-200 p-5 sm:p-6">
                                <div className="flex items-center justify-between pb-5 border-b border-stone-100">
                                    <div>
                                        <h2 className="font-serif text-2xl">Shop Profile</h2>
                                        <p className="text-sm text-stone-500 mt-1">The public trust layer beside each product.</p>
                                    </div>
                                    <button onClick={() => setIsEditingShop(!isEditingShop)} className="h-10 px-4 border border-stone-200 text-sm font-medium inline-flex items-center gap-2 hover:bg-stone-50">
                                        <Edit3 size={15} /> {isEditingShop ? 'Cancel' : 'Edit'}
                                    </button>
                                </div>

                                <div className="mt-6 grid grid-cols-1 lg:grid-cols-[1fr_280px] gap-8">
                                    <div className="space-y-5">
                                        <label className="block">
                                            <span className="text-sm font-medium text-stone-700">Shop name</span>
                                            <input disabled={!isEditingShop} value={shopName} onChange={(event) => setShopName(event.target.value)} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 outline-none disabled:text-stone-600 focus:border-stone-500" />
                                        </label>
                                        <label className="block">
                                            <span className="text-sm font-medium text-stone-700">Studio bio</span>
                                            <textarea disabled={!isEditingShop} rows={5} value={shopBio} onChange={(event) => setShopBio(event.target.value)} className="mt-2 w-full border border-stone-200 bg-stone-50 p-3 outline-none disabled:text-stone-600 focus:border-stone-500" />
                                        </label>
                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                            <label className="block">
                                                <span className="text-sm font-medium text-stone-700">Location</span>
                                                <input disabled={!isEditingShop} value={shopCity} onChange={(event) => setShopCity(event.target.value)} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 outline-none disabled:text-stone-600 focus:border-stone-500" />
                                            </label>
                                            <label className="block">
                                                <span className="text-sm font-medium text-stone-700">Daily capacity</span>
                                                <input disabled={!isEditingShop} type="number" min="1" value={dailyCapacity} onChange={(event) => setDailyCapacity(event.target.value)} className="mt-2 h-11 w-full border border-stone-200 bg-stone-50 px-3 outline-none disabled:text-stone-600 focus:border-stone-500" />
                                            </label>
                                        </div>
                                        {isEditingShop && (
                                            <button onClick={saveShopProfile} disabled={saving} className="h-11 px-5 bg-stone-900 text-white text-sm font-semibold disabled:opacity-60">
                                                {saving ? 'Saving...' : 'Save Shop Information'}
                                            </button>
                                        )}
                                    </div>
                                    <div className="border border-stone-200 p-5">
                                        <div className="h-32 w-32 rounded-full mx-auto bg-stone-100 border border-stone-200 flex items-center justify-center">
                                            <Store size={34} strokeWidth={1.4} className="text-stone-500" />
                                        </div>
                                        <p className="mt-5 text-center font-medium">{shopName}</p>
                                        <p className="mt-2 text-center text-sm text-stone-500">{shopCity}</p>
                                        <p className="mt-1 text-center text-xs text-stone-400">{dailyCapacity} orders per day</p>
                                    </div>
                                </div>
                            </div>
                        )}

                        {activeTab === 'settings' && (
                            <div className="bg-white border border-stone-200 p-8 text-center">
                                <Settings size={28} className="mx-auto text-stone-400" />
                                <h2 className="mt-4 font-serif text-2xl">Seller Settings</h2>
                                <p className="mt-2 text-sm text-stone-500">Delivery rules, payout details, and shop policies can plug in here next.</p>
                            </div>
                        )}
                    </section>
                </div>
            </main>
        </div>
    );
}
