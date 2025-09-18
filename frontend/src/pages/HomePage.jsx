import React from "react";
import {Link} from "react-router-dom";
import "./styles/HomePage.css";

function HomePage() {
    return (
        <div className="home-container">
            <div className="home-hero">
                <h1 className="home-title">Welcome to News Management</h1>
                <p className="home-subtitle">Manage news, authors and comments with a clean, modern UI.</p>
                <div className="home-actions">
                    <Link className="home-btn primary" to="/news">Go to News</Link>
                    <Link className="home-btn" to="/about">Learn more</Link>
                </div>
            </div>

            <div className="home-features">
                <div className="feature-card">
                    <h3>Fast search</h3>
                    <p>Filter by text and tags to quickly find what you need.</p>
                </div>
                <div className="feature-card">
                    <h3>Simple editing</h3>
                    <p>Create and update news in a convenient modal.</p>
                </div>
                <div className="feature-card">
                    <h3>Secure access</h3>
                    <p>JWT-based authentication protects your data and routes.</p>
                </div>
            </div>
        </div>
    );
}

export default HomePage;
