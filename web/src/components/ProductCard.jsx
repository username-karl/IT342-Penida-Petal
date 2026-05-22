import React, { useState } from 'react';
import { Plus, Store } from 'lucide-react';
import { Link } from 'react-router-dom';
import { mediaUrl } from '../services/api';

export default function ProductCard({ product }) {
    const [logoBroken, setLogoBroken] = useState(false);
    const image = product.image || product.imageUrl;
    const artisan = product.artisan || product.floristName;
    const subtitle = product.subtitle || product.description;
    const price = typeof product.price === 'number' ? `$${product.price.toFixed(2)}` : product.price;
    const logo = !logoBroken ? mediaUrl(product.floristLogoUrl) : '';

    return (
        <Link to={`/products/${product.id}`} className="group block" aria-label={`View ${product.name}`}>
            <div className="relative aspect-[4/5] overflow-hidden bg-stone-100 mb-4 rounded-sm">
                <img
                    src={image}
                    alt={product.name}
                    className="w-full h-full object-cover image-hover-zoom"
                />
                <div className="absolute bottom-4 right-4 translate-y-4 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-300">
                    <span className="block bg-white/90 backdrop-blur text-stone-900 p-3 rounded-full shadow-sm group-hover:bg-stone-900 group-hover:text-white transition-colors">
                        <Plus size={20} strokeWidth={1.5} />
                    </span>
                </div>
                {product.tag && (
                    <div className="absolute top-4 left-4 bg-stone-900 text-white text-[10px] uppercase font-bold px-2 py-1 tracking-wider">
                        {product.tag}
                    </div>
                )}
            </div>
            <div className="flex justify-between items-start">
                <div className="min-w-0 pr-3">
                    <div className="mb-2 flex items-center gap-2">
                        <span className="h-7 w-7 shrink-0 overflow-hidden rounded-full border border-stone-200 bg-stone-100 flex items-center justify-center">
                            {logo ? (
                                <img src={logo} alt="" onError={() => setLogoBroken(true)} className="h-full w-full object-cover" />
                            ) : (
                                <Store size={13} strokeWidth={1.5} className="text-stone-500" />
                            )}
                        </span>
                        <p className="truncate text-[10px] uppercase tracking-wider text-stone-400">{artisan}</p>
                    </div>
                    <h3 className="text-lg font-serif font-medium text-stone-900 leading-none mb-1 group-hover:underline decoration-stone-300 underline-offset-4">
                        {product.name}
                    </h3>
                    <p className="text-xs text-stone-500 line-clamp-2">{subtitle}</p>
                </div>
                <span className="text-sm font-medium text-stone-900">{price}</span>
            </div>
        </Link>
    );
}
