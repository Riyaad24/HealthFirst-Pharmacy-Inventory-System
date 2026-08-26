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
('CareChem', 'Ayesha Khan', '0215550102', 'orders@carechem.co.za', 'Cape Town'),
('Adcock Ingram', 'HealthFirst Supplier', '0115550103', 'orders@adcock.co.za', 'Johannesburg'),
('Johnson & Johnson', 'HealthFirst Supplier', '0115550104', 'orders@jnj.co.za', 'Johannesburg'),
('Cipla', 'HealthFirst Supplier', '0115550105', 'orders@cipla.co.za', 'Johannesburg'),
('Kenvue', 'HealthFirst Supplier', '0115550106', 'orders@kenvue.co.za', 'Johannesburg'),
('Reckitt', 'HealthFirst Supplier', '0115550107', 'orders@reckitt.co.za', 'Johannesburg'),
('Haleon', 'HealthFirst Supplier', '0115550108', 'orders@haleon.co.za', 'Johannesburg'),
('Sanofi', 'HealthFirst Supplier', '0115550109', 'orders@sanofi.co.za', 'Johannesburg'),
('Bayer', 'HealthFirst Supplier', '0115550110', 'orders@bayer.co.za', 'Johannesburg'),
('Novartis', 'HealthFirst Supplier', '0115550111', 'orders@novartis.co.za', 'Johannesburg'),
('GSK', 'HealthFirst Supplier', '0115550112', 'orders@gsk.co.za', 'Johannesburg'),
('UCB', 'HealthFirst Supplier', '0115550113', 'orders@ucb.co.za', 'Johannesburg'),
('Organon', 'HealthFirst Supplier', '0115550114', 'orders@organon.co.za', 'Johannesburg'),
('AstraZeneca', 'HealthFirst Supplier', '0115550115', 'orders@astrazeneca.co.za', 'Johannesburg'),
('Viatris', 'HealthFirst Supplier', '0115550116', 'orders@viatris.co.za', 'Johannesburg'),
('Teva', 'HealthFirst Supplier', '0115550117', 'orders@teva.co.za', 'Johannesburg'),
('Karo Healthcare', 'HealthFirst Supplier', '0115550118', 'orders@karohealthcare.co.za', 'Johannesburg'),
('HRA Pharma', 'HealthFirst Supplier', '0115550119', 'orders@hrapharma.co.za', 'Johannesburg');

INSERT INTO medicines(name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Paracetamol 500mg', 'HealthCare', 'Tablet', 35.50, 100, 20, '2027-06-30', 1),
('Cough Syrup', 'CareChem', 'Syrup', 48.00, 35, 10, '2026-11-30', 2),
('Vitamin C', 'HealthCare', 'Tablet', 62.75, 8, 15, '2026-09-15', 1),
('Panado', 'Adcock Ingram', 'Tablets', 35.50, 100, 20, '2027-06-30', 3),
('Panado', 'Adcock Ingram', 'Syrup', 42.00, 80, 15, '2027-06-30', 3),
('Adcodol', 'Adcock Ingram', 'Tablets', 38.00, 75, 15, '2027-06-30', 3),
('Myprodol', 'Adcock Ingram', 'Capsules', 55.00, 65, 15, '2027-06-30', 3),
('Allergex', 'Adcock Ingram', 'Tablets', 45.00, 70, 15, '2027-06-30', 3),
('Allergex', 'Adcock Ingram', 'Syrup', 49.00, 60, 15, '2027-06-30', 3),
('Compral', 'Adcock Ingram', 'Tablets', 32.00, 55, 10, '2027-06-30', 3),
('Corenza C', 'Adcock Ingram', 'Tablets', 48.00, 60, 15, '2027-06-30', 3),
('Sinutab', 'Adcock Ingram', 'Tablets', 52.00, 55, 10, '2027-06-30', 3),
('Adco-Dol', 'Adcock Ingram', 'Tablets', 40.00, 75, 15, '2027-06-30', 3),
('Adco-Salbutamol', 'Adcock Ingram', 'Tablets', 46.00, 50, 10, '2027-06-30', 3),
('Adco-Zolpidem', 'Adcock Ingram', 'Tablets', 68.00, 45, 10, '2027-06-30', 3),
('Adco-Naproxen', 'Adcock Ingram', 'Tablets', 44.00, 60, 15, '2027-06-30', 3),
('Benylin', 'Johnson & Johnson', 'Syrup', 65.00, 40, 10, '2027-06-30', 4),
('Linctagon', 'Cipla', 'Capsules', 58.00, 50, 10, '2027-06-30', 5),
('Linctagon', 'Cipla', 'Syrup', 62.00, 45, 10, '2027-06-30', 5),
('Betadine', 'Kenvue', 'Solution', 72.00, 35, 8, '2027-06-30', 6),
('Strepsils', 'Reckitt', 'Lozenges', 39.00, 90, 20, '2027-06-30', 7),
('Gaviscon', 'Reckitt', 'Liquid', 78.00, 40, 10, '2027-06-30', 7),
('Gaviscon', 'Reckitt', 'Tablets', 69.00, 45, 10, '2027-06-30', 7),
('Nurofen', 'Reckitt', 'Tablets', 55.00, 65, 15, '2027-06-30', 7),
('Imodium', 'Kenvue', 'Capsules', 61.00, 40, 10, '2027-06-30', 6),
('Voltaren', 'Haleon', 'Gel', 85.00, 35, 8, '2027-06-30', 8),
('Voltaren', 'Haleon', 'Tablets', 58.00, 45, 10, '2027-06-30', 8),
('Telfast', 'Sanofi', 'Tablets', 75.00, 50, 10, '2027-06-30', 9),
('Otrivin', 'Haleon', 'Nasal Spray', 63.00, 40, 10, '2027-06-30', 8),
('Eno', 'Haleon', 'Powder', 35.00, 80, 15, '2027-06-30', 8),
('Disprin', 'Reckitt', 'Tablets', 29.00, 100, 20, '2027-06-30', 7),
('Calpol', 'Kenvue', 'Syrup', 58.00, 55, 10, '2027-06-30', 6),
('Buscopan', 'Sanofi', 'Tablets', 54.00, 60, 15, '2027-06-30', 9),
('Rennie', 'Bayer', 'Tablets', 42.00, 75, 15, '2027-06-30', 10),
('Panado Extra', 'Adcock Ingram', 'Tablets', 43.00, 65, 15, '2027-06-30', 3),
('Panado Sinus', 'Adcock Ingram', 'Tablets', 46.00, 60, 15, '2027-06-30', 3),
('Panado Cold & Flu', 'Adcock Ingram', 'Tablets', 49.00, 60, 15, '2027-06-30', 3),
('Mypaid', 'Adcock Ingram', 'Tablets', 39.00, 55, 10, '2027-06-30', 3),
('Cataflam', 'Novartis', 'Tablets', 64.00, 45, 10, '2027-06-30', 11),
('Flagyl', 'Sanofi', 'Tablets', 52.00, 40, 10, '2027-06-30', 9),
('Augmentin', 'GSK', 'Tablets', 95.00, 35, 8, '2027-06-30', 12),
('Amoxil', 'GSK', 'Capsules', 82.00, 40, 10, '2027-06-30', 12),
('Zyrtec', 'UCB', 'Tablets', 71.00, 50, 10, '2027-06-30', 13),
('Claritin', 'Bayer', 'Tablets', 69.00, 45, 10, '2027-06-30', 10),
('Aerius', 'Organon', 'Tablets', 76.00, 40, 10, '2027-06-30', 14),
('Nexium', 'AstraZeneca', 'Tablets', 88.00, 35, 8, '2027-06-30', 15),
('Losec', 'AstraZeneca', 'Capsules', 79.00, 40, 10, '2027-06-30', 15),
('Buscopan', 'Sanofi', 'Injection', 110.00, 25, 5, '2027-06-30', 9),
('Centrum', 'Haleon', 'Tablets', 125.00, 30, 8, '2027-06-30', 8),
('Sinutab PE', 'Adcock Ingram', 'Tablets', 55.00, 50, 10, '2027-06-30', 3),
('Vicks', 'Kenvue', 'Syrup', 59.00, 50, 10, '2027-06-30', 6),
('Vicks', 'Kenvue', 'Lozenges', 43.00, 60, 15, '2027-06-30', 6),
('Difflam', 'Viatris', 'Lozenges', 67.00, 40, 10, '2027-06-30', 16),
('Bonjela', 'Haleon', 'Gel', 57.00, 35, 8, '2027-06-30', 8),
('Canesten', 'Bayer', 'Cream', 74.00, 45, 10, '2027-06-30', 10),
('Sudocrem', 'Teva', 'Cream', 92.00, 35, 8, '2027-06-30', 17),
('Bepanthen', 'Bayer', 'Cream', 86.00, 40, 10, '2027-06-30', 10),
('E45', 'Karo Healthcare', 'Cream', 98.00, 30, 8, '2027-06-30', 18),
('NorLevo', 'HRA Pharma', 'Tablets', 115.00, 25, 5, '2027-06-30', 19);