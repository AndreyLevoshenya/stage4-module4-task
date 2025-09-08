import React, { useEffect, useState } from "react";
import {useLocation, useParams} from "react-router-dom";
import NewsCard from "../components/NewsCard";
import "./styles/AuthorPage.css";
import {useSelector} from "react-redux";
import { api } from "../services/api";
import NotFoundPage from "./NotFoundPage";

function AuthorPage() {
    const { id } = useParams();
    const [author, setAuthor] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [notFound, setNotFound] = useState(false);

    const location = useLocation();

    const roles = useSelector((state) => state.auth.roles || []);

    useEffect(() => {
        let url;

        if (location.pathname.startsWith("/news/")) {
            url = `http://localhost:8080/api/v1/news/${id}/author`;
        } else {
            url = `http://localhost:8080/api/v1/authors/${id}`;
        }

        api.get(url)
            .then((data) => {
                setAuthor(data);
                setLoading(false);
            })
            .catch((err) => {
                if (err.status === 404) {
                    setNotFound(true);
                } else {
                    setError(err.message);
                }
                setLoading(false);
            });
    }, [id]);

    if (loading) return <p>Loading...</p>;
    if (notFound) return <NotFoundPage />;
    if (error) return <p style={{ color: 'crimson' }}>Error: {error}</p>;

    return (
        <div className="author-page">
            <h2>{author.name}</h2>
            <p><strong>Create Date:</strong> {new Date(author.createDate).toLocaleDateString()}</p>
            <p><strong>Last Update Date:</strong> {new Date(author.lastUpdateDate).toLocaleDateString()}</p>

            <h3>More news of this author</h3>
            {author.newsDtoResponseList && author.newsDtoResponseList.length > 0 ? (
                <div className="author-news-list">
                    {author.newsDtoResponseList.map(newsItem => (
                        <NewsCard
                            key={newsItem.id}
                            newsItem={newsItem}
                            onEdit={null}
                            onDelete={null}
                            isAdmin={roles.includes("ADMIN")} />
                    ))}
                </div>
            ) : (
                <p>This author hasn't got any news.</p>
            )}
        </div>
    );
}

export default AuthorPage;
