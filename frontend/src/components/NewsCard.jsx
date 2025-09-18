import React, {useCallback, useMemo} from "react";
import "./styles/NewsCard.css";
import {useNavigate} from "react-router-dom";
import NewsTags from "./NewsTags";
import NewsActionsPanel from "./NewsActionsPanel";

function NewsCard({newsItem, isAdmin, onAfterEdit, onAfterDelete}) {
    const navigate = useNavigate();

    const truncateText = useCallback((text, maxLength) => {
        if (!text) return "";
        return text.length > maxLength ? text.substring(0, maxLength) + "..." : text;
    }, []);

    const handleCardClick = useCallback(() => {
        navigate(`/news/${newsItem.id}`);
    }, [navigate, newsItem.id]);

    const handleAuthorClick = useCallback((e) => {
        e.stopPropagation();
        navigate(`/news/${newsItem.id}/author`);
    }, [navigate, newsItem.id]);

    const truncatedContent = useMemo(() =>
            truncateText(newsItem.content, 120),
        [newsItem.content, truncateText]
    );

    const formattedDate = useMemo(() =>
            new Date(newsItem.createDate).toLocaleDateString(),
        [newsItem.createDate]
    );

    return (<div className="news-card" onClick={handleCardClick}>
            <h5>{newsItem.title}</h5>
            <div className="news-date">{formattedDate}</div>
            <div className="news-author">
            <span
                className="news-author-text"
                onClick={handleAuthorClick}
            >{newsItem.authorDtoResponse?.name}
            </span>
            </div>
            <p>{truncatedContent}</p>

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
