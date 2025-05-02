import java.sql.*;

public class QueryService {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
    }

    // ✅ 查詢特定會員的訂票紀錄
    public static void getBookingRecordsByUser(int userId) {
        String sql = """
      SELECT b.id AS booking_id, m.title, s.show_time, seat.seat_number
      FROM bookings b
      JOIN showtimes s ON b.showtime_id = s.id
      JOIN movies m ON s.movie_uid = m.uid
      JOIN booking_seats bs ON b.id = bs.booking_id
      JOIN seats seat ON bs.seat_id = seat.id
      WHERE b.user_uid = ?
      ORDER BY b.id;
    """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n📄 會員 ID: " + userId + " 的訂票紀錄：");
            while (rs.next()) {
                System.out.printf("訂票ID: %d｜電影: %s｜時間: %s｜座位: %s\n",
                        rs.getInt("booking_id"),
                        rs.getString("title"),
                        rs.getTimestamp("show_time"),
                        rs.getString("seat_number"));
            }
        } catch (SQLException e) {
            System.out.println("❌ 查詢失敗: " + e.getMessage());
        }
    }

    // ✅ 查詢目前上架的所有電影（含場次）
    public static void getAvailableMoviesWithShowtimes() {
        String sql = """
      SELECT m.title, m.description, m.rating, s.show_time, t.type AS theater
      FROM movies m
      JOIN showtimes s ON m.uid = s.movie_uid
      JOIN theaters t ON s.theater_uid = t.uid
      WHERE m.is_active = TRUE
      ORDER BY m.title, s.show_time;
    """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n🎬 現正上映電影場次如下：");
            while (rs.next()) {
                System.out.printf("電影: %s｜簡介: %s｜分級: %s｜時間: %s｜廳別: %s\n",
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("rating"),
                        rs.getTimestamp("show_time"),
                        rs.getString("theater"));
            }
        } catch (SQLException e) {
            System.out.println("❌ 查詢失敗: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        getBookingRecordsByUser(1);
        getAvailableMoviesWithShowtimes();
    }
}
