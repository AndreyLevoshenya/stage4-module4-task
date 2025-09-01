import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import './styles/Header.css';
import logo from './assets/images/logo.png';

function Header() {
    const navigate = useNavigate();
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("token");
        setIsAuthenticated(!!token);
    }, []);

    const handleSignOut = () => {
        localStorage.removeItem("token");
        setIsAuthenticated(false);
        navigate("/login");
    };

    return (
        <header className="header">
            <div className="logo">
                <img src={logo} alt="Logo" className="logo-img" />
                <div className="logo-text">
                    <span>News</span>
                    <span>Management</span>
                </div>
            </div>

            <nav className="nav">
                <Link to="/">HOME</Link>
                <Link to="/news">NEWS</Link>
                <Link to="/about">ABOUT</Link>
            </nav>

            <div className="auth">
                {isAuthenticated ? (
                    <button className="signout-btn" onClick={handleSignOut}>SIGN OUT</button>
                ) : (
                    <>
                        <button className="signin-btn" onClick={() => navigate("/login")}>SIGN IN</button>
                        <button className="signup-btn" onClick={() => navigate("/register")}>SIGN UP</button>
                    </>
                )}
            </div>
        </header>
    );
}

export default Header;
