package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // 1. Sınıfın kendi nesnesini tuttuğu statik ve private değişken (Singleton Instance)
    private static DatabaseConnection instance;

    // 2. JDBC Bağlantı nesnesi
    private Connection connection;

//    // MySQL Veritabanı bilgileri (Kendi localhost bilgilerine göre güncelleyebilirsin)
//    private static final String URL = "jdbc:mysql://localhost:3306/envanter?useSSL=false&serverTimezone=UTC";
//    private static final String USERNAME = "root";
//    private static final String PASSWORD = "arda010203"; // Kendi MySQL şifren

    private static final String URL = "jdbc:mysql://localhost:3306/envanter?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Fatih123"; // Kendi MySQL şifren

    // 3. Private Constructor: Dışarıdan "new DatabaseConnection()" yapılmasını KESİNLİKLE engeller.
    private DatabaseConnection() {
        try {
            // MySQL JDBC sürücüsünü belleğe yükle
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Sadece bir kere çalışacak olan bağlantı kurma işlemi
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Veritabanı bağlantısı başarıyla kuruldu.");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
        }
    }

    // 4. Global Erişim Noktası: Dış dünyanın nesneye ulaşabildiği tek yer
    public static DatabaseConnection getInstance() {
        try {
            // Eğer instance daha önce hiç oluşturulmamışsa VEYA bağlantı bir şekilde kopmuşsa/kapanmışsa yeni üret
            if (instance == null || instance.getConnection().isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            System.err.println("Bağlantı durumu kontrol edilemedi: " + e.getMessage());
        }
        return instance;
    }

    // DAO sınıflarının SQL sorgularını çalıştırmak için bağlantıyı çağıracağı metot
    public Connection getConnection() {
        return connection;
    }
}
