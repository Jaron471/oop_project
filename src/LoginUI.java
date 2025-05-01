import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginUI() {
        setTitle("會員登入");
        setSize(350, 180);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 關閉此視窗不影響主視窗
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 10, 10));

        // 元件建立
        emailField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("登入");

        // 加入元件
        add(new JLabel("電子郵件:"));
        add(emailField);
        add(new JLabel("密碼:"));
        add(passwordField);
        add(new JLabel()); // 空白格
        add(loginButton);

        // 登入事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String pwd = new String(passwordField.getPassword());
                boolean success = UserService.login(email, pwd);
                JOptionPane.showMessageDialog(null,
                        success ? "✅ 登入成功！" : "❌ 登入失敗，請確認帳號密碼。",
                        "登入結果", JOptionPane.INFORMATION_MESSAGE);
                if (success) dispose(); // 登入成功後關閉登入視窗
            }
        });

        setVisible(true);
    }
}
