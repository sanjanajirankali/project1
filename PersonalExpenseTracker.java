import java.util.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;

public class PersonalExpenseTracker 
{
    // Enum for Expense Types (Fixed or Variable)
    enum ExpenseType 
    {
        FIXED, VARIABLE, RECURRING
    }
    // Class to represent individual expense entries
    static class Expense 
    {
        String category;
        double amount;
        String date;
        ExpenseType type;
        String timestamp;
        String currency;
        public Expense(String category, double amount, String date, ExpenseType type, String currency) 
        {
            this.category = category;
            this.amount = amount;
            this.date = date;
            this.type = type;
            this.timestamp = java.time.LocalDateTime.now().toString();
            this.currency = currency;
        }

        @Override
        public String toString() 
        {
            return "Category: " + category + ", Amount: " + currency + amount + ", Date: " + date + ", Type: " + type + ", Timestamp: " + timestamp;
        }
    }
    // Class to represent the Expense Tracker
    static class ExpenseTracker
    {
        ArrayList<Expense> expenses;
        double totalExpenses;
        double monthlyLimit;
        Map<String, Double> categoryTotals;
        Map<String, Double> monthlyCategoryTotals;
        Map<String, Double> userProfileExpenses;
        String currency;
        Map<String, ArrayList<Expense>> recurringExpenses;
        public ExpenseTracker(double monthlyLimit, String currency) 
        {
            this.expenses = new ArrayList<>();
            this.totalExpenses = 0.0;
            this.monthlyLimit = monthlyLimit;
            this.categoryTotals = new HashMap<>();
            this.monthlyCategoryTotals = new HashMap<>();
            this.userProfileExpenses = new HashMap<>();
            this.currency = currency;
            this.recurringExpenses = new HashMap<>();
        }
        public void addExpense(String category, double amount, String date, ExpenseType type) 
        {
            if (amount > 0) 
            {
                if (totalExpenses + amount > monthlyLimit) 
                {
                    System.out.println("Warning: Adding this expense exceeds your monthly limit!");
                }
                expenses.add(new Expense(category, amount, date, type, currency));
                totalExpenses += amount;
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                // Update monthly category totals
                String month = getMonthFromDate(date);
                monthlyCategoryTotals.put(month, monthlyCategoryTotals.getOrDefault(month, 0.0) + amount);
                System.out.println("Expense added successfully!");
            }
             else 
             {
                System.out.println("Amount should be greater than zero.");
            }
        }
        public void addRecurringExpense(String category, double amount, String startDate, int intervalDays) 
        {
            ExpenseType type = ExpenseType.RECURRING;
            Expense recurringExpense = new Expense(category, amount, startDate, type, currency);
            recurringExpenses.putIfAbsent(category, new ArrayList<>());
            recurringExpenses.get(category).add(recurringExpense);
            System.out.println("Recurring expense added for category: " + category + " with an interval of " + intervalDays + " days.");
        }
        public void generateRecurringReport() 
        {
            if (recurringExpenses.isEmpty()) 
            {
                System.out.println("No recurring expenses recorded.");
            }
             else 
            {
                System.out.println("Recurring Expense Breakdown:");
                for (Map.Entry<String, ArrayList<Expense>> entry : recurringExpenses.entrySet()) 
                {
                    System.out.println("Category: " + entry.getKey());
                    for (Expense expense : entry.getValue()) 
                    {
                        System.out.println(expense);
                    }
                }
            }
        }
        public void deductExpense(String category, double amount) 
        {
            if (amount <= 0 || amount > totalExpenses) 
            {
                System.out.println("Invalid amount to deduct.");
                return;
            }
            boolean found = false;
            for (Expense expense : expenses) 
            {
                if (expense.category.equalsIgnoreCase(category) && expense.amount >= amount) 
                {
                    expense.amount -= amount;
                    totalExpenses -= amount;
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) - amount);
                    if (expense.amount == 0) 
                    {
                        expenses.remove(expense);
                    }
                    found = true;
                    System.out.println("Expense deducted successfully!");
                    break;
                }
            }
            if (!found) 
            {
                System.out.println("No matching expense found or insufficient amount in the category.");
            }
        }
        public void displayAllExpenses() 
        {
            if (expenses.isEmpty()) 
            {
                System.out.println("No expenses recorded yet.");
            }
             else 
             {
                System.out.println("All Expenses:");
                for (Expense expense : expenses) 
                {
                    System.out.println(expense);
                }
            }
        }
        public void displayTotalExpenses() 
        {
            System.out.println("Total Expenses: " + currency + totalExpenses);
            if (totalExpenses > monthlyLimit) 
            {
                System.out.println("Warning: You have exceeded your monthly expense limit!");
            }
            else 
            {
                System.out.println("You are within your monthly limit of " + currency + monthlyLimit);
            }
        }
        public void displayCategoryReport() 
        {
            if (categoryTotals.isEmpty()) 
            {
                System.out.println("No categories found.");
            }
             else 
             {
                System.out.println("Expense Breakdown by Category:");
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) 
                {
                    System.out.println("Category: " + entry.getKey() + ", Total: " + currency + entry.getValue());
                }
            }
        }
        public void displayMonthlyReport() 
        {
            if (monthlyCategoryTotals.isEmpty()) 
            {
                System.out.println("No monthly data found.");
            }
             else 
             {
                System.out.println("Monthly Expense Breakdown:");
                for (Map.Entry<String, Double> entry : monthlyCategoryTotals.entrySet()) 
                {
                    System.out.println("Month: " + entry.getKey() + ", Total Expenses: " + currency + entry.getValue());
                }
            }
        }
        public void saveToFile(String fileName) 
        {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) 
            {
                writer.write("Monthly Limit: " + monthlyLimit + "\n");
                for (Expense expense : expenses) 
                {
                    writer.write(expense.category + "," + expense.amount + "," + expense.date + "," + expense.type + "," + expense.timestamp + "," + expense.currency + "\n");
                }
                System.out.println("Data saved to file.");
            } 
            catch (IOException e) 
            {
                System.out.println("Error saving to file: " + e.getMessage());
            }
        }
        public void loadFromFile(String fileName)
         {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))
             {
                String line = reader.readLine();
                if (line != null && line.startsWith("Monthly Limit: "))
                 {
                    monthlyLimit = Double.parseDouble(line.replace("Monthly Limit: ", ""));
                }
                while ((line = reader.readLine()) != null)
                 {
                    String[] parts = line.split(",");
                    if (parts.length == 6) {
                        String category = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        String date = parts[2];
                        ExpenseType type = ExpenseType.valueOf(parts[3].toUpperCase());
                        String timestamp = parts[4];
                        String currency = parts[5];
                        addExpense(category, amount, date, type);
                    }
                }
                System.out.println("Data loaded from file.");
            } catch (IOException e) {
                System.out.println("Error loading from file: " + e.getMessage());
            }
         }
        public void updateMonthlyLimit(double newLimit) 
        {
            this.monthlyLimit = newLimit;
            System.out.println("Monthly expense limit updated to " + currency + newLimit);
        }
        public void sortExpensesByAmount() 
        {
            expenses.sort(Comparator.comparingDouble(e -> e.amount));
            System.out.println("Expenses sorted by amount.");
        }

        public void sortExpensesByDate() 
        {
            expenses.sort(Comparator.comparing(e -> e.date));
            System.out.println("Expenses sorted by date.");
        }
        public void generateMonthlyReport() 
        {
            double monthTotal = 0;
            for (Expense expense : expenses) 
            {
                monthTotal += expense.amount;
            }
            System.out.println("Monthly Report: Total expenses for the month: " + currency + monthTotal);
            if (monthTotal > monthlyLimit) 
            {
                System.out.println("Warning: You've exceeded your monthly limit by " + currency + (monthTotal - monthlyLimit));
            } else 
            {
                System.out.println("You're within the monthly limit by " + currency + (monthlyLimit - monthTotal));
            }
        }
        public void generateYearlyReport() 
        {
            double yearTotal = 0;
            for (Expense expense : expenses) 
            {
                yearTotal += expense.amount;
            }
            System.out.println("Yearly Report: Total expenses for the year: " + currency + yearTotal);
        }
        public void filterExpensesByCategory(String category) 
        {
            boolean found = false;
            for (Expense expense : expenses) 
            {
                if (expense.category.equalsIgnoreCase(category)) 
                {
                    System.out.println(expense);
                    found = true;
                }
            }
            if (!found) 
            {
                System.out.println("No expenses found in the category: " + category);
            }
        }
        public void filterExpensesByDate(String date) 
        {
            boolean found = false;
            for (Expense expense : expenses) 
            {
                if (expense.date.equals(date)) 
                {
                    System.out.println(expense);
                    found = true;
                }
            }
            if (!found) 
            {
                System.out.println("No expenses found on the date: " + date);
            }
        }
        public void generateCustomReport(String startDate, String endDate) 
        {
            double customTotal = 0;
            for (Expense expense : expenses) 
            {
                if (isWithinDateRange(expense.date, startDate, endDate)) 
                {
                    customTotal += expense.amount;
                }
            }
            System.out.println("Custom Report: Total expenses from " + startDate + " to " + endDate + ": " + currency + customTotal);
        }
        private boolean isWithinDateRange(String expenseDate, String startDate, String endDate) 
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate expDate = LocalDate.parse(expenseDate, formatter);
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            return (expDate.isEqual(start) || expDate.isAfter(start)) && (expDate.isEqual(end) || expDate.isBefore(end));
        }
        private String getMonthFromDate(String date) 
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(date, formatter);
            return localDate.getMonth().toString();
        }
        // Added Method to Track Savings Progress
        public void trackSavings(double savingsGoal) 
        {
            double savingsProgress = savingsGoal - totalExpenses;
            System.out.println("Savings Goal: " + currency + savingsGoal);
            if (savingsProgress > 0) 
            {
                System.out.println("You're on track! You have " + currency + savingsProgress + " left to reach your goal.");
            } else 
            {
                System.out.println("Great job! You've exceeded your savings goal by " + currency + (-savingsProgress));
            }
        }
        // Method to Reset Expenses at the Beginning of New Year
        public void resetExpensesForNewYear() 
        {
            expenses.clear();
            totalExpenses = 0.0;
            categoryTotals.clear();
            monthlyCategoryTotals.clear();
            recurringExpenses.clear();
            System.out.println("Expenses have been reset for the new year.");
        }
    }
    public static void main(String[] args) 
    {
        Scanner scanner = new Scanner(System.in);
        ExpenseTracker tracker = null;

        System.out.println("Welcome to Personal Expense Tracker!");
        System.out.print("Set your monthly expense limit: ");
        double monthlyLimit = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        System.out.print("Select your currency (e.g., USD, EUR, GBP): ");
        String currency = scanner.nextLine();
        tracker = new ExpenseTracker(monthlyLimit, currency);
        while (true) 
        {
            System.out.println("\nPersonal Expense Tracker");
            System.out.println("1. Add Expense");
            System.out.println("2. Deduct Expense");
            System.out.println("3. View All Expenses");
            System.out.println("4. View Total Expenses");
            System.out.println("5. Set Monthly Limit");
            System.out.println("6. View Category Report");
            System.out.println("7. View Monthly Report");
            System.out.println("8. Save Data to File");
            System.out.println("9. Load Data from File");
            System.out.println("10. Sort Expenses by Amount");
            System.out.println("11. Sort Expenses by Date");
            System.out.println("12. Generate Monthly Report");
            System.out.println("13. Generate Yearly Report");
            System.out.println("14. Filter Expenses by Category");
            System.out.println("15. Filter Expenses by Date");
            System.out.println("16. Generate Custom Report");
            System.out.println("17. Track Savings Progress");
            System.out.println("18. Reset Expenses for New Year");
            System.out.println("19. Add Recurring Expense");
            System.out.println("20. View Recurring Expenses");
            System.out.println("21. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            if (choice == 1) 
            {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter expense category: ");
                String category = scanner.nextLine();
                System.out.print("Enter expense amount: ");
                double amount = scanner.nextDouble();
                System.out.print("Enter expense date (yyyy-MM-dd): ");
                String date = scanner.next();
                System.out.print("Enter expense type (fixed/variable/recurring): ");
                String typeString = scanner.next().toUpperCase();
                ExpenseType type = ExpenseType.valueOf(typeString);
                tracker.addExpense(category, amount, date, type);
            }
             else if (choice == 2) 
             {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter category to deduct from: ");
                String category = scanner.nextLine();
                System.out.print("Enter amount to deduct: ");
                double amount = scanner.nextDouble();
                tracker.deductExpense(category, amount);
            }
             else if (choice == 3) 
             {
                tracker.displayAllExpenses();

            }
             else if (choice == 4) 
             {
                tracker.displayTotalExpenses();

            }
             else if (choice == 5) 
             {
                System.out.print("Enter new monthly limit: ");
                double newLimit = scanner.nextDouble();
                tracker.updateMonthlyLimit(newLimit);

            }
             else if (choice == 6) 
             {
                tracker.displayCategoryReport();

            }
             else if (choice == 7) 
             {
                tracker.displayMonthlyReport();

            }
             else if (choice == 8) 
             {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter filename to save data: ");
                String fileName = scanner.nextLine();
                tracker.saveToFile(fileName);

            }
             else if (choice == 9) 
             {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter filename to load data: ");
                String fileName = scanner.nextLine();
                tracker.loadFromFile(fileName);

            }
             else if (choice == 10)
              {
                tracker.sortExpensesByAmount();

            } 
             else if (choice == 11) 
             {
                tracker.sortExpensesByDate();

            } 
            else if (choice == 12) 
            {
                tracker.generateMonthlyReport();

            } 
            else if (choice == 13) 
            {
                tracker.generateYearlyReport();

            }
             else if (choice == 14) 
             {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter category to filter by: ");
                String category = scanner.nextLine();
                tracker.filterExpensesByCategory(category);

            }
             else if (choice == 15) 
             {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter date (yyyy-MM-dd) to filter by: ");
                String date = scanner.nextLine();
                tracker.filterExpensesByDate(date);
            }
             else if (choice == 16) 
             {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter start date (yyyy-MM-dd): ");
                String startDate = scanner.nextLine();
                System.out.print("Enter end date (yyyy-MM-dd): ");
                String endDate = scanner.nextLine();
                tracker.generateCustomReport(startDate, endDate);

            }
             else if (choice == 17) 
             {
                System.out.print("Enter your savings goal: ");
                double savingsGoal = scanner.nextDouble();
                tracker.trackSavings(savingsGoal);

            }
             else if (choice == 18) 
             {
                tracker.resetExpensesForNewYear();

            }
             else if (choice == 19) 
             {
                scanner.nextLine(); // Consume newline
                System.out.print("Enter recurring expense category: ");
                String category = scanner.nextLine();
                System.out.print("Enter recurring expense amount: ");
                double amount = scanner.nextDouble();
                System.out.print("Enter recurring expense start date (yyyy-MM-dd): ");
                String startDate = scanner.next();
                System.out.print("Enter interval in days for the recurring expense: ");
                int intervalDays = scanner.nextInt();
                tracker.addRecurringExpense(category, amount, startDate, intervalDays);

            }
             else if (choice == 20) 
             {
                tracker.generateRecurringReport();

            }
             else if (choice == 21) 
             {
                System.out.println("Exiting...");
                break;

            }
             else 
             {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }
}
