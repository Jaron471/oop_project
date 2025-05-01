import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterUI extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField birthField;
    private JButton registerButton;

    public RegisterUI() {
        setTitle("會員註冊");
        setSize(350, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        // 建立元件
        emailField = new JTextField();
        passwordField = new JPasswordField();
        birthField = new JTextField();
        registerButton = new JButton("註冊");

        // 加入元件
        add(new JLabel("電子郵件:"));
        add(emailField);
        add(new JLabel("密碼:"));
        add(passwordField);
        add(new JLabel("生日 (YYYY-MM-DD):"));
        add(birthField);
        add(new JLabel()); // 空白格
        add(registerButton);

        // 註冊事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String pwd = new String(passwordField.getPassword());
                String birth = birthField.getText();

                boolean success = UserService.register(email, pwd, birth);
                JOptionPane.showMessageDialog(null,
                        success ? "✅ 註冊成功！" : "❌ 註冊失敗，請確認資料或帳號是否已存在。",
                        "註冊結果", JOptionPane.INFORMATION_MESSAGE);
                if (success) dispose(); // 註冊成功後關閉視窗
            }
        });

        setVisible(true);
    }
}
