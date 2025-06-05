import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471"
        );
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

    /**
     * 使用者註冊
     * @return "success" or error message
     */
    public static String register(String email, String password, LocalDate birthDate) {
        return tryRegister(email, password, birthDate.toString());
    }

    /**
     * 嘗試註冊，birthText 格式 yyyy-MM-dd
     */
    public static String tryRegister(String email, String password, String birthText) {
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(birthText);
        } catch (DateTimeParseException e) {
            return "❌ 出生年月日格式錯誤，請使用 yyyy-MM-dd";
        }

        String sql = "INSERT INTO users (email, password, birth_date) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            stmt.setString(2, hashPassword(password));
            stmt.setDate(3, Date.valueOf(birthDate));
            stmt.executeUpdate();
            return "success";
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                return "❌ 註冊失敗：信箱已存在";
            } else {
                return "❌ 註冊失敗：" + e.getMessage();
            }
        }
    }

    /**
     * 嘗試登入，成功回傳 uid，失敗回傳 Optional.empty()
     */
    public static Optional<Integer> login(String email, String password) {
        String sql = "SELECT uid, password FROM users WHERE email = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String inputHash  = hashPassword(password);
                    if (storedHash.equals(inputHash)) {
                        return Optional.of(rs.getInt("uid"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 登入錯誤：" + e.getMessage());
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;
        while (true) {
            System.out.println("請選擇操作：[1] 註冊 [2] 登入");
            String line = sc.nextLine();
            if ("1".equals(line) || "2".equals(line)) {
                choice = Integer.parseInt(line);
                break;
            }
            System.out.println("❌ 無效的選擇，請輸入 1 或 2");
        }

        System.out.print("請輸入電子郵件：");
        String email = sc.nextLine();
        System.out.print("請輸入密碼：");
        String password = sc.nextLine();

        if (choice == 1) {
            System.out.print("請輸入出生日期 (yyyy-MM-dd)：");
            String birth = sc.nextLine();
            String result = tryRegister(email, password, birth);
            System.out.println(result.equals("success") ? "✅ 註冊成功" : result);
        } else {
            Optional<Integer> maybeUid = login(email, password);
            if (maybeUid.isPresent()) {
                System.out.println("✅ 登入成功，歡迎 (uid=" + maybeUid.get() + ")");
            } else {
                System.out.println("❌ 登入失敗：帳號或密碼錯誤");
            }
        }
        sc.close();
    }
}
