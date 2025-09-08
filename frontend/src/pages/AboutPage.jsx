import React from "react";
import "./styles/AboutPage.css";

function AboutPage() {
    return (
        <div className="about-container">
            <div className="about-card">
                <h2 className="about-title">About this project</h2>
                <p className="about-text">
                    This is a React-based News Management UI built as part of the MJC School
                    Modern UI module. It demonstrates routing, authentication with JWT,
                    CRUD operations for news and comments, pagination, sorting, searching by
                    text and tags, and error handling integrated with a Spring Boot backend.
                </p>

                <h3 className="about-subtitle">Tech stack</h3>
                <ul className="about-list">
                    <li>React (functional components)</li>
                    <li>React Router</li>
                    <li>Redux Toolkit (auth state)</li>
                    <li>Bootstrap / custom CSS</li>
                    <li>Fetch API with a centralized helper</li>
                </ul>

                <h3 className="about-subtitle">Key features</h3>
                <ul className="about-list">
                    <li>Authentication and protected routes</li>
                    <li>News list with pagination and sorting</li>
                    <li>Search with tags and text</li>
                    <li>Create/Edit news in a single modal form</li>
                    <li>Comments for a news item</li>
                    <li>Graceful error pages and messages</li>
                </ul>

                <p className="about-footnote">
                    For more info, see project README inside the repository.
                </p>
            </div>
        </div>
    );
}

export default AboutPage;


