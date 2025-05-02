import java.sql.*;

public class SampleDataLoader {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
             Statement stmt = conn.createStatement()) {

            System.out.println("⚠️ 清空所有資料表...");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.executeUpdate("TRUNCATE TABLE booking_seats;");
            stmt.executeUpdate("TRUNCATE TABLE bookings;");
            stmt.executeUpdate("TRUNCATE TABLE showtimes;");
            stmt.executeUpdate("TRUNCATE TABLE seats;");
            stmt.executeUpdate("TRUNCATE TABLE theaters;");
            stmt.executeUpdate("TRUNCATE TABLE movies;");
            stmt.executeUpdate("TRUNCATE TABLE users;");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
            System.out.println("✅ 所有資料表已清空\n");

            // Users
            String hashedPassword = "8d969eef6ecad3c29a3a629280e686cff8ca85e5c6a7baf3e3f8f2ff7f6e0e66"; // SHA-256 for '123456'
            stmt.executeUpdate("INSERT INTO users (email, password, birth_date) VALUES ('test@example.com', '" + hashedPassword + "', '2000-01-01');");

            // Movies
            stmt.executeUpdate("INSERT INTO movies (title, duration, description, rating, is_active) VALUES ('Example Movie', 120, 'A test movie.', 'PG-13', TRUE);");

            // Theaters
            stmt.executeUpdate("INSERT INTO theaters (type, seat_count) VALUES ('大廳', 10);");
            stmt.executeUpdate("INSERT INTO theaters (type, seat_count) VALUES ('小廳', 5);");

            // Seats
            for (int i = 1; i <= 10; i++) {
                stmt.executeUpdate("INSERT INTO seats (theater_uid, seat_number) VALUES (1, 'A" + i + "');");
            }
            for (int i = 1; i <= 5; i++) {
                stmt.executeUpdate("INSERT INTO seats (theater_uid, seat_number) VALUES (2, 'B" + i + "');");
            }

            // Showtimes
            stmt.executeUpdate("INSERT INTO showtimes (movie_uid, theater_uid, show_time) VALUES (1, 1, '2025-05-02 18:30:00');");

            // Booking (for test)
            stmt.executeUpdate("INSERT INTO bookings (user_uid, showtime_id) VALUES (1, 1);");
            stmt.executeUpdate("INSERT INTO booking_seats (booking_id, seat_id) VALUES (1, 1);");

            System.out.println("✅ 測試資料已載入完成。");

        } catch (SQLException e) {
            System.out.println("❌ 資料載入失敗: " + e.getMessage());
        }
    }
}
