import {useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
import {useNavigate} from "react-router-dom";
import {loginWithOAuth2} from "../store/slices/authSlice";

function OAuth2Callback() {
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const {isAuthenticated, loading} = useSelector((state) => state.auth);

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const token = params.get("token");

        if (token) {
            dispatch(loginWithOAuth2(token));
        } else {
            navigate("/login?error=true");
        }
    }, [dispatch, navigate]);

    useEffect(() => {
        if (isAuthenticated && !loading) {
            navigate("/news");
        }
    }, [isAuthenticated, loading, navigate]);

    return <p>Loading...</p>;
}

export default OAuth2Callback;
