import React, { useState, useEffect } from "react";
import { Button, Form, Alert } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import AddNewsModal from "../components/AddNewsModal";
import DeleteNewsModal from "../components/DeleteNewsModal";
import NewsCard from "../components/NewsCard";
import "./styles/NewsPage.css";

function NewsPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();

    // --- State ---
    const [news, setNews] = useState([]);
    const [error, setError] = useState("");
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [roles, setRoles] = useState([]);
    const [selectedNews, setSelectedNews] = useState(null);

    // --- State from URL ---
    const [page, setPage] = useState(Number(searchParams.get("page") || 0));
    const [size, setSize] = useState(Number(searchParams.get("size") || 10));
    const [sortField, setSortField] = useState(searchParams.get("sortField") || "createDate");
    const [sort, setSort] = useState(searchParams.get("sort") || "DESC");
    const [search, setSearch] = useState(searchParams.get("search") || "");

    const [showAddModal, setShowAddModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);

    // --- Effects ---
    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            navigate("/login");
            return;
        }

        const decoded = jwtDecode(token);
        setRoles(decoded.authorities);

        fetchNews();
    }, [page, size, sort, search]);

    useEffect(() => {
        const params = {
            page,
            size,
            sortField,
            sort,
        };
        if (search) params.search = search;
        setSearchParams(params);
    }, [page, size, sortField, sort, search, setSearchParams]);

    // --- Fetch ---
    const fetchNews = async () => {
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(
                `http://localhost:8080/api/v1/news?page=${page}&size=${size}&sort=${sortField},${sort}&search=${encodeURIComponent(search)}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            if (!response.ok) throw new Error("Failed to load news");

            const data = await response.json();
            setNews(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
        } catch (err) {
            setError(err.message);
        }
    };

    // --- Handlers ---
    const handleSearchInput = (e) => {
        setSearch(e.target.value);
        setPage(0);
    };

    const handleAddNews = () => setShowAddModal(true);

    const handleEditNews = (newsItem) => {
        setSelectedNews(newsItem);
        setShowEditModal(true);
    };

    const handleDeleteNews = (newsItem) => {
        setSelectedNews(newsItem);
        setShowDeleteModal(true);
    };

    const confirmDeleteNews = async () => {
        try {
            const token = localStorage.getItem("token");
            await fetch(`http://localhost:8080/api/v1/news/${selectedNews.id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` },
            });
            setShowDeleteModal(false);
            fetchNews();
        } catch (err) {
            alert(err.message);
        }
    };

    const handleNewsAdded = async (newsData) => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch("http://localhost:8080/api/v1/news", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(newsData),
            });
            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.message || "Failed to add news");
            }
            setShowAddModal(false);
            fetchNews();
        } catch (err) {
            alert(err.message);
        }
    };

    const handleNewsEdited = async (newsData) => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/v1/news/${selectedNews.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(newsData),
            });
            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.message || "Failed to update news");
            }
            setShowEditModal(false);
            fetchNews();
        } catch (err) {
            alert(err.message);
        }
    };

    const renderPagination = () => {
        const pages = [];
        const neighbors = 2;
        if (page > 0) pages.push(<Button key="first" size="sm" onClick={() => setPage(0)}> &lt;&lt; </Button>);
        if (page > 0) pages.push(<Button key="prev" size="sm" onClick={() => setPage(page - 1)}> &lt; </Button>);

        for (let p = Math.max(0, page - neighbors); p <= Math.min(totalPages - 1, page + neighbors); p++) {
            pages.push(
                <Button
                    key={p}
                    size="sm"
                    variant={p === page ? "primary" : "light"}
                    onClick={() => setPage(p)}
                >
                    {p + 1}
                </Button>
            );
        }

        if (page < totalPages - 1) pages.push(<Button key="next" size="sm" onClick={() => setPage(page + 1)}> &gt; </Button>);
        if (page < totalPages - 1) pages.push(<Button key="last" size="sm" onClick={() => setPage(totalPages - 1)}> &gt;&gt; </Button>);

        return pages;
    };

    // --- Render ---
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
                        <button className="add-news-btn" onClick={handleAddNews}>
                            Add News
                        </button>
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
                    {news.map(n => (
                        <NewsCard
                            key={n.id}
                            newsItem={n}
                            onEdit={handleEditNews}
                            onDelete={handleDeleteNews}
                            isAdmin={roles.includes("ADMIN")}
                        />
                    ))}
                </div>

                <div className="pagination-wrapper">
                    <div className="pagination-buttons">{renderPagination()}</div>
                    <Form.Select
                        size="sm"
                        value={size}
                        onChange={(e) => {
                            setSize(Number(e.target.value));
                            setPage(0);
                        }}
                        className="pagination-size"
                    >
                        <option value={10}>10 / page</option>
                        <option value={20}>20 / page</option>
                        <option value={50}>50 / page</option>
                    </Form.Select>
                </div>
            </div>

            {showAddModal && (
                <AddNewsModal
                    isOpen={showAddModal}
                    onClose={() => setShowAddModal(false)}
                    onSave={handleNewsAdded}
                />
            )}

            {showEditModal && selectedNews && (
                <AddNewsModal
                    isOpen={showEditModal}
                    onClose={() => setShowEditModal(false)}
                    onSave={handleNewsEdited}
                    initialData={{
                        ...selectedNews,
                        tags: selectedNews.tagDtoResponseList
                    }}
                    isEdit
                />
            )}

            {showDeleteModal && selectedNews && (
                <DeleteNewsModal
                    isOpen={showDeleteModal}
                    onClose={() => setShowDeleteModal(false)}
                    onDelete={confirmDeleteNews}
                    newsTitle={selectedNews.title}
                />
            )}
        </div>
    );
}

export default NewsPage;
