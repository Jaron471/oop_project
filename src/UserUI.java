// UserUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class UserUI extends JFrame {
    private static final String URL  =
            "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PWD  = "Jaron471";

    private final int userId;
    private final String userEmail;
    private JComboBox<ShowtimeItem> showtimeCombo;

    public UserUI(String userEmail, int userId) {
        super("🎫 歡迎 " + userEmail);
        this.userEmail = userEmail;
        this.userId    = userId;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initUI();
        loadShowtimes();  // 背景載入場次
    }

    private void initUI() {
        // 左側導航
        JPanel nav = new JPanel(new GridLayout(4,1,10,10));
        nav.setBorder(BorderFactory.createEmptyBorder(20,10,20,10));
        nav.setPreferredSize(new Dimension(180,0));
        JButton homeBtn   = new JButton("🏠 推薦電影");
        JButton bookBtn   = new JButton("🎟 訂票");
        JButton recordBtn = new JButton("📋 訂票紀錄");
        JButton logoutBtn = new JButton("🚪 登出");
        nav.add(homeBtn); nav.add(bookBtn); nav.add(recordBtn); nav.add(logoutBtn);
        add(nav, BorderLayout.WEST);

        // 卡片面板
        CardLayout cl = new CardLayout();
        JPanel cards = new JPanel(cl);
        add(cards, BorderLayout.CENTER);

        // 1. 首頁
        JPanel homeP = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("🎬 熱門推薦電影：", SwingConstants.CENTER);
        lbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        homeP.add(lbl, BorderLayout.NORTH);
        homeP.add(new JLabel(new ImageIcon("resources/sample_movie.jpg")), BorderLayout.CENTER);

        // 2. 訂票
        JPanel bookP = new JPanel(new BorderLayout(0,10));
        JLabel sel = new JLabel("請選擇電影場次：", SwingConstants.CENTER);
        sel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        showtimeCombo = new JComboBox<>();
        JButton proceed = new JButton("➡️ 選擇座位");
        proceed.addActionListener(e -> onProceed());
        bookP.add(sel, BorderLayout.NORTH);
        bookP.add(showtimeCombo, BorderLayout.CENTER);
        bookP.add(proceed, BorderLayout.SOUTH);

        // 3. 查紀錄
        JPanel recP = new JPanel();
        JButton viewRec = new JButton("查詢紀錄");
        viewRec.addActionListener(e -> QueryService.getBookingRecordsByUser(userId));
        recP.add(viewRec);

        cards.add(homeP,   "home");
        cards.add(bookP,   "book");
        cards.add(recP,    "record");

        // 導航按鈕連動
        homeBtn.addActionListener(e -> cl.show(cards, "home"));
        bookBtn.addActionListener(e -> cl.show(cards, "book"));
        recordBtn.addActionListener(e -> cl.show(cards, "record"));
        logoutBtn.addActionListener(e -> {
            new MainUI().setVisible(true);
            dispose();
        });
    }

    private void loadShowtimes() {
        new SwingWorker<List<ShowtimeItem>, Void>() {
            @Override
            protected List<ShowtimeItem> doInBackground() throws Exception {
                List<ShowtimeItem> list = new ArrayList<>();
                String sql = """
                    SELECT s.id, m.title, s.show_time, t.type 
                      FROM showtimes s
                      JOIN movies m   ON s.movie_uid  = m.uid
                      JOIN theaters t ON s.theater_uid = t.uid
                   ORDER BY s.show_time
                """;
                try (Connection c = DriverManager.getConnection(URL, USER, PWD);
                     Statement  st = c.createStatement();
                     ResultSet  rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        int    id   = rs.getInt("id");
                        String title= rs.getString("title");
                        LocalDateTime time = rs.getTimestamp("show_time").toLocalDateTime();
                        String type = rs.getString("type");  // 假設 DB 裡已記錄「大廳/小廳」
                        list.add(new ShowtimeItem(id, title, time, type));
                    }
                }
                return list;
            }

            @Override
            protected void done() {
                try {
                    for (ShowtimeItem item : get()) {
                        showtimeCombo.addItem(item);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            UserUI.this,
                            "載入場次失敗：" + ex.getMessage(),
                            "錯誤",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void onProceed() {
        ShowtimeItem sel = (ShowtimeItem) showtimeCombo.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "請先選擇一個場次。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 將整個 ShowtimeItem 傳給 SeatSelectionUI 由那邊決定大/小廳與座位佈局
        new SeatSelectionUI(userId, sel).setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserUI("test@example.com", 1).setVisible(true));
    }
}
