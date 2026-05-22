import { Link, useLocation } from 'react-router-dom';
import { Check, Leaf } from 'lucide-react';

const formatPeso = (value) => `₱${Number(value || 0).toLocaleString('en-PH', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
})}`;

const paymentLabels = {
    COD: 'Cash on Delivery',
    GCASH: 'GCash',
    MAYA: 'Maya',
    CARD: 'Card',
};

export default function CheckoutConfirmation() {
    const location = useLocation();
    const order = location.state?.order;

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 flex items-center justify-center px-6">
            <div className="max-w-xl w-full bg-white/80 border border-stone-200 p-8 md:p-10 text-center">
                <div className="w-14 h-14 mx-auto rounded-full bg-green-50 border border-green-100 text-green-700 flex items-center justify-center mb-6">
                    <Check size={26} strokeWidth={1.8} />
                </div>
                <div className="inline-flex items-center gap-2 text-xs uppercase tracking-[0.22em] text-stone-500 mb-4">
                    <Leaf size={14} strokeWidth={1.5} />
                    Order Placed
                </div>
                <h1 className="text-5xl font-serif font-light text-stone-900 mb-4">
                    Your gift is pending.
                </h1>
                <p className="text-stone-600 leading-relaxed mb-8">
                    The florist can now review and prepare this order. Your selected payment method is saved with the order.
                </p>
                {order && (
                    <div className="border border-stone-100 bg-[#FDFCF8] p-5 text-left text-sm space-y-3 mb-8">
                        <div className="flex justify-between">
                            <span className="text-stone-500">Order</span>
                            <span className="font-medium">#{order.id}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-stone-500">Status</span>
                            <span className="font-medium">{order.status}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-stone-500">Delivery</span>
                            <span className="font-medium">{order.deliveryDate} / {order.timeSlot}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-stone-500">Payment</span>
                            <span className="font-medium">{paymentLabels[order.paymentMethod] || order.paymentMethod}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-stone-500">Total</span>
                            <span className="font-medium">{formatPeso(order.totalAmount)}</span>
                        </div>
                    </div>
                )}
                <div className="flex flex-col sm:flex-row gap-3 justify-center">
                    <Link to="/dashboard" className="h-11 px-6 bg-stone-900 text-white text-sm font-medium inline-flex items-center justify-center">
                        Back Home
                    </Link>
                    <Link to="/browse" className="h-11 px-6 border border-stone-300 text-stone-700 text-sm font-medium inline-flex items-center justify-center hover:border-stone-900 hover:text-stone-900">
                        Continue Shopping
                    </Link>
                </div>
            </div>
        </div>
    );
}
