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
        <div className="metric-grid">
          <div className="metric-card">
            <span>Total</span>
            <strong>{summary.totalSubmissions.toLocaleString()}</strong>
          </div>
          <div className="metric-card">
            <span>Average</span>
            <strong>{summary.averageSalary.toLocaleString()}</strong>
          </div>
          <div className="metric-card">
            <span>Median</span>
            <strong>{summary.medianSalary.toLocaleString()}</strong>
          </div>
          <div className="metric-card">
            <span>P25</span>
            <strong>{summary.p25Salary.toLocaleString()}</strong>
          </div>
          <div className="metric-card">
            <span>P75</span>
            <strong>{summary.p75Salary.toLocaleString()}</strong>
          </div>
          <div className="metric-card">
            <span>P90</span>
            <strong>{summary.p90Salary.toLocaleString()}</strong>
          </div>
          <div className="metric-card">
            <span>Min</span>
            <strong>{summary.minSalary.toLocaleString()}</strong>
          </div>
          <div className="metric-card">
            <span>Max</span>
            <strong>{summary.maxSalary.toLocaleString()}</strong>
          </div>
        </div>
      )}

      {!loading && (
        <div className="stats-columns">
          <div>
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

          <div>
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

          <div>
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

          <div>
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
