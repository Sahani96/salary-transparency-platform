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
      map[id] = { upvotes: counts.upvotes ?? 0, downvotes: counts.downvotes ?? 0 };
    }
  });
  return map;
}

function SearchPage({ token }) {
  const [filters, setFilters] = useState(initialFilters);
  const [options, setOptions] = useState({ countries: [], companies: [], jobTitles: [] });
  const [results, setResults] = useState({ results: [], totalElements: 0, totalPages: 0 });
  const [voteCounts, setVoteCounts] = useState({});
  const [message, setMessage] = useState("");

  const load = async (nextFilters = filters) => {
    try {
      const response = await api.search(nextFilters);
      setResults(response);
      setMessage("");
      const counts = await fetchVoteCounts(response.results ?? []);
      setVoteCounts(counts);
    } catch (error) {
      setMessage(error.message);
    }
  };

  useEffect(() => {
    api.searchFilters().then(setOptions).catch(() => undefined);
    load(initialFilters);
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const update = (event) => {
    const nextFilters = { ...filters, [event.target.name]: event.target.value, page: 0 };
    setFilters(nextFilters);
  };

  const submit = (event) => {
    event.preventDefault();
    load(filters);
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
        [submissionId]: { upvotes: counts.upvotes ?? 0, downvotes: counts.downvotes ?? 0 },
      }));
    } catch (error) {
      setMessage(error.message);
    }
  };

  return (
    <section className="panel">
      <div className="panel-heading">
        <p className="eyebrow">Search Service</p>
        <h2>Approved salary submissions</h2>
      </div>

      <form className="form-grid compact" onSubmit={submit}>
        <label>
          Country
          <input list="countries" name="country" value={filters.country} onChange={update} />
          <datalist id="countries">
            {options.countries?.map((value) => <option key={value} value={value} />)}
          </datalist>
        </label>
        <label>
          Company
          <input list="companies" name="company" value={filters.company} onChange={update} />
          <datalist id="companies">
            {options.companies?.map((value) => <option key={value} value={value} />)}
          </datalist>
        </label>
        <label>
          Job title
          <input list="jobTitles" name="role" value={filters.role} onChange={update} />
          <datalist id="jobTitles">
            {options.jobTitles?.map((value) => <option key={value} value={value} />)}
          </datalist>
        </label>
        <label>
          Tech stack
          <input name="techStack" value={filters.techStack} onChange={update} />
        </label>
        <label>
          Min salary
          <input name="minSalary" type="number" value={filters.minSalary} onChange={update} />
        </label>
        <label>
          Max salary
          <input name="maxSalary" type="number" value={filters.maxSalary} onChange={update} />
        </label>
        <button type="submit">Apply filters</button>
      </form>

      {message && <p className="feedback">{message}</p>}

      <div className="results-header">
        <strong>{results.totalElements || 0} matching records</strong>
      </div>

      <div className="card-list">
        {results.results?.map((item) => (
          <article className="result-card" key={item.id}>
            <div className="result-top">
              <div>
                <h3>{item.jobTitle}</h3>
                <p>{item.company} • {item.country}{item.city ? ` • ${item.city}` : ""}</p>
              </div>
              <div className="salary-pill">{item.currency} {item.baseSalary}</div>
            </div>
            <p className="muted">{item.experienceLevel} • {item.yearsOfExperience} years • {item.employmentType}</p>
            <p>{item.techStack || "Tech stack not specified"}</p>
            <div className="vote-row">
              <button
                type="button"
                className="vote-btn vote-btn-up"
                onClick={() => castVote(item.id, "UPVOTE")}
                title="Upvote"
              >
                ▲ {voteCounts[item.id]?.upvotes ?? 0}
              </button>
              <button
                type="button"
                className="vote-btn vote-btn-down"
                onClick={() => castVote(item.id, "DOWNVOTE")}
                title="Downvote"
              >
                ▼ {voteCounts[item.id]?.downvotes ?? 0}
              </button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

export default SearchPage;
