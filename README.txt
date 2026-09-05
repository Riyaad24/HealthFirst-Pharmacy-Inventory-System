HealthFirst Pharmacy Inventory System

USER GUIDE

Default application accounts
Admin username: admin
Admin password: admin123
Cashier username: cashier
Cashier password: cash123

The login screen does not display these credentials. Use this README when testing the application.

SETUP

1. Install MySQL and start the MySQL service.
2. Open database.sql in MySQL Workbench and run the complete script.
3. The script creates the pims database, tables, sample users, suppliers and medicines.
4. Add the MySQL Connector JAR file to the Java classpath if running from source.
5. Set the local MySQL credentials before running. In PowerShell use `$env:PIMS_DB_USER = "root"` and `$env:PIMS_DB_PASSWORD = "your MySQL password"`.
6. Run the executable in submission\HealthFirstPharmacyInventorySystem or compile and run src\PIMSApplication.java.

The application can also ask for the MySQL password when the password environment variable is not set.

USING THE APPLICATION

After signing in, each staff member enters their name. The name is shown in the workspace and is stored with their account for sales history.

Administrators can use Point of Sale, medicine management, supplier management, cashier user management, sales history and reports. Administrators can create, read, edit and delete medicines and suppliers.

Cashiers can use Point of Sale, stock checking, sales history and tax category changes. Cashiers cannot add, edit or delete medicines.

To make a sale, enter the medicine ID and quantity, add the item to the cart, enter the customer name and surname, then choose Cash or Card. Cash payments ask for the amount tendered and calculate the change. Card payments ask for Visa or Mastercard and use a masked demonstration card number.

Prescribed medicines have 0% tax. Over the Counter medicines have 15% tax. The category is shown in the medicine tables and on the receipt.

Completed invoices are saved automatically to sales history when checkout succeeds. Save to history closes the invoice without creating a receipt file. Print bill prints the invoice. Receipts show the customer, staff member, products, subtotal, tax, total, payment method, tendered amount and change.

Use Sign out to return to the login screen. Sales history keeps the product names, customer name, payment method, time and staff member who processed each sale.