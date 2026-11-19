# Order Management System (Java JDBC + PostgreSQL)

This is a beginner-friendly Java console program for the OOPJ lab. It uses core Java, JDBC `PreparedStatement`, and PostgreSQL. The PostgreSQL JDBC driver is included through Maven.

## 1. Create the database and table in PostgreSQL

Run the first command while connected to the default `postgres` database (for example, in pgAdmin's Query Tool). Database creation must be run separately because PostgreSQL does not allow `CREATE DATABASE` inside a transaction:

```sql
CREATE DATABASE ecommerce_db;
```

Then connect to `ecommerce_db` in pgAdmin and run:

```sql
CREATE TABLE Orders (
    order_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    product_name VARCHAR(100),
    quantity INT,
    unit_price DOUBLE PRECISION,
    status VARCHAR(30)
);
```

In `psql`, connect to the server, run the `CREATE DATABASE` statement, then switch databases with the psql command `\connect ecommerce_db` before running `CREATE TABLE`. `\connect` is a psql command, not SQL.

The Java program inserts sample rows with IDs 1001, 1002, and 1003. If one ID already exists, it reports the duplicate and continues with the remaining rows.

## 2. Configure the connection

The default connection is `jdbc:postgresql://localhost:5432/ecommerce_db` with username `postgres` and an empty password. Set environment variables in the VS Code terminal to match your PostgreSQL setup:

```powershell
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "your_postgresql_password"
```

Optional `DB_URL` can override the default URL. The program does not store a password in the source code.

## 3. Run in VS Code

1. Install JDK 17 or later, PostgreSQL, Maven, and the VS Code **Extension Pack for Java**.
2. Open this project folder in VS Code.
3. In pgAdmin or `psql`, create the database and table using the commands above. Those SQL commands are run in PostgreSQL, not in the Java terminal.
4. In VS Code's PowerShell terminal, set `DB_USER` and `DB_PASSWORD` as above, then run:

   ```powershell
   mvn compile
   mvn exec:java -Dexec.mainClass=OrderManagementSystem
   ```

5. Use the program's numbered menu to demonstrate each operation.

## Menu operations

1. Insert three sample orders.
2. Display orders matching an entered status, with quantity greater than 2, ordered by unit price descending.
3. Increase an existing order's quantity.
4. Change status to Pending, Shipped, or Delivered and display that order.
5. Display `SUM(quantity * unit_price)` for all orders.
6. Delete an order only if its status is Cancelled.
0. Exit.

To demonstrate option 6, create or update an order with status `Cancelled` in PostgreSQL first; option 4 intentionally only permits Pending, Shipped, or Delivered.
