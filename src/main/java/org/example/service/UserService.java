package org.example.service;

import org.example.dao.impl.UserDAO;
import org.example.model.entity.User;
import org.example.model.enums.Role;
import org.example.util.PasswordUtil;

import java.util.List;
import java.util.regex.Pattern;

public class UserService {

    private final UserDAO userDAO;

    // Şifre kuralları için Regex (Düzenli İfade)
    // En az 8 karakter, bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermeli
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Kullanıcı girişi için kimlik doğrulama işlemini yapar.
     */
    public User login(String username, String password) throws IllegalArgumentException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanıcı adı boş bırakılamaz.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Şifre boş bırakılamaz.");
        }

        User user = userDAO.authenticate(username, PasswordUtil.hash(password));

        if (user == null) {
            throw new IllegalArgumentException("Hatalı kullanıcı adı veya şifre!");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("Bu kullanıcının hesabı pasife alınmıştır, sisteme giremez.");
        }

        return user;
    }

    /**
     * Yeni bir kullanıcı kaydederken şifre ve e-posta kurallarını denetler.
     */
    public boolean registerUser(User user) throws IllegalArgumentException {
        // 1. Email formatı kontrolü
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Geçerli bir e-posta adresi giriniz.");
        }

        // 2. Şifre karmaşıklığı kontrolü (Aşağıdaki yardımcı metot kullanılıyor)
        // Not: user.getPasswordHash() içinde henüz düz metin şifre olduğunu varsayıyoruz.
        validatePasswordStrength(user.getPasswordHash());

        // 3. Şifreyi güvenli hale getir (Hash'le)
        user.setPasswordHash(PasswordUtil.hash(user.getPasswordHash()));

        return userDAO.insert(user);
    }

    /**
     * Şifrenin güvenlik kurallarına uygun olup olmadığını kontrol eder.
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Şifre en az 8 karakter uzunluğunda olmalıdır.");
        }

        // Büyük harf kontrolü
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Şifre en az bir büyük harf içermelidir.");
        }

        // Küçük harf kontrolü
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Şifre en az bir küçük harf içermelidir.");
        }

        // Rakam kontrolü
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Şifre en az bir rakam içermelidir.");
        }

        // Özel karakter kontrolü (Yeni eklenen kural)
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new IllegalArgumentException("Şifre en az bir özel karakter (!@#$%^&* vb.) içermelidir.");
        }
    }

    public List<User> getAllUsers() {
        return userDAO.getAll();
    }

    public boolean updateUser(int userId, boolean isActive, String roleStr) {
        User user = userDAO.getById(userId);
        if (user != null) {
            user.setActive(isActive);
            user.setRole(Role.valueOf(roleStr));
            return userDAO.update(user);
        }
        return false;
    }

    public boolean updateUserInfo(int userId, String newUsername, String newEmail) {
        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException("Kullanıcı adı boş bırakılamaz.");
        }
        if (newEmail == null || newEmail.isBlank() || !newEmail.contains("@")) {
            throw new IllegalArgumentException("Geçerli bir e-posta adresi giriniz.");
        }
        return userDAO.updateUsernameAndEmail(userId, newUsername.trim(), newEmail.trim());
    }

    public boolean deleteUser(int targetUserId) throws IllegalStateException {
        User targetUser = userDAO.getById(targetUserId);
        if (targetUser == null) {
            throw new IllegalStateException("Silinmek istenen kullanıcı bulunamadı.");
        }
        if (targetUser.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Güvenlik gereği 'ADMIN' rolündeki kullanıcılar sistemden silinemez.");
        }
        return userDAO.delete(targetUserId);
    }
}