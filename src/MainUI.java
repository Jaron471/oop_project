import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class MainUI extends JFrame {

    public MainUI() {
        setTitle("🎬 電影訂票系統 - 首頁");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("歡迎使用電影訂票系統", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JButton userBtn = new JButton("👤 使用者入口");
        userBtn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
        JButton adminBtn = new JButton("🛠️ 營運人員入口");
        adminBtn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));

        btnPanel.add(userBtn);
        btnPanel.add(adminBtn);
        add(btnPanel, BorderLayout.CENTER);

        userBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new UserEntryUI().setVisible(true));
            dispose();
        });

        adminBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "尚未實作 AdminUI，可以之後加入");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }
}
