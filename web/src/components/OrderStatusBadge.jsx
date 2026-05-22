const statusLabels = {
    PENDING: 'Pending',
    ACCEPTED: 'Accepted',
    ARRANGING: 'Arranging',
    PREPARING: 'Arranging',
    READY_FOR_PICKUP: 'Ready for Pickup',
    OUT_FOR_DELIVERY: 'Out for Delivery',
    DELIVERED: 'Delivered',
    COMPLETED: 'Delivered',
    CANCELLED: 'Cancelled',
};

export function getStatusLabel(status) {
    return statusLabels[status] || status || 'Pending';
}

export default function OrderStatusBadge({ status, compact = false }) {
    const tone = status === 'CANCELLED'
        ? 'bg-stone-100 text-stone-500 border-stone-200'
        : status === 'DELIVERED' || status === 'COMPLETED'
            ? 'bg-green-50 text-green-800 border-green-200'
        : status === 'OUT_FOR_DELIVERY'
            ? 'bg-stone-900 text-white border-stone-900'
            : status === 'ACCEPTED' || status === 'ARRANGING' || status === 'PREPARING' || status === 'READY_FOR_PICKUP'
                    ? 'bg-amber-50 text-amber-800 border-amber-200'
                    : 'bg-rose-50 text-rose-800 border-rose-200';

    return (
        <span className={`inline-flex items-center border uppercase tracking-[0.16em] font-semibold ${compact ? 'px-2.5 py-1 text-[10px]' : 'px-3 py-1.5 text-xs'} ${tone}`}>
            {getStatusLabel(status)}
        </span>
    );
}
