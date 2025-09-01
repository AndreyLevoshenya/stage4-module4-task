import React from "react";
import {Pencil, Trash} from "lucide-react";
import "./styles/NewsCard.css";
import {useNavigate} from "react-router-dom";
import NewsTags from "./NewsTags";

function NewsCard({newsItem, onEdit, onDelete, isAdmin}) {
    const navigate = useNavigate();

    const truncateText = (text, maxLength) => {
        if (!text) return "";
        return text.length > maxLength ? text.substring(0, maxLength) + "..." : text;
    };

    return (
        <div className="news-card"
             onClick={() => navigate(`/news/${newsItem.id}`)}>
            <h5>{newsItem.title}</h5>
            <div className="news-date">{new Date(newsItem.createDate).toLocaleDateString()}</div>
            <div className="news-author">{newsItem.authorDtoResponse?.name}</div>
            <p>{truncateText(newsItem.content, 120)}</p>

            <NewsTags tags={newsItem.tagDtoResponseList} />

            {isAdmin && (
                <div className="news-actions">
                    <button onClick={(e) => {
                        e.stopPropagation();
                        onEdit(newsItem);
                    }}>
                        <Pencil size={16}/>
                    </button>
                    <button onClick={(e) => {
                        e.stopPropagation();
                        onDelete(newsItem)
                    }}>
                        <Trash size={16}/>
                    </button>
                </div>
            )}
        </div>
    );
}

export default NewsCard;
