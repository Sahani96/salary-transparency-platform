import { useEffect, useState } from "react";
import { api } from "../api";

function StatsPage() {
  const [summary, setSummary] = useState(null);
  const [roleStats, setRoleStats] = useState([]);
  const [countryStats, setCountryStats] = useState([]);
  const [companyStats, setCompanyStats] = useState([]);
  const [levelStats, setLevelStats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.statsSummary(),
      api.statsByRole(),
      api.statsByCountry(),
      api.statsByCompany(),
      api.statsByLevel(),
    ])
      .then(([summaryRes, roleRes, countryRes, companyRes, levelRes]) => {
        setSummary(summaryRes);
        setRoleStats(roleRes.entries || []);
        setCountryStats(countryRes.entries || []);
        setCompanyStats(companyRes.entries || []);
        setLevelStats(levelRes.entries || []);
      })
      .catch((error) => setMessage(error.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="panel">
      <div className="panel-heading">
        <p className="eyebrow">Stats Service</p>
        <h2>Aggregated salary insights</h2>
      </div>

      {message && <p className="feedback error">{message}</p>}

      {loading && <p className="stats-loading">Loading statistics…</p>}

      {!loading && summary && (
        <div className="metric-section">
          <div className="metric-row-primary">
            <div className="metric-card-hero">
              <span className="metric-label">Total Submissions</span>
              <strong className="metric-value">{summary.totalSubmissions.toLocaleString()}</strong>
            </div>
            <div className="metric-card-hero">
              <span className="metric-label">Average Salary</span>
              <strong className="metric-value">{Number(summary.averageSalary).toLocaleString()}</strong>
            </div>
            <div className="metric-card-hero">
              <span className="metric-label">Median Salary</span>
              <strong className="metric-value">{Number(summary.medianSalary).toLocaleString()}</strong>
            </div>
          </div>

          <p className="metric-group-label">Distribution</p>
          <div className="metric-row-secondary">
            {[
              { label: "25th Percentile", value: summary.p25Salary },
              { label: "75th Percentile", value: summary.p75Salary },
              { label: "90th Percentile", value: summary.p90Salary },
              { label: "Minimum",          value: summary.minSalary },
              { label: "Maximum",          value: summary.maxSalary },
            ].map(({ label, value }) => (
              <div key={label} className="metric-chip">
                <span className="chip-label">{label}</span>
                <strong className="chip-value">{Number(value).toLocaleString()}</strong>
              </div>
            ))}
          </div>
        </div>
      )}

      {!loading && (
        <div className="stats-columns">
          <div className="stats-column">
            <h3>By role</h3>
            <div className="mini-table">
              {roleStats.length === 0 && <p className="stats-empty">No data</p>}
              {roleStats.map((item) => (
                <div key={item.groupName} className="mini-row">
                  <span>{item.groupName}</span>
                  <strong>{item.medianSalary.toLocaleString()}</strong>
                </div>
              ))}
            </div>
          </div>

          <div className="stats-column">
            <h3>By country</h3>
            <div className="mini-table">
              {countryStats.length === 0 && <p className="stats-empty">No data</p>}
              {countryStats.map((item) => (
                <div key={item.groupName} className="mini-row">
                  <span>{item.groupName}</span>
                  <strong>{item.averageSalary.toLocaleString()}</strong>
                </div>
              ))}
            </div>
          </div>

          <div className="stats-column">
            <h3>By company</h3>
            <div className="mini-table">
              {companyStats.length === 0 && <p className="stats-empty">No data</p>}
              {companyStats.map((item) => (
                <div key={item.groupName} className="mini-row">
                  <span>{item.groupName}</span>
                  <strong>{item.medianSalary.toLocaleString()}</strong>
                </div>
              ))}
            </div>
          </div>

          <div className="stats-column">
            <h3>By experience level</h3>
            <div className="mini-table">
              {levelStats.length === 0 && <p className="stats-empty">No data</p>}
              {levelStats.map((item) => (
                <div key={item.groupName} className="mini-row">
                  <span>{item.groupName}</span>
                  <strong>{item.medianSalary.toLocaleString()}</strong>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

export default StatsPage;
