import { useEffect, useState } from "react";
import "./App.css";
import { api } from "./api";
import LoginPage from "./pages/LoginPage";
import SearchPage from "./pages/SearchPage";
import StatsPage from "./pages/StatsPage";
import SubmitSalaryPage from "./pages/SubmitSalaryPage";

const routes = {
  "#/submit": "submit",
  "#/stats": "stats",
  "#/search": "search",
  "#/login": "login",
};

function getRoute() {
  return routes[window.location.hash] || "search";
}

function App() {
  const [route, setRoute] = useState(getRoute());
  const [session, setSession] = useState(() => {
    const raw = window.localStorage.getItem("salary-platform-session");
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    const onChange = () => setRoute(getRoute());
    window.addEventListener("hashchange", onChange);
    if (!window.location.hash) {
      window.location.hash = "#/search";
    }
    return () => window.removeEventListener("hashchange", onChange);
  }, []);

  useEffect(() => {
    if (!session?.token) return;
    api.validate(session.token).catch(() => {
      setSession(null);
      window.localStorage.removeItem("salary-platform-session");
    });
  }, [session]);

  const handleAuthenticated = (authResponse) => {
    const nextSession = {
      token: authResponse.token,
      username: authResponse.username,
      email: authResponse.email,
      userId: authResponse.userId,
    };
    setSession(nextSession);
    window.localStorage.setItem("salary-platform-session", JSON.stringify(nextSession));
    window.location.hash = "#/search";
  };

  const logout = () => {
    setSession(null);
    window.localStorage.removeItem("salary-platform-session");
    window.location.hash = "#/search";
  };

  if (route === "login" && !session) {
    return (
      <div className="login-shell">
        <div className="login-brand">
          <p className="eyebrow">Cloud Computing Coursework</p>
          <h1>Tech Salary Transparency Platform</h1>
          <p className="intro">
            Anonymous salary submissions, searchable approved records, and community-visible salary patterns for the tech industry.
          </p>
        </div>
        <LoginPage onAuthenticated={handleAuthenticated} />
      </div>
    );
  }

  return (
    <div className="shell">
      <header className="topbar">
        <span className="topbar-brand">Tech Salary Transparency Platform</span>
        <div className="topbar-session">
          {session ? (
            <>
              <strong>{session.username}</strong>
              <span>{session.email}</span>
              <button type="button" onClick={logout}>Log out</button>
            </>
          ) : (
            <a href="#/login" className="topbar-login-btn">Log in to vote</a>
          )}
        </div>
      </header>

      <aside className="sidebar">
        <nav className="nav">
          <a href="#/search" className={route === "search" ? "active" : ""}>Search</a>
          <a href="#/stats" className={route === "stats" ? "active" : ""}>Statistics</a>
          <a href="#/submit" className={route === "submit" ? "active" : ""}>Submit Salary</a>
        </nav>
      </aside>

      <main className="content">
        {route === "submit" && <SubmitSalaryPage token={session?.token} />}
        {route === "stats" && <StatsPage />}
        {route === "search" && <SearchPage token={session?.token} />}
        {route === "login" && session && (
          <SearchPage token={session.token} />
        )}
      </main>
    </div>
  );
}

export default App;
