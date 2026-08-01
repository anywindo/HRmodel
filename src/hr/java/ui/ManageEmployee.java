package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManageEmployee {

    private final Scanner scanner = new Scanner(System.in);
    private final List<String> employees = new ArrayList<>();

    public void showMenu() {
        int choice;

        do {
            System.out.println("\n=== Employee Management ===");
            System.out.println("1. View Employees");
            System.out.println("2. Add Employee");
            System.out.println("3. Update Employee");
            System.out.println("4. Delete Employee");
            System.out.println("5. Search Employee");
            System.out.println("0. Back");
            System.out.print("Choice: ");

            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Choice: ");
                scanner.next();
            }

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewEmployees();
                case 2 -> addEmployee();
                case 3 -> updateEmployee();
                case 4 -> deleteEmployee();
                case 5 -> searchEmployee();
                case 0 -> System.out.println("Returning...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void viewEmployees() {
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }

        System.out.println("\n=== Employee List ===");
        for (String employee : employees) {
            String[] data = employee.split("\\|");
            System.out.printf("ID: %s | Name: %s | Email: %s%n", data[0], data[1], data[2]);
        }
    }

    private void addEmployee() {
        System.out.println("\n=== Add Employee ===");

        System.out.print("Employee ID: ");
        String employeeId = scanner.nextLine().trim();

        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Middle Name: ");
        String middleName = scanner.nextLine().trim();

        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Country Code (e.g. +62): ");
        String countryCode = scanner.nextLine().trim();

        System.out.print("Phone Number: ");
        String phoneNumber = scanner.nextLine().trim();

        System.out.println("Gender:");
        System.out.println("1. Man");
        System.out.println("2. Woman");
        System.out.println("3. Non-binary");
        System.out.println("4. Agender");
        System.out.println("5. Genderfluid");
        System.out.println("6. Other");
        System.out.println("7. Prefer not to say");
        System.out.print("Choice: ");
        int gender = Integer.parseInt(scanner.nextLine());

        System.out.println("Sex:");
        System.out.println("1. Male");
        System.out.println("2. Female");
        System.out.println("3. Unspecified");
        System.out.print("Choice: ");
        int sex = Integer.parseInt(scanner.nextLine());

        System.out.print("Salary: ");
        String salary = scanner.nextLine().trim();

        System.out.println("Status:");
        System.out.println("1. Active");
        System.out.println("2. On Leave");
        System.out.println("3. Suspended");
        System.out.println("4. Resigned");
        System.out.println("5. Terminated");
        System.out.println("6. Retired");
        System.out.print("Choice: ");
        int status = Integer.parseInt(scanner.nextLine());

        System.out.println("\nEmployee captured successfully.");
        System.out.println("---------------------------------");
        System.out.println("Employee ID : " + employeeId);
        System.out.println("Name        : " + firstName + " " + middleName + " " + lastName);
        System.out.println("Email       : " + email);
        System.out.println("Phone       : " + countryCode + phoneNumber);
        System.out.println("Gender      : " + gender);
        System.out.println("Sex         : " + sex);
        System.out.println("Salary      : " + salary);
        System.out.println("Status      : " + status);

        employees.add(employeeId + "|" + firstName + " " + (middleName.isBlank() ? "" : middleName + " ") + lastName + "|" + email);
    }

    private void updateEmployee() {
        System.out.print("Enter Employee ID to update: ");
        String searchId = scanner.nextLine().trim();
        boolean found = false;
        for (int i = 0; i < employees.size(); i++) {
            String[] data = employees.get(i).split("\\|");
            if (data[0].equals(searchId)) {
                System.out.print("Enter new email: ");
                String newEmail = scanner.nextLine().trim();
                employees.set(i, data[0] + "|" + data[1] + "|" + newEmail);
                System.out.println("Employee email updated successfully.");
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Employee not found.");
        }
    }

    private void deleteEmployee() {
        System.out.print("Enter Employee ID to delete: ");
        String searchId = scanner.nextLine().trim();
        boolean removed = employees.removeIf(emp -> {
            String[] data = emp.split("\\|");
            return data[0].equals(searchId);
        });
        if (removed) {
            System.out.println("Employee deleted successfully.");
        } else {
            System.out.println("Employee not found.");
        }
    }

    private void searchEmployee() {
        System.out.print("Enter keyword to search: ");
        String keyword = scanner.nextLine().trim().toLowerCase();
        boolean found = false;
        for (String employee : employees) {
            String[] data = employee.split("\\|");
            if (data[0].toLowerCase().contains(keyword) ||
                data[1].toLowerCase().contains(keyword) ||
                data[2].toLowerCase().contains(keyword)) {
                if (!found) {
                    System.out.println("\n=== Search Results ===");
                }
                System.out.printf("ID: %s | Name: %s | Email: %s%n", data[0], data[1], data[2]);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching employees found.");
        }
    }
}
