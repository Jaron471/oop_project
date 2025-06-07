package service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import database.DatabaseConnector;

/**
 * 處理退票邏輯的服務層
 */
public class CancelBooking {
    /** 自訂例外，用於退票業務錯誤 */
    public static class CancelException extends Exception {
        public CancelException(String message) { super(message); }
        public CancelException(String message, Throwable cause) { super(message, cause); }
    }

    private static Connection connect() throws SQLException {
        return DatabaseConnector.connect();
    }

    /**
     * 退票：檢查時間限制，更新訂單狀態並釋放座位
     * @param bookingId 要退的訂單 ID
     * @throws CancelException 失敗原因
     */
    public static void cancelBooking(int bookingId) throws CancelException {
        Connection conn = null;
        try {
            conn = connect();
            // 關閉自動提交
            conn.setAutoCommit(false);

            // 1. 取得場次時間
            LocalDateTime showTime;
            String q1 = "SELECT s.show_time FROM bookings b JOIN showtimes s ON b.showtime_id=s.id WHERE b.id=?";
            try (PreparedStatement ps = conn.prepareStatement(q1)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        showTime = rs.getTimestamp("show_time").toLocalDateTime();
                    } else {
                        throw new CancelException("找不到訂票紀錄: " + bookingId);
                    }
                }
            }

            // 2. 檢查是否在退票時限內 (須於場次前 30 分鐘)
            LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
            if (now.isAfter(showTime.minusMinutes(30))) {
                throw new CancelException("已超過退票時間 (需於場次開始前 30 分鐘退票)");
            }

            // 3. 更新訂單狀態與退票時間
            String uSql = "UPDATE bookings SET status='CANCELLED', cancel_time=? WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(uSql)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setInt(2, bookingId);
                int affected = ps.executeUpdate();
                if (affected != 1) {
                    throw new CancelException("無法更新訂單狀態: " + bookingId);
                }
            }

            // 4. 刪除 booking_seats 以釋放座位
            String dSql = "DELETE FROM booking_seats WHERE booking_id=?";
            try (PreparedStatement ps = conn.prepareStatement(dSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            // 5. 提交
            conn.commit();
        } catch (SQLException e) {
            // 回滾
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw new CancelException("退票失敗: " + e.getMessage(), e);
        } finally {
            // 關閉連線
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    // CLI 測試
    public static void main(String[] args) {
        try {
            cancelBooking(1);
            System.out.println("✅ 退票成功");
        } catch (CancelException ex) {
            System.err.println("❌ " + ex.getMessage());
        }
    }
}