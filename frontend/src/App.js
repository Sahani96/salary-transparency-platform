import { useEffect, useState } from "react";
import "./App.css";
import { api } from "./api";
import LoginPage from "./pages/LoginPage";
import SearchPage from "./pages/SearchPage";
import StatsPage from "./pages/StatsPage";
import SubmitSalaryPage from "./pages/SubmitSalaryPage";

const routes = {
  "#/login": "login",
  "#/submit": "submit",
  "#/stats": "stats",
  "#/search": "search",
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
    if (!session?.token) {
      return;
    }
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
    window.location.hash = "#/submit";
  };

  const logout = () => {
    setSession(null);
    window.localStorage.removeItem("salary-platform-session");
    window.location.hash = "#/search";
  };

  return (
    <div className="shell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">Cloud Computing Coursework</p>
          <h1>Tech Salary Transparency Platform</h1>
          <p className="intro">
            Anonymous salary submissions, searchable approved records, and community-visible salary patterns for the tech industry.
          </p>
        </div>

        <nav className="nav">
          <a href="#/search" className={route === "search" ? "active" : ""}>Search</a>
          <a href="#/stats" className={route === "stats" ? "active" : ""}>Statistics</a>
          <a href="#/submit" className={route === "submit" ? "active" : ""}>Submit Salary</a>
          <a href="#/login" className={route === "login" ? "active" : ""}>{session ? "Account" : "Login / Signup"}</a>
        </nav>

        <div className="session-card">
          {session ? (
            <>
              <strong>{session.username}</strong>
              <span>{session.email}</span>
              <button type="button" onClick={logout}>Log out</button>
            </>
          ) : (
            <>
              <strong>Guest mode</strong>
              <span>Search and stats are public. Voting requires a signed-in account.</span>
            </>
          )}
        </div>
      </aside>

      <main className="content">
        {route === "login" && <LoginPage session={session} onAuthenticated={handleAuthenticated} onLogout={logout} />}
        {route === "submit" && <SubmitSalaryPage />}
        {route === "stats" && <StatsPage />}
        {route === "search" && <SearchPage token={session?.token} />}
      </main>
    </div>
  );
}

export default App;
