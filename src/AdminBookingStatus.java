import java.sql.*;

public class AdminBookingStatus {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root",
                "Jaron471"
        );
    }

    // ✅ 查詢某場次已訂座位狀態
    public static void showBookingStatus(int showtimeId) {
        String query = """
      SELECT s.seat_number
      FROM booking_seats bs
      JOIN bookings b ON bs.booking_id = b.id
      JOIN seats s ON bs.seat_id = s.id
      WHERE b.showtime_id = ?;
    """;
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, showtimeId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\uD83D\uDCFA 場次 ID: " + showtimeId + " 已被訂位座位如下：");
            while (rs.next()) {
                System.out.print(rs.getString("seat_number") + " ");
            }
            System.out.println();

        } catch (SQLException e) {
            System.out.println("❌ 查詢失敗: " + e.getMessage());
        }
    }

    // ✅ 刪除訂位（模擬修改訂票狀態 → 退票）
    public static void cancelBooking(int bookingId) {
        String deleteSeats = "DELETE FROM booking_seats WHERE booking_id = ?";
        String deleteBooking = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt1 = conn.prepareStatement(deleteSeats);
             PreparedStatement stmt2 = conn.prepareStatement(deleteBooking)) {

            conn.setAutoCommit(false);
            stmt1.setInt(1, bookingId);
            stmt1.executeUpdate();

            stmt2.setInt(1, bookingId);
            stmt2.executeUpdate();

            conn.commit();
            System.out.println("✅ 已成功取消訂票紀錄 ID: " + bookingId);

        } catch (SQLException e) {
            System.out.println("❌ 退票失敗: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        showBookingStatus(1);   // 測試   查詢場次 ID 為 1 的訂位狀態
        // cancelBooking(3);    // 測試取消某筆訂單 ID（可取消註解測試）
    }
}
