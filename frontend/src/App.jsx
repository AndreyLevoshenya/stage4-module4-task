import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import LoginPage from "./pages/LoginPage";
import NewsPage from "./pages/NewsPage";
import NewsDetailPage  from "./pages/NewsDetailPage";
import Layout from "./components/Layout";
import './App.css';
import './styles/global.css';
import RegisterPage from "./pages/RegisterPage";
import AuthorPage from "./pages/AuthorPage";
import NotFoundPage from "./pages/NotFoundPage";
import AboutPage from "./pages/AboutPage";
import HomePage from "./pages/HomePage";
import OAuthCallbackPage from "./pages/OAuth2CallbackPage";
import PublicLayout from "./components/PublicLayout";

function App() {
    const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);

    return (
        <Router>
            <Routes>
                <Route element={<PublicLayout />}>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                </Route>

                <Route path="/oauth2/callback" element={<OAuthCallbackPage />} />

                <Route
                    path="/"
                    element={isAuthenticated ? <Layout /> : <Navigate to="/login" />}
                >
                    <Route index element={<HomePage />} />
                    <Route path="/news" element={<NewsPage />} />
                    <Route path="/news/:id" element={<NewsDetailPage />} />
                    <Route path="/authors/:id" element={<AuthorPage />} />
                    <Route path="/news/:id/author" element={<AuthorPage />} />
                    <Route path="/about" element={<AboutPage />} />
                </Route>

                <Route path="/not-found" element={<NotFoundPage />} />
                <Route path="*" element={<NotFoundPage />} />
            </Routes>
        </Router>
    );
}

export default App;
