CREATE DATABASE IF NOT EXISTS pims;
USE pims;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier') NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT
);

CREATE TABLE medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100),
    medicine_type VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL,
    reorder_level INT NOT NULL,
    expiry_date DATE NOT NULL,
    supplier_id INT NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);

CREATE TABLE sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE sale_items (
    sale_item_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_sold INT NOT NULL,
    price_at_sale DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);

INSERT INTO users(username, password, role, full_name) VALUES
('admin', 'admin123', 'Admin', 'HealthFirst Administrator'),
('cashier', 'cash123', 'Cashier', 'HealthFirst Cashier');

INSERT INTO suppliers(name, contact_person, phone, email, address) VALUES
('MediSupply', 'Lebo Molefe', '0115550101', 'sales@medisupply.co.za', 'Johannesburg'),
('CareChem', 'Ayesha Khan', '0215550102', 'orders@carechem.co.za', 'Cape Town');

INSERT INTO medicines(name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Paracetamol 500mg', 'HealthCare', 'Tablet', 35.50, 100, 20, '2027-06-30', 1),
('Cough Syrup', 'CareChem', 'Syrup', 48.00, 35, 10, '2026-11-30', 2),
('Vitamin C', 'HealthCare', 'Tablet', 62.75, 8, 15, '2026-09-15', 1);