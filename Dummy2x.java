package Pen;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.Properties;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPCell;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.Multipart;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

public class Dummy2x {

    private int id;
    private String name;
    private String username;
    private int balance;

    public Dummy2x(int id, String name, String username, int balance) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.balance = balance;
    }

    private static Scanner scanner = new Scanner(System.in);
    private static Dummy2x currentCustomer;
    private static Map<String, int[]> productStocks = new HashMap<>();
    private static Map<String, ArrayList<Integer>> coupons = new HashMap<>();
    private static Map<String, String> lowStocks = new HashMap<>();
    private static Map<String, String> pastTransactions = new HashMap<>();
    private static Map<Integer, String> lowBalanceCustomers = new HashMap<>();
    private static int currentCustomerId;

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            System.out.println("Connected to the database successfully.");

            // Load product stocks
            loadProductStocks(connection);

            // Load coupons
            loadCoupons(connection);

            // Load low stocks
            loadLowStocks(connection);

            // Load past transactions
            loadPastTransactions(connection);

            // Load low balance customers
            loadLowBalance(connection);

            // Start the main menu
            mainMenu();

            // Close the connection
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void mainMenu() {
        int choice;
        do {
            System.out.println("\nMain Menu:");
            System.out.println("1. Login");
            System.out.println("2. New Customer");
            System.out.println("3. Exit");
            System.out.print("\nEnter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    newCustomer();
                    break;
                case 3:
                    System.out.println("Exiting the system...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 3);
    }

    private static void newCustomer() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            System.out.print("Enter your name : ");
            String newCustomerName = scanner.nextLine();
            System.out.print("Enter your email id : ");
            String newCustomerEmailID = scanner.nextLine();
            System.out.print("Enter your Password : ");
            String newCustomerPassword = scanner.nextLine();
            System.out.print("Amount to Shop wallet : ");
            Double newCustomerBalance = scanner.nextDouble();
            String insertCustomerSQL = "INSERT INTO Customer (name, emailid, password, balance) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertCustomerSQL);
            preparedStatement.setString(1, newCustomerName);
            preparedStatement.setString(2, newCustomerEmailID);
            preparedStatement.setString(3, newCustomerPassword);
            preparedStatement.setDouble(4, newCustomerBalance);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void login() {
        System.out.print("Enter your role (admin/customer): ");
        String role = scanner.nextLine();
        System.out.print("Enter your role password : ");
        String password = scanner.nextLine();

        if (role.equalsIgnoreCase("admin") && (password.equals("pass"))) {
            adminMenu();
        } else if (role.equalsIgnoreCase("customer") && (password.equals("pass"))) {
            customerMenu();
        } else {
            System.out.println("Invalid role. Please try again.");
        }
    }

    private static void adminMenu() {
        int choice;
        do {
            System.out.println("\nAdmin Menu:\n");
            System.out.println("1. Add new product");
            System.out.println("2. Manage stocks");
            System.out.println("3. Generate time limited coupon");
            System.out.println("4. View all transactions");
            System.out.println("5. View products with low stocks");
            System.out.println("6. Mail customers invoice details");
            System.out.println("7. View All Customers");
            System.out.println("8. View Customers with Low Balance");
            System.out.println("9. Search Transactions By Date ");
            System.out.println("10. Logout\n");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addNewProduct();
                    break;
                case 2:
                    manageStocks();
                    break;
                case 3:
                    generateCoupon();
                    break;
                case 4:
                    viewAllTransactions();
                    break;
                case 5:
                    viewLowStocks();
                    break;
                case 6:
                    mailCustomers();
                    break;
                case 7:
                    viewAllCustomers();
                    break;
                case 8:
                    viewLowBalance();
                    break;
                case 9:
                    searchTransactionByDate();
                    break;
                case 10:
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice.Please try again.");
            }
        } while (choice != 10);
    }

    private static void searchTransactionByDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        System.out.print("Enter transaction date (YYYY-MM-DD): ");
        String inputDateStr = scanner.nextLine();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM PastTransactions WHERE DATE(transaction_date) = ?");

            // Parse input date string to java.util.Date
            java.util.Date inputDate = dateFormat.parse(inputDateStr);

            // Set transaction date
            statement.setDate(1, new java.sql.Date(inputDate.getTime()));

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Print the header
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|                                                     Filtered Transactions                                                                 |");
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|   Transaction ID  |   Customer ID     |   Product Name                   |   Quantity   |   Price         |   Transaction Date            |");
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");

            // Print table data
            while (resultSet.next()) {
                System.out.printf("|   %-15s |   %-15s |   %-30s |   %-10d |   Rs. %-10d |   %-26s |\n",
                        resultSet.getString("transaction_id"),
                        resultSet.getString("customer_id"),
                        resultSet.getString("product_name"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("price"),
                        resultSet.getString("transaction_date"));
                System.out.println(
                        "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            }
            System.out.println();

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewLowBalance() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();
            loadLowBalance(connection);
            ResultSet resultSet = statement
                    .executeQuery("SELECT CustomerID, emailid FROM Customer WHERE balance < 10000");
            System.out.println("|-------------------------------------------|");
            System.out.println("|            Low Balance Customer           |");
            System.out.println("|-------------------------------------------|");
            System.out.println("|   Customer ID   |   Email ID              |");
            System.out.println("|-------------------------------------------|");

            // Print table data
            while (resultSet.next()) {
                int customerId = resultSet.getInt("CustomerID");
                String email = resultSet.getString("emailid");
                System.out.printf("|   %-13d |   %-17s |\n", customerId, email);
                System.out.println("|-------------------------------------------|");

            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void loadLowBalance(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement
                    .executeQuery("SELECT CustomerID, emailid FROM Customer WHERE balance < 10000");

            // Clear the previous data before loading new data
            lowBalanceCustomers.clear();

            // Populate the low balance customers HashMap
            while (resultSet.next()) {
                int customerId = resultSet.getInt("CustomerID");
                String email = resultSet.getString("emailid");
                lowBalanceCustomers.put(customerId, email);
            }

            // Close resources
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error loading low balance customers: " + e.getMessage());
        }
    }

    private static void viewAllCustomers() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management",
                    "root", "password");

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT CustomerID, name, emailid, balance FROM Customer");
            System.out.println(
                    "\n|----------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|                                    All Customers                                             |");
            System.out.println(
                    "|----------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|   Customer ID   |        Name         |                Email                |    Balance     |");
            System.out.println(
                    "|-----------------|---------------------|-------------------------------------|----------------|");

            // Print table data
            while (resultSet.next()) {
                System.out.printf("|   %-13d |   %-17s |   %-29s     |  %-13.2f |\n",
                        resultSet.getInt("CustomerID"),
                        resultSet.getString("name"),
                        resultSet.getString("emailid"),
                        resultSet.getDouble("balance"));
                System.out.println(
                        "|-----------------|---------------------|-------------------------------------|----------------|");
            }
            System.out.println();
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAllTransactions() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();
            loadPastTransactions(connection);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM PastTransactions");
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|                                                      All Transactions                                                                     |");
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|   Transaction ID  |   Customer ID     |   Product Name                   |   Quantity   |   Price         |   Transaction Date            |");
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");

            // Print table data
            while (resultSet.next()) {
                System.out.printf("|   %-15s |   %-15s |   %-30s |   %-10d |   Rs. %-10d |   %-26s |\n",
                        resultSet.getString("transaction_id"),
                        resultSet.getString("customer_id"),
                        resultSet.getString("product_name"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("price"),
                        resultSet.getString("transaction_date"));
                System.out.println(
                        "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            }
            System.out.println();

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewLowStocks() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();
            loadProductStocks(connection);
            loadLowStocks(connection);
            ResultSet resultSet = statement.executeQuery("SELECT * FROM ProductStocks WHERE stock_quantity < 5");

            System.out.println(
                    "|----------------------------------|------------------|");
            System.out.println(
                    "|          Product Name            |     Quantity     |");
            System.out.println(
                    "|----------------------------------|------------------|");

            // Print table data
            while (resultSet.next()) {
                System.out.printf("|   %-30s |   %-14d |\n",
                        resultSet.getString("product_name"),
                        resultSet.getInt("stock_quantity"));
            }
            System.out.println(
                    "|----------------------------------|------------------|");

            resultSet.close();
            loadLowStocks(connection);
            statement.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void mailCustomers() {
    	 final String username = "adminmail@outlook.com";
	        final String password = "adminpass";
	
	        Properties props = new Properties();
	        props.put("mail.smtp.auth", "true");
	        props.put("mail.smtp.starttls.enable", "true");
	        props.put("mail.smtp.host", "outlook.office365.com");
	        props.put("mail.smtp.port", "587");
	
	        Session session = Session.getInstance(props,
	          new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(username, password);
	            }	
	          });
	
	        try {
	
	            Message message = new MimeMessage(session);
	            message.setFrom(new InternetAddress("adminmail@outlook.com"));
	            message.setRecipients(Message.RecipientType.TO,
	                InternetAddress.parse("receivermail@gmail.com"));
	            message.setSubject("Test");
	            message.setText("THANKS FOR USING H E R O STYLUS");
	
	            Transport.send(message);
	
	            System.out.println("Done");
	
	        } catch (MessagingException e) {
	            throw new RuntimeException(e);
	        }
	    }

    private static void customerMenu() {
        String customerId;
        String password;
        String customerChoice = "no";

        do {
            System.out.print("\nExit  Customer Menu? (yes/no): ");
            customerChoice = scanner.nextLine();
            if (customerChoice.equalsIgnoreCase("yes"))
                break;
            else if (!customerChoice.equalsIgnoreCase("no"))
                break;
            System.out.print("\nEnter your customer ID: ");
            customerId = scanner.nextLine();
            System.out.print("Enter your password: ");
            password = scanner.nextLine();

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management",
                        "root", "password");
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "SELECT * FROM Customer WHERE CustomerID = " + customerId + " AND password = '" + password
                                + "'");

                if (resultSet.next()) {
                    currentCustomerId = Integer.parseInt(customerId);
                    System.out.println("Logged in successfully.");
                    currentCustomer = new Dummy2x(resultSet.getInt("CustomerID"), resultSet.getString("name"),
                            resultSet.getString("emailid"), resultSet.getInt("balance"));
                    customerSubMenu(); // remove the assignment statement
                } else {
                    System.out.println("Invalid customer ID or password. Please try again.");
                }
                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (customerChoice.equals("no"));
    }

    private static void customerSubMenu() {
        int choice;
        do {
            System.out.println("\nCustomer Menu:\n");
            System.out.println("1. View available coupons");
            System.out.println("2. Purchase products");
            System.out.println("3. View past transactions");
            System.out.println("4. Update balance");
            System.out.println("5. Logout");
            System.out.print("\nEnter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewCoupons();
                    break;
                case 2:
                    purchaseProducts();
                    break;
                case 3:
                    viewPastTransactions();
                    break;
                case 4:
                    System.out.print("Enter the amount to add to wallet : ");
                    Double addBalance = scanner.nextDouble();
                    updateBalance(currentCustomerId, addBalance);
                    break;
                case 5:
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 5);
    }

    private static void addNewProduct() {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter product price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter product quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();
            loadProductStocks(connection);
            statement.executeUpdate(
                    "INSERT INTO ProductStocks (product_name, stock_quantity,price) VALUES ('" + name + "', " + quantity
                            + ", " + price + ")");

            System.out.println("Product added successfully.");
            loadProductStocks(connection);
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void manageStocks() {
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();
            loadProductStocks(connection);
            statement.executeUpdate(
                    "UPDATE ProductStocks SET stock_quantity = " + quantity + " WHERE product_name = '" + name + "'");
            System.out.println("Product quantity updated successfully.");
            loadProductStocks(connection);
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void generateCoupon() {
        try {
            System.out.print("Enter coupon code: ");
            String code = scanner.nextLine();
            System.out.print("Enter discount percentage: ");
            int discount = scanner.nextInt();
            scanner.nextLine();

            LocalDate expirationDate = LocalDate.now().plusMonths(2);

            int expirationDateInt = Integer.parseInt(expirationDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")));

            // Create an ArrayList to store discount and expiration date
            ArrayList<Integer> couponData = new ArrayList<>();
            couponData.add(discount);
            couponData.add(expirationDateInt);

            coupons.put(code, couponData);

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO Coupons (coupon_code, discount_percentage, expiration_date) VALUES (?, ?, ?)");
            preparedStatement.setString(1, code);
            preparedStatement.setInt(2, discount);
            preparedStatement.setInt(3, expirationDateInt);

            preparedStatement.executeUpdate();

            System.out.println("Coupon generated successfully.");
            loadCoupons(connection);

            connection.close();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewCoupons() {
        System.out.println("\nAvailable Coupons:");
        for (Map.Entry<String, ArrayList<Integer>> entry : coupons.entrySet()) {
            String code = entry.getKey();
            ArrayList<Integer> data = entry.getValue();
            int discount = data.get(0);
            int expirationDate = data.get(1);
            System.out.println(code + " - " + discount + "% off until " + expirationDate);
        }
    }

    // Function to generate invoice
    public static void generateInvoice(String transactionUnique, double beforeDiscountTotal, double grandTotal) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root", "password")) {
            // Query to retrieve transaction details
            String transactionQuery = "SELECT transaction_id, product_name, quantity, price, transaction_date " +
                    "FROM PastTransactions " +
                    "WHERE transaction_id = ? AND customer_id = ?";
            PreparedStatement transactionStatement = connection.prepareStatement(transactionQuery);
            transactionStatement.setString(1, transactionUnique);
            transactionStatement.setInt(2, currentCustomerId); // Assuming currentCustomerId is available
            ResultSet transactionResult = transactionStatement.executeQuery();

            // Query to retrieve customer details
            String customerQuery = "SELECT name AS customer_name, emailid AS customer_email " +
                    "FROM Customer " +
                    "WHERE CustomerID = ?";
            PreparedStatement customerStatement = connection.prepareStatement(customerQuery);
            customerStatement.setInt(1, currentCustomerId); // Assuming currentCustomerId is available
            ResultSet customerResult = customerStatement.executeQuery();

            // Create PDF document
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("INV-" + transactionUnique + ".pdf"));
            document.open();

            // Add Brand Name as top Heading in Bold and Different Font
            Font brandFont = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
            Paragraph brandName = new Paragraph("HERO STYLUS", brandFont);
            brandName.setAlignment(Element.ALIGN_CENTER);
            brandName.setSpacingAfter(20); // Increase margin bottom
            document.add(brandName);

            // Add customer details
            String invId = "INV-"+ transactionUnique;
            document.add(new Paragraph("Invoice ID : "+ invId));
            // Fetch customer details
            if (customerResult.next()) {
                String customerName = customerResult.getString("customer_name");
                String customerEmail = customerResult.getString("customer_email");
                document.add(new Paragraph("Customer Name: " + customerName));
                document.add(new Paragraph("Email ID: " + customerEmail));
            }

            // Add transaction details in table format
            document.add(new Paragraph("\nTransaction Details:"));
            // Create table
            PdfPTable table = new PdfPTable(5); // Adjust the number of columns as needed
            table.setHorizontalAlignment(Element.ALIGN_CENTER); // Center-align the table
            // Add headers
            table.addCell("Transaction ID");
            table.addCell("Product");
            table.addCell("Quantity");
            table.addCell("Price");
            table.addCell("Transaction Date");
            // Add data from transaction query result
            while (transactionResult.next()) {
                table.addCell(transactionResult.getString("transaction_id"));
                table.addCell(transactionResult.getString("product_name"));
                table.addCell(Integer.toString(transactionResult.getInt("quantity")));
                table.addCell(Double.toString(transactionResult.getDouble("price")));
                table.addCell(transactionResult.getString("transaction_date"));
            }
            document.add(table);

            // Add spacing between table and price comparison after discount
            document.add(new Paragraph("\n\n"));

            // Add other details in classic invoice format
            // Add before discount total
            document.add(new Paragraph("Total Price Before Discount: " + beforeDiscountTotal));
            // Calculate and add discount
            double discount = 0;
            if(beforeDiscountTotal != grandTotal) {
                discount = 100 - (Math.abs(beforeDiscountTotal - grandTotal) / beforeDiscountTotal) * 100;
            }
            else {
                discount = 0;
            }            
            document.add(new Paragraph("Discount: " + discount + "%"));
            
            // Add Grand Total at right side
            Font totalFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
            Paragraph grandTotalPara = new Paragraph("Grand Total: " + grandTotal, totalFont);
            grandTotalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(grandTotalPara);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void purchaseProducts() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management",
                    "root", "password");

            String uuid = UUID.randomUUID().toString();
            String transactionUnique = "transaction" + uuid.substring(0, 3);
            loadProductStocks(connection);
            loadLowStocks(connection);
            loadPastTransactions(connection);
            loadCoupons(connection);
            double grandTotal = 0.0;
            int totalQuantity = 0;
            StringBuilder purchaseProductName = new StringBuilder();

            while (true) {
                System.out.println("\n");
                System.out.println(
                        "|--------------------------------------------------------------------|");
                System.out.println(
                        "|                         Available Products                         |");
                System.out.println(
                        "|--------------------------------------------------------------------|");
                System.out.println(
                        "|       Product Name          |   Quantity   |         Price         |");
                System.out.println(
                        "|-----------------------------|--------------|-----------------------|");
                // Print table data
                for (String productName : productStocks.keySet()) {
                    int quantity = productStocks.get(productName)[0];
                    int price = productStocks.get(productName)[1];
                    System.out.printf("|   %-25s |   %-10d |   Rs. %-15d |\n", productName, quantity, price);
                }
                System.out.println(
                        "|-----------------------------|--------------|-----------------------|");

                System.out.print("\nEnter product name to purchase (or 'done' to finish): ");
                String productName = scanner.nextLine();
                if ("done".equalsIgnoreCase(productName)) {
                    break;
                }
                purchaseProductName.append(productName).append(" ");
                System.out.print("Enter quantity: ");
                int quantity = scanner.nextInt();
                totalQuantity += quantity;
                scanner.nextLine();

                if (productStocks.containsKey(productName)) {
                    int price = productStocks.get(productName)[1];
                    int stock = productStocks.get(productName)[0];

                    if (stock >= quantity) {
                        grandTotal += (price * quantity);
                        updateStock(productName, (-1 * quantity));
                    } else {
                        System.out.println("Insufficient stock. Please try again later.");
                    }
                } else {
                    System.out.println("Product not found. Please try again later.");
                }
            }
            if (grandTotal == 0) {
                System.out.println("\nYou have not purchased anything !\n");
                customerSubMenu();
            }
            System.out.println("\nTotal price : Rs. " + grandTotal);
            System.out.print("\nDo you have a coupon? (yes/no): ");
            String couponChoice = scanner.nextLine();
            double beforeDiscountTotal = grandTotal;
            if ("yes".equalsIgnoreCase(couponChoice)) {
                grandTotal = applyCoupon(grandTotal);
            }
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "INSERT INTO PastTransactions (transaction_id, customer_id, product_name, quantity, price, transaction_date) VALUES ('"
                            + transactionUnique + "', '" + currentCustomerId + "', '" + purchaseProductName.toString()
                            + "', "
                            + totalQuantity + ", " + grandTotal + ", NOW())");

            System.out.println("\nProducts purchased successfully.");
            System.out.println("\nTransaction ID: " + transactionUnique);
            generateInvoice(transactionUnique,beforeDiscountTotal,grandTotal);
            loadCoupons(connection);
            loadLowStocks(connection);
            loadPastTransactions(connection);
            updateBalance(currentCustomerId, -grandTotal);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static double applyCoupon(double price) throws ParseException {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management",
                    "root", "password");
            System.out.print("Enter coupon code: ");
            String couponCode = scanner.nextLine();
            ArrayList<Integer> couponData = coupons.get(couponCode);
            if (couponData != null && couponData.size() == 2) {
                int discount = couponData.get(0);
                int expirationDateInt = couponData.get(1);

                // Get current date in DDMMYYYY format
                LocalDate currentDate = LocalDate.now();
                int currentDateInt = Integer.parseInt(currentDate.format(DateTimeFormatter.ofPattern("ddMMyyyy")));

                // To avoid truncation error

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
                Date date1 = simpleDateFormat.parse(String.valueOf(currentDateInt));
                Date date2 = simpleDateFormat.parse(String.valueOf(expirationDateInt));
                // Compare expiration date with current date
                if (date1.before(date2)) {
                    System.out.println("Discount applied: " + discount + " %");
                    double discountedPrice = price - (price * (discount / 100.0));
                    System.out.println("New total price: Rs. " + discountedPrice);

                    PreparedStatement insertCouponStmt = connection
                            .prepareStatement("INSERT INTO CustomerCoupons (customer_id, coupon_code) VALUES (?, ?)");
                    insertCouponStmt.setInt(1, currentCustomerId);
                    insertCouponStmt.setString(2, couponCode);
                    insertCouponStmt.executeUpdate();

                    // Remove used coupon from Coupons table
                    PreparedStatement removeCouponStmt = connection
                            .prepareStatement("DELETE FROM Coupons WHERE coupon_code = ?");
                    removeCouponStmt.setString(1, couponCode);
                    removeCouponStmt.executeUpdate();

                    coupons.remove(couponCode); // Remove used coupon from the list of available coupons
                    return discountedPrice;
                } else {
                    System.out.println("Coupon has expired.");
                }
            } else {
                System.out.println("Invalid coupon code or coupon data format.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error parsing expiration date: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return price;
    }

    private static void viewPastTransactions() {
        System.out.println("\nPast Transactions:");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();
            loadPastTransactions(connection);
            ResultSet resultSet = statement
                    .executeQuery("SELECT * FROM PastTransactions WHERE customer_id = " + currentCustomerId);
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|                                                     Past Transactions                                                                     |");
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            System.out.println(
                    "|   Transaction ID  |   Customer ID     |   Product Name                   |   Quantity   |   Price         |   Transaction Date            |");
            System.out.println(
                    "|-------------------------------------------------------------------------------------------------------------------------------------------|");

            // Print table data
            while (resultSet.next()) {
                System.out.printf("|   %-15s |   %-15s |   %-30s |   %-10d |   Rs. %-10d |   %-26s |\n",
                        resultSet.getString("transaction_id"),
                        resultSet.getString("customer_id"),
                        resultSet.getString("product_name"),
                        resultSet.getInt("quantity"),
                        resultSet.getInt("price"),
                        resultSet.getString("transaction_date"));
                System.out.println(
                        "|-------------------------------------------------------------------------------------------------------------------------------------------|");
            }
            System.out.println();
            resultSet.close();
            loadPastTransactions(connection);
            statement.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateBalance(int customerId, Double amount) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();

            // Get current balance
            ResultSet currentBalanceResult = statement.executeQuery(
                    "SELECT balance FROM Customer WHERE CustomerID = " + customerId);
            double currentBalance = 0.0;
            if (currentBalanceResult.next()) {
                currentBalance = currentBalanceResult.getDouble("balance");
            }
            System.out.println("Current Balance: " + currentBalance);
            currentBalanceResult.close();

            // Update balance
            statement.executeUpdate(
                    "UPDATE Customer SET balance = balance + " + amount + " WHERE CustomerID = " + customerId);

            // Get updated balance
            ResultSet updatedBalanceResult = statement.executeQuery(
                    "SELECT balance FROM Customer WHERE CustomerID = " + customerId);
            double updatedBalance = 0.0;
            if (updatedBalanceResult.next()) {
                updatedBalance = updatedBalanceResult.getDouble("balance");
            }
            System.out.println("Updated Balance: " + updatedBalance);
            updatedBalanceResult.close();

            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateStock(String productName, int quantity) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/store_management", "root",
                    "password");
            Statement statement = connection.createStatement();
            loadProductStocks(connection);
            statement.executeUpdate(
                    "UPDATE ProductStocks SET stock_quantity = stock_quantity + " + quantity + " WHERE product_name = '"
                            + productName + "'");

            statement.close();
            loadProductStocks(connection);
            loadLowStocks(connection);
            connection.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void loadProductStocks(Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM ProductStocks");

        while (resultSet.next()) {
            String name = resultSet.getString("product_name");
            int quantity = resultSet.getInt("stock_quantity");
            int price = resultSet.getInt("price");

            productStocks.put(name, new int[] { quantity, price });
        }

        resultSet.close();
        statement.close();
    }

    private static void loadCoupons(Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM Coupons");

        while (resultSet.next()) {
            String code = resultSet.getString("coupon_code");
            int expirationDate = resultSet.getInt("expiration_date");
            int discount = resultSet.getInt("discount_percentage");

            // Create an ArrayList to store discount and expiration date
            ArrayList<Integer> couponData = new ArrayList<>();
            couponData.add(discount);
            couponData.add(expirationDate);

            // Put coupon code and coupon data into the map
            coupons.put(code, couponData);
        }

        resultSet.close();
        statement.close();
    }

    private static void loadLowStocks(Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM ProductStocks WHERE stock_quantity < 5");

        while (resultSet.next()) {
            String name = resultSet.getString("product_name");
            int quantity = resultSet.getInt("stock_quantity");

            lowStocks.put(name, String.valueOf(quantity));
        }

        resultSet.close();
        statement.close();
    }

    private static void loadPastTransactions(Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement
                .executeQuery("SELECT * FROM PastTransactions WHERE customer_id = " + currentCustomerId);

        while (resultSet.next()) {
            String transactionId = resultSet.getString("transaction_id");
            String customerId = resultSet.getString("customer_id");
            String productName = resultSet.getString("product_name");
            int quantity = resultSet.getInt("quantity");
            int totalPrice = resultSet.getInt("price");

            pastTransactions.put(transactionId, customerId + ":" + productName + ":" + quantity + ":" + totalPrice);
        }

        resultSet.close();
        statement.close();
    }
}
