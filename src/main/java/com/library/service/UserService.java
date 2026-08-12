package com.library.service;

import com.library.model.Role;
import com.library.model.User;
import com.library.repository.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles login and user creation. Passwords are stored in plain text
 * on purpose — this project is scoped to Java + SQL fundamentals, not
 * security engineering (hashing would be the next natural step).
 */
public class UserService {

    private final UserRepository userRepository = new UserRepository();

    public Optional<User> login(String email, String password) throws SQLException {
        return userRepository.findByEmailAndPassword(email, password);
    }

    public User addUser(String name, String email, String password, Role role) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (userRepository.emailExists(email)) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        User user = new User(name, email, password, role);
        int id = userRepository.addUser(user);
        user.setId(id);
        return user;
    }
}
