import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { saveToken, getToken } from '../services/api'; // Use your existing helper

const OAuth2RedirectHandler = () => {
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        // Extract the ?token=... from the URL
        const params = new URLSearchParams(location.search);
        const token = params.get('token');

        if (token) {
            saveToken(token);
            console.log("Token saved:", getToken()); // add this
            navigate("/notes");
        } else {
            navigate("/login");
        }
    }, [location, navigate]);

    return (
        <div className="login-container">
            <div className="login-card" style={{ textAlign: 'center' }}>
                <h2>Finishing Login...</h2>
                <div className="loader"></div> {/* Optional: Add a CSS spinner here */}
            </div>
        </div>
    );
};

export default OAuth2RedirectHandler;