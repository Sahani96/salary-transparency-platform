import { useEffect, useState } from "react";
import { api } from "../api";

function StatsPage() {
  const [summary, setSummary] = useState(null);
  const [roleStats, setRoleStats] = useState([]);
  const [countryStats, setCountryStats] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    Promise.all([api.statsSummary(), api.statsByRole(), api.statsByCountry()])
      .then(([summaryResponse, roleResponse, countryResponse]) => {
        setSummary(summaryResponse);
        setRoleStats(roleResponse.items || []);
        setCountryStats(countryResponse.items || []);
      })
      .catch((error) => setMessage(error.message));
  }, []);

  return (
    <section className="panel">
      <div className="panel-heading">
        <p className="eyebrow">Stats Service</p>
        <h2>Aggregated salary insights</h2>
      </div>

      {message && <p className="feedback error">{message}</p>}

      {summary && (
        <div className="metric-grid">
          <div className="metric-card"><span>Total</span><strong>{summary.totalSubmissions}</strong></div>
          <div className="metric-card"><span>Average</span><strong>{summary.averageSalary}</strong></div>
          <div className="metric-card"><span>Median</span><strong>{summary.medianSalary}</strong></div>
          <div className="metric-card"><span>P90</span><strong>{summary.percentile90Salary}</strong></div>
        </div>
      )}

      <div className="stats-columns">
        <div>
          <h3>By role</h3>
          <div className="mini-table">
            {roleStats.map((item) => (
              <div key={item.group} className="mini-row">
                <span>{item.group}</span>
                <strong>{item.medianSalary}</strong>
              </div>
            ))}
          </div>
        </div>

        <div>
          <h3>By country</h3>
          <div className="mini-table">
            {countryStats.map((item) => (
              <div key={item.group} className="mini-row">
                <span>{item.group}</span>
                <strong>{item.averageSalary}</strong>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

export default StatsPage;
