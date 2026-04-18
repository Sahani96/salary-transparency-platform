-- ============================================================
-- Salary Transparency Platform - Database Initialization Script
-- PostgreSQL schema setup with logical separation
-- ============================================================

-- Create schemas
CREATE SCHEMA IF NOT EXISTS salary;

-- ============================================================
-- IDENTITY SCHEMA - User accounts and authentication
-- ============================================================

-- need to implement here


-- ============================================================
-- SALARY SCHEMA - Submissions and approved records
-- No personal identity information (email) stored here
-- ============================================================

CREATE TABLE IF NOT EXISTS salary.submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100),
    experience_level VARCHAR(20) NOT NULL CHECK (experience_level IN ('JUNIOR', 'MID', 'SENIOR', 'LEAD', 'PRINCIPAL')),
    years_of_experience INTEGER NOT NULL CHECK (years_of_experience >= 0),
    base_salary DECIMAL(15, 2) NOT NULL CHECK (base_salary >= 0),
    currency VARCHAR(10) NOT NULL DEFAULT 'LKR',
    employment_type VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME' CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'FREELANCE')),
    anonymize BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    tech_stack VARCHAR(500),
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_submissions_status ON salary.submissions(status);
CREATE INDEX IF NOT EXISTS idx_submissions_country ON salary.submissions(country);
CREATE INDEX IF NOT EXISTS idx_submissions_company ON salary.submissions(company);
CREATE INDEX IF NOT EXISTS idx_submissions_experience_level ON salary.submissions(experience_level);
CREATE INDEX IF NOT EXISTS idx_submissions_job_title ON salary.submissions(job_title);

-- ============================================================
-- COMMUNITY SCHEMA - Votes and community actions
-- ============================================================

-- need to implement here