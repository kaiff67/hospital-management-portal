CREATE DATABASE IF NOT EXISTS hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE hospital_db;
SET time_zone = '+00:00';

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(60) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL, role ENUM('ADMIN','RECEPTIONIST','DOCTOR','BILLING_STAFF') NOT NULL,
  doctor_id BIGINT NULL, active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS departments (
  department_id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(500), active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS patients (
  patient_id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(140) NOT NULL,
  date_of_birth DATE NOT NULL, gender VARCHAR(30) NOT NULL, phone VARCHAR(25) NOT NULL,
  address VARCHAR(500) NOT NULL, blood_group VARCHAR(5), registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  active BOOLEAN NOT NULL DEFAULT TRUE, INDEX idx_patient_name(full_name), INDEX idx_patient_phone(phone)
);
CREATE TABLE IF NOT EXISTS doctors (
  doctor_id BIGINT PRIMARY KEY AUTO_INCREMENT, full_name VARCHAR(140) NOT NULL,
  specialization VARCHAR(100) NOT NULL, department_id BIGINT NOT NULL, phone VARCHAR(25) NOT NULL,
  consultation_fee DECIMAL(10,2) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_doctor_department FOREIGN KEY(department_id) REFERENCES departments(department_id),
  CONSTRAINT chk_doctor_fee CHECK(consultation_fee >= 0), INDEX idx_doctor_name(full_name), INDEX idx_doctor_specialty(specialization)
);
SET @has_user_doctor_fk = (SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema = DATABASE() AND table_name = 'users' AND constraint_name = 'fk_user_doctor');
SET @user_doctor_ddl = IF(@has_user_doctor_fk = 0, 'ALTER TABLE users ADD CONSTRAINT fk_user_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)', 'SELECT 1');
PREPARE user_doctor_stmt FROM @user_doctor_ddl;
EXECUTE user_doctor_stmt;
DEALLOCATE PREPARE user_doctor_stmt;
CREATE TABLE IF NOT EXISTS doctor_availability (
  availability_id BIGINT PRIMARY KEY AUTO_INCREMENT, doctor_id BIGINT NOT NULL,
  day_of_week TINYINT NOT NULL, start_time TIME NOT NULL, end_time TIME NOT NULL,
  slot_minutes SMALLINT NOT NULL DEFAULT 30, active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_availability_doctor FOREIGN KEY(doctor_id) REFERENCES doctors(doctor_id),
  CONSTRAINT chk_availability_day CHECK(day_of_week BETWEEN 1 AND 7), CONSTRAINT chk_availability_time CHECK(start_time < end_time)
);
CREATE TABLE IF NOT EXISTS appointments (
  appointment_id BIGINT PRIMARY KEY AUTO_INCREMENT, patient_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL,
  start_at DATETIME NOT NULL, end_at DATETIME NOT NULL,
  status ENUM('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
  reason VARCHAR(500), created_by BIGINT NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_appointment_patient FOREIGN KEY(patient_id) REFERENCES patients(patient_id),
  CONSTRAINT fk_appointment_doctor FOREIGN KEY(doctor_id) REFERENCES doctors(doctor_id),
  CONSTRAINT fk_appointment_creator FOREIGN KEY(created_by) REFERENCES users(user_id),
  CONSTRAINT chk_appointment_time CHECK(start_at < end_at),
  INDEX idx_appointment_date(start_at), INDEX idx_appointment_doctor(doctor_id,start_at), INDEX idx_appointment_status(status), INDEX idx_appointment_patient(patient_id,start_at)
);
CREATE TABLE IF NOT EXISTS medical_records (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT, appointment_id BIGINT NOT NULL UNIQUE,
  patient_id BIGINT NOT NULL, doctor_id BIGINT NOT NULL, symptoms TEXT NOT NULL, diagnosis TEXT NOT NULL,
  notes TEXT, visit_at DATETIME NOT NULL, created_by BIGINT NOT NULL, updated_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY(appointment_id) REFERENCES appointments(appointment_id), FOREIGN KEY(patient_id) REFERENCES patients(patient_id),
  FOREIGN KEY(doctor_id) REFERENCES doctors(doctor_id), FOREIGN KEY(created_by) REFERENCES users(user_id), FOREIGN KEY(updated_by) REFERENCES users(user_id),
  INDEX idx_record_patient(patient_id,visit_at)
);
CREATE TABLE IF NOT EXISTS prescriptions (
  prescription_id BIGINT PRIMARY KEY AUTO_INCREMENT, record_id BIGINT NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(record_id) REFERENCES medical_records(record_id)
);
CREATE TABLE IF NOT EXISTS prescription_items (
  item_id BIGINT PRIMARY KEY AUTO_INCREMENT, prescription_id BIGINT NOT NULL, medicine_name VARCHAR(140) NOT NULL,
  dosage VARCHAR(100) NOT NULL, frequency VARCHAR(100) NOT NULL, duration VARCHAR(100) NOT NULL, instructions VARCHAR(500),
  FOREIGN KEY(prescription_id) REFERENCES prescriptions(prescription_id)
);
CREATE TABLE IF NOT EXISTS bills (
  bill_id BIGINT PRIMARY KEY AUTO_INCREMENT, appointment_id BIGINT NOT NULL UNIQUE, patient_id BIGINT NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL, discount DECIMAL(12,2) NOT NULL DEFAULT 0, total DECIMAL(12,2) NOT NULL,
  paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0, status ENUM('UNPAID','PARTIALLY_PAID','PAID') NOT NULL DEFAULT 'UNPAID',
  issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by BIGINT NOT NULL,
  FOREIGN KEY(appointment_id) REFERENCES appointments(appointment_id), FOREIGN KEY(patient_id) REFERENCES patients(patient_id), FOREIGN KEY(created_by) REFERENCES users(user_id),
  CONSTRAINT chk_bill_amounts CHECK(subtotal >= 0 AND discount >= 0 AND discount <= subtotal AND total = subtotal-discount AND paid_amount <= total), INDEX idx_bill_status(status), INDEX idx_bill_patient(patient_id)
);
CREATE TABLE IF NOT EXISTS bill_items (
  bill_item_id BIGINT PRIMARY KEY AUTO_INCREMENT, bill_id BIGINT NOT NULL, description VARCHAR(200) NOT NULL,
  quantity DECIMAL(10,2) NOT NULL, unit_price DECIMAL(10,2) NOT NULL, amount DECIMAL(12,2) NOT NULL,
  FOREIGN KEY(bill_id) REFERENCES bills(bill_id), CONSTRAINT chk_bill_item CHECK(quantity > 0 AND unit_price >= 0 AND amount = quantity*unit_price)
);
CREATE TABLE IF NOT EXISTS payments (
  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT, bill_id BIGINT NOT NULL, amount DECIMAL(12,2) NOT NULL,
  method ENUM('CASH','CARD','UPI','BANK_TRANSFER') NOT NULL, reference VARCHAR(100), paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status ENUM('RECORDED','VOIDED') NOT NULL DEFAULT 'RECORDED', created_by BIGINT NOT NULL,
  FOREIGN KEY(bill_id) REFERENCES bills(bill_id), FOREIGN KEY(created_by) REFERENCES users(user_id), CONSTRAINT chk_payment_amount CHECK(amount > 0), INDEX idx_payment_date(paid_at)
);
CREATE TABLE IF NOT EXISTS audit_logs (
  audit_id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT, action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(80) NOT NULL, entity_id BIGINT, occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  details VARCHAR(500), FOREIGN KEY(user_id) REFERENCES users(user_id), INDEX idx_audit_time(occurred_at)
);
