import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainUI extends JFrame {

    public MainUI() {
        setTitle("電影訂票系統 - 主選單");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JButton loginButton = new JButton("登入");
        JButton registerButton = new JButton("註冊");

        add(loginButton);
        add(registerButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginUI(); // 開啟登入頁面
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterUI(); // 開啟註冊頁面
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new MainUI();
    }
}
