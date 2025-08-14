import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BankingSystem {

    private String customerName;
    private int money;
    private int withdrawalMoney;
    private int accountNo;

    public BankingSystem(int accountNo, String name, int money, int withdrawalMoney) {
        this.accountNo = accountNo;
        this.customerName = name;
        this.money = money;
        this.withdrawalMoney = withdrawalMoney;
    }

    public void displayCustomer() {
        System.out.printf("Account No: %d, Name: %s, Money left: %d, Withdrawn money: %d%n",
                accountNo, customerName, money, withdrawalMoney);
    }

    public static BankingSystem findCustomerByAccountNo(List<BankingSystem> customers, int accountNo) {
        for (BankingSystem c : customers) {
            if (c.accountNo == accountNo)
                return c;
        }
        return null;
    }

    public void depositMoney(int amount) {
        if (amount > 0) {
            this.money += amount;
            System.out.println("Deposit successful. New balance: " + this.money);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdrawMoney(int amount) {
        if (amount > 0 && amount <= this.money) {
            this.money -= amount;
            this.withdrawalMoney += amount;
            System.out.println("Withdrawal successful. New balance: " + this.money);
        } else {
            System.out.println("Invalid or insufficient funds for withdrawal.");
        }
    }

    public static void SaveToFile(List<BankingSystem> customers) {
        try {
            File file = new File("Customer.json");
            if (file.createNewFile()) {
                System.out.println("New file created: " + file.getName());
            }
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write("[\n");
                for (int i = 0; i < customers.size(); i++) {
                    BankingSystem c = customers.get(i);
                    writer.write(String.format(
                            "  {\n" +
                                    "    \"accountNo\": %d,\n" +
                                    "    \"customerName\": \"%s\",\n" +
                                    "    \"moneyLeft\": %d,\n" +
                                    "    \"withdrawnMoney\": %d\n" +
                                    "  }%s\n",
                            c.accountNo, c.customerName, c.money, c.withdrawalMoney,
                            (i < customers.size() - 1) ? "," : ""));
                }
                writer.write("]\n");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while saving to file.");
            e.printStackTrace();
        }
    }

    public static List<BankingSystem> loadFromFile() {
        List<BankingSystem> customers = new ArrayList<>();
        try {
            File file = new File("Customer.json");
            if (!file.exists()) return customers;

            Scanner reader = new Scanner(file);
            StringBuilder json = new StringBuilder();
            while (reader.hasNextLine()) {
                json.append(reader.nextLine());
            }
            reader.close();

            String content = json.toString().replace("[", "").replace("]", "").trim();
            if (content.isEmpty()) return customers;

            String[] entries = content.split("\\},\\s*\\{");
            for (String entry : entries) {
                entry = entry.replace("{", "").replace("}", "").trim();
                String[] fields = entry.split(",");
                int accountNo = 0;
                String customerName = "";
                int moneyLeft = 0;
                int withdrawnMoney = 0;

                for (String field : fields) {
                    String[] kv = field.split(":");
                    if (kv.length < 2) continue;
                    String key = kv[0].replace("\"", "").trim();
                    String value = kv[1].replace("\"", "").trim();

                    switch (key) {
                        case "accountNo":
                            accountNo = Integer.parseInt(value);
                            break;
                        case "customerName":
                            customerName = value;
                            break;
                        case "moneyLeft":
                            moneyLeft = Integer.parseInt(value);
                            break;
                        case "withdrawnMoney":
                            withdrawnMoney = Integer.parseInt(value);
                            break;
                    }
                }
                customers.add(new BankingSystem(accountNo, customerName, moneyLeft, withdrawnMoney));
            }
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
        return customers;
    }

    public static int getIntInput(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.matches("\\d+")) {
                return Integer.parseInt(input);
            } else {
                System.out.println("Invalid input! Please enter numbers only.");
            }
        }
    }

    public static String getNameInput(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String name = sc.nextLine().trim();
            if (name.matches("[a-zA-Z ]+")) {
                return name;
            } else {
                System.out.println("Invalid name! Only alphabets and spaces are allowed.");
            }
        }
    }

    public static void addCustomer(List<BankingSystem> customers, Scanner sc, int nextAccountNo) {
        String name = getNameInput(sc, "Enter customer name: ");
        int moneyInAccount = getIntInput(sc, "Enter initial deposit: ");
        BankingSystem customer = new BankingSystem(nextAccountNo, name, moneyInAccount, 0);
        customers.add(customer);
        SaveToFile(customers);
        System.out.println("Customer added successfully! Generated Account No: " + nextAccountNo);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<BankingSystem> customers = loadFromFile();
        int nextAccountNo = customers.isEmpty() ? 1 : customers.get(customers.size() - 1).accountNo + 1;

        while (true) {
            System.out.println("\n--- Banking System Menu ---");
            System.out.println("1. Add customer");
            System.out.println("2. Deposit money");
            System.out.println("3. Withdraw money");
            System.out.println("4. Display customer info");
            System.out.println("5. Display all customers");
            System.out.println("6. Exit");
            int choice = getIntInput(sc, "Choose an option: ");

            switch (choice) {
                case 1:
                    addCustomer(customers, sc, nextAccountNo++);
                    break;
                case 2: {
                    int depAcc = getIntInput(sc, "Enter account number: ");
                    int depAmt = getIntInput(sc, "Enter amount to deposit: ");
                    BankingSystem depCustomer = findCustomerByAccountNo(customers, depAcc);
                    if (depCustomer != null) {
                        depCustomer.depositMoney(depAmt);
                        SaveToFile(customers);
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;
                }
                case 3: {
                    int witAcc = getIntInput(sc, "Enter account number: ");
                    int witAmt = getIntInput(sc, "Enter amount to withdraw: ");
                    BankingSystem witCustomer = findCustomerByAccountNo(customers, witAcc);
                    if (witCustomer != null) {
                        witCustomer.withdrawMoney(witAmt);
                        SaveToFile(customers);
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;
                }
                case 4: {
                    int dispAcc = getIntInput(sc, "Enter account number: ");
                    BankingSystem dispCustomer = findCustomerByAccountNo(customers, dispAcc);
                    if (dispCustomer != null) {
                        dispCustomer.displayCustomer();
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;
                }
                case 5:
                    if (customers.isEmpty()) {
                        System.out.println("No customers to display.");
                    } else {
                        for (BankingSystem c : customers) {
                            c.displayCustomer();
                        }
                    }
                    break;
                case 6:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
