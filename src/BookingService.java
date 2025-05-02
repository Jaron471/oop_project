import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

public class BookingService {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
    }

    private static int getUserAge(int userId, Connection conn) throws SQLException {
        String sql = "SELECT birth_date FROM users WHERE uid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LocalDate birth = rs.getDate("birth_date").toLocalDate();
                return Period.between(birth, LocalDate.now()).getYears();
            } else {
                throw new SQLException("找不到使用者 ID: " + userId);
            }
        }
    }

    private static String getMovieRating(int movieId, Connection conn) throws SQLException {
        String sql = "SELECT rating FROM movies WHERE uid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("rating");
            else throw new SQLException("找不到電影 ID: " + movieId);
        }
    }

    private static boolean isAgeAllowed(String rating, int age) {
        return switch (rating.toUpperCase()) {
            case "G" -> true;
            case "PG" -> age >= 10;
            case "PG-13" -> age >= 13;
            case "R" -> age >= 18;
            default -> false;
        };
    }

    private static boolean areSeatsAvailable(List<Integer> seatIds, int showtimeId, Connection conn) throws SQLException {
        String placeholders = String.join(",", seatIds.stream().map(id -> "?").toList());
        String sql = """
      SELECT bs.seat_id FROM booking_seats bs
      JOIN bookings b ON bs.booking_id = b.id
      WHERE b.showtime_id = ? AND bs.seat_id IN (%s)
    """.formatted(placeholders);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showtimeId);
            for (int i = 0; i < seatIds.size(); i++) {
                stmt.setInt(i + 2, seatIds.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            return !rs.next();
        }
    }

    public static int bookTickets(int userId, int showtimeId, List<Integer> seatIds) {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            int age = getUserAge(userId, conn);

            // 查出電影 ID 與分級
            String movieQuery = "SELECT movie_uid FROM showtimes WHERE id = ?";
            int movieId;
            try (PreparedStatement stmt = conn.prepareStatement(movieQuery)) {
                stmt.setInt(1, showtimeId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) throw new SQLException("找不到場次 ID: " + showtimeId);
                movieId = rs.getInt("movie_uid");
            }

            String rating = getMovieRating(movieId, conn);
            if (!isAgeAllowed(rating, age)) {
                System.out.println("❌ 年齡不符電影分級要求");
                return -1;
            }

            if (!areSeatsAvailable(seatIds, showtimeId, conn)) {
                System.out.println("❌ 有座位已被預訂");
                return -1;
            }

            // 新增 bookings
            String insertBooking = "INSERT INTO bookings (user_uid, showtime_id) VALUES (?, ?)";
            int bookingId;
            try (PreparedStatement stmt = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, showtimeId);
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    bookingId = rs.getInt(1);
                } else {
                    throw new SQLException("無法取得訂票 ID");
                }
            }

            // 插入 booking_seats
            String insertSeats = "INSERT INTO booking_seats (booking_id, seat_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSeats)) {
                for (int seatId : seatIds) {
                    stmt.setInt(1, bookingId);
                    stmt.setInt(2, seatId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            System.out.println("✅ 訂票成功，訂單 ID: " + bookingId);
            return bookingId;

        } catch (SQLException e) {
            System.out.println("❌ 訂票失敗: " + e.getMessage());
            return -1;
        }
    }

    public static void main(String[] args) {
        // 測試用：userId=1, showtimeId=1, seatIds=1,2
        bookTickets(1, 1, List.of(1, 2));
    }
}
