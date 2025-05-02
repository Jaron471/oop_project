import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateMySQL {

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS movie_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;");
            stmt.execute("USE movie_booking;");

            // 使用者表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  uid INT AUTO_INCREMENT PRIMARY KEY,
                  email VARCHAR(255) UNIQUE NOT NULL,
                  password VARCHAR(255) NOT NULL,
                  birth_date DATE NOT NULL
                );
            """);

            // 電影表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS movies (
                  uid INT AUTO_INCREMENT PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  duration INT NOT NULL,
                  description TEXT,
                  rating VARCHAR(10),
                  is_active BOOLEAN DEFAULT TRUE
                );
            """);

            // 放映廳表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS theaters (
                  uid INT AUTO_INCREMENT PRIMARY KEY,
                  type VARCHAR(50),
                  seat_count INT NOT NULL
                );
            """);

            // 座位表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS seats (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  theater_uid INT,
                  seat_number VARCHAR(10),
                  FOREIGN KEY (theater_uid) REFERENCES theaters(uid) ON DELETE CASCADE
                );
            """);

            // 放映場次表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS showtimes (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  movie_uid INT,
                  theater_uid INT,
                  show_time DATETIME,
                  FOREIGN KEY (movie_uid) REFERENCES movies(uid) ON DELETE CASCADE,
                  FOREIGN KEY (theater_uid) REFERENCES theaters(uid) ON DELETE CASCADE
                );
            """);

            // 訂票紀錄表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bookings (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  user_uid INT,
                  showtime_id INT,
                  booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE,
                  FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE
                );
            """);

            // 訂票座位對應表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS booking_seats (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  booking_id INT,
                  seat_id INT,
                  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
                  FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE
                );
            """);

            System.out.println("✅ 資料庫與資料表結構建立完成。");

        } catch (SQLException e) {
            System.out.println("❌ 建立資料庫失敗：" + e.getMessage());
        }
    }
}
