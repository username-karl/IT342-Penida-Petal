import React from 'react';
import { Plus } from 'lucide-react';

export default function ProductCard({ product }) {
    // Format price if it's a number
    const formattedPrice = typeof product.price === 'number' 
        ? new Intl.NumberFormat('en-PH', { 
            style: 'currency', 
            currency: 'PHP',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
          }).format(product.price)
        : product.price;

    return (
        <div className="group cursor-pointer">
            <div className="relative aspect-[4/5] overflow-hidden bg-stone-100 mb-4 rounded-sm">
                <img
                    src={product.image}
                    alt={product.name}
                    className="w-full h-full object-cover image-hover-zoom"
                />
                <div className="absolute bottom-4 right-4 translate-y-4 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-300">
                    <button className="bg-white/90 backdrop-blur text-stone-900 p-3 rounded-full shadow-sm hover:bg-stone-900 hover:text-white transition-colors">
                        <Plus size={20} strokeWidth={1.5} />
                    </button>
                </div>
                {product.tag && (
                    <div className="absolute top-4 left-4 bg-stone-900 text-white text-[10px] uppercase font-bold px-2 py-1 tracking-wider">
                        {product.tag}
                    </div>
                )}
            </div>
            <div className="flex justify-between items-start">
                <div className="flex-1 mr-4">
                    <p className="text-[10px] uppercase tracking-wider text-stone-400 mb-1">{product.artisan || 'Local Florist'}</p>
                    <h3 className="text-lg font-serif font-medium text-stone-900 leading-tight mb-1 group-hover:underline decoration-stone-300 underline-offset-4">
                        {product.name}
                    </h3>
                    <p className="text-xs text-stone-500 line-clamp-1">{product.subtitle}</p>
                </div>
                <span className="text-sm font-medium text-stone-900 whitespace-nowrap">{formattedPrice}</span>
            </div>
        </div>
    );
}
