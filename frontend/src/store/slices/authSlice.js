import {createAsyncThunk, createSlice} from "@reduxjs/toolkit";
import { decodeJwtPayload } from "../../utils/jwt";
import { buildApiUrl } from "../../config/constants";

export const loginUser = createAsyncThunk(
    "auth/loginUser",
    async ({ username, password }, thunkAPI) => {
        try {
            const response = await fetch(buildApiUrl("AUTH", "/authenticate"), {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                if (response.status === 401) {
                    return thunkAPI.rejectWithValue("Invalid username or password");
                }
                if (response.status >= 500) {
                    return thunkAPI.rejectWithValue("Server error. Please try again later.");
                }
                return thunkAPI.rejectWithValue("Login failed.");
            }

            return await response.json();
        } catch {
            return thunkAPI.rejectWithValue("Network error. Please check your connection.");
        }
    }
);

export const loginWithOAuth2 = createAsyncThunk(
    "auth/loginWithOAuth2",
    async (token, thunkAPI) => {
        try {
            const payload = decodeJwtPayload(token);
            return { token, payload };
        } catch (e) {
            return thunkAPI.rejectWithValue("Invalid OAuth2 token");
        }
    }
);

export const registerUser = createAsyncThunk(
    "auth/registerUser",
    async ({ firstname, lastname, username, password }, thunkAPI) => {
        try {
            const response = await fetch(buildApiUrl("AUTH", "/register"), {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstname, lastname, username, password }),
            });

            if (!response.ok) {
                if (response.status === 401) {
                    return thunkAPI.rejectWithValue("Invalid registration data");
                }
                if (response.status >= 500) {
                    return thunkAPI.rejectWithValue("Server error. Please try again later.");
                }
                return thunkAPI.rejectWithValue("Registration failed.");
            }

            return await response.json();
        } catch {
            return thunkAPI.rejectWithValue("Network error. Please check your connection.");
        }
    }
);

const token = localStorage.getItem("token");

const authSlice = createSlice({
    name: "auth",
    initialState: {
        token: token || null,
        isAuthenticated: !!token,
        roles: [],
        loading: false,
        error: null,
    },
    reducers: {
        logout: (state) => {
            state.token = null;
            state.isAuthenticated = false;
            state.roles = [];
            localStorage.removeItem("token");
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(loginWithOAuth2.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(loginUser.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(loginUser.fulfilled, (state, action) => {
                state.loading = false;
                state.token = action.payload.token;
                const payload = decodeJwtPayload(action.payload.token);
                state.roles = payload.authorities || [];
                state.isAuthenticated = true;
                localStorage.setItem("token", action.payload.token);
            })
            .addCase(loginUser.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(registerUser.fulfilled, (state, action) => {
                state.loading = false;
                state.token = action.payload.token;
                const payload = decodeJwtPayload(action.payload.token);
                state.roles = payload.authorities || [];
                state.isAuthenticated = true;
                localStorage.setItem("token", action.payload.token);
            })
            .addCase(registerUser.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            })

            .addCase(loginWithOAuth2.fulfilled, (state, action) => {
                state.token = action.payload.token;
                state.roles = action.payload.payload.authorities || [];
                state.isAuthenticated = true;
                state.loading = false;
                localStorage.setItem("token", action.payload.token);
            })
            .addCase(loginWithOAuth2.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    }
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
