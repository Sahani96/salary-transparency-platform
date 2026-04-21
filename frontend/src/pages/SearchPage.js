import { useEffect, useState } from "react";
import { api } from "../api";

const initialFilters = {
  country: "",
  company: "",
  role: "",
  techStack: "",
  minSalary: "",
  maxSalary: "",
  sortBy: "submittedAt",
  sortDir: "desc",
  page: 0,
  size: 10,
};

async function fetchVoteCounts(results) {
  const settled = await Promise.allSettled(
    results.map((item) =>
      api.getVoteCounts(item.id).then((counts) => [item.id, counts])
    )
  );
  const map = {};
  settled.forEach((r) => {
    if (r.status === "fulfilled") {
      const [id, counts] = r.value;
      map[id] = {
        upvotes: counts.upvotes ?? 0,
        downvotes: counts.downvotes ?? 0,
      };
    }
  });
  return map;
}

function SearchPage({ token }) {
  const [filters, setFilters] = useState(initialFilters);
  const [options, setOptions] = useState({
    countries: [],
    companies: [],
    jobTitles: [],
  });
  const [results, setResults] = useState({
    results: [],
    totalElements: 0,
    totalPages: 0,
  });
  const [voteCounts, setVoteCounts] = useState({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const isLoggedIn = Boolean(token);

  const load = async (nextFilters = filters) => {
    setLoading(true);
    setMessage("");
    try {
      const response = await api.search(nextFilters, token);
      setResults(response);
      const counts = await fetchVoteCounts(response.results ?? []);
      setVoteCounts(counts);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    api.searchFilters().then(setOptions).catch(() => undefined);
    load(initialFilters);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const update = (event) => {
    const nextFilters = {
      ...filters,
      [event.target.name]: event.target.value,
      page: 0,
    };
    setFilters(nextFilters);
  };

  const submit = (event) => {
    event.preventDefault();
    const reset = { ...filters, page: 0 };
    setFilters(reset);
    load(reset);
  };

  const goToPage = (newPage) => {
    const nextFilters = { ...filters, page: newPage };
    setFilters(nextFilters);
    load(nextFilters);
  };

  const castVote = async (submissionId, voteType) => {
    if (!token) {
      setMessage("Log in first if you want to vote.");
      return;
    }
    try {
      await api.vote({ submissionId, voteType }, token);
      const counts = await api.getVoteCounts(submissionId);
      setVoteCounts((prev) => ({
        ...prev,
        [submissionId]: {
          upvotes: counts.upvotes ?? 0,
          downvotes: counts.downvotes ?? 0,
        },
      }));
    } catch (error) {
      setMessage(error.message);
    }
  };

  const { page, size } = filters;
  const { totalElements, totalPages } = results;

  // Guests only see APPROVED records; logged-in users see everything.
  const visibleResults = isLoggedIn
    ? (results.results ?? [])
    : (results.results ?? []).filter((item) => !item.status || item.status === "APPROVED");

  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>
          Search Salary Records
        </h2>
        {isLoggedIn && (
          <p className="search-scope-note">
            You are logged in — showing both Approved and Pending records.
          </p>
        )}
      </div>

      <form className="form-grid compact" onSubmit={submit}>
        <label>
          Country
          <input
            list="countries"
            name="country"
            value={filters.country}
            onChange={update}
          />
          <datalist id="countries">
            {options.countries?.map((value) => (
              <option key={value} value={value} />
            ))}
          </datalist>
        </label>
        <label>
          Company
          <input
            list="companies"
            name="company"
            value={filters.company}
            onChange={update}
          />
          <datalist id="companies">
            {options.companies?.map((value) => (
              <option key={value} value={value} />
            ))}
          </datalist>
        </label>
        <label>
          Job title
          <input
            list="jobTitles"
            name="role"
            value={filters.role}
            onChange={update}
          />
          <datalist id="jobTitles">
            {options.jobTitles?.map((value) => (
              <option key={value} value={value} />
            ))}
          </datalist>
        </label>
        <label>
          Tech stack
          <input name="techStack" value={filters.techStack} onChange={update} />
        </label>
        <label>
          Min salary
          <input
            name="minSalary"
            type="number"
            value={filters.minSalary}
            onChange={update}
          />
        </label>
        <label>
          Max salary
          <input
            name="maxSalary"
            type="number"
            value={filters.maxSalary}
            onChange={update}
          />
        </label>
        <label>
          Sort by
          <select name="sortBy" value={filters.sortBy} onChange={update}>
            <option value="submittedAt">Date submitted</option>
            <option value="baseSalary">Base salary</option>
            <option value="yearsOfExperience">Years of experience</option>
          </select>
        </label>
        <label>
          Order
          <select name="sortDir" value={filters.sortDir} onChange={update}>
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </label>
        <button type="submit">Apply filters</button>
      </form>

      {message && <p className="feedback">{message}</p>}

      <div className="results-header">
        <strong>
          {loading ? "Searching\u2026" : `${visibleResults.length.toLocaleString()} matching records`}
        </strong>
        {totalPages > 1 && (
          <span className="page-indicator">
            Page {page + 1} of {totalPages}
          </span>
        )}
      </div>

      {!loading && (
        <div className="card-list">
          {visibleResults.map((item) => (
            <article className="result-card" key={item.id}>
              <div className="result-top">
                <div>
                  <h3 className="card-title">
                    {item.jobTitle}
                    {isLoggedIn && item.status && (
                      <span className={`status-badge ${item.status === "APPROVED" ? "badge-approved" : "badge-pending"}`}>
                        {item.status}
                      </span>
                    )}
                  </h3>
                  <p className="card-sub">
                    {item.company} &bull; {item.country}
                    {item.city ? ` \u2022 ${item.city}` : ""}
                  </p>
                </div>
                <div className="salary-pill">
                  {item.currency} {Number(item.baseSalary).toLocaleString()}
                </div>
              </div>
              <p className="card-meta">
                {item.experienceLevel} &bull; {item.yearsOfExperience} yrs &bull;{" "}
                {item.employmentType}
              </p>
              {item.techStack && (
                <div className="card-tech-pills">
                  {item.techStack.split(",").map((t) => t.trim()).filter(Boolean).map((t) => (
                    <span key={t} className="card-tech-chip">{t}</span>
                  ))}
                </div>
              )}
              <div className="vote-row">
                <button
                  type="button"
                  className="vote-btn vote-btn-up"
                  onClick={() => castVote(item.id, "UPVOTE")}
                  title={token ? "Upvote" : "Log in to vote"}
                  disabled={!token}
                >
                  ▲ {voteCounts[item.id]?.upvotes ?? 0}
                </button>
                <button
                  type="button"
                  className="vote-btn vote-btn-down"
                  onClick={() => castVote(item.id, "DOWNVOTE")}
                  title={token ? "Downvote" : "Log in to vote"}
                  disabled={!token}
                >
                  ▼ {voteCounts[item.id]?.downvotes ?? 0}
                </button>
                {!token && (
                  <a href="#/login" className="vote-login-hint">Log in to vote</a>
                )}
              </div>
            </article>
          ))}
        </div>
      )}

      {!loading && totalPages > 1 && (
        <div className="pagination">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => goToPage(page - 1)}
          >
            ← Previous
          </button>
          <span>
            {page + 1} / {totalPages}
          </span>
          <button
            type="button"
            disabled={page >= totalPages - 1}
            onClick={() => goToPage(page + 1)}
          >
            Next →
          </button>
        </div>
      )}
    </section>
  );
}

export default SearchPage;
