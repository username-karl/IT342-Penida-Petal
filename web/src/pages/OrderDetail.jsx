import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowLeft, CreditCard, Leaf, MapPin, Package } from 'lucide-react';
import OrderStatusBadge, { getStatusLabel } from '../components/OrderStatusBadge';
import ShippingInfoCard from '../components/ShippingInfoCard';
import { ordersAPI } from '../services/api';

const paymentLabels = {
    COD: 'Cash on Delivery',
    GCASH: 'GCash',
    MAYA: 'Maya',
    CARD: 'Card',
};

function currency(value) {
    return `PHP ${Number(value || 0).toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function deliveryLabel(order) {
    if (!order?.deliveryDate) return order?.timeSlot || 'Delivery pending';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' })} / ${order.timeSlot}`;
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
            <header className="sticky top-0 z-40 border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-6">
                    <Link to="/profile" className="inline-flex min-h-11 items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-950" aria-label="Back to profile">
                        <ArrowLeft size={18} />
                        Back
                    </Link>
                    <h1 className="font-serif text-xl tracking-tight">Order Details</h1>
                    <Link to="/dashboard" className="inline-flex min-h-11 items-center gap-2 font-serif text-lg tracking-tight text-stone-950" aria-label="Petal home">
                        <Leaf size={17} strokeWidth={1.5} />
                        Petal
                    </Link>
                </div>
            </header>

            <main className="mx-auto max-w-6xl px-6 py-10">
                {loading ? (
                    <div className="space-y-4">
                        <div className="h-36 animate-pulse bg-stone-100" />
                        <div className="h-52 animate-pulse bg-stone-100" />
                        <div className="h-32 animate-pulse bg-stone-100" />
                    </div>
                ) : error ? (
                    <div className="border border-red-200 bg-red-50 p-5 text-sm text-red-700">{error}</div>
                ) : order && (
                    <div className="grid grid-cols-1 gap-5 lg:grid-cols-[1fr_360px]">
                        <div className="space-y-5">
                            <section className="border border-stone-200 bg-white/80 p-6">
                                <p className="text-xs uppercase tracking-[0.18em] text-stone-400">{order.orderNumber}</p>
                                <div className="mt-3 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                                    <div>
                                        <h2 className="font-serif text-4xl text-stone-950">{getStatusLabel(order.status)}</h2>
                                        <p className="mt-1 text-sm text-stone-500">{deliveryLabel(order)}</p>
                                    </div>
                                    <OrderStatusBadge status={order.status} />
                                </div>
                            </section>

                            <ShippingInfoCard orderId={order.id} shipping={order.shipping} to={`/orders/${order.id}/shipping`} />

                            <section className="border border-stone-200 bg-white/80 p-5">
                                <div className="flex items-center gap-3">
                                    <MapPin size={19} className="text-stone-500" />
                                    <h2 className="font-serif text-2xl text-stone-950">Delivery Address</h2>
                                </div>
                                <p className="mt-4 text-sm font-semibold text-stone-900">{order.recipientName}</p>
                                <p className="mt-1 text-sm leading-relaxed text-stone-500">{order.recipientAddress}</p>
                            </section>

                            <section className="border border-stone-200 bg-white/80">
                                <div className="flex items-center gap-3 border-b border-stone-100 p-5">
                                    <Package size={19} className="text-stone-500" />
                                    <h2 className="font-serif text-2xl text-stone-950">Order Items</h2>
                                </div>
                                <div className="divide-y divide-stone-100">
                                    {(order.items || []).map((item) => (
                                        <div key={`${order.id}-${item.productId}`} className="flex gap-4 p-5">
                                            <img src={item.imageUrl} alt={item.productName} className="h-24 w-20 border border-stone-200 bg-stone-100 object-cover" />
                                            <div className="min-w-0 flex-1">
                                                <h3 className="font-serif text-xl text-stone-950">{item.productName}</h3>
                                                <p className="mt-1 text-sm text-stone-500">{item.floristName}</p>
                                                <p className="mt-2 text-sm text-stone-500">Qty {item.quantity} / {currency(item.unitPrice)} each</p>
                                            </div>
                                            <p className="text-sm font-semibold">{currency(item.lineTotal)}</p>
                                        </div>
                                    ))}
                                </div>
                            </section>
                        </div>

                        <aside className="space-y-5">
                            <section className="border border-stone-200 bg-white/80 p-5 lg:sticky lg:top-24">
                                <div className="flex items-center gap-3">
                                    <CreditCard size={19} className="text-stone-500" />
                                    <h2 className="font-serif text-2xl text-stone-950">Payment Summary</h2>
                                </div>
                                <dl className="mt-5 space-y-3 text-sm">
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Payment</dt><dd className="font-medium">{paymentLabels[order.paymentMethod] || order.paymentMethod}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Subtotal</dt><dd className="font-medium">{currency(order.totalAmount)}</dd></div>
                                    <div className="flex justify-between gap-4"><dt className="text-stone-500">Delivery</dt><dd className="font-medium">Included</dd></div>
                                    <div className="border-t border-stone-100 pt-3 flex justify-between gap-4"><dt className="text-stone-500">Total</dt><dd className="font-semibold text-stone-950">{currency(order.totalAmount)}</dd></div>
                                </dl>
                                {order.cardMessage && (
                                    <div className="mt-5 border-t border-stone-100 pt-4">
                                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">Card Message</p>
                                        <p className="mt-2 text-sm leading-relaxed text-stone-600">{order.cardMessage}</p>
                                    </div>
                                )}
                            </section>
                        </aside>
                    </div>
                )}
            </main>
        </div>
    );
}
