import React, {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import './styles/NewsDetailPage.css';
import NewsTags from "../components/NewsTags";

function NewsDetailPage() {
    const {id} = useParams();
    const [news, setNews] = useState(null);
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [newComment, setNewComment] = useState("");
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("token");

        fetch(`http://localhost:8080/api/v1/news/${id}`, {
            headers: {"Authorization": `Bearer ${token}`},
        })
            .then((res) => res.json())
            .then((data) => {
                setNews(data);
                setLoading(false);
            })
            .catch((err) => console.error("Error loading news:", err));
    }, [id]);

    useEffect(() => {
        const fetchComments = async () => {
            try {
                const token = localStorage.getItem("token");
                const response = await fetch(`http://localhost:8080/api/v1/news/${id}/comments`, {
                    headers: {"Authorization": `Bearer ${token}`}
                });
                if (!response.ok) throw new Error("Ошибка загрузки комментариев");
                const data = await response.json();
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
            const token = localStorage.getItem("token");
            const response = await fetch(`http://localhost:8080/api/v1/comments`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({content: newComment, newsId: news.id}),
            });
            if (!response.ok) throw new Error("Ошибка при добавлении комментария");
            const addedComment = await response.json();
            setComments(prev => [addedComment, ...prev]);
            setNewComment("");
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <p>Loading...</p>;
    if (!news) return <p>News not found</p>;

    return (
        <div className="news-details-container">
            <div className="news-detail">
                <h2 className="news-details-title">{news.title}</h2>
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
