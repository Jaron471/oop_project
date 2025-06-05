import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateMySQL {

    public static void main(String[] args) {
        String url  = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pwd  = "Jaron471";

        try (Connection conn = DriverManager.getConnection(url, user, pwd);
             Statement stmt = conn.createStatement()) {

            // 1. 建資料庫並切換
            stmt.executeUpdate("""
                CREATE DATABASE IF NOT EXISTS movie_booking
                  CHARACTER SET utf8mb4
                  COLLATE utf8mb4_general_ci;
            """);
            stmt.execute("USE movie_booking;");

            // 2. 使用者表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                  uid        INT AUTO_INCREMENT PRIMARY KEY,
                  email      VARCHAR(255) UNIQUE NOT NULL,
                  password   VARCHAR(255) NOT NULL,
                  birth_date DATE NOT NULL,
                  INDEX idx_users_email (email)
                ) ENGINE=InnoDB;
            """);

            // 3. 電影表（新增 image_path 欄位，用來存海報路徑）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS movies (
                  uid          INT AUTO_INCREMENT PRIMARY KEY,
                  title        VARCHAR(255)       NOT NULL,
                  duration     INT                NOT NULL,
                  description  TEXT,
                  image_path   VARCHAR(255)       DEFAULT NULL,
                  rating       ENUM(
                                      '普遍級',
                                      '保護級',
                                      '輔導12歲級',
                                      '輔導15歲級',
                                      '限制級'
                                   ) NOT NULL,
                  is_active    BOOLEAN            NOT NULL DEFAULT TRUE,
                  CHECK (duration > 0)
                ) ENGINE=InnoDB;
            """);

            // 4. 放映廳表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS theaters (
                  uid         INT AUTO_INCREMENT PRIMARY KEY,
                  hall_type   ENUM('大廳','小廳') NOT NULL,
                  seat_count  INT                NOT NULL,
                  CHECK (seat_count > 0)
                ) ENGINE=InnoDB;
            """);

            // 5. 座位表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS seats (
                  id            INT AUTO_INCREMENT PRIMARY KEY,
                  theater_uid   INT                 NOT NULL,
                  seat_row      CHAR(1)             NOT NULL,
                  seat_col      INT                 NOT NULL,
                  UNIQUE KEY uk_seat (theater_uid, seat_row, seat_col),
                  FOREIGN KEY (theater_uid)
                    REFERENCES theaters(uid)
                    ON DELETE CASCADE
                ) ENGINE=InnoDB;
            """);

            // 6. 放映場次表（預設票價改 0）
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS showtimes (
                  id           INT AUTO_INCREMENT PRIMARY KEY,
                  movie_uid    INT     NOT NULL,
                  theater_uid  INT     NOT NULL,
                  show_time    DATETIME NOT NULL,
                  price        DECIMAL(6,2) NOT NULL DEFAULT 0.00,
                  UNIQUE KEY uk_showtime (theater_uid, show_time),
                  FOREIGN KEY (movie_uid)
                    REFERENCES movies(uid)
                    ON DELETE RESTRICT,
                  FOREIGN KEY (theater_uid)
                    REFERENCES theaters(uid)
                    ON DELETE RESTRICT,
                  INDEX idx_showtime_movie_time (movie_uid, show_time)
                ) ENGINE=InnoDB;
            """);

            // 7. 訂票紀錄表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS bookings (
                  id             INT AUTO_INCREMENT PRIMARY KEY,
                  user_uid       INT      NOT NULL,
                  showtime_id    INT      NOT NULL,
                  booking_time   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  status         ENUM('BOOKED','CANCELLED') NOT NULL DEFAULT 'BOOKED',
                  cancel_time    TIMESTAMP NULL DEFAULT NULL,
                  FOREIGN KEY (user_uid)
                    REFERENCES users(uid)
                    ON DELETE RESTRICT,
                  FOREIGN KEY (showtime_id)
                    REFERENCES showtimes(id)
                    ON DELETE RESTRICT,
                  INDEX idx_bookings_user (user_uid),
                  INDEX idx_bookings_showtime (showtime_id)
                ) ENGINE=InnoDB;
            """);

            // 8. 訂票座位表
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS booking_seats (
                  id           INT AUTO_INCREMENT PRIMARY KEY,
                  booking_id   INT NOT NULL,
                  showtime_id  INT NOT NULL,
                  seat_id      INT NOT NULL,
                  FOREIGN KEY (booking_id)
                    REFERENCES bookings(id)
                    ON DELETE CASCADE,
                  FOREIGN KEY (showtime_id)
                    REFERENCES showtimes(id)
                    ON DELETE CASCADE,
                  FOREIGN KEY (seat_id)
                    REFERENCES seats(id)
                    ON DELETE RESTRICT,
                  UNIQUE KEY uk_booking_seat (showtime_id, seat_id)
                ) ENGINE=InnoDB;
            """);

            System.out.println("✅ 已成功建立 movie_booking 資料庫及所有資料表（含海報 image_path 欄位）！");
        } catch (SQLException e) {
            System.err.println("❌ 建立資料庫失敗：" + e.getMessage());
        }
    }
}
