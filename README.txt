HealthFirst Pharmacy Inventory System

Default login details
Admin: admin / admin123
Cashier: cashier / cash123

How to run
1. Install MySQL and run database.sql.
2. Add the MySQL Connector JAR file to the Java project classpath.
3. Open PIMSApplication.java in the src folder.
4. Set the MySQL credentials before starting the program. In PowerShell use `$env:PIMS_DB_USER = "root"` and `$env:PIMS_DB_PASSWORD = "your MySQL password"`.
5. Compile and run PIMSApplication.

After signing in, each staff member enters their name. The name is shown in the workspace and is stored with their account for sales history.

The Admin account can use medicine management, supplier management, user management, sales history and reports.
The Cashier account can use the point of sale screen, stock checking and their own sales history.

Completed invoices are saved automatically to sales history when checkout succeeds. The Save to history button closes the invoice without creating a receipt file. The Print bill button prints the invoice and confirms that it is saved in sales history.

The application uses prepared statements for login, medicine inserts and sales. A sale is saved in a transaction so the sale header, sale items and stock update are handled together.