import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import NotesPage from "./pages/NotesPage";
import OAuth2RedirectHandler from "./components/OAuth2RedirectHandler";
import { getToken } from "./services/api";
import "./index.css";

const ProtectedRoute = ({ children }) => {
  return getToken() ? children : <Navigate to="/" />
};

function App() {

  return (
      <BrowserRouter>
          <Routes>
            <Route path='/' element={<Navigate to="/login" />} />
            <Route path='/login' element={<LoginPage/>} />
            <Route path='/register' element={<RegisterPage/>} />
            <Route path='/oauth2/redirect' element={<OAuth2RedirectHandler />} />
            <Route path='/notes' element={
              <ProtectedRoute>
                  <NotesPage />
              </ProtectedRoute>
            } />
          </Routes>
      </BrowserRouter>
  );
}

export default App;