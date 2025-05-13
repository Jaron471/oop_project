import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class BookingUI extends JFrame {

    public BookingUI(int userId) {
        setTitle("🎟 電影訂票系統");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("請選擇場次並選擇座位進行訂票", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // 場次下拉選單
        JComboBox<String> showtimeDropdown = new JComboBox<>();
        List<Integer> showtimeIds = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                "root", "Jaron471");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, show_time FROM showtimes")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String time = rs.getString("show_time");
                showtimeDropdown.addItem("場次 ID: " + id + " - " + time);
                showtimeIds.add(id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ 載入場次失敗: " + e.getMessage());
        }

        JPanel showtimePanel = new JPanel();
        showtimePanel.add(new JLabel("選擇場次: "));
        showtimePanel.add(showtimeDropdown);
        add(showtimePanel, BorderLayout.NORTH);

        // 座位選擇區（模擬）
        JPanel seatPanel = new JPanel(new GridLayout(4, 5, 10, 10));
        JCheckBox[] seatCheckboxes = new JCheckBox[20];
        for (int i = 0; i < 20; i++) {
            seatCheckboxes[i] = new JCheckBox("座位 " + (i + 1));
            seatPanel.add(seatCheckboxes[i]);
        }

        JScrollPane scrollPane = new JScrollPane(seatPanel);
        scrollPane.setPreferredSize(new Dimension(600, 250));
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton confirmBtn = new JButton("✅ 確認訂票");
        JButton cancelBtn = new JButton("❌ 取消");
        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        confirmBtn.addActionListener(e -> {
            int selectedIndex = showtimeDropdown.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this, "請選擇場次");
                return;
            }
            int showtimeId = showtimeIds.get(selectedIndex);

            List<Integer> seatIds = new ArrayList<>();
            for (int i = 0; i < seatCheckboxes.length; i++) {
                if (seatCheckboxes[i].isSelected()) {
                    seatIds.add(i + 1); // seatId 模擬
                }
            }

            if (seatIds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請至少選擇一個座位");
                return;
            }

            int result = BookingService.bookTickets(userId, showtimeId, seatIds);
            if (result != -1) {
                JOptionPane.showMessageDialog(this, "✅ 訂票成功，訂單 ID: " + result);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ 訂票失敗，請確認條件是否符合。");
            }
        });

        cancelBtn.addActionListener(e -> dispose());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookingUI(1).setVisible(true));
    }
}
