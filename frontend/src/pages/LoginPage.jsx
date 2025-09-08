import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Form, Button, Alert } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import { loginUser } from "../store/slices/authSlice";
import "./styles/AuthPage.css";

function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [usernameError, setUsernameError] = useState("");
    const [passwordError, setPasswordError] = useState("");

    const dispatch = useDispatch();
    const navigate = useNavigate();

    const { loading, error, isAuthenticated } = useSelector((state) => state.auth);

    const validateUsername = (value) => {
        if (!value.trim()) return "Username cannot be blank";
        if (value.length < 3 || value.length > 30) return "Username must be 3 to 30 characters long";
        return "";
    };

    const validatePassword = (value) => {
        if (!value.trim()) return "Password cannot be blank";
        if (value.length < 4 || value.length > 30) return "Password must be 4 to 30 characters long";
        return "";
    };

    const handleLogin = async (e) => {
        e.preventDefault();

        const uError = validateUsername(username);
        const pError = validatePassword(password);

        setUsernameError(uError);
        setPasswordError(pError);

        if (uError || pError) return;

        const result = await dispatch(loginUser({ username, password }));

        if (result.meta.requestStatus === "fulfilled") {
            navigate("/news");
        }
    };

    return (
        <div className="auth-page">
            <Container className="auth-container">
                <h2>Login</h2>
                {error && <Alert variant="danger">{error}</Alert>}
                <Form onSubmit={handleLogin}>
                    <Form.Group controlId="username" className="form-group">
                        <Form.Control
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => {
                                setUsername(e.target.value);
                                setUsernameError(validateUsername(e.target.value));
                            }}
                            isInvalid={!!usernameError}
                            autoFocus
                        />
                        <Form.Control.Feedback type="invalid">
                            {usernameError}
                        </Form.Control.Feedback>
                    </Form.Group>

                    <Form.Group controlId="password" className="form-group">
                        <Form.Control
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => {
                                setPassword(e.target.value);
                                setPasswordError(validatePassword(e.target.value));
                            }}
                            isInvalid={!!passwordError}
                        />
                        <Form.Control.Feedback type="invalid">
                            {passwordError}
                        </Form.Control.Feedback>
                    </Form.Group>

                    <Button type="submit" className="btn-primary" disabled={loading}>
                        {loading ? "Signing in..." : "Sign In"}
                    </Button>
                </Form>
            </Container>
        </div>
    );
}

export default LoginPage;
