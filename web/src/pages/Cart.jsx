import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, Leaf, Minus, Plus, ShoppingBag, Trash2 } from 'lucide-react';
import { cartAPI } from '../services/api';

const formatPeso = (value) => `₱${Number(value || 0).toLocaleString('en-PH', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
})}`;

export default function Cart() {
    const [cart, setCart] = useState({ items: [], subtotal: 0 });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const loadCart = async () => {
        setLoading(true);
        setError('');

        try {
            const response = await cartAPI.getCart();
            setCart(response.data.data || { items: [], subtotal: 0 });
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Unable to load cart');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadCart();
    }, []);

    const updateQuantity = async (item, nextQuantity) => {
        if (nextQuantity < 1) {
            return;
        }

        await cartAPI.updateItem(item.id, nextQuantity);
        await loadCart();
    };

    const removeItem = async (itemId) => {
        await cartAPI.removeItem(itemId);
        await loadCart();
    };

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900">
            <header className="h-20 border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="max-w-7xl mx-auto px-6 h-full flex items-center justify-between">
                    <Link to="/dashboard" className="inline-flex items-center gap-2 text-2xl font-serif tracking-tight">
                        <Leaf size={20} strokeWidth={1.5} />
                        Petal
                    </Link>
                    <Link to="/browse" className="inline-flex items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-900">
                        <ArrowLeft size={16} strokeWidth={1.5} />
                        Continue shopping
                    </Link>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-6 py-12">
                <div className="mb-10">
                    <p className="text-xs uppercase tracking-[0.22em] text-stone-500 mb-3">Shopping Cart</p>
                    <h1 className="text-5xl md:text-6xl font-serif font-light tracking-tight">Your Petal Basket</h1>
                </div>

                {error && (
                    <div className="mb-8 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {error}
                    </div>
                )}

                {loading ? (
                    <div className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8 animate-pulse">
                        <div className="space-y-4">
                            {[...Array(3)].map((_, index) => (
                                <div key={index} className="h-36 bg-stone-100 border border-stone-200" />
                            ))}
                        </div>
                        <div className="h-64 bg-stone-100 border border-stone-200" />
                    </div>
                ) : cart.items.length === 0 ? (
                    <div className="border border-stone-200 bg-white/80 px-6 py-16 text-center">
                        <ShoppingBag size={32} strokeWidth={1.5} className="mx-auto text-stone-400 mb-5" />
                        <p className="text-3xl font-serif text-stone-900 mb-2">Your basket is empty</p>
                        <p className="text-sm text-stone-500 mb-8">Browse local arrangements and add a gift to begin checkout.</p>
                        <Link to="/browse" className="inline-flex items-center justify-center h-11 px-6 bg-stone-900 text-white text-sm font-medium">
                            Browse Products
                        </Link>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8 items-start">
                        <section className="space-y-4">
                            {cart.items.map((item) => (
                                <div key={item.id} className="bg-white/80 border border-stone-200 p-4 flex gap-4">
                                    <Link to={`/products/${item.productId}`} className="w-28 h-32 bg-stone-100 overflow-hidden shrink-0">
                                        <img src={item.productImageUrl} alt={item.productName} className="w-full h-full object-cover" />
                                    </Link>
                                    <div className="flex-1 min-w-0">
                                        <p className="text-[10px] uppercase tracking-widest text-stone-400 mb-1">{item.floristName}</p>
                                        <Link to={`/products/${item.productId}`} className="font-serif text-2xl text-stone-900 hover:underline decoration-stone-300 underline-offset-4">
                                            {item.productName}
                                        </Link>
                                        <p className="text-sm text-stone-500 mt-2">{formatPeso(item.unitPrice)}</p>
                                        <div className="flex flex-wrap items-center justify-between gap-4 mt-5">
                                            <div className="inline-flex h-10 border border-stone-200 bg-white">
                                                <button
                                                    type="button"
                                                    onClick={() => updateQuantity(item, item.quantity - 1)}
                                                    className="w-10 flex items-center justify-center text-stone-600 hover:bg-stone-50"
                                                >
                                                    <Minus size={14} strokeWidth={1.5} />
                                                </button>
                                                <div className="w-12 flex items-center justify-center border-x border-stone-200 text-sm font-medium">{item.quantity}</div>
                                                <button
                                                    type="button"
                                                    onClick={() => updateQuantity(item, item.quantity + 1)}
                                                    className="w-10 flex items-center justify-center text-stone-600 hover:bg-stone-50"
                                                >
                                                    <Plus size={14} strokeWidth={1.5} />
                                                </button>
                                            </div>
                                            <button
                                                type="button"
                                                onClick={() => removeItem(item.id)}
                                                className="inline-flex items-center gap-2 text-sm text-red-700 hover:text-red-900"
                                            >
                                                <Trash2 size={15} strokeWidth={1.5} />
                                                Remove
                                            </button>
                                        </div>
                                    </div>
                                    <div className="hidden sm:block text-right font-medium text-stone-900">
                                        {formatPeso(item.lineTotal)}
                                    </div>
                                </div>
                            ))}
                        </section>

                        <aside className="bg-white/80 border border-stone-200 p-6 sticky top-24">
                            <h2 className="text-3xl font-serif text-stone-900 mb-6">Order Summary</h2>
                            <div className="space-y-4 text-sm border-b border-stone-100 pb-5 mb-5">
                                <div className="flex justify-between">
                                    <span className="text-stone-500">Subtotal</span>
                                    <span className="font-medium">{formatPeso(cart.subtotal)}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-stone-500">Delivery</span>
                                    <span className="font-medium">Calculated at checkout</span>
                                </div>
                            </div>
                            <div className="flex justify-between items-center mb-6">
                                <span className="text-stone-500">Total</span>
                                <span className="text-3xl font-serif text-stone-900">{formatPeso(cart.subtotal)}</span>
                            </div>
                            <Link to="/checkout" className="w-full h-12 bg-stone-900 text-white text-sm font-medium hover:bg-stone-800 inline-flex items-center justify-center">
                                Proceed to Checkout
                            </Link>
                            <p className="text-xs text-stone-400 mt-4 leading-relaxed">
                                Checkout scheduling and mock payment are the next flow after cart persistence.
                            </p>
                        </aside>
                    </div>
                )}
            </main>
        </div>
    );
}
