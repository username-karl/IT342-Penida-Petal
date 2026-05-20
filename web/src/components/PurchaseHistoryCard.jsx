import { Link } from 'react-router-dom';
import { CheckCircle2, Truck } from 'lucide-react';

export function currency(value) {
    const number = Number(value || 0);
    return `PHP ${number.toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

export function statusLabel(status) {
    const labels = {
        PENDING: 'Order Placed',
        ACCEPTED: 'Accepted',
        ARRANGING: 'Being Arranged',
        PREPARING: 'Being Arranged',
        READY_FOR_PICKUP: 'Ready for Pickup',
        OUT_FOR_DELIVERY: 'Out for Delivery',
        DELIVERED: 'Delivered',
        COMPLETED: 'Delivered',
        CANCELLED: 'Cancelled',
    };
    return labels[status] || status;
}

export function statusClass(status) {
    if (status === 'COMPLETED' || status === 'DELIVERED' || status === 'READY_FOR_PICKUP') return 'bg-green-50 text-green-800 border-green-200';
    if (status === 'CANCELLED') return 'bg-stone-100 text-stone-500 border-stone-200';
    if (status === 'ACCEPTED' || status === 'ARRANGING' || status === 'PREPARING') return 'bg-amber-50 text-amber-800 border-amber-200';
    if (status === 'OUT_FOR_DELIVERY') return 'bg-stone-900 text-white border-stone-900';
    return 'bg-rose-50 text-rose-800 border-rose-200';
}

export function paymentLabel(value) {
    const labels = {
        COD: 'Cash on Delivery',
        GCASH: 'GCash',
        MAYA: 'Maya',
        CARD: 'Card',
    };
    return labels[value] || value;
}

export function deliveryLabel(order) {
    if (!order.deliveryDate) return order.timeSlot || 'Delivery pending';
    const date = new Date(`${order.deliveryDate}T00:00:00`);
    return `${date.toLocaleDateString('en-PH', { month: 'long', day: 'numeric', year: 'numeric' })} / ${order.timeSlot}`;
}

export default function PurchaseHistoryCard({ order, compact = false }) {
    const isDelivered = order.status === 'COMPLETED' || order.status === 'DELIVERED';

    return (
        <Link to={`/orders/${order.id}`} className="block border border-stone-200 bg-white p-4 transition-colors hover:border-stone-300 sm:p-5">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div className="flex min-w-0 gap-4">
                    <div className="flex h-14 w-14 shrink-0 items-center justify-center border border-stone-200 bg-stone-100 text-stone-500">
                        {isDelivered ? <CheckCircle2 size={22} /> : <Truck size={22} />}
                    </div>
                    <div className="min-w-0">
                        <p className="text-xs uppercase tracking-[0.18em] text-stone-400">{order.orderNumber}</p>
                        <h4 className="mt-1 font-serif text-xl text-stone-950">{order.itemSummary}</h4>
                        <p className="mt-1 text-sm text-stone-500">{deliveryLabel(order)}</p>
                        <p className="mt-1 text-xs text-stone-400">Deliver to {order.recipientName}</p>
                        <p className="mt-1 text-xs text-stone-400">Payment: {paymentLabel(order.paymentMethod)}</p>
                    </div>
                </div>
                <div className="sm:text-right">
                    <span className={`inline-flex border px-2.5 py-1 text-xs font-medium ${statusClass(order.status)}`}>
                        {statusLabel(order.status)}
                    </span>
                    <p className="mt-2 font-semibold text-stone-950">{currency(order.totalAmount)}</p>
                </div>
            </div>

            {!compact && (
                <div className="mt-4 grid grid-cols-1 gap-3 border-t border-stone-100 pt-4 sm:grid-cols-2">
                    {(order.items || []).map((item) => (
                        <div key={`${order.id}-${item.productId}`} className="flex min-w-0 gap-3">
                            <img src={item.imageUrl} alt={item.productName} className="h-14 w-12 border border-stone-200 bg-stone-100 object-cover" />
                            <div className="min-w-0">
                                <p className="truncate text-sm font-medium text-stone-900">{item.productName}</p>
                                <p className="text-xs text-stone-500">{item.floristName}</p>
                                <p className="text-xs text-stone-400">Qty {item.quantity} / {currency(item.lineTotal)}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </Link>
    );
}
