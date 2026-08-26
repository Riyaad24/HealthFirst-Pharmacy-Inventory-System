HealthFirst Pharmacy Inventory Management System

Default login details
Admin: admin / admin123
Cashier: cashier / cash123

How to run
1. Install MySQL and run database.sql.
2. Add the MySQL Connector JAR file to the Java project classpath.
3. Open PIMSApplication.java in the src folder.
4. Change DB_USER and DB_PASSWORD if the local MySQL account is different.
5. Compile and run PIMSApplication.

The Admin account can use medicine management, supplier viewing, user viewing and reports.
The Cashier account can use the point of sale screen and stock checking.

The application uses prepared statements for login, medicine inserts and sales. A sale is saved in a transaction so the sale header, sale items and stock update are handled together.