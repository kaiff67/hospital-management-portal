USE hospital_db;
-- Development-only examples; intentionally fictional and contain no credentials.
INSERT INTO departments(name,description) VALUES
('General Medicine','Fictional development department'),('Cardiology','Fictional development department')
ON DUPLICATE KEY UPDATE description=VALUES(description);
