import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

/**
 * 座位選擇介面，支援大廳/小廳固定結構及自訂排限制
 */
public class SeatSelectionUI extends JFrame {
    private final int userId;
    private final ShowtimeItem showtime;
    private final Map<Integer, JButton> seatButtons = new HashMap<>();
    private final Set<Integer> selectedSeats = new HashSet<>();
    private static final String[] BIG_ROWS = {"A","B","C","D","E","F","G","H","I","J","K","L","M"};
    private static final String[] SMALL_ROWS = {"A","B","C","D","E","F","G","H","I"};

    public SeatSelectionUI(int userId, ShowtimeItem showtime) {
        super(String.format("選擇座位 - %s %s (%s)",
                showtime.getTitle(),
                showtime.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                showtime.getHallType()));
        this.userId = userId;
        this.showtime = showtime;
        initUI();
        renderGrid();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        add(new JLabel("請選擇座位，選取後按「確認訂票」。", SwingConstants.CENTER), BorderLayout.NORTH);
        add(new JPanel(), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmBtn = new JButton("確認訂票");
        JButton cancelBtn  = new JButton("取消");
        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);
        add(btnPanel, BorderLayout.SOUTH);
        cancelBtn.addActionListener(e -> dispose());
        confirmBtn.addActionListener(this::onConfirm);
        setSize(800,600);
        setLocationRelativeTo(null);
    }

    /**
     * 根據排名回傳對應座位號碼陣列，0 表示空白
     */
    private int[] getRowLayout(String row) {
        int[] arr = new int[39];
        for (int i = 1; i <= 39; i++) {
            boolean show;
            switch (row) {
                case "A":
                    show = (i >= 8 && i <= 11) || (i >= 14 && i <= 25) || (i >= 28 && i <= 31);
                    break;
                case "B":
                    show = (i >= 5 && i <= 11) || (i >= 14 && i <= 25) || (i >= 28 && i <= 34);
                    break;
                case "L":
                    show = true; // L 排完整 1-39
                    break;
                case "M":
                    show = (i >= 1 && i <= 8) || (i >= 31 && i <= 38);
                    break;
                default:
                    show = (i >= 1 && i <= 11) || (i >= 14 && i <= 25) || (i >= 28 && i <= 38);
                    break;
            }
            arr[i - 1] = show ? i : 0;
        }
        return arr;
    }

    /**
     * 讀取 DB 與渲染座位格子
     */
    private void renderGrid() {
        String[] rows = "大廳".equals(showtime.getHallType()) ? BIG_ROWS : SMALL_ROWS;
        int cols = rows.length == BIG_ROWS.length ? 39 : 18;
        JPanel grid = new JPanel(new GridLayout(rows.length, cols, 3, 3));

        // 讀取座位 ID 映射
        Map<String,Integer> idMap = new HashMap<>();
        String sql1 = "SELECT id, seat_row, seat_col FROM seats WHERE theater_uid=(SELECT theater_uid FROM showtimes WHERE id=?)";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC","root","Jaron471");
             PreparedStatement ps = conn.prepareStatement(sql1)) {
            ps.setInt(1, showtime.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idMap.put(rs.getString("seat_row") + "_" + rs.getInt("seat_col"), rs.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "載入座位失敗："+ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }

        // 讀取已訂訂
        Set<Integer> booked = new HashSet<>();
        String sql2 = "SELECT bs.seat_id FROM booking_seats bs JOIN bookings b ON bs.booking_id=b.id WHERE b.showtime_id=?";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC","root","Jaron471");
             PreparedStatement ps2 = conn.prepareStatement(sql2)) {
            ps2.setInt(1, showtime.getId());
            try (ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) booked.add(rs2.getInt("seat_id"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 建按鈕格
        for (String row : rows) {
            int[] layout = getRowLayout(row);
            for (int num : layout) {
                if (num == 0) {
                    grid.add(new JLabel());
                } else {
                    JButton btn = new JButton(row + num);
                    Integer sid = idMap.get(row + "_" + num);
                    if (sid == null) {
                        btn.setEnabled(false);
                    } else {
                        btn.setEnabled(!booked.contains(sid));
                        btn.addActionListener((ActionEvent e) -> {
                            if (selectedSeats.contains(sid)) {
                                selectedSeats.remove(sid);
                                btn.setBackground(null);
                            } else {
                                selectedSeats.add(sid);
                                btn.setBackground(Color.CYAN);
                            }
                        });
                        seatButtons.put(sid, btn);
                    }
                    grid.add(btn);
                }
            }
        }

        getContentPane().remove(1);
        add(new JScrollPane(grid), BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private void onConfirm(ActionEvent e) {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先選擇座位。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Optional<Integer> res = BookingService.bookTickets(userId, showtime.getId(), new ArrayList<>(selectedSeats));
            if (res.isPresent()) {
                JOptionPane.showMessageDialog(this, "✅ 訂票成功！訂單 ID: " + res.get(), "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ 訂票失敗。", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ 訂票錯誤："+ ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ShowtimeItem dummy = new ShowtimeItem(1, "測試電影", LocalDateTime.now().plusDays(1), "大廳");
            new SeatSelectionUI(1, dummy).setVisible(true);
        });
    }
}