import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, Calendar, CreditCard, Leaf, MessageSquare, MapPin, User } from 'lucide-react';
import { cartAPI, ordersAPI } from '../services/api';

const formatPeso = (value) => `₱${Number(value || 0).toLocaleString('en-PH', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
})}`;

const today = new Date().toISOString().slice(0, 10);

const paymentOptions = [
    { value: 'COD', label: 'Cash on Delivery', helper: 'Pay the rider when the flowers arrive.' },
    { value: 'GCASH', label: 'GCash', helper: 'Mobile wallet payment reserved for confirmation.' },
    { value: 'MAYA', label: 'Maya', helper: 'Pay through Maya after florist confirmation.' },
    { value: 'CARD', label: 'Card', helper: 'Card capture placeholder for this build.' },
];

function paymentLabel(value) {
    return paymentOptions.find((option) => option.value === value)?.label || value;
}

export default function Checkout() {
    const navigate = useNavigate();
    const [cart, setCart] = useState({ items: [], subtotal: 0 });
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({
        recipientName: '',
        recipientAddress: '',
        cardMessage: '',
        deliveryDate: today,
        timeSlot: 'AM',
        paymentMethod: 'COD',
    });

    useEffect(() => {
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

        loadCart();
    }, []);

    const cardCharacters = formData.cardMessage.length;
    const canSubmit = useMemo(() => cart.items.length > 0 && !submitting, [cart.items.length, submitting]);

    const updateField = (field, value) => {
        setFormData((current) => ({ ...current, [field]: value }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setSubmitting(true);
        setError('');

        try {
            const response = await ordersAPI.createOrder(formData);
            navigate('/checkout/confirmation', { state: { order: response.data.data } });
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Unable to place order');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900">
            <header className="h-20 border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="max-w-7xl mx-auto px-6 h-full flex items-center justify-between">
                    <Link to="/dashboard" className="inline-flex items-center gap-2 text-2xl font-serif tracking-tight">
                        <Leaf size={20} strokeWidth={1.5} />
                        Petal
                    </Link>
                    <Link to="/cart" className="inline-flex items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-900">
                        <ArrowLeft size={16} strokeWidth={1.5} />
                        Back to basket
                    </Link>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-6 py-12">
                <div className="mb-10">
                    <p className="text-xs uppercase tracking-[0.22em] text-stone-500 mb-3">Checkout</p>
                    <h1 className="text-5xl md:text-6xl font-serif font-light tracking-tight">Schedule the Gift</h1>
                </div>

                {error && (
                    <div className="mb-8 border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {error}
                    </div>
                )}

                {loading ? (
                    <div className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8 animate-pulse">
                        <div className="h-[520px] bg-stone-100 border border-stone-200" />
                        <div className="h-72 bg-stone-100 border border-stone-200" />
                    </div>
                ) : cart.items.length === 0 ? (
                    <div className="border border-stone-200 bg-white/80 px-6 py-16 text-center">
                        <p className="text-3xl font-serif text-stone-900 mb-2">Your basket is empty</p>
                        <p className="text-sm text-stone-500 mb-8">Add an arrangement before checkout.</p>
                        <Link to="/browse" className="inline-flex items-center justify-center h-11 px-6 bg-stone-900 text-white text-sm font-medium">
                            Browse Products
                        </Link>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-[1fr_380px] gap-8 items-start">
                        <section className="bg-white/80 border border-stone-200 p-6 md:p-8 space-y-8">
                            <div>
                                <div className="flex items-center gap-2 mb-4">
                                    <User size={18} strokeWidth={1.5} className="text-stone-500" />
                                    <h2 className="text-3xl font-serif text-stone-900">Recipient</h2>
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    <label className="block">
                                        <span className="text-xs uppercase tracking-wider text-stone-500 mb-2 block">Recipient Name</span>
                                        <input
                                            required
                                            value={formData.recipientName}
                                            onChange={(event) => updateField('recipientName', event.target.value)}
                                            className="w-full h-12 border border-stone-200 bg-[#FDFCF8] px-4 text-sm outline-none focus:border-stone-900"
                                        />
                                    </label>
                                    <label className="block">
                                        <span className="text-xs uppercase tracking-wider text-stone-500 mb-2 block">Delivery Date</span>
                                        <input
                                            required
                                            type="date"
                                            min={today}
                                            value={formData.deliveryDate}
                                            onChange={(event) => updateField('deliveryDate', event.target.value)}
                                            className="w-full h-12 border border-stone-200 bg-[#FDFCF8] px-4 text-sm outline-none focus:border-stone-900"
                                        />
                                    </label>
                                </div>
                            </div>

                            <div>
                                <div className="flex items-center gap-2 mb-4">
                                    <MapPin size={18} strokeWidth={1.5} className="text-stone-500" />
                                    <h2 className="text-3xl font-serif text-stone-900">Delivery Address</h2>
                                </div>
                                <textarea
                                    required
                                    value={formData.recipientAddress}
                                    onChange={(event) => updateField('recipientAddress', event.target.value)}
                                    rows={4}
                                    placeholder="Street, barangay, city, landmark"
                                    className="w-full border border-stone-200 bg-[#FDFCF8] px-4 py-3 text-sm outline-none focus:border-stone-900 resize-none"
                                />
                            </div>

                            <div>
                                <div className="flex items-center gap-2 mb-4">
                                    <Calendar size={18} strokeWidth={1.5} className="text-stone-500" />
                                    <h2 className="text-3xl font-serif text-stone-900">Time Slot</h2>
                                </div>
                                <div className="grid grid-cols-2 gap-3">
                                    {['AM', 'PM'].map((slot) => (
                                        <label key={slot} className={`h-14 border flex items-center justify-center cursor-pointer ${formData.timeSlot === slot ? 'border-stone-900 bg-stone-900 text-white' : 'border-stone-200 bg-[#FDFCF8] text-stone-600 hover:border-stone-500'}`}>
                                            <input
                                                type="radio"
                                                name="timeSlot"
                                                value={slot}
                                                checked={formData.timeSlot === slot}
                                                onChange={(event) => updateField('timeSlot', event.target.value)}
                                                className="hidden"
                                            />
                                            {slot === 'AM' ? 'Morning (AM)' : 'Afternoon (PM)'}
                                        </label>
                                    ))}
                                </div>
                            </div>

                            <div>
                                <div className="flex items-center gap-2 mb-4">
                                    <MessageSquare size={18} strokeWidth={1.5} className="text-stone-500" />
                                    <h2 className="text-3xl font-serif text-stone-900">Card Message</h2>
                                </div>
                                <textarea
                                    maxLength={200}
                                    value={formData.cardMessage}
                                    onChange={(event) => updateField('cardMessage', event.target.value)}
                                    rows={4}
                                    placeholder="Write a short note for the recipient"
                                    className="w-full border border-stone-200 bg-[#FDFCF8] px-4 py-3 text-sm outline-none focus:border-stone-900 resize-none"
                                />
                                <p className="text-xs text-stone-400 mt-2">{cardCharacters}/200 characters</p>
                            </div>

                            <div>
                                <div className="flex items-center gap-2 mb-4">
                                    <CreditCard size={18} strokeWidth={1.5} className="text-stone-500" />
                                    <h2 className="text-3xl font-serif text-stone-900">Payment Method</h2>
                                </div>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                                    {paymentOptions.map((option) => (
                                        <label key={option.value} className={`border p-4 cursor-pointer transition-colors ${formData.paymentMethod === option.value ? 'border-stone-900 bg-stone-900 text-white' : 'border-stone-200 bg-[#FDFCF8] text-stone-700 hover:border-stone-500'}`}>
                                            <input
                                                type="radio"
                                                name="paymentMethod"
                                                value={option.value}
                                                checked={formData.paymentMethod === option.value}
                                                onChange={(event) => updateField('paymentMethod', event.target.value)}
                                                className="hidden"
                                            />
                                            <span className="block text-sm font-semibold">{option.label}</span>
                                            <span className={`mt-1 block text-xs leading-relaxed ${formData.paymentMethod === option.value ? 'text-white/70' : 'text-stone-500'}`}>{option.helper}</span>
                                        </label>
                                    ))}
                                </div>
                            </div>
                        </section>

                        <aside className="bg-white/80 border border-stone-200 p-6 sticky top-24">
                            <h2 className="text-3xl font-serif text-stone-900 mb-6">Order Summary</h2>
                            <div className="space-y-4 mb-6">
                                {cart.items.map((item) => (
                                    <div key={item.id} className="flex gap-3">
                                        <img src={item.productImageUrl} alt={item.productName} className="w-14 h-16 object-cover bg-stone-100" />
                                        <div className="flex-1 min-w-0">
                                            <p className="text-sm font-medium text-stone-900 truncate">{item.productName}</p>
                                            <p className="text-xs text-stone-500">Qty {item.quantity}</p>
                                        </div>
                                        <p className="text-sm font-medium">{formatPeso(item.lineTotal)}</p>
                                    </div>
                                ))}
                            </div>
                            <div className="border-t border-stone-100 pt-5 space-y-4 text-sm">
                                <div className="flex justify-between">
                                    <span className="text-stone-500">Subtotal</span>
                                    <span className="font-medium">{formatPeso(cart.subtotal)}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-stone-500">Delivery</span>
                                    <span className="font-medium">Included</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-stone-500">Payment</span>
                                    <span className="font-medium">{paymentLabel(formData.paymentMethod)}</span>
                                </div>
                            </div>
                            <div className="border-t border-stone-100 mt-5 pt-5 flex justify-between items-center">
                                <span className="text-stone-500">Total</span>
                                <span className="text-3xl font-serif text-stone-900">{formatPeso(cart.subtotal)}</span>
                            </div>
                            <button
                                type="submit"
                                disabled={!canSubmit}
                                className="mt-6 w-full h-12 bg-stone-900 text-white text-sm font-medium hover:bg-stone-800 disabled:opacity-50 disabled:cursor-not-allowed inline-flex items-center justify-center gap-2"
                            >
                                <CreditCard size={17} strokeWidth={1.5} />
                                {submitting ? 'Placing Order...' : 'Place Order'}
                            </button>
                            <p className="text-xs text-stone-400 mt-4 leading-relaxed">
                                Payment is recorded for order tracking. Online payment capture is still a placeholder in this build.
                            </p>
                        </aside>
                    </form>
                )}
            </main>
        </div>
    );
}
