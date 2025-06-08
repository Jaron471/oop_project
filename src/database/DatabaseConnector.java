package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.InputStream;
import java.sql.SQLException;

public class DatabaseConnector {
    public static Connection connect() {
        try {
            // 載入設定檔
            Properties props = new Properties();
            try (InputStream input = DatabaseConnector.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    throw new RuntimeException("找不到 db.properties 設定檔");
                }
                props.load(input);
            }

            // 讀取屬性
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // 建立連線
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        connect(); // 測試連線
    }
}
