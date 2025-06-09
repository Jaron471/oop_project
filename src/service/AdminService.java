package service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.math.BigDecimal;
import database.DatabaseConnector;


/**
 * 營運人員後端服務層：
 * 1. 場次訂票查詢
 * 2. 強制退票（更新訂單狀態）
 * 3. 新增電影
 * 4. 排片管理（新增／刪除場次）
 * 5. 刪除電影（下檔，含 cascade 處理）
 * 6. 場次時間調整（衝突檢查）
 */
public class AdminService {

    public static class ConflictException extends Exception {
        public ConflictException(String message) { super(message); }
    }

    public static class BookingRecord {
        public final int bookingId;
        public final String userEmail;
        public final String seatNumber;
        public BookingRecord(int bookingId, String userEmail, String seatNumber) {
            this.bookingId = bookingId;
            this.userEmail = userEmail;
            this.seatNumber = seatNumber;
        }
    }

    public static class ShowtimeInfo {
        public final int id;
        public final String movieTitle;
        public final LocalDateTime showTime;
        public final String hallType;
        public final BigDecimal price;
        public final int theaterId;    // 新增

        private static final DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        public ShowtimeInfo(int id,
                            String movieTitle,
                            LocalDateTime showTime,
                            String hallType,
                            BigDecimal price,
                            int theaterId) {
            this.id         = id;
            this.movieTitle = movieTitle;
            this.showTime   = showTime;
            this.hallType   = hallType;
            this.price      = price;
            this.theaterId  = theaterId;
        }

        @Override
        public String toString() {
            return String.format(
                    "%d: %s | %s | 第%d%s",
                    id,
                    movieTitle,
                    showTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    theaterId,
                    hallType
            );
        }
    }

    private static Connection connect() throws SQLException {
        return DatabaseConnector.connect();
    }

    /**
     * 查詢某場次的所有訂票紀錄
     */
    public static List<BookingRecord> getShowtimeBookings(int showtimeId) throws SQLException {
        String sql = ""
                + "SELECT b.id AS booking_id, u.email, "
                + "       CONCAT(s.seat_row,s.seat_col) AS seat_number, "
                + "       b.status "
                + "FROM bookings b "
                + "JOIN users u ON b.user_uid=u.uid "
                + "JOIN booking_seats bs ON b.id=bs.booking_id "
                + "JOIN seats s ON bs.seat_id=s.id "
                + "WHERE b.showtime_id=? "
                + "ORDER BY b.id";
        List<BookingRecord> list = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingRecord(
                            rs.getInt("booking_id"),
                            rs.getString("email"),
                            rs.getString("seat_number")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * 強制退票：更新訂單狀態為 CANCELLED，並記錄退票時間
     */
    public static boolean cancelBookingAdmin(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id=?";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 新增電影（不包含場次）
     * @return 新電影的 uid
     */
    public static int createMovie(String title,
                                  int duration,
                                  String description,
                                  String rating,
                                  String imagePath) throws SQLException {
        String sql = ""
                + "INSERT INTO movies(title,duration,description,image_path,rating,is_active) "
                + "VALUES(?,?,?,?,?,TRUE)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, title);
                    ps.setInt(2, duration);
                    ps.setString(3, description);
                    ps.setString(4, imagePath);
                    ps.setString(5, rating);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) newId = rs.getInt(1);
                        else throw new SQLException("取得新電影 ID 失敗");
                    }
                }
                conn.commit();
                return newId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * 新增場次（排片），並檢查同廳時間衝突
     * @return 新場次 ID
     */
    public static int createShowtime(int movieId,
                                     int theaterId,
                                     LocalDateTime time,
                                     BigDecimal price)
            throws SQLException, ConflictException {
        String getDuration = "SELECT duration FROM movies WHERE uid=?";
        String getOthers = """
        SELECT s.show_time, m.duration FROM showtimes s
        JOIN movies m ON s.movie_uid = m.uid
        WHERE s.theater_uid = ?
    """;
        String insert = "INSERT INTO showtimes(movie_uid,theater_uid,show_time,price) VALUES(?,?,?,?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                int duration;
                // 取得此電影片長
                try (PreparedStatement ps = conn.prepareStatement(getDuration)) {
                    ps.setInt(1, movieId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("找不到電影");
                        duration = rs.getInt("duration");
                    }
                }

                LocalDateTime endTime = time.plusMinutes(duration);

                // 檢查此廳其他場次是否有時間重疊
                try (PreparedStatement ps = conn.prepareStatement(getOthers)) {
                    ps.setInt(1, theaterId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            LocalDateTime otherStart = rs.getTimestamp("show_time").toLocalDateTime();
                            LocalDateTime otherEnd = otherStart.plusMinutes(rs.getInt("duration"));

                            boolean overlap = time.isBefore(otherEnd) && otherStart.isBefore(endTime);
                            if (overlap)
                                throw new ConflictException("場次時間與其他場次重疊");
                        }
                    }
                }

                // 寫入場次
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, movieId);
                    ps.setInt(2, theaterId);
                    ps.setTimestamp(3, Timestamp.valueOf(time));
                    ps.setBigDecimal(4, price);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) newId = rs.getInt(1);
                        else throw new SQLException("取得新場次 ID 失敗");
                    }
                }

                conn.commit();
                return newId;

            } catch (SQLException | ConflictException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * 刪除場次
     */
    public static boolean deleteShowtime(int showtimeId) throws SQLException {
        String delBS = "DELETE bs FROM booking_seats bs JOIN bookings b ON bs.booking_id = b.id WHERE b.showtime_id=?";
        String delB  = "DELETE FROM bookings WHERE showtime_id=?";
        String delST = "DELETE FROM showtimes WHERE id=?";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(delBS);
                     PreparedStatement ps2 = conn.prepareStatement(delB)) {
                    ps1.setInt(1, showtimeId);
                    ps1.executeUpdate();
                    ps2.setInt(1, showtimeId);
                    ps2.executeUpdate();
                }

                int affected;
                try (PreparedStatement ps = conn.prepareStatement(delST)) {
                    ps.setInt(1, showtimeId);
                    affected = ps.executeUpdate();
                }

                conn.commit();
                return affected > 0;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * 刪除電影（下檔），先手動 cascade 刪除該電影所有相關
     * showtimes、bookings、booking_seats，再刪 movies
     */
    public static boolean deleteMovie(int movieId) throws SQLException {
        String sel = "SELECT id FROM showtimes WHERE movie_uid=?";
        String delBS = "DELETE bs "
                + "FROM booking_seats bs "
                + "JOIN bookings b ON bs.booking_id=b.id "
                + "WHERE b.showtime_id=?";
        String delB  = "DELETE FROM bookings WHERE showtime_id=?";
        String delST = "DELETE FROM showtimes WHERE movie_uid=?";
        String delM  = "DELETE FROM movies WHERE uid=?";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                // 找出所有 showtime IDs
                List<Integer> sids = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(sel)) {
                    ps.setInt(1, movieId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) sids.add(rs.getInt(1));
                    }
                }
                // 刪除 booking_seats 與 bookings
                try (PreparedStatement psBS = conn.prepareStatement(delBS);
                     PreparedStatement psB  = conn.prepareStatement(delB)) {
                    for (int sid : sids) {
                        psBS.setInt(1, sid);
                        psBS.executeUpdate();
                        psB.setInt(1, sid);
                        psB.executeUpdate();
                    }
                }
                // 刪除 showtimes
                try (PreparedStatement ps = conn.prepareStatement(delST)) {
                    ps.setInt(1, movieId);
                    ps.executeUpdate();
                }
                // 刪除 movies
                int affected;
                try (PreparedStatement ps = conn.prepareStatement(delM)) {
                    ps.setInt(1, movieId);
                    affected = ps.executeUpdate();
                }
                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * 修改場次時間（含同廳衝突檢查）
     */
    public static void updateShowtimeTime(int showtimeId, LocalDateTime newTime)
            throws SQLException, ConflictException {
        String getTheaterAndMovie = "SELECT theater_uid, movie_uid FROM showtimes WHERE id=?";
        String getDuration = "SELECT duration FROM movies WHERE uid=?";
        String getAllOtherShowtimes = """
        SELECT s.show_time, m.duration FROM showtimes s
        JOIN movies m ON s.movie_uid = m.uid
        WHERE s.theater_uid = ? AND s.id <> ?
    """;
        String updateTime = "UPDATE showtimes SET show_time=? WHERE id=?";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                int theaterId, movieId, newDuration;

                // 取得本場場次資訊（廳與電影 ID）
                try (PreparedStatement ps = conn.prepareStatement(getTheaterAndMovie)) {
                    ps.setInt(1, showtimeId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("找不到場次");
                        theaterId = rs.getInt("theater_uid");
                        movieId = rs.getInt("movie_uid");
                    }
                }

                // 查該電影的片長
                try (PreparedStatement ps = conn.prepareStatement(getDuration)) {
                    ps.setInt(1, movieId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("找不到電影");
                        newDuration = rs.getInt("duration");
                    }
                }

                LocalDateTime newEndTime = newTime.plusMinutes(newDuration);

                // 查同一廳的其他場次，檢查是否有重疊
                try (PreparedStatement ps = conn.prepareStatement(getAllOtherShowtimes)) {
                    ps.setInt(1, theaterId);
                    ps.setInt(2, showtimeId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            LocalDateTime oldStart = rs.getTimestamp("show_time").toLocalDateTime();
                            LocalDateTime oldEnd = oldStart.plusMinutes(rs.getInt("duration"));

                            boolean overlap = newTime.isBefore(oldEnd) && oldStart.isBefore(newEndTime);
                            if (overlap) {
                                throw new ConflictException("場次時間與其他場次重疊");
                            }
                        }
                    }
                }

                // 若沒衝突則更新
                try (PreparedStatement ps = conn.prepareStatement(updateTime)) {
                    ps.setTimestamp(1, Timestamp.valueOf(newTime));
                    ps.setInt(2, showtimeId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException | ConflictException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * 列出所有現行場次資訊 (含電影、廳別、時間、價格)
     */
    public static List<ShowtimeInfo> getAllShowtimes() throws SQLException {
        String sql =
                "SELECT "
                        + "  s.id, "
                        + "  m.title, "
                        + "  s.show_time, "
                        + "  t.hall_type, "
                        + "  s.price, "
                        + "  t.uid AS theater_id "
                        + "FROM showtimes s "
                        + "JOIN movies   m ON s.movie_uid   = m.uid "
                        + "JOIN theaters t ON s.theater_uid = t.uid "
                        + "ORDER BY s.show_time";

        List<ShowtimeInfo> list = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ShowtimeInfo(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getTimestamp("show_time").toLocalDateTime(),
                        rs.getString("hall_type"),
                        rs.getBigDecimal("price"),
                        rs.getInt("theater_id")      // <-- 新增這裡
                ));
            }
        }
        return list;
    }
}
