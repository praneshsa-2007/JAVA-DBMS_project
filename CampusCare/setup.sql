-- Student Complaint Registration System : MySQL schema
CREATE DATABASE IF NOT EXISTS complaint_db;
USE complaint_db;

CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(20)  PRIMARY KEY,
    name       VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS staff (
    staff_id   INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    department VARCHAR(50)  NOT NULL,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL DEFAULT '1234'
);

CREATE TABLE IF NOT EXISTS complaints (
    complaint_id      INT AUTO_INCREMENT PRIMARY KEY,
    student_id        VARCHAR(20)  NOT NULL,
    category          VARCHAR(30)  NOT NULL,
    location          VARCHAR(100) NOT NULL,
    description       VARCHAR(500) NOT NULL,
    status            ENUM('PENDING','ASSIGNED','IN_PROGRESS','RESOLVED')
                      NOT NULL DEFAULT 'PENDING',
    assigned_staff_id INT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id)        REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_staff_id) REFERENCES staff(staff_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS complaint_history (
    history_id   INT AUTO_INCREMENT PRIMARY KEY,
    complaint_id INT         NOT NULL,
    old_status   VARCHAR(20),
    new_status   VARCHAR(20) NOT NULL,
    remarks      VARCHAR(255),
    changed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (complaint_id) REFERENCES complaints(complaint_id) ON DELETE CASCADE
);

-- Sample staff accounts with username (admin_<staff_name>) and password "1234"
INSERT INTO staff (name, department, username, password) VALUES
    ('Ravi Kumar',  'Classroom Facilities', 'admin_ravi',    '1234'),
    ('Meena Devi',  'Wifi / Internet',     'admin_meena',   '1234'),
    ('Arun Prasad', 'Water supply',        'admin_arun',    '1234'),
    ('Suresh Babu', 'Cleanliness',         'admin_suresh',  '1234'),
    ('Nishant',     'Electrical Issue',    'admin_nishant', '1234'),
    ('Tarun',       'Other',               'admin_tarun',   '1234')
ON DUPLICATE KEY UPDATE name=VALUES(name), department=VALUES(department), password=VALUES(password);
