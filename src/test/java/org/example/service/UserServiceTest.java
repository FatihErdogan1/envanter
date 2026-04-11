package org.example.service;

import org.example.dao.impl.UserDAO;
import org.example.model.entity.User;
import org.example.model.enums.Role;
import org.example.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService İş Kuralları Testleri")
class UserServiceTest {

    @Test
    @DisplayName("Kullanıcı adı null ise IllegalArgumentException fırlatmalı")
    void login_nullUsername_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            assertThrows(IllegalArgumentException.class, () -> service.login(null, "Sifre123!"));
        }
    }

    @Test
    @DisplayName("Kullanıcı adı boş ise IllegalArgumentException fırlatmalı")
    void login_emptyUsername_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            assertThrows(IllegalArgumentException.class, () -> service.login("  ", "Sifre123!"));
        }
    }

    @Test
    @DisplayName("Şifre boş ise IllegalArgumentException fırlatmalı")
    void login_emptyPassword_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            assertThrows(IllegalArgumentException.class, () -> service.login("admin", "  "));
        }
    }

    @Test
    @DisplayName("DAO null döndürdüğünde (kullanıcı bulunamadı) IllegalArgumentException fırlatmalı")
    void login_userNotFound_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.authenticate(anyString(), anyString())).thenReturn(null))) {

            UserService service = new UserService();
            assertThrows(IllegalArgumentException.class, () -> service.login("yanlis", "Sifre123!"));
        }
    }

    @Test
    @DisplayName("Hesabı pasif olan kullanıcı giriş yapamamalı → IllegalStateException")
    void login_inactiveUser_throwsException() {
        User inaktif = new User(1, "ali", "Sifre123!", "ali@test.com", Role.STAFF, false);

        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.authenticate(anyString(), anyString())).thenReturn(inaktif))) {

            UserService service = new UserService();
            assertThrows(IllegalStateException.class, () -> service.login("ali", "Sifre123!"));
        }
    }

    @Test
    @DisplayName("Geçerli aktif kullanıcı giriş yapabilmeli ve User nesnesi dönmeli")
    void login_validActiveUser_returnsUser() {
        User aktif = new User(1, "admin", "Sifre123!", "admin@test.com", Role.ADMIN, true);
        String hashedPassword = PasswordUtil.hash("Sifre123!");

        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.authenticate("admin", hashedPassword)).thenReturn(aktif))) {

            UserService service = new UserService();
            User result = service.login("admin", "Sifre123!");

            assertNotNull(result);
            assertEquals("admin", result.getUsername());
        }
    }

    @Test
    @DisplayName("E-posta @ içermiyorsa IllegalArgumentException fırlatmalı")
    void registerUser_invalidEmail_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            User user = new User(0, "yeni", "Sifre123!", "gecersizemail.com", Role.STAFF, true);

            assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
        }
    }

    @Test
    @DisplayName("Şifre 8 karakterden kısa ise IllegalArgumentException fırlatmalı")
    void registerUser_shortPassword_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            // 7 karakterlik yetersiz şifre
            User user = new User(0, "yeni", "Ab123!4", "yeni@test.com", Role.STAFF, true);

            assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
        }
    }

    @Test
    @DisplayName("Şifre büyük harf içermiyorsa IllegalArgumentException fırlatmalı")
    void registerUser_noUppercase_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            // Hepsi küçük harf
            User user = new User(0, "yeni", "sifre123!", "yeni@test.com", Role.STAFF, true);

            assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
        }
    }

    @Test
    @DisplayName("Şifre rakam içermiyorsa IllegalArgumentException fırlatmalı")
    void registerUser_noDigit_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            // Rakam yok
            User user = new User(0, "yeni", "SifreOzel!", "yeni@test.com", Role.STAFF, true);

            assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
        }
    }

    @Test
    @DisplayName("Şifre özel karakter içermiyorsa IllegalArgumentException fırlatmalı")
    void registerUser_noSpecialChar_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            // Özel karakter yok
            User user = new User(0, "yeni", "Sifre1234", "yeni@test.com", Role.STAFF, true);

            assertThrows(IllegalArgumentException.class, () -> service.registerUser(user));
        }
    }

    @Test
    @DisplayName("Tam 8 karakterlik güçlü şifre geçerlidir ve DAO'ya hash'lenmiş olarak gönderilmeli")
    void registerUser_exactEightCharStrongPassword_isValid() {
        String strongPass = "Abc123!4";
        try (MockedConstruction<UserDAO> mockedDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            UserService service = new UserService();
            User user = new User(0, "yeni", strongPass, "yeni@test.com", Role.STAFF, true);

            boolean result = service.registerUser(user);

            assertTrue(result);
            assertNotEquals(strongPass, user.getPasswordHash());
            assertEquals(PasswordUtil.hash(strongPass), user.getPasswordHash());
            verify(mockedDao.constructed().get(0), times(1)).insert(user);
        }
    }

    @Test
    @DisplayName("Geçerli kullanıcı kaydında DAO insert hash'lenmiş şifreyle çağrılmalı")
    void registerUser_validUser_callsDaoInsert() {
        String validPass = "Güvenli123!";
        try (MockedConstruction<UserDAO> mockedDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.insert(any())).thenReturn(true))) {

            UserService service = new UserService();
            User user = new User(0, "yenikullanici", validPass, "yeni@test.com", Role.STAFF, false);

            boolean result = service.registerUser(user);

            assertTrue(result);
            assertNotEquals(validPass, user.getPasswordHash());
            assertEquals(PasswordUtil.hash(validPass), user.getPasswordHash());
            verify(mockedDao.constructed().get(0), times(1)).insert(user);
        }
    }

    @Test
    @DisplayName("getAllUsers çağrıldığında DAO getAll sonucu dönmeli")
    void getAllUsers_returnsList() {
        List<User> mockList = Arrays.asList(
                new User(1, "admin", "hash1", "admin@test.com", Role.ADMIN, true),
                new User(2, "ali", "hash2", "ali@test.com", Role.STAFF, true)
        );

        try (MockedConstruction<UserDAO> mockedDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getAll()).thenReturn(mockList))) {

            UserService service = new UserService();

            List<User> result = service.getAllUsers();

            assertEquals(2, result.size());
            verify(mockedDao.constructed().get(0), times(1)).getAll();
        }
    }

    @Test
    @DisplayName("updateUser: kullanıcı bulunamazsa false dönmeli")
    void updateUser_userNotFound_returnsFalse() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getById(anyInt())).thenReturn(null))) {

            UserService service = new UserService();

            boolean result = service.updateUser(99, true, "STAFF");

            assertFalse(result);
        }
    }

    @Test
    @DisplayName("updateUser: geçerli kullanıcıda rol ve aktiflik güncellenmeli, DAO update çağrılmalı")
    void updateUser_existingUser_updatesAndReturnsTrue() {
        User existing = new User(2, "ali", "hash", "ali@test.com", Role.STAFF, true);

        try (MockedConstruction<UserDAO> mockedDao = mockConstruction(UserDAO.class, (mock, ctx) -> {
            when(mock.getById(2)).thenReturn(existing);
            when(mock.update(existing)).thenReturn(true);
        })) {
            UserService service = new UserService();

            boolean result = service.updateUser(2, false, "ADMIN");

            assertTrue(result);
            assertFalse(existing.isActive());
            assertEquals(Role.ADMIN, existing.getRole());
            verify(mockedDao.constructed().get(0), times(1)).update(existing);
        }
    }

    @Test
    @DisplayName("Silinmek istenen kullanıcı bulunamazsa IllegalStateException fırlatmalı")
    void deleteUser_userNotFound_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getById(anyInt())).thenReturn(null))) {

            UserService service = new UserService();
            assertThrows(IllegalStateException.class, () -> service.deleteUser(99));
        }
    }

    @Test
    @DisplayName("ADMIN rolündeki kullanıcı silinemez → IllegalStateException")
    void deleteUser_adminUser_throwsException() {
        User admin = new User(1, "admin", "Sifre123!", "admin@test.com", Role.ADMIN, true);

        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.getById(1)).thenReturn(admin))) {

            UserService service = new UserService();
            assertThrows(IllegalStateException.class, () -> service.deleteUser(1));
        }
    }

    @Test
    @DisplayName("STAFF kullanıcısı silinebilir, DAO delete çağrılmalı")
    void deleteUser_staffUser_callsDaoDelete() {
        User staff = new User(2, "ali", "Sifre123!", "ali@test.com", Role.STAFF, true);

        try (MockedConstruction<UserDAO> mockedDao = mockConstruction(UserDAO.class, (mock, ctx) -> {
            when(mock.getById(2)).thenReturn(staff);
            when(mock.delete(2)).thenReturn(true);
        })) {
            UserService service = new UserService();

            boolean result = service.deleteUser(2);

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1)).delete(2);
        }
    }

    @Test
    @DisplayName("updateUserInfo: boş kullanıcı adı verilirse IllegalArgumentException fırlatmalı")
    void updateUserInfo_blankUsername_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            assertThrows(IllegalArgumentException.class, () -> service.updateUserInfo(1, "   ", "mail@test.com"));
        }
    }

    @Test
    @DisplayName("updateUserInfo: geçersiz e-posta verilirse IllegalArgumentException fırlatmalı")
    void updateUserInfo_invalidEmail_throwsException() {
        try (MockedConstruction<UserDAO> ignoredDao = mockConstruction(UserDAO.class)) {
            UserService service = new UserService();
            assertThrows(IllegalArgumentException.class, () -> service.updateUserInfo(1, "ahmet", "gecersizmail.com"));
        }
    }

    @Test
    @DisplayName("updateUserInfo: geçerli veride DAO updateUsernameAndEmail çağrılmalı")
    void updateUserInfo_validData_callsDaoMethod() {
        try (MockedConstruction<UserDAO> mockedDao = mockConstruction(UserDAO.class,
                (mock, ctx) -> when(mock.updateUsernameAndEmail(5, "ahmet", "ahmet@test.com")).thenReturn(true))) {

            UserService service = new UserService();
            boolean result = service.updateUserInfo(5, "ahmet", "ahmet@test.com");

            assertTrue(result);
            verify(mockedDao.constructed().get(0), times(1))
                    .updateUsernameAndEmail(5, "ahmet", "ahmet@test.com");
        }
    }
}