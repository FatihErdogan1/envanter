package org.example.controller;

import org.example.model.entity.User;
import org.example.model.enums.Role;
import org.example.service.UserService;

import java.util.List;

public class UserController {
    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public User login(String username, String password) throws IllegalArgumentException {
        return userService.login(username, password);
    }

    public boolean registerUser(String username, String password, String email, String roleStr) throws IllegalArgumentException {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password); // İleride hash'lenecek
        user.setEmail(email);
        user.setRole(Role.valueOf(roleStr));
        user.setActive(false);

        return userService.registerUser(user);
    }

    // UserController.java içine eklenecek metotlar
    public List<User> getAllUsers() {
        return userService.getAllUsers(); // UserService üzerinden tüm listeyi çeker
    }

    public boolean updateUserStatus(int userId, boolean isActive, String roleStr) {
        return userService.updateUser(userId, isActive, roleStr);
    }

    // UserController.java içine eklenecek metot
    public boolean deleteUser(int id) throws IllegalStateException {
        return userService.deleteUser(id);
    }

    public boolean updateUserInfo(int userId, String newUsername, String newEmail) {
        return userService.updateUserInfo(userId, newUsername, newEmail);
    }
}
