import React from "react";
import { Link, useLocation } from "react-router-dom";
import "./styles/NotFoundPage.css";

function NotFoundPage() {
    const location = useLocation();
    const originalPath = location.state?.from || location.pathname;
    return (
        <div className="notfound-container">
            <div className="notfound-card">
                <h2 className="notfound-title">Page not found</h2>
                <p className="notfound-text">We couldn't find the page "{originalPath}".</p>
                <Link className="notfound-link" to="/news">Go to News</Link>
            </div>
        </div>
    );
}

export default NotFoundPage;
