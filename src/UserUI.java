import model.ShowtimeItem;
import service.CancelBooking;
import service.QueryService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import database.DatabaseConnector;

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

        JPanel nav = new JPanel(new GridLayout(5, 1, 10, 10));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        JButton btnHome   = new JButton("🏠 推薦電影");
        JButton btnBook   = new JButton("🎟 訂票");
        JButton btnRecord = new JButton("📋 訂票紀錄");
        JButton btnMovie  = new JButton("📽 電影查詢");
        JButton btnLogout = new JButton("🚪 登出");
        nav.add(btnHome); nav.add(btnBook); nav.add(btnRecord); nav.add(btnMovie); nav.add(btnLogout);
        add(nav, BorderLayout.WEST);

        JPanel homePanel   = createHomePanel();
        JPanel bookPanel   = createBookPanel();
        JPanel recordPanel = createRecordPanel();
        JPanel moviePanel  = createMoviePanel();
        mainPanel.add(homePanel,   "home");
        mainPanel.add(bookPanel,   "book");
        mainPanel.add(recordPanel, "record");
        mainPanel.add(moviePanel,  "movie");
        add(mainPanel, BorderLayout.CENTER);

        btnHome.addActionListener(e -> cardLayout.show(mainPanel, "home"));
        btnBook.addActionListener(e -> cardLayout.show(mainPanel, "book"));
        btnRecord.addActionListener(e -> cardLayout.show(mainPanel, "record"));
        btnMovie.addActionListener(e -> cardLayout.show(mainPanel, "movie"));
        btnLogout.addActionListener(e -> {
            new LoginFrameUI().setVisible(true);
            dispose();
        });
    }

    private String getRandomMovieImagePath() {
        List<String> paths = new ArrayList<>();
        String sql = "SELECT image_path FROM movies WHERE image_path IS NOT NULL AND image_path != ''";
        try (Connection conn=DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                paths.add(rs.getString("image_path"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (!paths.isEmpty()) {
            return paths.get((int)(Math.random() * paths.size()));
        }
        return null;
    }

    private JPanel createHomePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("🎬 熱門推薦電影：", SwingConstants.CENTER);
        lbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        p.add(lbl, BorderLayout.NORTH);

        String imagePath = getRandomMovieImagePath();
        if (imagePath != null) {
            ImageIcon icon = new ImageIcon(imagePath);
            JLabel imgLabel = new JLabel(icon, SwingConstants.CENTER);
            p.add(imgLabel, BorderLayout.CENTER);
        } else {
            p.add(new JLabel("尚未提供推薦圖片", SwingConstants.CENTER), BorderLayout.CENTER);
        }

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

        JButton cancelBtn = new JButton("取消訂票");
        cancelBtn.addActionListener(this::onCancelBooking);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(btn);
        top.add(cancelBtn);

        p.add(top, BorderLayout.NORTH);
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
        SwingUtilities.invokeLater(() -> {
            showtimeCombo.removeAllItems();
            List<ShowtimeItem> list = new ArrayList<>();
            String sql = "SELECT s.id, m.title, s.show_time, t.hall_type " +
                    "FROM showtimes s " +
                    "JOIN movies m   ON s.movie_uid  = m.uid " +
                    "JOIN theaters t ON s.theater_uid = t.uid " +
                    "ORDER BY s.show_time";
            try (Connection conn=DatabaseConnector.connect();
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
                JOptionPane.showMessageDialog(this, "載入場次失敗："+ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
            list.forEach(showtimeCombo::addItem);
        });
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

    private void onCancelBooking(ActionEvent e) {
        try {
            List<QueryService.BookingRecord> recs = QueryService.getBookingRecordsByUser(userId);
            if (recs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "目前沒有可取消的訂票。", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] options = recs.stream()
                    .map(r -> "訂單 #" + r.bookingId + " | " + r.movieTitle + " | " + r.showTime)
                    .toArray(String[]::new);

            String choice = (String) JOptionPane.showInputDialog(this, "請選擇要取消的訂單：", "取消訂票",
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice != null) {
                int idx = java.util.Arrays.asList(options).indexOf(choice);
                int bookingId = recs.get(idx).bookingId;

                int ok = JOptionPane.showConfirmDialog(this, "確定要取消訂單 #" + bookingId + " 嗎？", "確認", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                    try {
                        CancelBooking.cancelBooking(bookingId);
                        JOptionPane.showMessageDialog(this, "✅ 已取消訂票 #" + bookingId);
                    } catch (CancelBooking.CancelException ex) {
                        JOptionPane.showMessageDialog(this, "❌ 退票失敗：" + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "取消訂票時發生錯誤：" + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
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
