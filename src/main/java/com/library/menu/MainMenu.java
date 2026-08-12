package com.library.menu;

import com.library.model.Role;
import com.library.model.User;
import com.library.service.UserService;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry-point menu: login, then hand off to AdminMenu or BorrowerMenu
 * based on the logged-in user's role. No registration flow for
 * borrowers here — admins create accounts via "Add User" (per spec,
 * there is no self-signup/membership-card flow).
 */
public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService = new UserService();
    private final AdminMenu adminMenu = new AdminMenu(scanner);
    private final BorrowerMenu borrowerMenu = new BorrowerMenu(scanner);

    public void start() {
        System.out.println("=========================================");
        System.out.println("   LIBRARY MANAGEMENT SYSTEM");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Login");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> login();
                case "0" -> {
                    System.out.println("Goodbye.");
                    running = false;
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
        scanner.close();
    }

    private void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            Optional<User> userOpt = userService.login(email, password);
            if (userOpt.isEmpty()) {
                System.out.println("Invalid email or password.");
                return;
            }

            User user = userOpt.get();
            System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");

            if (user.getRole() == Role.ADMIN) {
                adminMenu.show(user);
            } else {
                borrowerMenu.show(user);
            }
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
        }
    }
}
