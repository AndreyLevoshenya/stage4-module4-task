import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import {Alert, Button, Form} from "react-bootstrap";
import {useDispatch, useSelector} from "react-redux";
import {registerUser} from "../store/slices/authSlice";
import {validateFirstname, validateLastname, validatePassword, validateUsername} from "../utils/validation";
import "./styles/AuthPage.css";
import {googleOAuth2} from "../services/oauthService";

function RegisterPage() {
    const [firstname, setFirstname] = useState("");
    const [lastname, setLastname] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [firstnameError, setFirstnameError] = useState("");
    const [lastnameError, setLastnameError] = useState("");
    const [usernameError, setUsernameError] = useState("");
    const [passwordError, setPasswordError] = useState("");

    const dispatch = useDispatch();
    const navigate = useNavigate();

    const {loading, error} = useSelector((state) => state.auth);


    const handleRegister = async (e) => {
        e.preventDefault();

        const fError = validateFirstname(firstname);
        const lError = validateLastname(lastname);
        const uError = validateUsername(username);
        const pError = validatePassword(password);

        setFirstnameError(fError);
        setLastnameError(lError);
        setUsernameError(uError);
        setPasswordError(pError);

        if (fError || lError || uError || pError) return;

        const result = await dispatch(registerUser({firstname, lastname, username, password}));

        if (result.meta.requestStatus === "fulfilled") {
            navigate("/news");
        }
    };

    const handleGoogleLogin = () => {
        googleOAuth2.redirectToBackend();
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
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
                        <Form.Control.Feedback type="invalid">{firstnameError}</Form.Control.Feedback>
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
                        />
                        <Form.Control.Feedback type="invalid">{lastnameError}</Form.Control.Feedback>
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
                        />
                        <Form.Control.Feedback type="invalid">{usernameError}</Form.Control.Feedback>
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
                        <Form.Control.Feedback type="invalid">{passwordError}</Form.Control.Feedback>
                    </Form.Group>

                    <Button type="submit" className="btn-primary" disabled={loading}>
                        {loading ? "Registering..." : "Register"}
                    </Button>

                    <div className="auth-divider"><span>or</span></div>

                    <Button type="button" className="btn-google" onClick={handleGoogleLogin} disabled={loading}>
                        Continue with Google
                    </Button>
                </Form>
            </div>
        </div>
    );
}

export default RegisterPage;
