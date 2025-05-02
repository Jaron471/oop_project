import java.sql.*;
import java.time.LocalDateTime;

public class CancelBooking {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
    }

    public static boolean cancelBooking(int bookingId) {
        try (Connection conn = connect()) {
            // 取得該訂票紀錄對應的場次時間
            String getShowTime = "SELECT s.show_time FROM bookings b JOIN showtimes s ON b.showtime_id = s.id WHERE b.id = ?";
            try (PreparedStatement ps = conn.prepareStatement(getShowTime)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("❌ 找不到訂票紀錄");
                        return false;
                    }
                    Timestamp showTime = rs.getTimestamp("show_time");
                    LocalDateTime now = LocalDateTime.now();
                    if (showTime.toLocalDateTime().minusMinutes(30).isBefore(now)) {
                        System.out.println("❌ 已超過退票時間 (須於場次開始前30分鐘退票)");
                        return false;
                    }
                }
            }

            // 刪除訂票座位與訂票紀錄
            try (PreparedStatement delSeats = conn.prepareStatement("DELETE FROM booking_seats WHERE booking_id = ?");
                 PreparedStatement delBooking = conn.prepareStatement("DELETE FROM bookings WHERE id = ?")) {
                delSeats.setInt(1, bookingId);
                delSeats.executeUpdate();

                delBooking.setInt(1, bookingId);
                int rows = delBooking.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ 退票成功，ID: " + bookingId);
                    return true;
                } else {
                    System.out.println("❌ 無法退票 (找不到資料)");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ 退票失敗: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        cancelBooking(1); // 測試退訂訂票 ID = 1
    }
}
