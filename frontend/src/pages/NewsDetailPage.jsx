import React, {useEffect, useState} from "react";
import {useLocation, useNavigate, useParams} from "react-router-dom";
import './styles/NewsDetailPage.css';
import NewsTags from "../components/NewsTags";
import NewsActionsPanel from "../components/NewsActionsPanel";
import {useSelector} from "react-redux";
import {api} from "../services/api";
import NotFoundPage from "./NotFoundPage";
import LoadingSpinner from "../components/LoadingSpinner";
import {validateComment} from "../utils/validation";
import {handleError} from "../utils/errorHandler";
import {CONFIG} from "../config/constants"

function NewsDetailPage() {
    const {id} = useParams();
    const [news, setNews] = useState(null);
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [newComment, setNewComment] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [notFound, setNotFound] = useState(false);
    const [commentError, setCommentError] = useState("");

    const navigate = useNavigate();
    const location = useLocation();

    const roles = useSelector((state) => state.auth.roles || []);


    useEffect(() => {
        api.get(`${CONFIG.API.BASE_URL}${CONFIG.API.ENDPOINTS.NEWS}/${id}`)
            .then((data) => {
                setNews(data);
                setLoading(false);
            })
            .catch((err) => {
                handleError(err, {
                    onNotFound: () => {
                        setNotFound(true);
                        setLoading(false);
                    },
                    onGenericError: () => {
                        setError(err.message);
                        setLoading(false);
                    }
                });
            });
    }, [id, navigate, location.pathname]);

    useEffect(() => {
        const fetchComments = async () => {
            try {
                const data = await api.get(`${CONFIG.API.BASE_URL}${CONFIG.API.ENDPOINTS.NEWS}/${id}/comments`);
                setComments(data.content);
            } catch (err) {
                setError(err.message);
            }
        };
        fetchComments();
    }, [id]);

    const handleAddComment = async () => {
        const commentValidation = validateComment(newComment);
        if (commentValidation) {
            setCommentError(commentValidation);
            return;
        }

        setCommentError("");
        setSubmitting(true);
        try {
            const addedComment = await api.post(`${CONFIG.API.BASE_URL}${CONFIG.API.ENDPOINTS.COMMENTS}`, {
                content: newComment,
                newsId: news.id
            });
            setComments(prev => [addedComment, ...prev]);
            setNewComment("");
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <LoadingSpinner text="Loading news..."/>;
    if (notFound) return <NotFoundPage/>;
    if (error) return <p style={{color: 'crimson'}}>{error}</p>;
    if (!news) return <NotFoundPage/>;

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
                    <textarea
                        placeholder="Write a comment..."
                        value={newComment}
                        onChange={(e) => {
                            const value = e.target.value;
                            setNewComment(value);
                            setCommentError(validateComment(value));
                        }}
                    />
                    {commentError && <p className="error-text">{commentError}</p>}
                    <button onClick={handleAddComment}
                            disabled={submitting || !!commentError}>
                        {submitting ? "Sending..." : "Send"}
                    </button>
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
