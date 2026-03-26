import { createContext, useContext, useState, useEffect } from "react";
import axios from "axios";

const AuthContext = createContext(null);

const API_BASE = import.meta.env.VITE_API_URL || "";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem("authToken"));
  const [loading, setLoading] = useState(true);

  // On mount: restore session from localStorage
  useEffect(() => {
    const storedToken = localStorage.getItem("authToken");
    const storedUser = localStorage.getItem("authUser");
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await axios.post(`${API_BASE}/api/auth/login`, {
      email,
      password,
    });
    const { token: newToken, user: newUser } = response.data;
    localStorage.setItem("authToken", newToken);
    localStorage.setItem("authUser", JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
    return newUser;
  };

  const register = async (name, email, password, role = "DEVELOPER") => {
    const response = await axios.post(`${API_BASE}/api/auth/register`, {
      name,
      email,
      password,
      role,
    });
    const { token: newToken, user: newUser } = response.data;
    localStorage.setItem("authToken", newToken);
    localStorage.setItem("authUser", JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
    return newUser;
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("authUser");
    setToken(null);
    setUser(null);
  };

  const isAdmin = () => user?.role === "ADMIN";
  const isDeveloper = () => user?.role === "DEVELOPER";

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        logout,
        isAdmin,
        isDeveloper,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
