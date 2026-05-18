import { useState } from "react";
import { loginAPI, saveToken } from "../services/api";
import { useNavigate } from "react-router-dom";
import "./LoginPage.css";

function LoginPage() {
    const [form, setForm] = useState({
        username: "",
        password: ""
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    // Simple validation
    const validate = () => {
        let newErrors = {};
        if (!form.username.trim()) newErrors.username = "Username required";
        if (!form.password.trim()) newErrors.password = "Password required";
        return newErrors;
    };

    const handleLogin = async () => {

        const validationErrors = validate();

        if (Object.keys(validationErrors).length > 0) {
            setError(validationErrors);
            return;
        }

        try {
            setLoading(true);
            setError({});

            const data = await loginAPI(form.username, form.password);
            console.log("Login response:", data); // ← add this
            saveToken(data.token);

            navigate("/notes");

        } catch (error) {
            console.log("FULL ERROR:", error);
            console.log("ERROR MESSAGE:", error.message);
            setError("Invalid username or password");

        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h2>Welcome Back 👋</h2>

                {/*Username*/}
                <input
                    className="input"
                    name="username"
                    type="text"
                    placeholder="Username"
                    value={form.username}
                    onChange={handleChange}
                />
                {error.username && <p className="error">{error.username}</p>}

                {/*Password*/}
                <div className="password-box">
                    <input
                        className="input"
                        name="password"
                        type={showPassword ? "text" : "password"}
                        placeholder="Password"
                        value={form.password}
                        onChange={handleChange}
                    />
                    <span onClick={() => setShowPassword(!showPassword)}>
                        {showPassword ? "Hide" : "Show"}
                    </span>
                </div>
                {/*error*/}
                {error.password && <p className="error">{error.password}</p>}
                {error && typeof error === "string" && <p className="error">{error}</p>}
                {/*LoginButton*/}
                <button
                    className="login-btn"
                    onClick={handleLogin}
                    disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>

                <p className="register-link">
                    Don't have an account? <span onClick={() => navigate("/register")}>Register</span>
                </p>
                <div className="divider"><span>OR</span></div>

                <button
                    className="google-btn"
                    onClick={() => window.location.href = "http://localhost:8080/oauth2/authorization/google"}
                >
                    <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" width="20" />
                    Continue with Google
                </button>
            </div>

        </div>
    )
}

export default LoginPage;