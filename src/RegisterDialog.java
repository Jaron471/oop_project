import service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import database.DatabaseConnector;

/**
 * 使用者註冊介面
 */
public class RegisterDialog extends JDialog {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField birthDateField = new JTextField(10);
    private final JButton registerButton = new JButton("註冊");
    private final JButton cancelButton = new JButton("取消");

    public RegisterDialog(JFrame owner) {
        super(owner, "使用者註冊", true);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Email
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("電子郵件："), gbc);
        gbc.gridx = 1;
        form.add(emailField, gbc);

        // 密碼
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("密碼："), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        // 出生日期
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("出生日期 (yyyy-MM-dd)："), gbc);
        gbc.gridx = 1;
        form.add(birthDateField, gbc);

        add(form, BorderLayout.CENTER);

        // 按鈕區
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(registerButton);
        add(buttons, BorderLayout.SOUTH);

        // Action
        registerButton.addActionListener((ActionEvent e) -> onRegister());
        cancelButton.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void onRegister() {
        String email = emailField.getText().trim().toLowerCase();
        String pwd   = new String(passwordField.getPassword());
        String birth = birthDateField.getText().trim();
        String result = UserService.tryRegister(email, pwd, birth);
        if ("success".equals(result)) {
            JOptionPane.showMessageDialog(
                    this,
                    "✅ 註冊成功！請使用新帳號登入。",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    result,
                    "註冊失敗",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
