import React, { useState, useEffect, useCallback, useMemo } from "react";
import { Form, Alert } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useSelector } from "react-redux";
import NewsFormModal from "../components/NewsFormModal";
import NewsCard from "../components/NewsCard";
import "./styles/NewsPage.css";
import PaginationComponent from "../components/PaginationComponent";
import NotFoundPage from "./NotFoundPage";
import LoadingSpinner from "../components/LoadingSpinner";
import { showError } from "../utils/notifications";
import { CONFIG } from "../config/constants";

import { fetchNews, addNews } from "../services/NewsService";

function NewsPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();

    const [news, setNews] = useState([]);
    const [error, setError] = useState("");
    const [notFound, setNotFound] = useState(false);
    const [loading, setLoading] = useState(true);
    const [searchLoading, setSearchLoading] = useState(false);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [page, setPage] = useState(Number(searchParams.get("page") || CONFIG.UI.PAGINATION.DEFAULT_PAGE));
    const [size, setSize] = useState(Number(searchParams.get("size") || CONFIG.UI.PAGINATION.DEFAULT_SIZE));
    const [sortField, setSortField] = useState(searchParams.get("sortField") || "createDate");
    const [sort, setSort] = useState(searchParams.get("sort") || "DESC");
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [searchInput, setSearchInput] = useState(searchParams.get("search") || "");

    const [showAddModal, setShowAddModal] = useState(false);

    const roles = useSelector((state) => state.auth.roles || []);
    const isAdmin = useMemo(() => roles.includes("ADMIN"), [roles]);

    useEffect(() => {
        const timer = setTimeout(() => {
            if (searchInput !== search) {
                setSearchLoading(true);
                setSearch(searchInput);
                setPage(CONFIG.UI.PAGINATION.DEFAULT_PAGE);
            }
        }, CONFIG.UI.DEBOUNCE_DELAY);

        return () => clearTimeout(timer);
    }, [searchInput, search]);

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            navigate("/login");
            return;
        }
        loadNews();
    }, [page, size, sort, search]);

    useEffect(() => {
        const params = { page, size, sortField, sort };
        if (search) params.search = search;
        setSearchParams(params);
    }, [page, size, sortField, sort, search, setSearchParams]);

    const loadNews = useCallback(async () => {
        setError("");
        if (page === CONFIG.UI.PAGINATION.DEFAULT_PAGE && size === CONFIG.UI.PAGINATION.DEFAULT_SIZE && sortField === "createDate" && sort === "DESC" && !search) {
            setLoading(true);
        } else {
            setSearchLoading(true);
        }
        
        try {
            const data = await fetchNews({ page, size, sortField, sort, search });
            setNews(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
        } catch (err) {
            if (err.status === 404 || err.errorCode === "000001") {
                setNotFound(true);
            } else {
                setError(err.message);
            }
        } finally {
            setLoading(false);
            setSearchLoading(false);
        }
    }, [page, size, sortField, sort, search]);

    const handleSearchInput = useCallback((e) => {
        setSearchInput(e.target.value);
    }, []);

    const handleAddNews = useCallback(() => setShowAddModal(true), []);

    const handleNewsAdded = useCallback(async (newsData) => {
        try {
            await addNews(newsData);
            setShowAddModal(false);
            loadNews();
        } catch (err) {
            showError(err.message);
        }
    }, [loadNews]);

    if (loading) {
        return <LoadingSpinner text="Loading news..." />;
    }

    if (notFound) {
        return <NotFoundPage />;
    }

    return (
        <div className="news-page">
            <div className="news-container">
                {error && <Alert variant="danger">{error}</Alert>}

                <div className="news-controls">
                    <div className="top-row">
                        <div className="search-container">
                            <input
                                className="news-search"
                                type="text"
                                placeholder="Search by text or tags (#tag)"
                                value={searchInput}
                                onChange={handleSearchInput}
                            />
                            {searchLoading && (
                                <div className="search-spinner">
                                    <div className="spinner"></div>
                                </div>
                            )}
                        </div>
                        {(
                            <button className="add-news-btn" onClick={handleAddNews}>
                                Add News
                            </button>
                        )}
                    </div>

                    <div className="sort-row">
                        <Form.Select
                            size="sm"
                            value={`${sortField}_${sort}`}
                            onChange={(e) => {
                                const [field, direction] = e.target.value.split("_");
                                setSortField(field);
                                setSort(direction);
                                setPage(CONFIG.UI.PAGINATION.DEFAULT_PAGE);
                            }}
                            className="sort-select"
                        >
                            <option value="createDate_DESC">Date Descending</option>
                            <option value="createDate_ASC">Date Ascending</option>
                            <option value="title_DESC">Title Descending</option>
                            <option value="title_ASC">Title Ascending</option>
                        </Form.Select>
                    </div>
                </div>

                <div className="news-count">Total news: {totalElements}</div>

                <div className="news-cards">
                    {news.map((n) => (
                        <NewsCard
                            key={n.id}
                            newsItem={n}
                            isAdmin={isAdmin}
                            onAfterEdit={loadNews}
                            onAfterDelete={loadNews}
                        />
                    ))}
                </div>

                <PaginationComponent
                    page={page}
                    size={size}
                    totalPages={totalPages}
                    onPageChange={(p) => setPage(p)}
                    onSizeChange={(s) => {
                        setSize(s);
                        setPage(CONFIG.UI.PAGINATION.DEFAULT_PAGE);
                    }}
                />
            </div>

            {showAddModal && (
                <NewsFormModal
                    isOpen={showAddModal}
                    onClose={() => setShowAddModal(false)}
                    onSave={handleNewsAdded}
                />
            )}
        </div>
    );
}

export default NewsPage;
