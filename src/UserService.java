import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("❌ 無法加密密碼", e);
        }
    }

    // 支援 GUI 傳入 LocalDate 的註冊方法
    public static String register(String email, String password, LocalDate birthDate) {
        return tryRegister(email, password, birthDate.toString());
    }

    public static String tryRegister(String email, String password, String birthText) {
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(birthText);
        } catch (DateTimeParseException e) {
            return "出生年月日格式錯誤，請使用 yyyy-mm-dd";
        }

        String sql = "INSERT INTO users (email, password, birth_date) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, hashPassword(password));
            stmt.setDate(3, Date.valueOf(birthDate));
            stmt.executeUpdate();
            System.out.println("✅ 註冊成功");
            return "success";
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                return "❌ 註冊失敗：信箱已存在";
            } else {
                return "❌ 註冊失敗: " + e.getMessage();
            }
        }
    }

    public static boolean login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, hashPassword(password));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("✅ 登入成功，歡迎：" + email);
                return true;
            } else {
                System.out.println("❌ 登入失敗：帳號或密碼錯誤");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ 登入錯誤: " + e.getMessage());
            return false;
        }
    }

    public static int getUserId(String email, String password) {
        String sql = "SELECT uid FROM users WHERE email = ? AND password = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, hashPassword(password));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("uid");
            }
        } catch (SQLException e) {
            System.out.println("❌ 取得使用者 ID 失敗: " + e.getMessage());
        }
        return -1; // 表示查無帳號或密碼錯誤
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice = 0;
        while (true) {
            try {
                System.out.println("請選擇操作：[1] 註冊 [2] 登入");
                choice = Integer.parseInt(sc.nextLine());
                if (choice == 1 || choice == 2) break;
                System.out.println("❌ 無效的選擇，請重新輸入");
            } catch (NumberFormatException e) {
                System.out.println("❌ 請輸入數字 1 或 2");
            }
        }

        System.out.print("請輸入電子郵件：");
        String email = sc.nextLine();
        System.out.print("請輸入密碼：");
        String password = sc.nextLine();

        if (choice == 1) {
            System.out.print("請輸入出生日期 (yyyy-mm-dd)：");
            String birth = sc.nextLine();
            String result = tryRegister(email, password, birth);
            System.out.println(result);
        } else {
            boolean success = login(email, password);
            if (!success) {
                System.out.println("登入失敗");
            }
        }
        sc.close();
    }
}
