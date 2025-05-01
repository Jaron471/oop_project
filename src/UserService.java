import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    // 註冊功能
    public static boolean register(String email, String password, String birthDate) {
        String sql = "INSERT INTO users (email, password, birth_date) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password); // 實際應加密
            stmt.setString(3, birthDate); // 格式：YYYY-MM-DD
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
            return false;
        }
    }

    // 登入功能
    public static boolean login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // 若有資料代表登入成功
        } catch (SQLException e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            return false;
        }
    }

    public static void printAllUsers() {
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("uid")
                        + ", Email: " + rs.getString("email")
                        + ", Birth: " + rs.getDate("birth_date"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to query users: " + e.getMessage());
        }
    }


    // 測試用主函式
    public static void main(String[] args) {
        boolean registered = register("test@example.com", "123456", "2000-01-01");
        System.out.println(registered ? "✅ 註冊成功" : "❌ 註冊失敗");

        boolean loggedIn = login("test@example.com", "123456");
        System.out.println(loggedIn ? "✅ 登入成功" : "❌ 登入失敗");

        printAllUsers();

    }
}
