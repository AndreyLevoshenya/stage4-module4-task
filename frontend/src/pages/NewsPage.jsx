import React, { useState, useEffect } from "react";
import { Form, Alert } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useSelector } from "react-redux";
import NewsFormModal from "../components/NewsFormModal";
import NewsCard from "../components/NewsCard";
import "./styles/NewsPage.css";
import PaginationComponent from "../components/PaginationComponent";
import NotFoundPage from "./NotFoundPage";

import { fetchNews, addNews } from "../services/NewsService";

function NewsPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();

    // --- State ---
    const [news, setNews] = useState([]);
    const [error, setError] = useState("");
    const [notFound, setNotFound] = useState(false);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    // --- State from URL ---
    const [page, setPage] = useState(Number(searchParams.get("page") || 0));
    const [size, setSize] = useState(Number(searchParams.get("size") || 10));
    const [sortField, setSortField] = useState(searchParams.get("sortField") || "createDate");
    const [sort, setSort] = useState(searchParams.get("sort") || "DESC");
    const [search, setSearch] = useState(searchParams.get("search") || "");

    const [showAddModal, setShowAddModal] = useState(false);

    // --- Effects ---
    const roles = useSelector((state) => state.auth.roles || []);

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

    // --- Fetch ---
    const loadNews = async () => {
        setError("");
        try {
            const data = await fetchNews({ page, size, sortField, sort, search });
            setNews(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
        } catch (err) {
            if (err.status === 404) {
                setNotFound(true);
            } else {
                setError(err.message);
            }
        }
    };

    // --- Handlers ---
    const handleSearchInput = (e) => {
        setSearch(e.target.value);
        setPage(0);
    };

    const handleAddNews = () => setShowAddModal(true);

    const handleNewsAdded = async (newsData) => {
        try {
            await addNews(newsData);
            setShowAddModal(false);
            loadNews();
        } catch (err) {
            // Prefer backend error message
            alert(err.message);
        }
    };

    // --- Render ---
    if (notFound) {
        return <NotFoundPage />;
    }

    return (
        <div className="news-page">
            <div className="news-container">
                {error && <Alert variant="danger">{error}</Alert>}

                <div className="news-controls">
                    <div className="top-row">
                        <input
                            className="news-search"
                            type="text"
                            placeholder="Search by text or tags (#tag)"
                            value={search}
                            onChange={handleSearchInput}
                        />
                        {roles.includes("ADMIN") && (
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
                                setPage(0);
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
                            isAdmin={roles.includes("ADMIN")}
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
                        setPage(0);
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
