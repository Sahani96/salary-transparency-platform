import { useState } from "react";
import { api } from "../api";

function LoginPage({ session, onAuthenticated, onLogout }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ username: "", email: "", password: "" });
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const updateField = (event) => {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    try {
      const payload = mode === "signup" ? form : { username: form.username, password: form.password };
      const response = mode === "signup" ? await api.signup(payload) : await api.login(payload);
      onAuthenticated(response);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  if (session) {
    return (
      <section className="panel">
        <div className="panel-heading">
          <p className="eyebrow">Account</p>
          <h2>Signed in as {session.username}</h2>
        </div>
        <p className="muted">{session.email}</p>
        <button type="button" onClick={onLogout}>Log out</button>
      </section>
    );
  }

  return (
    <section className="panel">
      <div className="panel-heading">
        <p className="eyebrow">Identity Service</p>
        <h2>{mode === "login" ? "Log in to vote" : "Create a new account"}</h2>
      </div>

      <div className="tab-row">
        <button type="button" className={mode === "login" ? "active" : ""} onClick={() => setMode("login")}>Login</button>
        <button type="button" className={mode === "signup" ? "active" : ""} onClick={() => setMode("signup")}>Signup</button>
      </div>

      <form className="form-grid" onSubmit={submit}>
        <label>
          Username
          <input name="username" value={form.username} onChange={updateField} required />
        </label>
        {mode === "signup" && (
          <label>
            Email
            <input name="email" type="email" value={form.email} onChange={updateField} required />
          </label>
        )}
        <label>
          Password
          <input name="password" type="password" value={form.password} onChange={updateField} required />
        </label>
        <button type="submit" disabled={loading}>{loading ? "Working..." : mode === "login" ? "Log in" : "Create account"}</button>
      </form>

      {message && <p className="feedback error">{message}</p>}
    </section>
  );
}

export default LoginPage;
