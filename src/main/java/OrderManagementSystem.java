import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/** A small console-based order manager using JDBC and PostgreSQL. */
public class OrderManagementSystem {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final String URL = setting("DB_URL", "jdbc:postgresql://localhost:5432/ecommerce_db");
    private static final String USER = setting("DB_USER", "postgres");
    private static final String PASSWORD = setting("DB_PASSWORD", "");

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Order Management System ===");
            System.out.println("1. Insert three sample orders");
            System.out.println("2. Find orders by status (quantity > 2)");
            System.out.println("3. Add quantity to an order");
            System.out.println("4. Update order status");
            System.out.println("5. Display total order value");
            System.out.println("6. Delete a Cancelled order");
            System.out.println("0. Exit");
            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> insertSampleOrders();
                    case 2 -> findOrdersByStatus();
                    case 3 -> addQuantity();
                    case 4 -> updateStatus();
                    case 5 -> displayTotalValue();
                    case 6 -> deleteCancelledOrder();
                    case 0 -> running = false;
                    default -> System.out.println("Please choose a listed option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                System.out.println("Check that PostgreSQL is running and the connection settings are correct.");
            }
        }
        INPUT.close();
        System.out.println("Goodbye.");
    }

    private static void insertSampleOrders() throws SQLException {
        String sql = "INSERT INTO Orders (order_id, customer_name, product_name, quantity, unit_price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Object[][] orders = {
                {1001, "Aarav Sharma", "Notebook", 4, 45.50, "Pending"},
                {1002, "Diya Patel", "Headphones", 2, 899.00, "Shipped"},
                {1003, "Kabir Singh", "USB Cable", 5, 149.00, "Delivered"}
        };

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] order : orders) {
                statement.setInt(1, (int) order[0]);
                statement.setString(2, (String) order[1]);
                statement.setString(3, (String) order[2]);
                statement.setInt(4, (int) order[3]);
                statement.setDouble(5, (double) order[4]);
                statement.setString(6, (String) order[5]);
                try {
                    statement.executeUpdate();
                    System.out.println("Inserted sample order " + order[0] + ".");
                } catch (SQLException e) {
                    if ("23505".equals(e.getSQLState())) {
                        System.out.println("Order ID " + order[0] + " already exists; skipped it.");
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private static void findOrdersByStatus() throws SQLException {
        System.out.print("Enter status: ");
        String status = INPUT.nextLine().trim();
        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet result = statement.executeQuery()) {
                printOrders(result);
            }
        }
    }

    private static void addQuantity() throws SQLException {
        int orderId = readInt("Enter order ID: ");
        int additional = readInt("Enter additional quantity: ");
        if (additional <= 0) {
            System.out.println("Additional quantity must be greater than zero.");
            return;
        }
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, additional);
            statement.setInt(2, orderId);
            if (statement.executeUpdate() == 0) {
                System.out.println("No order found with ID " + orderId + ".");
            } else {
                System.out.println("Order quantity updated.");
            }
        }
    }

    private static void updateStatus() throws SQLException {
        int orderId = readInt("Enter order ID: ");
        System.out.print("Enter new status (Pending, Shipped, Delivered): ");
        String status = INPUT.nextLine().trim();
        if (!status.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Shipped")
                && !status.equalsIgnoreCase("Delivered")) {
            System.out.println("Status must be Pending, Shipped, or Delivered.");
            return;
        }

        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizeStatus(status));
            statement.setInt(2, orderId);
            if (statement.executeUpdate() == 0) {
                System.out.println("No order found with ID " + orderId + ".");
                return;
            }
        }
        displayOrder(orderId);
    }

    private static String normalizeStatus(String status) {
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }

    private static void displayOrder(int orderId) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                printOrders(result);
            }
        }
    }

    private static void displayTotalValue() throws SQLException {
        String sql = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                System.out.printf("Total order value: %.2f%n", result.getDouble("total_value"));
            }
        }
    }

    private static void deleteCancelledOrder() throws SQLException {
        int orderId = readInt("Enter the order ID to delete: ");
        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setString(2, "Cancelled");
            if (statement.executeUpdate() == 0) {
                System.out.println("Order not found or its status is not Cancelled; nothing was deleted.");
            } else {
                System.out.println("Cancelled order deleted.");
            }
        }
    }

    private static void printOrders(ResultSet result) throws SQLException {
        boolean found = false;
        while (result.next()) {
            found = true;
            System.out.printf("ID: %d | Customer: %s | Product: %s | Quantity: %d | Unit price: %.2f | Status: %s%n",
                    result.getInt("order_id"), result.getString("customer_name"),
                    result.getString("product_name"), result.getInt("quantity"),
                    result.getDouble("unit_price"), result.getString("status"));
        }
        if (!found) {
            System.out.println("No matching orders found.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(INPUT.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}
