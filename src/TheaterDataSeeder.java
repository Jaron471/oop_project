import java.sql.*;
import java.util.*;

/**
 * 預設影廳種別資料：建立 6 個影廳（3 大廳、3 小廳），並自動產生對應座位
 * 大廳佈局依照指定結構：每排使用不同號碼區塊
 */
public class TheaterDataSeeder {
    private static final String URL  = "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PWD  = "Jaron471";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            // 清空舊影廳與座位
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            stmt.executeUpdate("TRUNCATE TABLE booking_seats");
            stmt.executeUpdate("TRUNCATE TABLE bookings");
            stmt.executeUpdate("TRUNCATE TABLE showtimes");
            stmt.executeUpdate("TRUNCATE TABLE seats");
            stmt.executeUpdate("TRUNCATE TABLE theaters");
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");

            // 插入 3 個大廳
            for (int i = 1; i <= 3; i++) {
                stmt.executeUpdate("INSERT INTO theaters(hall_type,seat_count) VALUES ('大廳',39)");
            }
            // 插入 3 個小廳
            for (int i = 1; i <= 3; i++) {
                stmt.executeUpdate("INSERT INTO theaters(hall_type,seat_count) VALUES ('小廳',18)");
            }

            // 撈出所有影廳
            List<Integer> ids = new ArrayList<>();
            List<String> types = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery("SELECT uid, hall_type FROM theaters")) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                    types.add(rs.getString(2));
                }
            }

            // 為各影廳產生座位
            for (int idx = 0; idx < ids.size(); idx++) {
                int tid = ids.get(idx);
                String type = types.get(idx);
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO seats(theater_uid,seat_row,seat_col) VALUES (?, ?, ?)")) {
                    if ("大廳".equals(type)) {
                        for (char r = 'A'; r <= 'M'; r++) {
                            int[] layout = getBigHallRow(r);
                            for (int num : layout) {
                                if (num == 0) continue;
                                ps.setInt(1, tid);
                                ps.setString(2, String.valueOf(r));
                                ps.setInt(3, num);
                                ps.addBatch();
                            }
                        }
                    } else {
                        for (char r = 'A'; r <= 'I'; r++) {
                            int[] layout = getSmallHallRow();
                            for (int num : layout) {
                                if (num == 0) continue;
                                ps.setInt(1, tid);
                                ps.setString(2, String.valueOf(r));
                                ps.setInt(3, num);
                                ps.addBatch();
                            }
                        }
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            System.out.println("✅ 已建立 6 個影廳(3大廳,3小廳)及依指定佈局產生座位");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ 資料匯入失敗: " + e.getMessage());
        }
    }

    // 返回大廳指定排的座位號碼，三區塊及過道，用 0 作為跳過
    private static int[] getBigHallRow(char r) {
        switch(r) {
            case 'A': return new int[]{8,9,10,11,0,14,15,16,17,18,19,20,21,22,23,24,25,0,28,29,30,31,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            case 'B': return new int[]{5,6,7,8,9,10,11,0,14,15,16,17,18,19,20,21,22,23,24,25,0,28,29,30,31,32,33,34,0,0,0,0,0,0,0,0,0};
            case 'L': {
                int[] arr = new int[39];
                for (int i=0;i<39;i++) arr[i]=i+1;
                return arr;
            }
            case 'M': return new int[]{1,2,3,4,5,6,7,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,31,32,33,34,35,36,37,38};
            default: return new int[]{1,2,3,4,5,6,7,8,9,10,11,0,14,15,16,17,18,19,20,21,22,23,24,25,0,28,29,30,31,32,33,34,35,36,37,38,0,0,0};
        }
    }

    // 返回小廳每排座位號碼 1-4、5-12、13-16
    private static int[] getSmallHallRow() {
        return new int[]{1,2,3,4,0,5,6,7,8,9,10,11,12,0,13,14,15,16};
    }
}
