import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 查詢服務，提供會員訂票紀錄與在架電影場次查詢
 */
public class QueryService {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471"
        );
    }

    /**
     * 取得會員所有訂票紀錄
     * @param userId 會員 ID
     * @return List of BookingRecord
     * @throws SQLException on DB error
     */
    public static List<BookingRecord> getBookingRecordsByUser(int userId) throws SQLException {
        String sql = """
            SELECT b.id AS booking_id,
                   m.title,
                   s.show_time,
                   CONCAT(seat.seat_row, seat.seat_col) AS seat
              FROM bookings b
              JOIN showtimes s ON b.showtime_id = s.id
              JOIN movies m ON s.movie_uid = m.uid
              JOIN booking_seats bs ON b.id = bs.booking_id
              JOIN seats seat ON bs.seat_id = seat.id
             WHERE b.user_uid = ?
             ORDER BY b.id, seat
        """;
        List<BookingRecord> records = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingRecord rec = new BookingRecord(
                            rs.getInt("booking_id"),
                            rs.getString("title"),
                            rs.getTimestamp("show_time").toLocalDateTime(),
                            rs.getString("seat")
                    );
                    records.add(rec);
                }
            }
        }
        return records;
    }

    /**
     * 取得所有上架電影及其場次
     * @return List of MovieShowtime
     * @throws SQLException on DB error
     */
    public static List<MovieShowtime> getAvailableMoviesWithShowtimes() throws SQLException {
        String sql = """
            SELECT m.title,
                   m.description,
                   m.rating,
                   s.show_time,
                   t.hall_type AS theater
              FROM movies m
              JOIN showtimes s   ON m.uid = s.movie_uid
              JOIN theaters t    ON s.theater_uid = t.uid
             WHERE m.is_active = TRUE
             ORDER BY m.title, s.show_time
        """;
        List<MovieShowtime> list = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MovieShowtime ms = new MovieShowtime(
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("rating"),
                        rs.getTimestamp("show_time").toLocalDateTime(),
                        rs.getString("theater")
                );
                list.add(ms);
            }
        }
        return list;
    }

    // DTO classes
    public static class BookingRecord {
        public final int bookingId;
        public final String movieTitle;
        public final java.time.LocalDateTime showTime;
        public final String seat;

        public BookingRecord(int bookingId, String movieTitle, java.time.LocalDateTime showTime, String seat) {
            this.bookingId  = bookingId;
            this.movieTitle = movieTitle;
            this.showTime   = showTime;
            this.seat        = seat;
        }

        @Override
        public String toString() {
            return String.format("訂票ID:%d | 電影:%s | 時間:%s | 座位:%s",
                    bookingId, movieTitle, showTime, seat);
        }
    }

    public static class MovieShowtime {
        public final String title;
        public final String description;
        public final String rating;
        public final java.time.LocalDateTime showTime;
        public final String theater;

        public MovieShowtime(String title, String description, String rating,
                             java.time.LocalDateTime showTime, String theater) {
            this.title       = title;
            this.description = description;
            this.rating      = rating;
            this.showTime    = showTime;
            this.theater     = theater;
        }

        @Override
        public String toString() {
            return String.format("電影:%s | %s | 分級:%s | 時間:%s | 廳別:%s",
                    title, description, rating, showTime, theater);
        }
    }
}