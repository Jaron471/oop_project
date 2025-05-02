import java.time.LocalDate;
import java.util.List;

public class SystemTest {
    public static void main(String[] args) {
        // 🔁 Step 1: 重建資料
        System.out.println("===== 載入測試資料 =====");
        SampleDataLoader.main(null);

        // 👤 Step 2: 測試註冊
        System.out.println("\n===== 測試註冊 =====");
        UserService.register("newuser@example.com", "654321", LocalDate.of(2005, 3, 15)); // 合法年齡
        UserService.register("test@example.com", "123456", LocalDate.of(2000, 1, 1)); // 錯誤：帳號重複

        // 🔐 Step 3: 測試登入
        System.out.println("\n===== 測試登入 =====");
        UserService.login("newuser@example.com", "654321"); // 正確
        UserService.login("newuser@example.com", "wrongpw"); // 錯誤密碼
        UserService.login("notfound@example.com", "654321"); // 錯誤帳號

        // 🎟 Step 4: 測試訂票
        System.out.println("\n===== 測試訂票 =====");
        int validBookingId = BookingService.bookTickets(2, 1, List.of(2, 3)); // user 2 成功訂票
        BookingService.bookTickets(2, 1, List.of(2, 3)); // 錯誤：座位已被訂
        BookingService.bookTickets(99, 1, List.of(4));   // 錯誤：無此使用者
        BookingService.bookTickets(2, 999, List.of(4));  // 錯誤：無此場次
        BookingService.bookTickets(2, 1, List.of(999));  // 錯誤：座位不存在

        // 年齡不足測試
        UserService.register("teen@example.com", "111111", LocalDate.of(2015, 1, 1)); // 未滿 PG-13
        BookingService.bookTickets(3, 1, List.of(5)); // 錯誤：年齡不符

        // ❌ Step 5: 測試退票失敗（不存在）
        System.out.println("\n===== 測試退票失敗（ID 不存在） =====");
        CancelBooking.cancelBooking(999);

        // ✅ Step 6: 測試退票成功
        System.out.println("\n===== 測試退票成功 =====");
        CancelBooking.cancelBooking(validBookingId);

        // 🔍 Step 7: 查詢電影與場次
        System.out.println("\n===== 查詢電影與場次 =====");
        QueryService.getAvailableMoviesWithShowtimes();

        // 📋 Step 8: 查詢訂票紀錄
        System.out.println("\n===== 查詢訂票紀錄（User 1）=====");
        QueryService.getBookingRecordsByUser(1); // 有
        System.out.println("\n===== 查詢訂票紀錄（User 2）=====");
        QueryService.getBookingRecordsByUser(2); // 已退票應為空
        System.out.println("\n===== 查詢訂票紀錄（User 3）=====");
        QueryService.getBookingRecordsByUser(3); // 年齡不符無訂票紀錄
    }
}
