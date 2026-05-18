import { useState } from "react";
import { registerAPI } from "../services/api";
import { useNavigate } from "react-router-dom";
import "./RegisterPage.css";

function RegisterPage() {

  const [form, setForm] = useState({
    username: '',
    email: '',
    password: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  const validate = () => {
    let newErrors = {};

    if (!form.username.trim()) newErrors.username = "Username is required";
    if (!form.email.trim()) newErrors.email = "Email is required";
    if (!form.email.includes("@")) newErrors.email = "Valid email required";
    if (!form.password.trim()) newErrors.password = "Password is required";
    if (form.password.length < 6) newErrors.password = "Min 6 characters";

    return newErrors;
  };

  const handleRegister = async () => {
    const validationErrors = validate();

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    try {
      setLoading(true);
      setErrors({});
      
      await registerAPI(form.username, form.email, form.password);
      
      navigate("/login");

    } catch (err) {
      const backendErrors = err.response?.data?.errors;

      if (backendErrors) {
        setErrors(backendErrors);
      } else {
        setErrors({ general: "Registration failed. Please try again." });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-container">

      <div className="register-card">
        <h2>Create Account 🚀</h2>

        {/* Username */}
        <input
          name="username"
          className="input"
          type="text"
          placeholder="Username"
          value={form.username}
          onChange={handleChange}
        />

        {errors.username && <p className="error">{errors.username}</p>}

        {/* Email */}
        <input
          name="email"
          className="input"
          type="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
        />
        {errors.email && <p className="error">{errors.email}</p>}

        {/* Password */}
        <div className="password-box">
          <input
            name="password"
            className="input"
            type={showPassword ? "text" : "password"}
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
          />
          <span onClick={() => setShowPassword(!showPassword)}>
            {showPassword ? "Hide" : "Show"}
          </span>
        </div>

        {errors.password && <p className="error">{errors.password}</p>}

        {/* General Error */}
        {errors.general && <p className="error">{errors.general}</p>}

        <button 
          className="register-btn" 
          onClick={handleRegister}
          disabled={loading}>
          {loading ? "Registering..." : "Register"}
        </button>

        <p className="login-link">
          Already have an account?{" "}<span onClick={() => navigate("/login")}>Login</span>
        </p>
      </div>

    </div>
  );
}

export default RegisterPage;