import React from "react";
import "./styles/NewsCard.css";
import {useNavigate} from "react-router-dom";
import NewsTags from "./NewsTags";
import NewsActionsPanel from "./NewsActionsPanel";

function NewsCard({ newsItem, isAdmin, onAfterEdit, onAfterDelete }) {
    const navigate = useNavigate();

    const truncateText = (text, maxLength) => {
        if (!text) return "";
        return text.length > maxLength ? text.substring(0, maxLength) + "..." : text;
    };

    return (<div className="news-card"
                 onClick={() => navigate(`/news/${newsItem.id}`)}>
            <h5>{newsItem.title}</h5>
            <div className="news-date">{new Date(newsItem.createDate).toLocaleDateString()}</div>
            <div className="news-author">
            <span
                className="news-author-text"
                onClick={(e) => {
                    e.stopPropagation();
                    navigate(`/news/${newsItem.id}/author`);
                }}
            >{newsItem.authorDtoResponse?.name}
            </span>
            </div>
            <p>{truncateText(newsItem.content, 120)}</p>

            <NewsTags tags={newsItem.tagDtoResponseList}/>

            {isAdmin && (
                <NewsActionsPanel
                    newsItem={newsItem}
                    onAfterEdit={onAfterEdit}
                    onAfterDelete={onAfterDelete}
                />
            )}
        </div>
    );
}

export default NewsCard;
