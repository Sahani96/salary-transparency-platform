-- ============================================================
-- Salary Transparency Platform - Seed Data for Development
-- ============================================================


-- Salary submissions with mixed statuses
INSERT INTO salary.submissions (id, job_title, company, country, city, experience_level, years_of_experience, base_salary, currency, employment_type, anonymize, status, tech_stack, submitted_at) VALUES
    -- APPROVED entries (will appear in search and stats)
    ('11111111-1111-1111-1111-111111111111', 'Software Engineer', 'WSO2', 'Sri Lanka', 'Colombo', 'MID', 3, 150000.00, 'LKR', 'FULL_TIME', false, 'APPROVED', 'Java, Spring Boot, React', '2025-01-15 10:00:00'),
    ('22222222-2222-2222-2222-222222222222', 'Senior Software Engineer', 'IFS', 'Sweden', 'Linköping', 'SENIOR', 6, 35000.00, 'SEK', 'FULL_TIME', false, 'APPROVED', 'C#, .NET, Angular', '2025-02-01 11:00:00'),
    ('33333333-3333-3333-3333-333333333333', 'DevOps Engineer', 'Sysco LABS', 'America', 'New York', 'MID', 4, 200000.00, 'LKR', 'FULL_TIME', false, 'APPROVED', 'AWS, Docker, Kubernetes, Terraform', '2025-02-15 09:00:00'),
    ('44444444-4444-4444-4444-444444444444', 'Frontend Developer', 'Calcey Technologies', 'Sri Lanka', 'Colombo', 'JUNIOR', 1, 8000.00, 'USD', 'FULL_TIME', false, 'APPROVED', 'React, TypeScript, CSS', '2025-03-01 14:00:00'),
    ('55555555-5555-5555-5555-555555555555', 'Tech Lead', 'Virtusa', 'Sri Lanka', 'Colombo', 'LEAD', 8, 500000.00, 'LKR', 'FULL_TIME', true, 'APPROVED', 'Java, Microservices, AWS', '2025-03-10 10:30:00'),
    ('66666666-6666-6666-6666-666666666666', 'QA Engineer', 'WSO2', 'America', 'New York', 'MID', 3, 12000.00, 'USD', 'FULL_TIME', false, 'APPROVED', 'Selenium, Java, Playwright', '2025-03-15 10:30:00'),
    ('99999999-9999-9999-9999-999999999999', 'Software Engineer', 'Pagero', 'Sri Lanka', 'Colombo', 'MID', 2, 130000.00, 'LKR', 'FULL_TIME', false, 'APPROVED', 'Java, Spring Boot, PostgreSQL', '2025-04-10 09:00:00'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Backend Developer', 'Rootcode', 'Sri Lanka', 'Colombo', 'JUNIOR', 1, 90000.00, 'LKR', 'FULL_TIME', false, 'APPROVED', 'Node.js, Express, MongoDB', '2025-04-12 15:00:00'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaabbbbbb', 'Backend Developer', 'Cambio', 'Sweden', 'Stockholm', 'JUNIOR', 1, 9000.00, 'SEK', 'FULL_TIME', false, 'APPROVED', 'Node.js, Express, MongoDB', '2025-04-12 15:00:00'),
    -- PENDING entries (awaiting votes)
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Cloud Architect', 'Virtusa', 'Sri Lanka', 'Colombo', 'PRINCIPAL', 12, 800000.00, 'LKR', 'FULL_TIME', true, 'PENDING', 'AWS, GCP, Kubernetes', '2025-04-14 10:00:00'),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Software Engineer', 'Zone24x7', 'Sri Lanka', 'Colombo', 'MID', 3, 170000.00, 'LKR', 'FULL_TIME', false, 'PENDING', 'Python, Django, React', '2025-04-15 10:00:00'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'UI/UX Designer', 'Arimac', 'Sri Lanka', 'Colombo', 'MID', 4, 140000.00, 'LKR', 'FULL_TIME', false, 'PENDING', 'Figma, Adobe XD, CSS', '2025-04-16 10:00:00'),

    -- Freelance entries
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Freelance Developer', 'Self-employed', 'America', 'New York', 'SENIOR', 7, 45000.00, 'USD', 'FREELANCE', true, 'APPROVED', 'React, Node.js, AWS', '2025-04-08 10:00:00')
ON CONFLICT DO NOTHING;