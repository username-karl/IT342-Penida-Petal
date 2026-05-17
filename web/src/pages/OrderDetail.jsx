import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Leaf, Truck } from 'lucide-react';
import { ordersAPI } from '../services/api';

const paymentLabels = {
    COD: 'Cash on Delivery',
    GCASH: 'GCash',
    MAYA: 'Maya',
    CARD: 'Card',
};

const statusLabels = {
    PENDING: 'Order Placed',
    PREPARING: 'Being Prepared',
    READY_FOR_PICKUP: 'Ready for Pickup',
    COMPLETED: 'Delivered',
    CANCELLED: 'Cancelled',
};

function currency(value) {
    return `PHP ${Number(value || 0).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function deliveryLabel(order) {
    if (!order?.deliveryDate) return order?.timeSlot || 'Delivery pending';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' })} · ${order.timeSlot}`;
}

function statusClass(status) {
    if (status === 'COMPLETED' || status === 'READY_FOR_PICKUP') return 'bg-green-50 text-green-800 border-green-200';
    if (status === 'CANCELLED') return 'bg-stone-100 text-stone-500 border-stone-200';
    if (status === 'PREPARING') return 'bg-amber-50 text-amber-800 border-amber-200';
    return 'bg-rose-50 text-rose-800 border-rose-200';
}

export default function OrderDetail() {
    const { id } = useParams();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadOrder = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await ordersAPI.getBuyerOrder(id);
                setOrder(response.data.data);
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load order');
            } finally {
                setLoading(false);
            }
        };

        loadOrder();
    }, [id]);

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900">
            <nav className="border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="max-w-6xl mx-auto h-16 px-6 flex items-center justify-between">
                    <Link to="/dashboard" className="text-xl font-serif tracking-tight inline-flex items-center gap-2">
                        <Leaf size={18} strokeWidth={1.5} /> Petal
                    </Link>
                    <Link to="/profile" className="inline-flex items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-900">
                        <ArrowLeft size={16} /> Back to profile
                    </Link>
                </div>
            </nav>

            <main className="max-w-6xl mx-auto px-6 py-10">
                {loading ? (
                    <div className="h-96 bg-stone-100 animate-pulse border border-stone-200" />
                ) : error ? (
                    <div className="border border-red-200 bg-red-50 p-5 text-sm text-red-700">{error}</div>
                ) : order && (
                    <div className="grid grid-cols-1 lg:grid-cols-[1fr_340px] gap-6">
                        <section className="bg-white border border-stone-200">
                            <div className="p-6 border-b border-stone-100">
                                <p className="text-xs uppercase tracking-[0.2em] text-stone-400">{order.orderNumber}</p>
                                <h1 className="mt-2 font-serif text-4xl">Order Details</h1>
                                <p className="mt-2 text-sm text-stone-500">{deliveryLabel(order)}</p>
                            </div>
                            <div className="divide-y divide-stone-100">
                                {(order.items || []).map((item) => (
                                    <div key={`${order.id}-${item.productId}`} className="p-5 flex gap-4">
                                        <img src={item.imageUrl} alt={item.productName} className="h-24 w-20 object-cover border border-stone-200 bg-stone-100" />
                                        <div className="flex-1 min-w-0">
                                            <h2 className="font-serif text-2xl">{item.productName}</h2>
                                            <p className="mt-1 text-sm text-stone-500">{item.floristName}</p>
                                            <p className="mt-2 text-sm text-stone-500">Qty {item.quantity} · {currency(item.unitPrice)} each</p>
                                        </div>
                                        <p className="font-semibold whitespace-nowrap">{currency(item.lineTotal)}</p>
                                    </div>
                                ))}
                            </div>
                        </section>

                        <aside className="space-y-4">
                            <div className="bg-white border border-stone-200 p-5">
                                <div className="flex items-center justify-between">
                                    <span className={`inline-flex border px-2.5 py-1 text-xs font-medium ${statusClass(order.status)}`}>{statusLabels[order.status] || order.status}</span>
                                    {order.status === 'COMPLETED' ? <CheckCircle2 size={20} /> : <Truck size={20} />}
                                </div>
                                <dl className="mt-5 space-y-3 text-sm">
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Recipient</dt><dd className="font-medium text-right">{order.recipientName}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Address</dt><dd className="font-medium text-right">{order.recipientAddress}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Payment</dt><dd className="font-medium">{paymentLabels[order.paymentMethod] || order.paymentMethod}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Total</dt><dd className="font-semibold">{currency(order.totalAmount)}</dd></div>
                                </dl>
                                {order.cardMessage && (
                                    <div className="mt-5 border-t border-stone-100 pt-4">
                                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">Card Message</p>
                                        <p className="mt-2 text-sm text-stone-600 leading-relaxed">{order.cardMessage}</p>
                                    </div>
                                )}
                            </div>
                        </aside>
                    </div>
                )}
            </main>
        </div>
    );
}
