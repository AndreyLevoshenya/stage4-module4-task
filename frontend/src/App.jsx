import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import NewsPage from "./pages/NewsPage";
import NewsDetailPage  from "./pages/NewsDetailPage";
import Layout from "./components/Layout";
import './App.css';
import RegisterPage from "./pages/RegisterPage";

function App() {
    const isAuthenticated = !!localStorage.getItem("token");

    return (
        <Router>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                <Route
                    path="/"
                    element={isAuthenticated ? <Layout /> : <Navigate to="/login" />}
                >
                    <Route path="news" element={<NewsPage />} />
                    <Route path="/news/:id" element={<NewsDetailPage />} />
                    {/* Можешь добавить другие, например About */}
                    {/* <Route path="about" element={<AboutPage />} /> */}
                </Route>

                <Route path="*" element={<Navigate to="/login" />} />
            </Routes>
        </Router>
    );
}

export default App;
