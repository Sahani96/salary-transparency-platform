import { useState } from "react";
import { api } from "../api";

const initialForm = {
  jobTitle: "",
  company: "",
  country: "",
  city: "",
  experienceLevel: "MID",
  yearsOfExperience: 3,
  baseSalary: "",
  currency: "LKR",
  employmentType: "FULL_TIME",
  anonymize: false,
  techStack: "",
};

function SubmitSalaryPage() {
  const [form, setForm] = useState(initialForm);
  const [created, setCreated] = useState(null);
  const [message, setMessage] = useState("");

  const update = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    try {
      const response = await api.submitSalary({
        ...form,
        yearsOfExperience: Number(form.yearsOfExperience),
        baseSalary: Number(form.baseSalary),
      });
      setCreated(response);
      setMessage("Submission created. It starts as PENDING until the community verifies it.");
      setForm(initialForm);
    } catch (error) {
      setMessage(error.message);
    }
  };

  return (
    <section className="panel">
      <div className="panel-heading">
        <p className="eyebrow">Salary Service</p>
        <h2>Submit an anonymous salary record</h2>
      </div>

      <form className="form-grid" onSubmit={submit}>
        <label>
          Job title
          <input name="jobTitle" value={form.jobTitle} onChange={update} required />
        </label>
        <label>
          Company
          <input name="company" value={form.company} onChange={update} required />
        </label>
        <label>
          Country
          <input name="country" value={form.country} onChange={update} required />
        </label>
        <label>
          City
          <input name="city" value={form.city} onChange={update} />
        </label>
        <label>
          Experience level
          <select name="experienceLevel" value={form.experienceLevel} onChange={update}>
            <option>JUNIOR</option>
            <option>MID</option>
            <option>SENIOR</option>
            <option>LEAD</option>
            <option>PRINCIPAL</option>
          </select>
        </label>
        <label>
          Years of experience
          <input name="yearsOfExperience" type="number" min="0" value={form.yearsOfExperience} onChange={update} required />
        </label>
        <label>
          Base salary
          <input name="baseSalary" type="number" min="0" value={form.baseSalary} onChange={update} required />
        </label>
        <label>
          Currency
          <input name="currency" value={form.currency} onChange={update} required />
        </label>
        <label>
          Employment type
          <select name="employmentType" value={form.employmentType} onChange={update}>
            <option>FULL_TIME</option>
            <option>PART_TIME</option>
            <option>CONTRACT</option>
            <option>FREELANCE</option>
          </select>
        </label>
        <label className="checkbox">
          <input name="anonymize" type="checkbox" checked={form.anonymize} onChange={update} />
          Hide identity details in the final record
        </label>
        <label className="span-2">
          Tech stack
          <textarea name="techStack" rows="4" value={form.techStack} onChange={update} />
        </label>
        <button type="submit">Submit record</button>
      </form>

      {message && <p className="feedback">{message}</p>}
      {created && (
        <div className="submission-preview">
          <strong>Created submission</strong>
          <p>ID: {created.id}</p>
          <p>Status: {created.status}</p>
        </div>
      )}
    </section>
  );
}

export default SubmitSalaryPage;
