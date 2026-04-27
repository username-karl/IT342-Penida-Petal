import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, ChevronRight, BookOpen, Truck, CreditCard, Star, FileText } from 'lucide-react';
import { useAuth } from '../../auth/context/AuthContext';

export default function SellerEducation() {
    const { logout, user } = useAuth();
    const navigate = useNavigate();
    const [activeSection, setActiveSection] = useState('getting_started');

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const isArtisan = user?.role === 'artisan' || user?.role === 'ARTISAN' || user?.role === 'ROLE_FLORIST';

    return (
        <div className="min-h-screen bg-[#FDFCF8] text-stone-900 selection:bg-stone-200 selection:text-stone-900 font-sans">
            {/* ─── NAVIGATION ─── */}
            <nav className="fixed top-0 w-full z-50 bg-[#FDFCF8]/90 backdrop-blur-md border-b border-stone-200 py-4">
                <div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
                    <Link to="/dashboard" className="text-xl font-serif tracking-tight text-stone-900">
                        Petal
                    </Link>
                    <div className="flex items-center gap-6">
                        <Link to="/seller-centre" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Seller Centre
                        </Link>
                        <Link to="/dashboard" className="text-sm font-medium text-stone-500 hover:text-stone-900 transition-colors">
                            Return to Shop
                        </Link>
                        <button onClick={handleLogout} className="text-sm font-medium text-stone-900 hover:text-red-700 transition-colors">
                            Sign Out
                        </button>
                    </div>
                </div>
            </nav>

            <main className="pt-24 pb-24">
                {/* ─── HEADER & SEARCH ─── */}
                <header className="bg-stone-900 py-16 px-6">
                    <div className="max-w-4xl mx-auto text-center">
                        <h1 className="text-4xl md:text-5xl font-serif text-white mb-6">Artisan Education Hub</h1>
                        <p className="text-stone-300 font-light text-lg mb-8 max-w-2xl mx-auto">Master the art of selling on Petal. Discover guides, best practices, and resources to grow your floral studio.</p>

                        <div className="relative max-w-xl mx-auto">
                            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-stone-400" size={20} strokeWidth={1.5} />
                            <input
                                type="text"
                                placeholder="Search articles, guides, or tutorials..."
                                className="w-full pl-12 pr-4 py-4 rounded-sm border-none bg-white/10 text-white placeholder:text-stone-400 focus:outline-none focus:ring-2 focus:ring-stone-400 backdrop-blur-sm transition-all"
                            />
                        </div>
                    </div>
                </header>

                {/* ─── CONTENT GRID ─── */}
                <div className="max-w-6xl mx-auto px-6 mt-12 grid grid-cols-1 lg:grid-cols-12 gap-12">

                    {/* ─── SIDEBAR NAVIGATION ─── */}
                    <aside className="lg:col-span-3">
                        <div className="sticky top-24">
                            <h3 className="text-xs uppercase tracking-widest text-stone-500 font-bold mb-6 px-4">Categories</h3>
                            <nav className="space-y-1">
                                <button
                                    onClick={() => setActiveSection('getting_started')}
                                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-sm text-sm transition-colors ${activeSection === 'getting_started' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}
                                >
                                    <BookOpen size={16} /> Getting Started
                                </button>
                                <button
                                    onClick={() => setActiveSection('listing')}
                                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-sm text-sm transition-colors ${activeSection === 'listing' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}
                                >
                                    <FileText size={16} /> Curation & Listing
                                </button>
                                <button
                                    onClick={() => setActiveSection('marketing')}
                                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-sm text-sm transition-colors ${activeSection === 'marketing' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}
                                >
                                    <Star size={16} /> Marketing Your Studio
                                </button>
                                <button
                                    onClick={() => setActiveSection('shipping')}
                                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-sm text-sm transition-colors ${activeSection === 'shipping' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}
                                >
                                    <Truck size={16} /> Packaging & Shipping
                                </button>
                                <button
                                    onClick={() => setActiveSection('finance')}
                                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-sm text-sm transition-colors ${activeSection === 'finance' ? 'bg-stone-100 text-stone-900 font-medium' : 'text-stone-600 hover:bg-stone-50'}`}
                                >
                                    <CreditCard size={16} /> Finance & Payouts
                                </button>
                            </nav>
                        </div>
                    </aside>

                    {/* ─── MAIN ARTICLE ─── */}
                    <div className="lg:col-span-9">
                        <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-stone-400 mb-8 border-b border-stone-200 pb-4">
                            <span className="hover:text-stone-900 cursor-pointer transition-colors">Education Hub</span>
                            <ChevronRight size={14} />
                            <span className="hover:text-stone-900 cursor-pointer transition-colors">Getting Started</span>
                            <ChevronRight size={14} />
                            <span className="text-stone-900 font-bold">Selling on Petal</span>
                        </div>

                        <article className="prose prose-stone max-w-none prose-headings:font-serif prose-headings:font-normal prose-h2:text-3xl prose-h3:text-2xl prose-p:font-light prose-p:leading-relaxed prose-p:text-stone-600">
                            <h1 className="text-4xl md:text-5xl font-serif text-stone-900 mb-6">The Definitive Guide to Selling on Petal</h1>
                            <p className="text-xl text-stone-500 font-light mb-12">Welcome to Petal's Artisan community. This guide covers everything you need to know to establish your studio, list your creations, and deliver exceptional floral experiences to our curated audience.</p>

                            <div className="bg-stone-50 border border-stone-200 rounded-sm p-6 mb-12">
                                <h4 className="text-sm font-bold uppercase tracking-wider text-stone-900 mb-4 flex items-center gap-2">
                                    <Star size={16} className="text-stone-900" /> Pro Tip: Your Artisan Aesthetic
                                </h4>
                                <p className="text-stone-600 text-sm leading-relaxed mb-0">Petal buyers seek premium, artisanal, and sustainable floral designs. Ensure your studio name, logo, and product photography reflect a high-end, curated aesthetic. Natural lighting and clean backgrounds (like our signature Petal Ivory) perform best.</p>
                            </div>

                            <h2 className="border-b border-stone-200 pb-2 mb-6">1. Setting up your Studio</h2>
                            <p>Your studio profile is your digital storefront. It tells the story of your craft and sets buyer expectations.</p>
                            <ul className="list-disc pl-5 mb-8 space-y-2 text-stone-600 font-light">
                                <li><strong>Studio Bio:</strong> Write a compelling bio detailing your experience, floral philosophy, and sourcing methods.</li>
                                <li><strong>Fulfillment Settings:</strong> Configure your preparation times accurately. Floral arrangements often require lead time.</li>
                                <li><strong>Coverage Area:</strong> Clearly define your delivery radius or shipping capabilities if you offer nationwide delivery for dried stems.</li>
                            </ul>

                            <h2 className="border-b border-stone-200 pb-2 mb-6">2. The Curation & Listing Process</h2>
                            <p>Listing a product on Petal requires attention to detail. Every product represents the Petal standard of excellence.</p>
                            <ul className="list-disc pl-5 mb-8 space-y-2 text-stone-600 font-light">
                                <li><strong>High-Resolution Imagery:</strong> Upload at least 3 crisp, beautifully styled images per product.</li>
                                <li><strong>Accurate Descriptions:</strong> Include dimensions, expected lifespan (for cuts vs. dried), and care instructions.</li>
                                <li><strong>Categorization:</strong> Tag your products with the correct moods and occasions (e.g., "Sympathy", "Romance", "Wildflower").</li>
                            </ul>

                            <blockquote className="border-l-4 border-stone-900 pl-6 italic text-stone-700 my-10 font-serif text-xl">
                                "Our most successful Artisans treat every delivery not just as a transaction, but as a memorable gifting experience."
                            </blockquote>

                            <h2 className="border-b border-stone-200 pb-2 mb-6">3. Packaging & Quality Standards</h2>
                            <p>The unboxing experience is critical to buyer satisfaction and retention on Petal.</p>
                            <ul className="list-disc pl-5 mb-8 space-y-2 text-stone-600 font-light">
                                <li><strong>Eco-Friendly Materials:</strong> We highly encourage the use of biodegradable or recyclable packaging. Avoiding single-use plastics is a core Petal value.</li>
                                <li><strong>Hydration:</strong> Ensure fresh cuts are shipped with adequate water sources (eco-wraps or small vials) to survive transit.</li>
                                <li><strong>Branding:</strong> Include a personalized care card or a subtle studio tag inside the Petal-standard outer delivery box.</li>
                            </ul>

                            <h2 className="border-b border-stone-200 pb-2 mb-6">4. Receiving Payouts</h2>
                            <p>Petal processes payments securely and ensures Artisans are compensated promptly.</p>
                            <ul className="list-disc pl-5 mb-8 space-y-2 text-stone-600 font-light">
                                <li><strong>Schedule:</strong> Payouts are generated every Tuesday for orders completed in the prior week.</li>
                                <li><strong>Fees:</strong> Petal charges a flat 8% curation fee on successful transactions. No listing fees.</li>
                                <li><strong>Dashboard:</strong> Track your upcoming disbursements directly in the Finance tab of your Seller Centre.</li>
                            </ul>

                        </article>

                        <div className="mt-16 pt-8 border-t border-stone-200 flex justify-between items-center">
                            <div>
                                <p className="text-sm text-stone-500">Was this article helpful?</p>
                                <div className="flex gap-4 mt-2">
                                    <button className="px-4 py-2 border border-stone-200 text-sm font-medium hover:bg-stone-50 transition-colors rounded-sm">Yes</button>
                                    <button className="px-4 py-2 border border-stone-200 text-sm font-medium hover:bg-stone-50 transition-colors rounded-sm">No</button>
                                </div>
                            </div>
                            <button className="text-sm font-medium text-stone-900 hover:text-stone-600 transition-colors flex items-center gap-2">
                                Next Article: Optimize your Listings <ChevronRight size={16} />
                            </button>
                        </div>

                    </div>
                </div>
            </main>
        </div>
    );
}
