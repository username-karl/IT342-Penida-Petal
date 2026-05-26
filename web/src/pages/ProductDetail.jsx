import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
    ArrowLeft, Heart, Leaf, MapPin, MessageCircle, Minus, Package,
    Plus, ShieldCheck, ShoppingBag, Star, Store, Truck
} from 'lucide-react';
import { cartAPI, mediaUrl, productsAPI, reviewsAPI } from '../services/api';

const fallbackGallery = [
    '/images/product_pampas_1771726515735.png',
    '/images/product_eucalyptus_1771726530879.png',
    '/images/product_cotton_1771726545396.png',
    '/images/product_aurora_hydrangea_1771726583839.png',
];

export default function ProductDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [activeImage, setActiveImage] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [cartMessage, setCartMessage] = useState('');
    const [cartError, setCartError] = useState('');
    const [addingToCart, setAddingToCart] = useState(false);
    const [floristLogoBroken, setFloristLogoBroken] = useState(false);
    const [reviews, setReviews] = useState({ averageRating: 0, totalReviews: 0, recentReviews: [] });

    useEffect(() => {
        const fetchProduct = async () => {
            setLoading(true);
            setError('');

            try {
                const response = await productsAPI.getProduct(id);
                const nextProduct = response.data.data;
                setProduct(nextProduct);
                setActiveImage(nextProduct?.imageUrl || '');
                
                try {
                    const reviewsRes = await reviewsAPI.getProductReviews(id);
                    setReviews(reviewsRes.data.data);
                } catch (rErr) {
                    console.error('Failed to load reviews', rErr);
                }
            } catch (err) {
                setError(err.response?.data?.message || err.message || 'Unable to load product');
            } finally {
                setLoading(false);
            }
        };

        fetchProduct();
    }, [id]);

    const priceValue = Number(product?.price || 0);
    const price = product ? `₱${priceValue.toLocaleString('en-PH', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '';
    const galleryImages = product
        ? [product.imageUrl, ...fallbackGallery.filter((image) => image !== product.imageUrl)].filter(Boolean).slice(0, 5)
        : [];

    const handleAddToCart = async () => {
        setAddingToCart(true);
        setCartMessage('');
        setCartError('');

        try {
            await cartAPI.addItem({ productId: Number(id), quantity });
            setCartMessage('Added to cart');
        } catch (err) {
            setCartError(err.response?.data?.message || err.message || 'Unable to add item to cart');
        } finally {
            setAddingToCart(false);
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
                    <div className="flex items-center gap-5">
                        <button
                            type="button"
                            onClick={() => navigate('/dashboard')}
                            className="hidden sm:inline-flex items-center gap-2 text-sm font-medium text-stone-600 hover:text-stone-900"
                        >
                            <ArrowLeft size={16} strokeWidth={1.5} />
                            Back to collection
                        </button>
                        <Link to="/cart" className="relative inline-flex h-10 w-10 items-center justify-center border border-stone-200 bg-white/70 text-stone-800 hover:border-stone-900" aria-label="View basket">
                            <ShoppingBag size={19} strokeWidth={1.5} />
                        </Link>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-6 py-12 md:py-20">
                {loading ? (
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 animate-pulse">
                        <div className="aspect-[4/5] bg-stone-100 rounded-sm" />
                        <div className="py-8">
                            <div className="h-3 w-32 bg-stone-100 mb-6" />
                            <div className="h-14 w-3/4 bg-stone-100 mb-5" />
                            <div className="h-6 w-28 bg-stone-100 mb-10" />
                            <div className="space-y-3">
                                <div className="h-4 w-full bg-stone-100" />
                                <div className="h-4 w-5/6 bg-stone-100" />
                                <div className="h-4 w-2/3 bg-stone-100" />
                            </div>
                        </div>
                    </div>
                ) : error ? (
                    <div className="border border-red-100 bg-red-50 px-6 py-12 text-center">
                        <p className="font-serif text-2xl text-red-900 mb-2">Product unavailable</p>
                        <p className="text-sm text-red-700 mb-6">{error}</p>
                        <Link to="/dashboard" className="inline-flex items-center gap-2 text-sm font-medium text-stone-900 border-b border-stone-400 pb-1">
                            <ArrowLeft size={16} strokeWidth={1.5} />
                            Return to collection
                        </Link>
                    </div>
                ) : product && (
                    <div className="space-y-8">
                        <section className="grid grid-cols-1 lg:grid-cols-[minmax(0,1fr)_460px] gap-8 lg:gap-12 items-start bg-white/80 border border-stone-200 p-4 md:p-6">
                            <div>
                                <div className="aspect-[4/5] overflow-hidden rounded-sm bg-stone-100">
                                    <img
                                        src={mediaUrl(activeImage || product.imageUrl)}
                                        alt={product.name}
                                        className="w-full h-full object-cover"
                                    />
                                </div>
                                <div className="grid grid-cols-5 gap-3 mt-3">
                                    {galleryImages.map((image, index) => (
                                        <button
                                            key={`${image}-${index}`}
                                            type="button"
                                            onClick={() => setActiveImage(image)}
                                            className={`aspect-square overflow-hidden border rounded-sm bg-stone-100 ${activeImage === image ? 'border-stone-900' : 'border-stone-200 hover:border-stone-500'}`}
                                        >
                                            <img src={mediaUrl(image)} alt={`${product.name} view ${index + 1}`} className="w-full h-full object-cover" />
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div className="lg:py-2">
                                <div className="flex flex-wrap gap-2 mb-5">
                                    {(product.moodTags || []).filter(Boolean).map((tag) => (
                                        <Link
                                            key={tag}
                                            to={`/shop-by-mood?mood=${encodeURIComponent(tag)}`}
                                            className="border border-stone-200 bg-[#FDFCF8] px-3 py-1 text-[10px] uppercase tracking-widest text-stone-500 hover:border-stone-900 hover:text-stone-900"
                                        >
                                            {tag}
                                        </Link>
                                    ))}
                                </div>

                                <p className="text-xs uppercase tracking-[0.22em] text-stone-500 mb-3">Artisan Arrangement</p>
                                <h1 className="text-4xl md:text-5xl font-serif font-light leading-[0.95] tracking-tight mb-4">
                                    {product.name}
                                </h1>

                                <div className="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm text-stone-500 mb-5">
                                    <span className="flex items-center gap-1 text-stone-600">
                                        <Star size={16} strokeWidth={1.5} className={reviews.totalReviews > 0 ? "fill-current text-stone-800" : ""} />
                                        {reviews.totalReviews > 0 ? `${reviews.averageRating.toFixed(1)} · ${reviews.totalReviews} honest notes` : 'No reviews yet'}
                                    </span>
                                    <span className={product.inStock ? 'text-green-700' : 'text-red-700'}>
                                        {product.inStock ? 'In stock' : 'Out of stock'}
                                    </span>
                                </div>

                                <div className="bg-stone-50 border-y border-stone-200 px-4 py-5 mb-6">
                                    <p className="text-4xl font-serif text-stone-900">{price}</p>
                                    <p className="text-xs text-stone-500 mt-1">Includes arrangement preparation and local shop handling</p>
                                </div>

                                <div className="space-y-4 mb-6 text-sm">
                                    <div className="flex gap-3">
                                        <Truck size={18} strokeWidth={1.5} className="text-stone-500 mt-0.5" />
                                        <div>
                                            <p className="font-medium text-stone-900">Delivery</p>
                                            <p className="text-stone-500">Ships from Cebu City. Choose date and AM/PM slot during checkout.</p>
                                        </div>
                                    </div>
                                    <div className="flex gap-3">
                                        <ShieldCheck size={18} strokeWidth={1.5} className="text-stone-500 mt-0.5" />
                                        <div>
                                            <p className="font-medium text-stone-900">Petal buyer protection</p>
                                            <p className="text-stone-500">Mock payment only for this build. No real card charge is collected.</p>
                                        </div>
                                    </div>
                                </div>

                                <div className="flex items-center gap-4 mb-7">
                                    <span className="text-sm text-stone-500 w-20">Quantity</span>
                                    <div className="inline-flex h-10 border border-stone-200 bg-white">
                                        <button
                                            type="button"
                                            onClick={() => setQuantity((value) => Math.max(1, value - 1))}
                                            className="w-10 flex items-center justify-center text-stone-600 hover:bg-stone-50"
                                        >
                                            <Minus size={14} strokeWidth={1.5} />
                                        </button>
                                        <div className="w-12 flex items-center justify-center border-x border-stone-200 text-sm font-medium">{quantity}</div>
                                        <button
                                            type="button"
                                            onClick={() => setQuantity((value) => Math.min(10, value + 1))}
                                            className="w-10 flex items-center justify-center text-stone-600 hover:bg-stone-50"
                                        >
                                            <Plus size={14} strokeWidth={1.5} />
                                        </button>
                                    </div>
                                </div>

                                <div className="grid grid-cols-1 sm:grid-cols-[1fr_auto] gap-3">
                                    <button
                                        type="button"
                                        onClick={handleAddToCart}
                                        disabled={!product.inStock || addingToCart}
                                        className="inline-flex items-center justify-center gap-2 h-12 px-8 bg-stone-900 text-white text-sm font-medium rounded-sm hover:bg-stone-800 disabled:opacity-50 disabled:cursor-not-allowed"
                                    >
                                        <ShoppingBag size={17} strokeWidth={1.5} />
                                        {addingToCart ? 'Adding...' : product.inStock ? 'Add to Cart' : 'Currently Unavailable'}
                                    </button>
                                    <button type="button" className="inline-flex items-center justify-center gap-2 h-12 px-5 border border-stone-300 text-sm font-medium text-stone-700 hover:border-stone-900 hover:text-stone-900">
                                        <Heart size={17} strokeWidth={1.5} />
                                        Save
                                    </button>
                                </div>
                                {cartMessage && <p className="mt-3 text-sm text-green-700">{cartMessage}</p>}
                                {cartError && <p className="mt-3 text-sm text-red-700">{cartError}</p>}
                            </div>
                        </section>

                        <section className="bg-white/80 border border-stone-200 p-5 md:p-6 flex flex-col md:flex-row md:items-center justify-between gap-5">
                            <div className="flex items-center gap-4">
                                <div className="w-14 h-14 rounded-full bg-stone-100 border border-stone-200 overflow-hidden flex items-center justify-center">
                                    {!floristLogoBroken && product.floristLogoUrl ? (
                                        <img
                                            src={mediaUrl(product.floristLogoUrl)}
                                            alt=""
                                            onError={() => setFloristLogoBroken(true)}
                                            className="h-full w-full object-cover"
                                        />
                                    ) : (
                                        <Store size={24} strokeWidth={1.5} className="text-stone-500" />
                                    )}
                                </div>
                                <div>
                                    <p className="font-serif text-2xl text-stone-900">{product.floristName || 'Local Petal Florist'}</p>
                                    {product.floristBio && (
                                        <p className="mt-1 max-w-xl text-sm text-stone-500 line-clamp-2">{product.floristBio}</p>
                                    )}
                                    <p className="text-sm text-stone-500 flex items-center gap-1 mt-1">
                                        <MapPin size={14} strokeWidth={1.5} />
                                        Cebu, Philippines
                                    </p>
                                </div>
                            </div>
                            <button type="button" disabled className="inline-flex items-center justify-center gap-2 h-10 px-5 border border-stone-200 bg-stone-50 text-sm font-medium text-stone-400 cursor-not-allowed">
                                <MessageCircle size={16} strokeWidth={1.5} />
                                Messaging not enabled
                            </button>
                        </section>

                        <section className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8">
                            <div className="bg-white/80 border border-stone-200 p-6 md:p-8">
                                <h2 className="text-3xl font-serif text-stone-900 mb-5">Product Description</h2>
                                <p className="text-stone-600 leading-relaxed mb-6">{product.description}</p>
                                <p className="text-stone-600 leading-relaxed">
                                    Each Petal arrangement is prepared by a local artisan florist and styled for gifting moments by mood. Colors, foliage density, and wrapping details may vary slightly depending on seasonal availability, while keeping the same emotional tone and overall composition.
                                </p>
                            </div>
                            <div className="bg-white/80 border border-stone-200 p-6 md:p-8">
                                <h2 className="text-3xl font-serif text-stone-900 mb-5">Details</h2>
                                <dl className="space-y-4 text-sm">
                                    <div className="flex justify-between gap-6">
                                        <dt className="text-stone-500">Category</dt>
                                        <dd className="font-medium text-stone-900 text-right">Floral arrangement</dd>
                                    </div>
                                    <div className="flex justify-between gap-6">
                                        <dt className="text-stone-500">Mood</dt>
                                        <dd className="font-medium text-stone-900 text-right">{(product.moodTags || []).filter(Boolean).join(', ') || 'Giftable'}</dd>
                                    </div>
                                    <div className="flex justify-between gap-6">
                                        <dt className="text-stone-500">Delivery area</dt>
                                        <dd className="font-medium text-stone-900 text-right">Cebu City</dd>
                                    </div>
                                    <div className="flex justify-between gap-6">
                                        <dt className="text-stone-500">Time slots</dt>
                                        <dd className="font-medium text-stone-900 text-right">AM / PM</dd>
                                    </div>
                                </dl>
                            </div>
                        </section>

                        <section className="bg-white/80 border border-stone-200 p-6 md:p-8">
                            <h2 className="text-3xl font-serif text-stone-900 mb-6">Honest Notes</h2>
                            {reviews.totalReviews > 0 ? (
                                <div className="space-y-6">
                                    {reviews.recentReviews.map((review) => (
                                        <div key={review.id} className="border-b border-stone-100 pb-6 last:border-0 last:pb-0">
                                            <div className="flex items-center justify-between mb-2">
                                                <span className="font-medium text-sm text-stone-900">{review.reviewerName}</span>
                                                <span className="text-sm font-medium text-stone-600">{review.productRating}/5</span>
                                            </div>
                                            {review.comment && (
                                                <p className="text-stone-600 leading-relaxed">{review.comment}</p>
                                            )}
                                        </div>
                                    ))}
                                    {reviews.totalReviews > reviews.recentReviews.length && (
                                        <button className="text-sm font-medium border-b border-stone-400 pb-0.5 hover:text-stone-600">
                                            Read All Reviews
                                        </button>
                                    )}
                                </div>
                            ) : (
                                <p className="text-stone-500 italic">No notes yet for this arrangement.</p>
                            )}
                        </section>
                    </div>
                )}
            </main>
        </div>
    );
}
