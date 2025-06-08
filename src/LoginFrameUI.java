import service.UserService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import database.DatabaseConnector;

/**
 * 使用者登入介面
 */
public class LoginFrameUI extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = new JButton("登入");
    private final JButton registerButton = new JButton("註冊");

    public LoginFrameUI() {
        super("電影訂票系統 - 登入");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 表單區
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("電子郵件："), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // 密碼
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("密碼："), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // 按鈕區
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ActionListeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                String pwd = new String(passwordField.getPassword());

                // 管理員登入條件
                if (email.equals("admin") && pwd.equals("admin")) {
                    JOptionPane.showMessageDialog(
                            LoginFrameUI.this,
                            "👑 管理員登入成功！",
                            "訊息",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    SwingUtilities.invokeLater(() -> {
                        new AdminUI().setVisible(true);
                    });
                    dispose();
                    return;
                }

                // 一般使用者登入
                Optional<Integer> uidOpt = UserService.login(email, pwd);
                if (uidOpt.isPresent()) {
                    int uid = uidOpt.get();
                    JOptionPane.showMessageDialog(
                            LoginFrameUI.this,
                            "✅ 登入成功！",
                            "訊息",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    SwingUtilities.invokeLater(() -> {
                        new UserUI(uid, email).setVisible(true);
                    });
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                            LoginFrameUI.this,
                            "❌ 登入失敗：帳號或密碼錯誤",
                            "錯誤",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 開啟註冊視窗（需自行實作 RegisterDialog）
                SwingUtilities.invokeLater(() -> {
                    new RegisterDialog(LoginFrameUI.this).setVisible(true);
                });
            }
        });

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public static void main(String[] args) {
        // 啟動登入介面
        SwingUtilities.invokeLater(() -> {
            LoginFrameUI frame = new LoginFrameUI();
            frame.setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            LoginFrameUI frame = new LoginFrameUI();
            frame.setVisible(true);
        });
    }
}
