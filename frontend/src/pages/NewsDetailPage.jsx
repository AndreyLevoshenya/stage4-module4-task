import React, {useEffect, useState} from "react";
import {useParams, useNavigate, useLocation} from "react-router-dom";
import './styles/NewsDetailPage.css';
import NewsTags from "../components/NewsTags";
import NewsActionsPanel from "../components/NewsActionsPanel";
import {useSelector} from "react-redux";
import { api } from "../services/api";
import NotFoundPage from "./NotFoundPage";

function NewsDetailPage() {
    const {id} = useParams();
    const [news, setNews] = useState(null);
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [newComment, setNewComment] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [notFound, setNotFound] = useState(false);

    const navigate = useNavigate();
    const location = useLocation();

    const roles = useSelector((state) => state.auth.roles || []);

    useEffect(() => {
        api.get(`http://localhost:8080/api/v1/news/${id}`)
            .then((data) => {
                setNews(data);
                setLoading(false);
            })
            .catch((err) => {
                if (err.status === 404) {
                    setNotFound(true);
                    setLoading(false);
                    return;
                }
                setError(err.message);
                setLoading(false);
            });
    }, [id, navigate, location.pathname]);

    useEffect(() => {
        const fetchComments = async () => {
            try {
                const data = await api.get(`http://localhost:8080/api/v1/news/${id}/comments`);
                setComments(data.content);
            } catch (err) {
                setError(err.message);
            }
        };
        fetchComments();
    }, [id]);

    const handleAddComment = async () => {
        if (!newComment.trim()) return;
        setSubmitting(true);
        try {
            const addedComment = await api.post(`http://localhost:8080/api/v1/comments`, { content: newComment, newsId: news.id });
            setComments(prev => [addedComment, ...prev]);
            setNewComment("");
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <p>Loading...</p>;
    if (notFound) return <NotFoundPage />;
    if (error) return <p style={{ color: 'crimson' }}>{error}</p>;
    if (!news) return <NotFoundPage />;

    return (
        <div className="news-details-container">
            {roles.includes("ADMIN") && (
                <NewsActionsPanel
                    newsItem={news}
                    onAfterEdit={(updated) => setNews(updated)}
                    onAfterDelete={() => navigate("/news")}
                />
            )}
            <div className="news-detail">
                <h2 className="news-details-title">{news.title}</h2>
                <span
                    className="news-author-text"
                    onClick={(e) => {
                        e.stopPropagation();
                        navigate(`/news/${news.id}/author`);
                    }}
                >{news.authorDtoResponse?.name}
                </span>
                <p className="news-details-content">{news.content}</p>
                <NewsTags tags={news.tagDtoResponseList}/>
                <h3>Comments</h3>
                <div className="add-comment">
                    <textarea placeholder="Write a comment..." value={newComment}
                              onChange={(e) => setNewComment(e.target.value)}/>
                    <button onClick={handleAddComment}
                            disabled={submitting}> {submitting ? "Sending..." : "Send"} </button>
                </div>
                {comments.length === 0 ? (<p>No comments yet</p>) : (comments.map((comment) => (
                    <div key={comment.id} className="comment">
                        <p className="comment-text">{comment.content}</p>
                        <span className="comment-date"> {new Date(comment.createDate).toLocaleString()} </span>
                    </div>)))}
            </div>
        </div>
    )
}

export default NewsDetailPage;
