package org.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // 1. Sınıfın kendi nesnesini tuttuğu statik ve private değişken (Singleton Instance)
    private static DatabaseConnection instance;

    // 2. JDBC Bağlantı nesnesi
    private Connection connection;

    // MySQL bağlantı bilgileri koda gömülmez. Öncelik sırası:
    //   1) Ortam değişkenleri: DB_URL, DB_USER, DB_PASSWORD
    //   2) db.properties dosyası (önce çalışma dizini, sonra classpath): db.url, db.user, db.password
    //   3) Varsayılanlar: aşağıdaki localhost URL'i, "root" kullanıcısı ve boş şifre
    private static final String PROPERTIES_FILE = "db.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/envanter?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";

    // 3. Private Constructor: Dışarıdan "new DatabaseConnection()" yapılmasını KESİNLİKLE engeller.
    private DatabaseConnection() {
        Properties fileProperties = loadProperties();
        String url = resolve("DB_URL", "db.url", DEFAULT_URL, fileProperties);
        String username = resolve("DB_USER", "db.user", DEFAULT_USERNAME, fileProperties);
        String password = resolve("DB_PASSWORD", "db.password", DEFAULT_PASSWORD, fileProperties);
        try {
            // MySQL JDBC sürücüsünü belleğe yükle
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Sadece bir kere çalışacak olan bağlantı kurma işlemi
            this.connection = DriverManager.getConnection(url, username, password);
            System.out.println("Veritabanı bağlantısı başarıyla kuruldu.");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
        }
    }

    // Ortam değişkeni -> db.properties -> varsayılan değer sırasıyla ilk dolu değeri döndürür
    private static String resolve(String envName, String propertyKey, String defaultValue, Properties fileProperties) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        String fileValue = fileProperties.getProperty(propertyKey);
        if (fileValue != null && !fileValue.isEmpty()) {
            return fileValue;
        }
        return defaultValue;
    }

    // db.properties dosyasını önce çalışma dizininden, bulunamazsa classpath'ten okur
    private static Properties loadProperties() {
        Properties properties = new Properties();
        Path file = Paths.get(PROPERTIES_FILE);
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                properties.load(reader);
                return properties;
            } catch (IOException e) {
                System.err.println(PROPERTIES_FILE + " okunamadı: " + e.getMessage());
            }
        }
        try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in != null) {
                properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.err.println(PROPERTIES_FILE + " (classpath) okunamadı: " + e.getMessage());
        }
        return properties;
    }

    // 4. Global Erişim Noktası: Dış dünyanın nesneye ulaşabildiği tek yer
    public static DatabaseConnection getInstance() {
        try {
            // Eğer instance daha önce hiç oluşturulmamışsa VEYA bağlantı hiç kurulamamışsa/kopmuşsa/kapanmışsa yeni üret
            if (instance == null || instance.getConnection() == null || instance.getConnection().isClosed()) {
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
