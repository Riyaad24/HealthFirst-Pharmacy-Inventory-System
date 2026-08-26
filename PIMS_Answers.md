# HealthFirst Pharmacy Inventory System

## System design

The system uses a MySQL relational database and a Java Swing interface. The tables are separated so that one supplier can provide many medicines, one user can process many sales, and one sale can contain many sale items. The sale_items table prevents repeating several medicine columns inside the sales table.

## Login and access control

The login screen checks the username and password with a prepared JDBC statement. After a successful login, the role is read from the users table. An Admin receives tabs for medicines, suppliers, users and reports. A Cashier receives only Point of Sale and Stock Check tabs. This means that the user interface follows the role stored in the database.

## Point of Sale process

The cashier enters a medicine number and quantity. The program checks the available stock before adding the medicine to the cart. During checkout it creates one record in sales and one record for each cart line in sale_items. It then reduces quantity_in_stock and displays a bill window. The database transaction is committed only when all parts of the sale succeed.

## Reports

The report screen provides four reports. The sales report groups sales by date and calculates the total amount. The item wise report shows how many units of each medicine were sold. The low stock report compares quantity_in_stock with reorder_level. The expiry report finds medicines with an expiry date between today and one month from today.

## Security and improvements

This student version stores simple sample passwords because the brief is for a demonstration. A production system should store password hashes, use different database credentials, validate every input, and record an audit trail. The database user should also have only the permissions required by the application.

The source code is in src/PIMSApplication.java and the database setup is in database.sql. The executable and screenshots would be created on the testing computer after the MySQL connection and Java Connector have been configured.