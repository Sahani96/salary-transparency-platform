import { useEffect, useState } from "react";
import { api } from "../api";

const initialFilters = {
  country: "",
  company: "",
  jobTitle: "",
  techStack: "",
  minSalary: "",
  maxSalary: "",
  sortBy: "submittedAt",
  sortDirection: "desc",
  page: 0,
  size: 10,
};

function SearchPage({ token }) {
  const [filters, setFilters] = useState(initialFilters);
  const [options, setOptions] = useState({ countries: [], companies: [], jobTitles: [] });
  const [results, setResults] = useState({ content: [], totalElements: 0, totalPages: 0 });
  const [message, setMessage] = useState("");

  const load = async (nextFilters = filters) => {
    try {
      const response = await api.search(nextFilters);
      setResults(response);
      setMessage("");
    } catch (error) {
      setMessage(error.message);
    }
  };

  useEffect(() => {
    api.searchFilters().then(setOptions).catch(() => undefined);
    api.search(initialFilters)
      .then((response) => {
        setResults(response);
        setMessage("");
      })
      .catch((error) => setMessage(error.message));
  }, []);

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
      setMessage(`Vote recorded: ${voteType}`);
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
          <input list="jobTitles" name="jobTitle" value={filters.jobTitle} onChange={update} />
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
        {results.content?.map((item) => (
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
              <button type="button" onClick={() => castVote(item.id, "UPVOTE")}>Upvote</button>
              <button type="button" onClick={() => castVote(item.id, "DOWNVOTE")}>Downvote</button>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

export default SearchPage;
