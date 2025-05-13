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

            // 建資料庫並切換
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS movie_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;");
            stmt.execute("USE movie_booking;");

            // 使用者表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  uid INT AUTO_INCREMENT PRIMARY KEY,
                  email VARCHAR(255) UNIQUE NOT NULL,
                  password VARCHAR(255) NOT NULL,
                  birth_date DATE NOT NULL
                ) ENGINE=InnoDB;
            """);

            // 電影表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS movies (
                  uid INT AUTO_INCREMENT PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  duration INT NOT NULL,
                  description TEXT,
                  rating ENUM('G','PG','PG-13','R') NOT NULL DEFAULT 'G',
                  is_active BOOLEAN NOT NULL DEFAULT TRUE,
                  INDEX(idx_title) (title)
                ) ENGINE=InnoDB;
            """);

            // 放映廳表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS theaters (
                  uid INT AUTO_INCREMENT PRIMARY KEY,
                  type VARCHAR(50) NOT NULL,
                  seat_count INT NOT NULL
                ) ENGINE=InnoDB;
            """);

            // 座位表（同廳同座號唯一）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS seats (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  theater_uid INT NOT NULL,
                  seat_number VARCHAR(10) NOT NULL,
                  UNIQUE(theater_uid, seat_number),
                  FOREIGN KEY (theater_uid) REFERENCES theaters(uid) ON DELETE CASCADE
                ) ENGINE=InnoDB;
            """);

            // 放映場次表（含票價）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS showtimes (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  movie_uid INT NOT NULL,
                  theater_uid INT NOT NULL,
                  show_time DATETIME NOT NULL,
                  price DECIMAL(6,2) NOT NULL DEFAULT 250.00,
                  FOREIGN KEY (movie_uid) REFERENCES movies(uid) ON DELETE CASCADE,
                  FOREIGN KEY (theater_uid) REFERENCES theaters(uid) ON DELETE CASCADE,
                  INDEX(idx_movie_time) (movie_uid, show_time)
                ) ENGINE=InnoDB;
            """);

            // 訂票紀錄表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bookings (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  user_uid INT NOT NULL,
                  showtime_id INT NOT NULL,
                  booking_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE,
                  FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE,
                  INDEX(idx_user_booking) (user_uid)
                ) ENGINE=InnoDB;
            """);

            // 訂票座位對應表（同場次同座位不可重複）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS booking_seats (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  booking_id INT NOT NULL,
                  showtime_id INT NOT NULL,
                  seat_id INT NOT NULL,
                  FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
                  FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE,
                  FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
                  UNIQUE(showtime_id, seat_id)
                ) ENGINE=InnoDB;
            """);

            System.out.println("✅ 資料庫與資料表結構（加強版）建立完成。");

        } catch (SQLException e) {
            System.out.println("❌ 建立資料庫失敗：" + e.getMessage());
        }
    }
}
