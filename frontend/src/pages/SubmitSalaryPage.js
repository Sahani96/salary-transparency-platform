import { useEffect, useRef, useState } from "react";
import { api } from "../api";

const CURRENCIES = ["USD", "LKR", "EUR", "GBP", "AUD", "CAD", "SGD", "INR", "JPY", "AED"];
const EXPERIENCE_LEVELS = ["JUNIOR", "MID", "SENIOR", "LEAD", "PRINCIPAL"];
const EMPLOYMENT_TYPES = ["FULL_TIME", "PART_TIME", "CONTRACT", "FREELANCE"];

const EMPTY_FORM = {
  jobTitle: "",
  company: "",
  country: "",
  city: "",
  yearsOfExperience: "",
  baseSalary: "",
  currency: "USD",
  experienceLevel: "",
  employmentType: "",
  anonymize: false,
};

function SubmitSalaryPage({ token }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [techPills, setTechPills] = useState([]);
  const [techInput, setTechInput] = useState("");
  const [jobTitles, setJobTitles] = useState([]);
  const [message, setMessage] = useState({ text: "", type: "" });
  const [loading, setLoading] = useState(false);
  const techInputRef = useRef(null);

  useEffect(() => {
    api.searchFilters()
      .then((data) => setJobTitles(data.jobTitles || []))
      .catch(() => undefined);
  }, []);

  const update = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: type === "checkbox" ? checked : value }));
  };

  const addTechPill = () => {
    const tag = techInput.trim();
    if (tag && !techPills.includes(tag)) {
      setTechPills((prev) => [...prev, tag]);
    }
    setTechInput("");
  };

  const onTechKeyDown = (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      addTechPill();
    } else if (e.key === "Backspace" && techInput === "" && techPills.length > 0) {
      setTechPills((prev) => prev.slice(0, -1));
    }
  };

  const removePill = (tag) => setTechPills((prev) => prev.filter((p) => p !== tag));

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ text: "", type: "" });
    try {
      await api.submitSalary({
        ...form,
        yearsOfExperience: Number(form.yearsOfExperience),
        baseSalary: Number(form.baseSalary),
        techStack: techPills.join(", "),
      }, token);
      setMessage({ text: "Submission received! Your salary record is under review. Thank you for contributing.", type: "success" });
      setForm(EMPTY_FORM);
      setTechPills([]);
      setTechInput("");
    } catch (error) {
      setMessage({ text: error.message, type: "error" });
    } finally {
      setLoading(false);
    }
  };

  const isFormValid =
    form.jobTitle.trim() &&
    form.company.trim() &&
    form.country.trim() &&
    form.yearsOfExperience !== "" &&
    form.baseSalary !== "";

  return (
    <section className="panel">
      <div className="panel-heading">
        <p className="eyebrow">Salary Service</p>
        <h2>Submit your salary</h2>
        <p className="submit-intro">
          Submissions are completely anonymous. No account is needed.
        </p>
      </div>

      <form className="form-grid" onSubmit={submit}>
        {/* Job title with suggestions */}
        <label>
          Job title
          <input
            list="job-title-suggestions"
            name="jobTitle"
            value={form.jobTitle}
            onChange={update}
            placeholder="e.g. Software Engineer"
            required
          />
          <datalist id="job-title-suggestions">
            {jobTitles.map((t) => <option key={t} value={t} />)}
          </datalist>
        </label>

        <label>
          Company
          <input name="company" value={form.company} onChange={update} placeholder="e.g. Sysco Labs" required />
        </label>

        <label>
          Country
          <input name="country" value={form.country} onChange={update} placeholder="e.g. Sri Lanka" required />
        </label>

        <label>
          City
          <input name="city" value={form.city} onChange={update} placeholder="e.g. Colombo" />
        </label>

        <label>
          Years of experience
          <input name="yearsOfExperience" type="number" min="0" max="50" value={form.yearsOfExperience} onChange={update} placeholder="e.g. 3" required />
        </label>

        <label>
          Base salary
          <input name="baseSalary" type="number" min="0" value={form.baseSalary} onChange={update} placeholder="e.g. 120000" required />
        </label>

        {/* Currency dropdown */}
        <label>
          Currency
          <select name="currency" value={form.currency} onChange={update} required>
            {CURRENCIES.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
        </label>

        {/* Experience level dropdown */}
        <label>
          Experience level
          <select name="experienceLevel" value={form.experienceLevel} onChange={update}>
            <option value="">— Select level —</option>
            {EXPERIENCE_LEVELS.map((l) => (
              <option key={l} value={l}>{l.charAt(0) + l.slice(1).toLowerCase()}</option>
            ))}
          </select>
        </label>

        {/* Employment type dropdown */}
        <label>
          Employment type
          <select name="employmentType" value={form.employmentType} onChange={update}>
            <option value="">— Select type —</option>
            {EMPLOYMENT_TYPES.map((t) => (
              <option key={t} value={t}>{t.replace("_", " ").charAt(0) + t.replace("_", " ").slice(1).toLowerCase()}</option>
            ))}
          </select>
        </label>

        {/* Tech stack pill input */}
        <label className="span-2">
          Tech stack
          <div className="pill-input-box" onClick={() => techInputRef.current?.focus()}>
            {techPills.map((tag) => (
              <span key={tag} className="tech-pill">
                {tag}
                <button type="button" className="pill-remove" onClick={() => removePill(tag)} aria-label={`Remove ${tag}`}>×</button>
              </span>
            ))}
            <input
              ref={techInputRef}
              className="pill-text-input"
              value={techInput}
              onChange={(e) => setTechInput(e.target.value)}
              onKeyDown={onTechKeyDown}
              onBlur={addTechPill}
              placeholder={techPills.length === 0 ? "Type a technology and press Enter…" : ""}
            />
          </div>
          <span className="field-hint">Press Enter to add each technology (e.g. React, Java, PostgreSQL)</span>
        </label>

        <button className="span-2 submit-btn" type="submit" disabled={loading || !isFormValid}>
          {loading ? (
            <span className="submit-btn-inner">
              <span className="submit-spinner" />
              Submitting…
            </span>
          ) : (
            <span className="submit-btn-inner">
              Submit salary record
            </span>
          )}
        </button>
      </form>

      {message.text && (
        <p className={`feedback${message.type === "error" ? " error" : " success"}`}>
          {message.text}
        </p>
      )}
    </section>
  );
}

export default SubmitSalaryPage;
