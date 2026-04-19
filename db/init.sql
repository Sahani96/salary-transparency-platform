-- ============================================================
-- Salary Transparency Platform - Database Initialization Script
-- PostgreSQL schema setup with logical separation
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create schemas
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS salary;
CREATE SCHEMA IF NOT EXISTS community;

-- ============================================================
-- IDENTITY SCHEMA - User accounts and authentication
-- ============================================================

CREATE TABLE IF NOT EXISTS identity.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_username ON identity.users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON identity.users(email);


-- ============================================================
-- SALARY SCHEMA - Submissions and approved records
-- No personal identity information (email) stored here
-- ============================================================
DROP TABLE IF EXISTS salary.submissions;

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

CREATE TABLE IF NOT EXISTS community.votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id UUID NOT NULL REFERENCES salary.submissions(id) ON DELETE CASCADE,
    voter_user_id UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    vote_type VARCHAR(20) NOT NULL CHECK (vote_type IN ('UPVOTE', 'DOWNVOTE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_votes_submission_user UNIQUE (submission_id, voter_user_id)
);

CREATE INDEX IF NOT EXISTS idx_votes_submission_id ON community.votes(submission_id);
CREATE INDEX IF NOT EXISTS idx_votes_voter_user_id ON community.votes(voter_user_id);
