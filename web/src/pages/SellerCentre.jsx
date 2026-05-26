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
    Upload,
    Wallet,
    X,
    MessageSquare,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Grainient from '../components/Grainient';
import { floristAPI, mediaUrl, ordersAPI, productsAPI, reviewsAPI, sellerConversationsAPI } from '../services/api';
import { Star } from 'lucide-react';

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

const orderWorkflowColumns = [
    { status: 'PENDING', title: 'New', helper: 'Awaiting florist confirmation', accent: 'border-rose-200 bg-rose-50/70' },
    { status: 'ACCEPTED', title: 'Accepted', helper: 'Confirmed by the studio', accent: 'border-amber-200 bg-amber-50/70' },
    { status: 'ARRANGING', title: 'Arranging', helper: 'Bouquet in progress', accent: 'border-amber-200 bg-amber-50/70' },
    { status: 'READY_FOR_PICKUP', title: 'Ready', helper: 'Prepared for rider pickup', accent: 'border-green-200 bg-green-50/70' },
    { status: 'OUT_FOR_DELIVERY', title: 'Out for Delivery', helper: 'With courier', accent: 'border-stone-300 bg-stone-100/80' },
    { status: 'DELIVERED', title: 'Delivered', helper: 'Completed gifts', accent: 'border-green-200 bg-green-50/70' },
    { status: 'CANCELLED', title: 'Cancelled', helper: 'Closed without delivery', accent: 'border-stone-200 bg-stone-50' },
];

const navGroups = [
    {
        label: 'Operations',
        items: [
            { id: 'overview', label: 'Overview', icon: BarChart3 },
            { id: 'orders', label: 'Orders', icon: ClipboardList },
            { id: 'reviews', label: 'Customer Notes', icon: Star },
            { id: 'messages', label: 'Messages', icon: MessageSquare },
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

function normalizeSellerStatus(status) {
    const normalized = (status || 'PENDING').trim().toUpperCase().replaceAll(' ', '_');
    if (normalized === 'PREPARING') return 'ARRANGING';
    if (normalized === 'SHIPPED') return 'OUT_FOR_DELIVERY';
    if (normalized === 'COMPLETED') return 'DELIVERED';
    return normalized;
}

function statusClass(status) {
    const normalized = normalizeSellerStatus(status);
    if (normalized === 'DELIVERED' || normalized === 'READY_FOR_PICKUP') return 'bg-green-50 text-green-800 border-green-200';
    if (normalized === 'CANCELLED') return 'bg-stone-100 text-stone-500 border-stone-200';
    if (normalized === 'ACCEPTED' || normalized === 'ARRANGING') return 'bg-amber-50 text-amber-800 border-amber-200';
    if (normalized === 'OUT_FOR_DELIVERY') return 'bg-stone-900 text-white border-stone-900';
    return 'bg-rose-50 text-rose-800 border-rose-200';
}

function statusLabel(status) {
    const labels = {
        PENDING: 'New',
        ACCEPTED: 'Accepted',
        ARRANGING: 'Arranging',
        PREPARING: 'Arranging',
        READY_FOR_PICKUP: 'Ready for Pickup',
        OUT_FOR_DELIVERY: 'Out for Delivery',
        DELIVERED: 'Delivered',
        COMPLETED: 'Delivered',
        CANCELLED: 'Cancelled',
    };
    return labels[status] || labels[normalizeSellerStatus(status)] || status;
}

function paymentLabel(value) {
    const labels = {
        COD: 'COD',
        GCASH: 'GCash',
        MAYA: 'Maya',
        CARD: 'Card',
    };
    return labels[value] || value;
}

function nextStatus(status) {
    const normalized = normalizeSellerStatus(status);
    if (normalized === 'PENDING') return 'ACCEPTED';
    if (normalized === 'ACCEPTED') return 'ARRANGING';
    if (normalized === 'ARRANGING') return 'READY_FOR_PICKUP';
    return null;
}

function nextStatusLabel(status) {
    const next = nextStatus(status);
    if (!next) return '';
    if (next === 'ACCEPTED') return 'Accept Order';
    if (next === 'ARRANGING') return 'Mark Arranging';
    if (next === 'READY_FOR_PICKUP') return 'Mark Ready';
    return '';
}

function deliveryLabel(order) {
    if (!order.deliveryDate) return order.timeSlot || 'No delivery window';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'short', day: 'numeric' })}, ${order.timeSlot}`;
}

function detailActionLabel(status) {
    const normalized = normalizeSellerStatus(status);
    if (normalized === 'READY_FOR_PICKUP') return 'Update shipping';
    if (normalized === 'OUT_FOR_DELIVERY') return 'View tracking';
    if (normalized === 'DELIVERED') return 'View proof';
    return 'View details';
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
    const [floristReviews, setFloristReviews] = useState({ averageRating: 0, totalReviews: 0, recentReviews: [] });
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
    const [shopLogoUrl, setShopLogoUrl] = useState('');
    const [shopLogoBroken, setShopLogoBroken] = useState(false);
    const [logoUploading, setLogoUploading] = useState(false);
    const [logoError, setLogoError] = useState('');
    const [dailyCapacity, setDailyCapacity] = useState(12);
    const [updatingOrderId, setUpdatingOrderId] = useState(null);

    const [conversations, setConversations] = useState([]);
    const [activeConversationId, setActiveConversationId] = useState(null);
    const [threadMessages, setThreadMessages] = useState([]);
    const [messageInput, setMessageInput] = useState('');

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
                
                try {
                    const reviewsResponse = await reviewsAPI.getFloristReviews(florist.id);
                    setFloristReviews(reviewsResponse.data.data);
                } catch (rErr) {
                    console.error('Unable to fetch florist reviews', rErr);
                }

                setShopName(florist.storeName || (user?.name ? `${user.name}'s Studio` : 'My Floral Studio'));
                setShopBio(florist.bio || 'Locally composed preserved floral pieces for thoughtful Cebu gifting, prepared with careful wrapping and delivery-ready notes.');
                setShopCity(florist.city || 'Cebu, Philippines');
                setShopLogoUrl(florist.logoUrl || '');
                setShopLogoBroken(false);
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

    useEffect(() => {
        if (activeTab === 'messages' && isArtisan) {
            sellerConversationsAPI.getConversations()
                .then(res => setConversations(res.data.data || []))
                .catch(err => console.error('Failed to load conversations', err));
        }
    }, [activeTab, isArtisan]);

    useEffect(() => {
        if (activeTab === 'messages' && activeConversationId) {
            const fetchMessages = () => {
                sellerConversationsAPI.getMessages(activeConversationId)
                    .then(res => setThreadMessages(res.data.data || []))
                    .catch(console.error);
            };
            fetchMessages();
            const interval = setInterval(fetchMessages, 10000);
            return () => clearInterval(interval);
        } else {
            setThreadMessages([]);
        }
    }, [activeTab, activeConversationId]);

    const metrics = useMemo(() => {
        const liveCount = products.filter((product) => product.inStock).length;
        const soldOutCount = products.length - liveCount;
        const inventoryValue = products.reduce((sum, product) => sum + Number(product.price || 0), 0);
        const actionableOrders = orders.filter((order) => !['DELIVERED', 'COMPLETED', 'CANCELLED'].includes(order.status));
        const today = new Date().toISOString().slice(0, 10);
        const dueToday = actionableOrders.filter((order) => order.deliveryDate === today).length;

        return [
            { label: 'Live Listings', value: liveCount, helper: `${soldOutCount} paused or sold out`, icon: Package },
            { label: 'Orders to Prepare', value: actionableOrders.length, helper: `${dueToday} deliveries due today`, icon: Truck },
            { label: 'Average Studio Rating', value: floristReviews.averageRating ? floristReviews.averageRating.toFixed(1) : '—', helper: `${floristReviews.totalReviews} total customer notes`, icon: Star },
        ];
    }, [orders, products, floristReviews]);

    const orderBoardColumns = useMemo(() => {
        const grouped = orders.reduce((acc, order) => {
            const status = normalizeSellerStatus(order.status);
            acc[status] = [...(acc[status] || []), order];
            return acc;
        }, {});

        return orderWorkflowColumns
            .filter((column) => column.status !== 'CANCELLED' || grouped.CANCELLED?.length)
            .map((column) => ({
                ...column,
                orders: grouped[column.status] || [],
            }));
    }, [orders]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const resetForm = () => {
        setFormData({ ...blankListing, floristName: shopName, floristLogoUrl: shopLogoUrl });
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
            floristLogoUrl: product.floristLogoUrl || shopLogoUrl,
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
            floristLogoUrl: formData.floristLogoUrl || shopLogoUrl,
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
            floristLogoUrl: product.floristLogoUrl || shopLogoUrl,
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
                logoUrl: shopLogoUrl,
                maxDailyCapacity: Number(dailyCapacity),
            });
            const florist = response.data.data;
            setShopName(florist.storeName || shopName);
            setShopBio(florist.bio || shopBio);
            setShopCity(florist.city || shopCity);
            setShopLogoUrl(florist.logoUrl || shopLogoUrl);
            setShopLogoBroken(false);
            setDailyCapacity(florist.maxDailyCapacity || dailyCapacity);
            setIsEditingShop(false);
            setNotice('Shop profile saved.');
        } catch (err) {
            setError(apiErrorMessage(err, 'Unable to save shop profile'));
        } finally {
            setSaving(false);
        }
    };

    const uploadShopLogo = async (event) => {
        const file = event.target.files?.[0];
        if (!file) return;

        setLogoUploading(true);
        setLogoError('');
        setError('');
        setNotice('');
        try {
            const response = await floristAPI.uploadProfileImage(file);
            const logoUrl = response.data.data?.logoUrl || '';
            setShopLogoUrl(logoUrl);
            setShopLogoBroken(false);
            setProducts((current) => current.map((product) => ({ ...product, floristLogoUrl: logoUrl })));
            setFormData((current) => ({ ...current, floristLogoUrl: logoUrl }));
            setNotice('Shop logo uploaded.');
        } catch (err) {
            setLogoError(apiErrorMessage(err, 'Unable to upload shop logo'));
        } finally {
            setLogoUploading(false);
            event.target.value = '';
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

    const handleSendMessage = async (e) => {
        e.preventDefault();
        if (!messageInput.trim() || !activeConversationId) return;
        
        try {
            await sellerConversationsAPI.sendMessage(activeConversationId, { content: messageInput.trim() });
            setMessageInput('');
            const res = await sellerConversationsAPI.getMessages(activeConversationId);
            setThreadMessages(res.data.data || []);
        } catch (err) {
            console.error('Failed to send message', err);
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
                        <div className="absolute inset-0">
                            <Grainient
                                color1="#F1D1B8"
                                color2="#7E9F8A"
                                color3="#183D35"
                                timeSpeed={0.16}
                                colorBalance={-0.12}
                                warpStrength={1.65}
                                warpFrequency={5.8}
                                warpSpeed={1.2}
                                warpAmplitude={38}
                                blendAngle={-18}
                                blendSoftness={0.12}
                                rotationAmount={360}
                                noiseScale={1.6}
                                grainAmount={0.12}
                                grainScale={2.8}
                                grainAnimated={false}
                                contrast={1.35}
                                gamma={1.0}
                                saturation={1.05}
                                centerX={-0.18}
                                centerY={0.04}
                                zoom={0.78}
                            />
                        </div>
                        <div className="absolute inset-0 bg-stone-950/45 pointer-events-none" />
                        <div className="absolute inset-0 bg-gradient-to-r from-stone-950/55 via-stone-950/20 to-transparent pointer-events-none" />
                        <div className="relative">
                            <p className="text-xs uppercase tracking-[0.24em] text-white/70 mb-4">Florist Operations</p>
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
                                                <Link key={order.id} to={`/seller-orders/${order.id}`} className="w-full p-5 text-left hover:bg-stone-50 transition-colors grid grid-cols-1 md:grid-cols-[1fr_auto] gap-3">
                                                    <div>
                                                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">{order.orderNumber}</p>
                                                        <p className="mt-2 font-medium text-stone-950">{order.itemSummary}</p>
                                                        <p className="text-sm text-stone-500">{order.buyerName} · {deliveryLabel(order)} · {paymentLabel(order.paymentMethod)}</p>
                                                    </div>
                                                    <div className="md:text-right">
                                                        <span className={`inline-flex border px-2.5 py-1 text-xs font-medium ${statusClass(order.status)}`}>{statusLabel(order.status)}</span>
                                                        <p className="mt-2 font-semibold">{currency(order.sellerSubtotal)}</p>
                                                    </div>
                                                </Link>
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
                            <div className="space-y-4">
                                <div className="bg-white border border-stone-200 p-5 flex flex-col md:flex-row md:items-center justify-between gap-4">
                                    <div>
                                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">Florist Kanban</p>
                                        <h2 className="mt-1 font-serif text-2xl">Orders by Status</h2>
                                        <p className="text-sm text-stone-500 mt-1">Move early orders with quick actions. Shipping and proof steps stay in order details.</p>
                                    </div>
                                    <div className="flex items-center gap-3 text-sm text-stone-500">
                                        <Truck size={17} />
                                        <span>{orders.length} active board item{orders.length === 1 ? '' : 's'}</span>
                                    </div>
                                </div>

                                {loading ? (
                                    <div className="grid grid-cols-1 gap-4 lg:flex lg:overflow-x-auto">
                                        {[1, 2, 3, 4].map((item) => (
                                            <div key={item} className="h-64 bg-stone-100 animate-pulse border border-stone-200 lg:min-w-[290px] lg:flex-1" />
                                        ))}
                                    </div>
                                ) : orders.length ? (
                                    <div className="grid grid-cols-1 gap-4 lg:flex lg:overflow-x-auto lg:pb-3">
                                        {orderBoardColumns.map((column) => (
                                            <section key={column.status} className="bg-white border border-stone-200 lg:min-w-[290px] lg:flex-1">
                                                <div className={`border-b px-4 py-4 ${column.accent}`}>
                                                    <div className="flex items-start justify-between gap-3">
                                                        <div>
                                                            <h3 className="font-serif text-xl text-stone-950">{column.title}</h3>
                                                            <p className="mt-1 text-xs leading-relaxed text-stone-500">{column.helper}</p>
                                                        </div>
                                                        <span className="inline-flex h-8 min-w-8 items-center justify-center border border-white/70 bg-white px-2 text-xs font-semibold text-stone-700">
                                                            {column.orders.length}
                                                        </span>
                                                    </div>
                                                </div>
                                                <div className="space-y-3 p-3">
                                                    {column.orders.map((order) => (
                                                        <article key={order.id} className="border border-stone-200 bg-[#FDFCF8] p-4 shadow-sm">
                                                            <div className="flex items-start justify-between gap-3">
                                                                <div className="min-w-0">
                                                                    <p className="text-[11px] uppercase tracking-[0.18em] text-stone-400">{order.orderNumber}</p>
                                                                    <h4 className="mt-2 font-serif text-lg leading-tight text-stone-950">{order.itemSummary}</h4>
                                                                </div>
                                                                <span className={`shrink-0 border px-2 py-1 text-[10px] font-medium ${statusClass(order.status)}`}>{statusLabel(order.status)}</span>
                                                            </div>

                                                            <dl className="mt-4 space-y-2 text-xs text-stone-500">
                                                                <div className="flex justify-between gap-3">
                                                                    <dt>Buyer</dt>
                                                                    <dd className="min-w-0 truncate text-right font-medium text-stone-800">{order.buyerName}</dd>
                                                                </div>
                                                                <div className="flex justify-between gap-3">
                                                                    <dt>Recipient</dt>
                                                                    <dd className="min-w-0 truncate text-right text-stone-700">{order.recipientName}</dd>
                                                                </div>
                                                                <div className="flex justify-between gap-3">
                                                                    <dt>Delivery</dt>
                                                                    <dd className="text-right text-stone-700">{deliveryLabel(order)}</dd>
                                                                </div>
                                                                <div className="flex justify-between gap-3">
                                                                    <dt>Payment</dt>
                                                                    <dd className="text-right text-stone-700">{paymentLabel(order.paymentMethod)}</dd>
                                                                </div>
                                                            </dl>

                                                            <div className="mt-4 flex items-center justify-between gap-3 border-t border-stone-100 pt-3">
                                                                <p className="font-semibold text-stone-950">{currency(order.sellerSubtotal)}</p>
                                                                <div className="flex flex-wrap justify-end gap-1.5">
                                                                    {order.fulfillmentImageUrl && (
                                                                        <span className="border border-green-200 bg-green-50 px-2 py-1 text-[10px] font-medium uppercase tracking-wider text-green-800">Prep photo</span>
                                                                    )}
                                                                    {order.proofImageUrl && (
                                                                        <span className="border border-rose-200 bg-rose-50 px-2 py-1 text-[10px] font-medium uppercase tracking-wider text-rose-800">Proof</span>
                                                                    )}
                                                                </div>
                                                            </div>

                                                            <div className="mt-4 grid grid-cols-1 gap-2">
                                                                {nextStatus(order.status) ? (
                                                                    <button
                                                                        type="button"
                                                                        onClick={() => updateOrderStatus(order, nextStatus(order.status))}
                                                                        disabled={updatingOrderId === order.id}
                                                                        className="inline-flex min-h-10 items-center justify-center border border-stone-900 bg-stone-900 px-3 text-xs font-semibold uppercase tracking-widest text-white hover:bg-stone-800 disabled:cursor-not-allowed disabled:border-stone-200 disabled:bg-stone-200 disabled:text-stone-500"
                                                                    >
                                                                        {updatingOrderId === order.id ? 'Updating...' : nextStatusLabel(order.status)}
                                                                    </button>
                                                                ) : (
                                                                    <Link to={`/seller-orders/${order.id}`} className="inline-flex min-h-10 items-center justify-center border border-stone-900 bg-stone-900 px-3 text-xs font-semibold uppercase tracking-widest text-white hover:bg-stone-800">
                                                                        {detailActionLabel(order.status)}
                                                                    </Link>
                                                                )}
                                                                <Link to={`/seller-orders/${order.id}`} className="inline-flex min-h-10 items-center justify-center border border-stone-200 bg-white px-3 text-xs font-semibold uppercase tracking-widest text-stone-700 hover:border-stone-900 hover:text-stone-950">
                                                                    View details
                                                                </Link>
                                                            </div>
                                                        </article>
                                                    ))}
                                                    {!column.orders.length && (
                                                        <div className="border border-dashed border-stone-200 bg-stone-50 px-4 py-6 text-center text-sm text-stone-500">
                                                            No orders in {column.title.toLowerCase()}.
                                                        </div>
                                                    )}
                                                </div>
                                            </section>
                                        ))}
                                    </div>
                                ) : (
                                    <div className="border border-dashed border-stone-300 bg-white px-5 py-12 text-center text-stone-500">
                                        No seller orders yet. Customer checkout orders will land here once they include your products.
                                    </div>
                                )}
                            </div>
                        )}

                        {activeTab === 'reviews' && (
                            <div className="bg-white border border-stone-200">
                                <div className="p-5 border-b border-stone-100">
                                    <h2 className="font-serif text-2xl text-stone-900 mb-1">Customer Notes</h2>
                                    <p className="text-sm text-stone-500">Honest reviews from completed deliveries.</p>
                                </div>
                                <div className="p-5">
                                    {floristReviews.totalReviews > 0 ? (
                                        <div className="space-y-6">
                                            {floristReviews.recentReviews.map((review) => (
                                                <div key={review.id} className="border-b border-stone-100 pb-6 last:border-0 last:pb-0">
                                                    <div className="flex items-start justify-between mb-2">
                                                        <div>
                                                            <span className="font-medium text-sm text-stone-900">{review.reviewerName}</span>
                                                            <span className="text-xs text-stone-500 block mt-0.5">Rating: {review.floristRating}/5</span>
                                                        </div>
                                                        <span className="text-xs uppercase tracking-widest text-stone-400">
                                                            {new Date(review.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                                                        </span>
                                                    </div>
                                                    {review.comment && (
                                                        <p className="text-stone-600 leading-relaxed mt-2">{review.comment}</p>
                                                    )}
                                                </div>
                                            ))}
                                        </div>
                                    ) : (
                                        <div className="border border-dashed border-stone-300 bg-stone-50 px-5 py-12 text-center text-stone-500">
                                            No customer notes yet. Notes will appear here once buyers review your completed deliveries.
                                        </div>
                                    )}
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
                                                                <img src={mediaUrl(product.imageUrl)} alt={product.name} className="w-14 h-16 object-cover bg-stone-100 border border-stone-200" />
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
                                                <img src={mediaUrl(formData.imageUrl || blankListing.imageUrl)} alt="" className="w-full h-48 object-cover" />
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
                                        <div>
                                            <span className="text-sm font-medium text-stone-700">Store logo</span>
                                            <div className="mt-2 flex flex-col sm:flex-row sm:items-center gap-3">
                                                <div className="h-16 w-16 shrink-0 overflow-hidden rounded-full border border-stone-200 bg-stone-100 flex items-center justify-center">
                                                    {shopLogoUrl && !shopLogoBroken ? (
                                                        <img src={mediaUrl(shopLogoUrl)} alt="" onError={() => setShopLogoBroken(true)} className="h-full w-full object-cover" />
                                                    ) : (
                                                        <Store size={22} strokeWidth={1.4} className="text-stone-500" />
                                                    )}
                                                </div>
                                                <div className="min-w-0">
                                                    <label className={`h-11 px-4 border border-stone-200 bg-stone-50 text-sm font-medium inline-flex items-center justify-center gap-2 ${isEditingShop ? 'cursor-pointer hover:bg-white' : 'opacity-60 cursor-not-allowed'}`}>
                                                        <Upload size={15} /> {logoUploading ? 'Uploading...' : 'Upload Logo'}
                                                        <input type="file" accept="image/png,image/jpeg,image/jpg,image/webp" onChange={uploadShopLogo} disabled={!isEditingShop || logoUploading || saving} className="sr-only" />
                                                    </label>
                                                    {logoError && <p className="mt-2 text-xs text-red-700">{logoError}</p>}
                                                    {shopLogoUrl && <p className="mt-2 max-w-full truncate text-xs text-stone-500">{shopLogoUrl}</p>}
                                                </div>
                                            </div>
                                        </div>
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
                                        <div className="h-32 w-32 rounded-full mx-auto bg-stone-100 border border-stone-200 overflow-hidden flex items-center justify-center">
                                            {shopLogoUrl && !shopLogoBroken ? (
                                                <img src={mediaUrl(shopLogoUrl)} alt="" onError={() => setShopLogoBroken(true)} className="h-full w-full object-cover" />
                                            ) : (
                                                <Store size={34} strokeWidth={1.4} className="text-stone-500" />
                                            )}
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

                        {activeTab === 'messages' && (
                            <div className="bg-[#FDFCF8] border border-stone-200 h-[600px] flex flex-col md:flex-row overflow-hidden shadow-sm">
                                {/* Left Pane */}
                                <div className="w-full md:w-1/3 border-b md:border-b-0 md:border-r border-stone-200 flex flex-col bg-white overflow-hidden">
                                    <div className="p-4 border-b border-stone-100 bg-[#FDFCF8]">
                                        <h2 className="font-serif text-xl text-stone-900">Conversations</h2>
                                    </div>
                                    <div className="flex-1 overflow-y-auto">
                                        {conversations.map(conv => (
                                            <button 
                                                key={conv.id} 
                                                onClick={() => setActiveConversationId(conv.id)}
                                                className={`w-full text-left p-4 border-b border-stone-100 transition-colors block ${activeConversationId === conv.id ? 'bg-[#F4F1EA] border-l-2 border-l-stone-400' : 'hover:bg-stone-50'}`}
                                            >
                                                <p className="font-medium text-stone-900">{conv.buyerName || 'Buyer'}</p>
                                                <p className="text-xs text-stone-500 mt-1 line-clamp-1">{conv.lastMessage || 'No messages yet'}</p>
                                            </button>
                                        ))}
                                        {!conversations.length && (
                                            <div className="p-8 text-center text-sm text-stone-400">No active conversations. Notes from buyers will appear here.</div>
                                        )}
                                    </div>
                                </div>
                                
                                {/* Right Pane */}
                                <div className="flex-1 flex flex-col bg-[#FDFCF8] overflow-hidden">
                                    {activeConversationId ? (
                                        <>
                                            <div className="flex-1 overflow-y-auto p-6 space-y-6">
                                                {threadMessages.map(msg => {
                                                    const isSeller = msg.senderType === 'SELLER';
                                                    return (
                                                        <div key={msg.id} className={`flex flex-col ${isSeller ? 'items-end' : 'items-start'}`}>
                                                            <div className="text-[10px] uppercase tracking-widest text-stone-400 mb-1">{isSeller ? shopName : 'Buyer'}</div>
                                                            <div className={`p-4 max-w-[85%] text-sm leading-relaxed border ${isSeller ? 'bg-[#F4F1EA] border-[#E8E4D9] text-stone-800' : 'bg-white border-stone-200 text-stone-700'}`}>
                                                                {msg.content}
                                                            </div>
                                                            <div className="text-[10px] text-stone-400 mt-1.5">
                                                                {new Date(msg.createdAt).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })}
                                                            </div>
                                                        </div>
                                                    )
                                                })}
                                                {!threadMessages.length && (
                                                    <div className="text-center text-sm text-stone-400 mt-10">Start the conversation...</div>
                                                )}
                                            </div>
                                            <div className="p-4 border-t border-stone-200 bg-white">
                                                <form onSubmit={handleSendMessage} className="flex gap-3">
                                                    <input 
                                                        type="text" 
                                                        value={messageInput}
                                                        onChange={e => setMessageInput(e.target.value)}
                                                        placeholder="Write a note..." 
                                                        className="flex-1 h-11 border border-stone-200 bg-stone-50 px-4 text-sm outline-none focus:border-stone-400"
                                                    />
                                                    <button 
                                                        type="submit" 
                                                        disabled={!messageInput.trim()}
                                                        className="h-11 px-6 bg-stone-900 text-white text-sm font-semibold hover:bg-stone-800 disabled:opacity-50 transition-colors uppercase tracking-widest text-[11px]"
                                                    >
                                                        Send Note
                                                    </button>
                                                </form>
                                            </div>
                                        </>
                                    ) : (
                                        <div className="flex-1 flex items-center justify-center text-sm text-stone-400">
                                            Select a conversation to read notes
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}
                    </section>
                </div>
            </main>
        </div>
    );
}
