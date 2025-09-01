import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Form, Button, Alert } from "react-bootstrap";
import Header from "../components/Header";
import Footer from "../components/Footer";
import './styles/RegisterPage.css';

function RegisterPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [firstname, setFirstname] = useState("");
    const [lastname, setLastname] = useState("");
    const [error, setError] = useState("");
    const [firstnameError, setFirstnameError] = useState("");
    const [lastnameError, setLastnameError] = useState("");
    const [usernameError, setUsernameError] = useState("");
    const [passwordError, setPasswordError] = useState("");
    const navigate = useNavigate();

    const validateFirstname = (value) => {
        if (!value.trim()) return "Firstname cannot be blank";
        if (value.length < 3 || value.length > 32) return "Firstname must be 3 to 32 characters long";
        return "";
    };
    const validateLastname = (value) => {
        if (!value.trim()) return "Lastname cannot be blank";
        if (value.length < 3 || value.length > 32) return "Lastname must be 3 to 32 characters long";
        return "";
    };

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

    const handleRegister = async (e) => {
        e.preventDefault();

        const uError = validateUsername(username);
        const pError = validatePassword(password);

        setUsernameError(uError);
        setPasswordError(pError);

        if (uError || pError) return;

        try {
            const response = await fetch("http://localhost:8080/api/v1/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstname, lastname, username, password }),
            });

            if (!response.ok) {
                if (response.status === 401) setError("Invalid data");
                else if (response.status >= 500) setError("Server error. Please try again later.");
                else setError("Register failed.");
                return;
            }

            const data = await response.json();
            localStorage.setItem("token", data.token);
            navigate("/news");
        } catch {
            setError("Network error. Please check your connection.");
        }
    };

    return (
        <div className="register-page">
            <Header />
            <Container className="register-container">
                <h2>Register</h2>
                {error && <Alert variant="danger">{error}</Alert>}
                <Form onSubmit={handleRegister}>
                    <Form.Group controlId="firstname" className="form-group">
                        <Form.Control
                            type="text"
                            placeholder="Firstname"
                            value={firstname}
                            onChange={(e) => {
                                setFirstname(e.target.value);
                                setFirstnameError(validateFirstname(e.target.value));
                            }}
                            isInvalid={!!firstnameError}
                            autoFocus
                        />
                        <Form.Control.Feedback type="invalid">
                            {firstnameError}
                        </Form.Control.Feedback>
                    </Form.Group>
                    <Form.Group controlId="lastname" className="form-group">
                        <Form.Control
                            type="text"
                            placeholder="Lastname"
                            value={lastname}
                            onChange={(e) => {
                                setLastname(e.target.value);
                                setLastnameError(validateLastname(e.target.value));
                            }}
                            isInvalid={!!lastnameError}
                            autoFocus
                        />
                        <Form.Control.Feedback type="invalid">
                            {lastnameError}
                        </Form.Control.Feedback>
                    </Form.Group>
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

                    <Button type="submit" className="btn-primary">
                        Sign In
                    </Button>
                </Form>
            </Container>
            <Footer />
        </div>
    );
}

export default RegisterPage;
