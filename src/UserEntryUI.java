import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class UserEntryUI extends JFrame {

    public UserEntryUI() {
        setTitle("🔐 使用者登入");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("登入帳號", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JLabel emailLabel = new JLabel("信箱:");
        JTextField emailField = new JTextField();
        JLabel pwdLabel = new JLabel("密碼:");
        JPasswordField pwdField = new JPasswordField();

        inputPanel.add(emailLabel);
        inputPanel.add(emailField);
        inputPanel.add(pwdLabel);
        inputPanel.add(pwdField);

        add(inputPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton loginBtn = new JButton("登入");
        JButton goToRegisterBtn = new JButton("前往註冊");

        btnPanel.add(loginBtn);
        btnPanel.add(goToRegisterBtn);
        add(btnPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pwd = new String(pwdField.getPassword());
            if (email.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請填寫信箱與密碼");
                return;
            }
            int userId = UserService.getUserId(email, pwd);
            if (userId != -1) {
                SwingUtilities.invokeLater(() -> new UserUI(email, userId).setVisible(true));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "登入失敗，帳號或密碼錯誤");
            }
        });

        goToRegisterBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new UserRegisterUI().setVisible(true));
            dispose();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserEntryUI().setVisible(true));
    }
}
