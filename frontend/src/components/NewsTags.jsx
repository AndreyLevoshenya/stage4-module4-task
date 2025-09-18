import React from "react";
import "./styles/NewsTags.css";

function NewsTags({tags}) {
    if (!tags || tags.length === 0) return null;

    return (
        <div className="news-tags">
            {tags.map(tag => (
                <span key={tag.id}>#{tag.name}</span>
            ))}
        </div>
    );
}

export default NewsTags;
