import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 會員主畫面：首頁、訂票、訂票紀錄、電影查詢、登出
 */
public class UserUI extends JFrame {
    private final int userId;
    private final String userEmail;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private JComboBox<ShowtimeItem> showtimeCombo;

    public UserUI(int userId, String userEmail) {
        super("🎫 歡迎 " + userEmail);
        this.userId = userId;
        this.userEmail = userEmail;
        initUI();
        loadShowtimes();
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 左側導航按鈕
        JPanel nav = new JPanel(new GridLayout(5, 1, 10, 10));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        JButton btnHome   = new JButton("🏠 推薦電影");
        JButton btnBook   = new JButton("🎟 訂票");
        JButton btnRecord = new JButton("📋 訂票紀錄");
        JButton btnMovie  = new JButton("📽 電影查詢");
        JButton btnLogout = new JButton("🚪 登出");
        nav.add(btnHome); nav.add(btnBook); nav.add(btnRecord); nav.add(btnMovie); nav.add(btnLogout);
        add(nav, BorderLayout.WEST);

        // 主面板卡片
        JPanel homePanel   = createHomePanel();
        JPanel bookPanel   = createBookPanel();
        JPanel recordPanel = createRecordPanel();
        JPanel moviePanel  = createMoviePanel();
        mainPanel.add(homePanel,   "home");
        mainPanel.add(bookPanel,   "book");
        mainPanel.add(recordPanel, "record");
        mainPanel.add(moviePanel,  "movie");
        add(mainPanel, BorderLayout.CENTER);

        // 導航事件
        btnHome.addActionListener(e -> cardLayout.show(mainPanel, "home"));
        btnBook.addActionListener(e -> cardLayout.show(mainPanel, "book"));
        btnRecord.addActionListener(e -> cardLayout.show(mainPanel, "record"));
        btnMovie.addActionListener(e -> cardLayout.show(mainPanel, "movie"));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true); // 假定 LoginFrame 在同 package
            dispose();
        });
    }

    private JPanel createHomePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("🎬 熱門推薦電影：", SwingConstants.CENTER);
        lbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        p.add(lbl, BorderLayout.NORTH);
        p.add(new JLabel(new ImageIcon("resources/sample_movie.jpg"), SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }

    private JPanel createBookPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        JLabel lbl = new JLabel("請選擇電影場次：", SwingConstants.CENTER);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        showtimeCombo = new JComboBox<>();
        JButton btn = new JButton("➡️ 選擇座位");
        btn.addActionListener(this::onProceed);
        p.add(lbl, BorderLayout.NORTH);
        p.add(showtimeCombo, BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createRecordPanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        JButton btn = new JButton("查詢訂票紀錄");
        btn.addActionListener(this::onQueryRecords);
        p.add(btn, BorderLayout.NORTH);
        return p;
    }

    private JPanel createMoviePanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        JButton btn = new JButton("顯示目前上映電影");
        btn.addActionListener(this::onDisplayMovies);
        p.add(btn, BorderLayout.NORTH);
        return p;
    }

    private void loadShowtimes() {
        // 資料庫查詢屬於耗時操作，改在背景執行以免卡住畫面
        new Thread(() -> {
            List<ShowtimeItem> list = new ArrayList<>();
            String sql = "SELECT s.id, m.title, s.show_time, t.hall_type " +
                    "FROM showtimes s " +
                    "JOIN movies m   ON s.movie_uid  = m.uid " +
                    "JOIN theaters t ON s.theater_uid = t.uid " +
                    "ORDER BY s.show_time";
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                    "root", "Jaron471");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    LocalDateTime dt = rs.getTimestamp("show_time").toLocalDateTime();
                    String hall = rs.getString("hall_type");
                    list.add(new ShowtimeItem(id, title, dt, hall));
                }
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "載入場次失敗：" + ex.getMessage(),
                        "錯誤",
                        JOptionPane.ERROR_MESSAGE
                ));
            }
            SwingUtilities.invokeLater(() -> {
                showtimeCombo.removeAllItems();
                list.forEach(showtimeCombo::addItem);
            });
        }).start();
    }

    private void onProceed(ActionEvent e) {
        ShowtimeItem sel = (ShowtimeItem) showtimeCombo.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "請先選擇場次", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new SeatSelectionUI(userId, sel).setVisible(true);
    }

    private void onQueryRecords(ActionEvent e) {
        try {
            List<QueryService.BookingRecord> recs = QueryService.getBookingRecordsByUser(userId);
            JTextArea ta = new JTextArea();
            recs.forEach(r -> ta.append(r + "\n"));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "訂票紀錄", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "查詢紀錄失敗："+ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDisplayMovies(ActionEvent e) {
        try {
            List<QueryService.MovieShowtime> list = QueryService.getAvailableMoviesWithShowtimes();
            JTextArea ta = new JTextArea();
            list.forEach(ms -> ta.append(ms + "\n"));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "目前上映電影", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "查詢電影失敗："+ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserUI(1, "test@example.com").setVisible(true));
    }
}