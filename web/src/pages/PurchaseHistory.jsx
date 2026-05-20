import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertCircle, ArrowLeft, Gift, Leaf, PackageCheck, Truck } from 'lucide-react';
import PurchaseHistoryCard, { currency } from '../components/PurchaseHistoryCard';
import { ordersAPI } from '../services/api';

export default function PurchaseHistory() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const loadOrders = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await ordersAPI.getBuyerOrders();
                setOrders(response.data.data || []);
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load gift history');
            } finally {
                setLoading(false);
            }
        };

        loadOrders();
    }, []);

    const stats = useMemo(() => {
        const delivered = orders.filter((order) => order.status === 'DELIVERED' || order.status === 'COMPLETED').length;
        const active = orders.filter((order) => !['DELIVERED', 'COMPLETED', 'CANCELLED'].includes(order.status)).length;
        const total = orders.reduce((sum, order) => sum + Number(order.totalAmount || 0), 0);
        return { delivered, active, total };
    }, [orders]);

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900">
            <header className="sticky top-0 z-40 border-b border-stone-200 bg-[#FDFCF8]/95 backdrop-blur-md">
                <div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-6">
                    <Link to="/profile" className="inline-flex min-h-11 items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-950" aria-label="Back to profile">
                        <ArrowLeft size={18} />
                        Back to profile
                    </Link>
                    <Link to="/dashboard" className="inline-flex min-h-11 items-center gap-2 font-serif text-lg tracking-tight text-stone-950" aria-label="Petal home">
                        <Leaf size={17} strokeWidth={1.5} />
                        Petal
                    </Link>
                </div>
            </header>

            <main className="mx-auto max-w-6xl px-6 py-10">
                <section className="border border-stone-200 bg-white/80 p-6 sm:p-8">
                    <p className="text-xs uppercase tracking-[0.22em] text-stone-400">Account archive</p>
                    <div className="mt-3 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div>
                            <h1 className="font-serif text-4xl text-stone-950 md:text-5xl">Gift History</h1>
                            <p className="mt-2 max-w-2xl text-sm leading-relaxed text-stone-500">
                                Review every bouquet gift, delivery state, recipient, and order detail from one dedicated page.
                            </p>
                        </div>
                        <Link to="/dashboard" className="inline-flex min-h-11 items-center justify-center border border-stone-900 px-5 text-sm font-medium text-stone-900 transition-colors hover:bg-stone-900 hover:text-white">
                            Continue shopping
                        </Link>
                    </div>
                </section>

                <section className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-3">
                    <div className="border border-stone-200 bg-white p-5">
                        <Gift size={18} className="text-stone-500" />
                        <p className="mt-4 text-xs uppercase tracking-widest text-stone-400">Total gifts</p>
                        <p className="mt-1 font-serif text-3xl text-stone-950">{orders.length}</p>
                    </div>
                    <div className="border border-stone-200 bg-white p-5">
                        <Truck size={18} className="text-stone-500" />
                        <p className="mt-4 text-xs uppercase tracking-widest text-stone-400">Active gifts</p>
                        <p className="mt-1 font-serif text-3xl text-stone-950">{stats.active}</p>
                    </div>
                    <div className="border border-stone-200 bg-white p-5">
                        <PackageCheck size={18} className="text-stone-500" />
                        <p className="mt-4 text-xs uppercase tracking-widest text-stone-400">Gift total</p>
                        <p className="mt-1 font-serif text-3xl text-stone-950">{currency(stats.total)}</p>
                    </div>
                </section>

                <section className="mt-8">
                    <div className="mb-5 flex flex-col gap-2 border-b border-stone-200 pb-4 sm:flex-row sm:items-end sm:justify-between">
                        <div>
                            <h2 className="font-serif text-2xl text-stone-900">All Gifts</h2>
                            <p className="mt-1 text-sm text-stone-500">
                                {orders.length ? `Showing ${orders.length} gift${orders.length === 1 ? '' : 's'}` : 'Your gifts will appear here after checkout.'}
                            </p>
                        </div>
                    </div>

                    {error && (
                        <div className="mb-4 flex items-center gap-2 border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                            <AlertCircle size={16} /> {error}
                        </div>
                    )}

                    {loading ? (
                        <div className="space-y-4">
                            {[1, 2, 3].map((item) => (
                                <div key={item} className="h-32 animate-pulse bg-stone-100" />
                            ))}
                        </div>
                    ) : orders.length ? (
                        <div className="space-y-4">
                            {orders.map((order) => (
                                <PurchaseHistoryCard key={order.id} order={order} />
                            ))}
                        </div>
                    ) : (
                        <div className="border border-dashed border-stone-300 bg-stone-50 px-6 py-12 text-center">
                            <PackageCheck className="mx-auto mb-3 text-stone-400" size={28} />
                            <h3 className="font-serif text-2xl text-stone-900">No gifts yet</h3>
                            <p className="mt-1 text-sm text-stone-500">Bouquet gifts from checkout will be saved here for easy review.</p>
                            <Link to="/dashboard" className="mt-5 inline-flex min-h-11 items-center justify-center bg-stone-900 px-5 text-sm font-semibold text-white hover:bg-stone-800">
                                Browse flowers
                            </Link>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}
