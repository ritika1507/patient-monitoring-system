-- Create patients table
CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT,
    room_number VARCHAR(20),
    assigned_doctor VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create alert rules table
CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    vital_type VARCHAR(50) NOT NULL,
    condition VARCHAR(20) NOT NULL,
    threshold_value VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_id VARCHAR(100) UNIQUE NOT NULL,
    patient_id VARCHAR(50) REFERENCES patients(id),
    rule_id BIGINT REFERENCES alert_rules(id),
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN',
    triggered_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    acknowledged_by VARCHAR(100),
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_alerts_patient ON alerts(patient_id);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_triggered ON alerts(triggered_at DESC);

-- Insert sample patients
INSERT INTO patients (id, name, age, room_number, assigned_doctor, status) VALUES
('P001', 'John Doe', 45, 'ICU-101', 'Dr. Smith', 'ACTIVE'),
('P002', 'Jane Smith', 32, 'ICU-102', 'Dr. Johnson', 'ACTIVE'),
('P003', 'Bob Wilson', 67, 'ICU-103', 'Dr. Smith', 'ACTIVE'),
('P004', 'Alice Brown', 54, 'Ward-201', 'Dr. Davis', 'ACTIVE'),
('P005', 'Charlie Green', 29, 'Ward-202', 'Dr. Johnson', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Insert default alert rules
INSERT INTO alert_rules (rule_name, vital_type, condition, threshold_value, severity) VALUES
('High Heart Rate', 'HEART_RATE', 'GT', '120', 'CRITICAL'),
('Low Oxygen Level', 'OXYGEN_LEVEL', 'LT', '90', 'CRITICAL'),
('Low Heart Rate', 'HEART_RATE', 'LT', '50', 'WARNING'),
('High Temperature', 'TEMPERATURE', 'GT', '38.5', 'WARNING'),
('High Blood Pressure', 'BLOOD_PRESSURE_SYSTOLIC', 'GT', '140', 'WARNING')
ON CONFLICT DO NOTHING;