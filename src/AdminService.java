import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminService {
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
    }

    // ✅ 查詢某場次的所有訂票座位
    public static void viewShowtimeBookings(int showtimeId) {
        String sql = """
      SELECT b.id AS booking_id, u.email, s.seat_number
      FROM bookings b
      JOIN users u ON b.user_uid = u.uid
      JOIN booking_seats bs ON b.id = bs.booking_id
      JOIN seats s ON bs.seat_id = s.id
      WHERE b.showtime_id = ?
      ORDER BY b.id;
    """;
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showtimeId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n📋 場次 ID: " + showtimeId + " 的訂票紀錄：");
            while (rs.next()) {
                System.out.printf("訂票ID: %d｜會員: %s｜座位: %s\n",
                        rs.getInt("booking_id"),
                        rs.getString("email"),
                        rs.getString("seat_number"));
            }
        } catch (SQLException e) {
            System.out.println("❌ 查詢失敗: " + e.getMessage());
        }
    }

    // ✅ 修改電影是否上架
    public static void setMovieStatus(int movieId, boolean active) {
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE movies SET is_active = ? WHERE uid = ?")) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, movieId);
            int affected = stmt.executeUpdate();
            System.out.println(affected > 0 ? "✅ 已修改電影狀態" : "❌ 找不到電影");
        } catch (SQLException e) {
            System.out.println("❌ 上下架電影失敗: " + e.getMessage());
        }
    }

    // ✅ 修改場次時間（需檢查是否與同廳其他場次衝突）
    public static void updateShowtimeTime(int showtimeId, LocalDateTime newTime) {
        String getTheater = "SELECT theater_uid FROM showtimes WHERE id = ?";
        String checkConflict = """
      SELECT * FROM showtimes 
      WHERE theater_uid = ? AND show_time = ? AND id != ?
    """;
        try (Connection conn = connect()) {
            int theaterId;
            try (PreparedStatement stmt = conn.prepareStatement(getTheater)) {
                stmt.setInt(1, showtimeId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("❌ 查無此場次");
                    return;
                }
                theaterId = rs.getInt("theater_uid");
            }
            try (PreparedStatement stmt = conn.prepareStatement(checkConflict)) {
                stmt.setInt(1, theaterId);
                stmt.setTimestamp(2, Timestamp.valueOf(newTime));
                stmt.setInt(3, showtimeId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    System.out.println("❌ 新時間與其他場次衝突");
                    return;
                }
            }
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE showtimes SET show_time = ? WHERE id = ?")) {
                update.setTimestamp(1, Timestamp.valueOf(newTime));
                update.setInt(2, showtimeId);
                update.executeUpdate();
                System.out.println("✅ 場次時間已更新");
            }
        } catch (SQLException e) {
            System.out.println("❌ 修改場次失敗: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        viewShowtimeBookings(1);
        setMovieStatus(1, false); // 下架
        setMovieStatus(1, true);  // 上架
        updateShowtimeTime(1, LocalDateTime.of(2025, 5, 2, 20, 0));
    }
}
