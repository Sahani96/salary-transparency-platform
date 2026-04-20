const API_BASE = process.env.REACT_APP_API_URL || "http://localhost:8080";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
      ...(options.headers || {}),
    },
    ...options,
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(data?.message || "Request failed");
  }

  return data;
}

export const api = {
  login(payload) {
    return request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },
  signup(payload) {
    return request("/api/auth/signup", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },
  validate(token) {
    return request("/api/auth/validate", { token });
  },
  submitSalary(payload) {
    return request("/api/salaries", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },
  search(params) {
    const query = new URLSearchParams(
      Object.entries(params).filter(([, value]) => value !== "" && value !== null && value !== undefined)
    ).toString();
    return request(`/api/search${query ? `?${query}` : ""}`);
  },
  searchFilters() {
    return request("/api/search/filters");
  },
  statsSummary() {
    return request("/api/stats/summary");
  },
  statsByRole() {
    return request("/api/stats/by-role");
  },
  statsByCountry() {
    return request("/api/stats/by-country");
  },
  statsByCompany() {
    return request("/api/stats/by-company");
  },
  statsByLevel() {
    return request("/api/stats/by-level");
  },
  vote(payload, token) {
    return request("/api/votes", {
      method: "POST",
      token,
      body: JSON.stringify(payload),
    });
  },
  getVoteCounts(submissionId) {
    return request(`/api/votes/submission/${submissionId}`);
  },
};
