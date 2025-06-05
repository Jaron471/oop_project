import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

/**
 * 處理訂票邏輯的服務層
 */
public class BookingService {

    // 自訂例外，用於業務邏輯錯誤
    public static class BookingException extends Exception {
        public BookingException(String message) { super(message); }
        public BookingException(String message, Throwable cause) { super(message, cause); }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471"
        );
    }

    private static int getUserAge(int userId, Connection conn) throws SQLException {
        String sql = "SELECT birth_date FROM users WHERE uid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate birth = rs.getDate("birth_date").toLocalDate();
                    return Period.between(birth, LocalDate.now()).getYears();
                }
            }
        }
        throw new SQLException("找不到使用者 ID: " + userId);
    }

    private static String getMovieRating(int movieId, Connection conn) throws SQLException {
        String sql = "SELECT rating FROM movies WHERE uid = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("rating");
                }
            }
        }
        throw new SQLException("找不到電影 ID: " + movieId);
    }

    private static boolean isAgeAllowed(String rating, int age) {
        if (rating == null) return false;

        switch (rating) {
            case "普遍級":
                // 適合所有年齡
                return true;
            case "保護級":
                // 建議 6 歲以上，未滿 6 歲需由家長或成年人陪同
                return age >= 6;
            case "輔導12歲級":
            case "輔12級":
                // 未滿 12 歲需由家長或成年人陪同
                return age >= 12;
            case "輔導15歲級":
            case "輔15級":
                // 未滿 15 歲需由家長或成年人陪同
                return age >= 15;
            case "限制級":
                // 僅限 18 歲以上
                return age >= 18;
            default:
                // 其他不明分級一律不允許
                return false;
        }
    }

    private static boolean areSeatsAvailable(List<Integer> seatIds, int showtimeId, Connection conn) throws SQLException {
        if (seatIds.isEmpty()) return true;
        String placeholders = String.join(",", seatIds.stream().map(id -> "?").toList());
        String sql = String.format(
                "SELECT 1 FROM booking_seats bs JOIN bookings b ON bs.booking_id=b.id " +
                        "WHERE b.showtime_id=? AND bs.seat_id IN (%s)", placeholders
        );
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, showtimeId);
            for (int i = 0; i < seatIds.size(); i++) {
                stmt.setInt(i + 2, seatIds.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        }
    }

    /**
     * 嘗試訂票，成功回傳訂單 ID，失敗拋出 BookingException
     */
    public static Optional<Integer> bookTickets(int userId, int showtimeId, List<Integer> seatIds) throws BookingException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);

            // 1. 取得場次對應的電影ID與場次時間
            int movieId;
            LocalDateTime showTime;
            String q1 = "SELECT movie_uid, show_time FROM showtimes WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(q1)) {
                ps.setInt(1, showtimeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        movieId = rs.getInt("movie_uid");
                        showTime = rs.getTimestamp("show_time").toLocalDateTime();
                    } else {
                        throw new BookingException("找不到場次 ID: " + showtimeId);
                    }
                }
            }

            // 2. 檢查年齡分級
            int age = getUserAge(userId, conn);
            String rating = getMovieRating(movieId, conn);
            if (!isAgeAllowed(rating, age)) {
                throw new BookingException("年齡不符電影分級要求");
            }

            // 3. 檢查座位可用性
            if (!areSeatsAvailable(seatIds, showtimeId, conn)) {
                throw new BookingException("有座位已被預訂");
            }

            // 4. 建立訂單
            String insBook = "INSERT INTO bookings (user_uid, showtime_id) VALUES (?, ?)";
            int bookingId;
            try (PreparedStatement ps = conn.prepareStatement(insBook, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, showtimeId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        bookingId = rs.getInt(1);
                    } else {
                        throw new BookingException("無法取得訂票 ID");
                    }
                }
            }

            // 5. 插入座位
            String insSeat = "INSERT INTO booking_seats (booking_id, showtime_id, seat_id) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insSeat)) {
                for (int sid : seatIds) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, showtimeId);
                    ps.setInt(3, sid);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return Optional.of(bookingId);
        } catch (SQLException e) {
            throw new BookingException("訂票失敗: " + e.getMessage(), e);
        }
    }

    // CLI 測試示例
    public static void main(String[] args) {
        try {
            Optional<Integer> res = bookTickets(1, 1, List.of(1,2));
            res.ifPresentOrElse(
                    id -> System.out.println("✅ 訂票成功，訂單 ID: " + id),
                    () -> System.out.println("❌ 訂票失敗"));
        } catch (BookingException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
