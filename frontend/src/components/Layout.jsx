import React from "react";
import Header from "./Header";
import {Outlet} from "react-router-dom";
import Footer from "./Footer";
import './styles/Layout.css';

function Layout() {
    return (
        <div className="layout-container">
            <Header/>
            <div className="layout-content">
                <Outlet/>
            </div>
            <Footer/>
        </div>
    );
}

export default Layout;