import javax.swing.*;
import java.awt.*;

public class UserRegisterUI extends JFrame {

    public UserRegisterUI() {
        setTitle("📝 使用者註冊");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("註冊帳號", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        JLabel emailLabel = new JLabel("信箱:");
        JTextField emailField = new JTextField();
        JLabel pwdLabel = new JLabel("密碼:");
        JPasswordField pwdField = new JPasswordField();
        JLabel birthLabel = new JLabel("出生年月日 (yyyy-mm-dd):");
        JTextField birthField = new JTextField();

        inputPanel.add(emailLabel);
        inputPanel.add(emailField);
        inputPanel.add(pwdLabel);
        inputPanel.add(pwdField);
        inputPanel.add(birthLabel);
        inputPanel.add(birthField);

        add(inputPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton registerBtn = new JButton("註冊");
        JButton backBtn = new JButton("返回登入");

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        registerBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pwd = new String(pwdField.getPassword());
            String birth = birthField.getText().trim();

            if (email.isEmpty() || pwd.isEmpty() || birth.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請完整填寫所有欄位");
                return;
            }

            String result = UserService.tryRegister(email, pwd, birth);
            if (result.equals("success")) {
                int userId = UserService.getUserId(email, pwd);
                SwingUtilities.invokeLater(() -> new UserUI(email, userId).setVisible(true));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, result);
            }
        });

        backBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new UserEntryUI().setVisible(true));
            dispose();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserRegisterUI().setVisible(true));
    }
}
