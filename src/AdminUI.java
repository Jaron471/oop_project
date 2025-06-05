import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 營運人員管理介面
 * 功能：
 * 1. 查詢／取消訂票
 * 2. 新增電影（上檔）
 * 3. 刪除電影（下檔）
 * 4. 新增場次（排片）
 * 5. 刪除場次
 * 6. 修改場次時間
 */
public class AdminUI extends JFrame {
    // 共用時間格式
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Tab1：訂票狀態
    private JComboBox<AdminService.ShowtimeInfo> cbBookingShowtimes;
    private JTextArea taBookings;
    private JTextField tfCancelBookingId;
    private JButton btnCancelBooking;

    // Tab2：電影管理
    private JTextField tfTitle, tfDuration, tfDesc, tfImagePath, tfMovieShowTime;
    private JComboBox<String> cbRating;
    private JComboBox<TheaterItem> cbMovieTheaters = new JComboBox<>();
    private JButton btnBrowseImage, btnAddMovie;
    private JComboBox<MovieItem> cbMoviesDelete   = new JComboBox<>();  // <-- new 出來
    private JButton btnDeleteMovie;

    // Tab3：場次管理
    private JComboBox<MovieItem> cbSTMovies       = new JComboBox<>();  // <-- new 出來
    private JComboBox<TheaterItem> cbSTTheaters  = new JComboBox<>();
    private JTextField tfSTTime;
    private JButton btnCreateShowtime;

    private JComboBox<AdminService.ShowtimeInfo> cbDeleteShowtime = new JComboBox<>();
    private JButton btnDeleteShowtime;

    private JComboBox<AdminService.ShowtimeInfo> cbUpdateShowtime = new JComboBox<>();
    private JTextField tfUpdateSTTime;
    private JButton btnUpdateShowtime;

    public AdminUI() {
        super("營運人員後台");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("訂票狀態", createBookingTab());
        tabs.addTab("電影管理", createMovieTab());
        tabs.addTab("場次管理", createShowtimeTab());

        add(tabs);
    }

    private JPanel createBookingTab() {
        JPanel p = new JPanel(new BorderLayout(10,10));

        // 上方：選場次
        cbBookingShowtimes = new JComboBox<>();
        loadShowtimesInto(cbBookingShowtimes);
        cbBookingShowtimes.addActionListener(e -> refreshBookings());
        p.add(cbBookingShowtimes, BorderLayout.NORTH);

        // 中央：顯示訂單
        taBookings = new JTextArea();
        taBookings.setEditable(false);
        p.add(new JScrollPane(taBookings), BorderLayout.CENTER);

        // 下方：取消訂單
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        bottom.add(new JLabel("訂單 ID:"));
        tfCancelBookingId = new JTextField(5);
        bottom.add(tfCancelBookingId);
        btnCancelBooking = new JButton("取消訂票");
        btnCancelBooking.addActionListener(e -> {
            try {
                int bid = Integer.parseInt(tfCancelBookingId.getText().trim());
                boolean ok = AdminService.cancelBookingAdmin(bid);
                JOptionPane.showMessageDialog(this,
                        ok ? "✅ 訂單已取消" : "❌ 找不到訂單",
                        "結果", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                refreshBookings();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "❌ 取消失敗: " + ex.getMessage(),
                        "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottom.add(btnCancelBooking);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createMovieTab() {
        JPanel p = new JPanel(new BorderLayout(10,10));

        // 上半：新增電影 + 排片
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx=0; gbc.gridy=0; top.add(new JLabel("片名:"),gbc);
        gbc.gridx=1; tfTitle=new JTextField(20); top.add(tfTitle,gbc);

        gbc.gridx=0; gbc.gridy=1; top.add(new JLabel("片長(分):"),gbc);
        gbc.gridx=1; tfDuration=new JTextField(5); top.add(tfDuration,gbc);

        gbc.gridx=0; gbc.gridy=2; top.add(new JLabel("簡介:"),gbc);
        gbc.gridx=1; tfDesc=new JTextField(30); top.add(tfDesc,gbc);

        gbc.gridx = 0; gbc.gridy = 3; top.add(new JLabel("分級:"), gbc);
        gbc.gridx = 1;
        cbRating = new JComboBox<>(new String[]{
                "普遍級","保護級","輔導12歲級","輔導15歲級","限制級"
        });
        top.add(cbRating, gbc);

        gbc.gridx=0; gbc.gridy=4; top.add(new JLabel("海報:"),gbc);
        gbc.gridx=1; tfImagePath=new JTextField(20); top.add(tfImagePath,gbc);
        gbc.gridx=2; btnBrowseImage=new JButton("選擇檔案"); top.add(btnBrowseImage,gbc);
        btnBrowseImage.addActionListener(e -> {
            JFileChooser fc=new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images","jpg","png","jpeg"));
            if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
                tfImagePath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        gbc.gridx=0; gbc.gridy=5; top.add(new JLabel("廳別:"),gbc);
        gbc.gridx=1; cbMovieTheaters=new JComboBox<>(); loadTheaters(); top.add(cbMovieTheaters,gbc);

        gbc.gridx=0; gbc.gridy=6; top.add(new JLabel("首場時間:"),gbc);
        gbc.gridx=1; tfMovieShowTime=new JTextField("yyyy-MM-dd HH:mm",16); top.add(tfMovieShowTime,gbc);

        gbc.gridx=1; gbc.gridy=7; btnAddMovie=new JButton("新增電影並排首場"); top.add(btnAddMovie,gbc);
        btnAddMovie.addActionListener(e->{
            try {
                String title=tfTitle.getText().trim();
                int dur=Integer.parseInt(tfDuration.getText().trim());
                String desc=tfDesc.getText().trim();
                String rating = (String) cbRating.getSelectedItem();
                String img=tfImagePath.getText().trim();
                TheaterItem ti=(TheaterItem)cbMovieTheaters.getSelectedItem();
                LocalDateTime time=LocalDateTime.parse(tfMovieShowTime.getText().trim(),DTF);

                int mid=AdminService.createMovie(title,dur,desc,rating,img);
                AdminService.createShowtime(mid, ti.id, time, BigDecimal.ZERO);
                JOptionPane.showMessageDialog(this,"✅ 上架成功");
                loadMoviesDelete();
                loadShowtimesInto(cbBookingShowtimes);
                loadShowtimesInto(cbUpdateShowtime);
                loadShowtimesInto(cbDeleteShowtime);
            } catch(Exception ex){
                JOptionPane.showMessageDialog(this,"❌ 發生錯誤: "+ex.getMessage(),
                        "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        p.add(top, BorderLayout.NORTH);

        // 下半：刪除電影
        JPanel bot=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        bot.add(new JLabel("電影:"));
        cbMoviesDelete=new JComboBox<>();
        loadMoviesDelete();
        bot.add(cbMoviesDelete);
        btnDeleteMovie=new JButton("刪除電影");
        btnDeleteMovie.addActionListener(e->{
            try {
                MovieItem mi=(MovieItem)cbMoviesDelete.getSelectedItem();
                boolean ok=AdminService.deleteMovie(mi.id);
                JOptionPane.showMessageDialog(this,
                        ok?"✅ 刪除成功":"❌ 刪除失敗",
                        "結果",ok? JOptionPane.INFORMATION_MESSAGE: JOptionPane.ERROR_MESSAGE);
                loadMoviesDelete();
                loadShowtimesInto(cbBookingShowtimes);
            } catch(Exception ex){
                JOptionPane.showMessageDialog(this,"❌ 發生錯誤: "+ex.getMessage(),
                        "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        bot.add(btnDeleteMovie);
        p.add(bot, BorderLayout.SOUTH);

        return p;
    }

    private JPanel createShowtimeTab() {
        JPanel p=new JPanel(new BorderLayout(10,10));

        // 創建區
        JPanel createP=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        createP.add(new JLabel("電影:"));
        cbSTMovies=new JComboBox<>(); loadMoviesInto(cbSTMovies);
        createP.add(cbSTMovies);
        createP.add(new JLabel("廳別:"));
        cbSTTheaters=new JComboBox<>(); loadTheaters(); createP.add(cbSTTheaters);
        createP.add(new JLabel("時間:"));
        tfSTTime=new JTextField("yyyy-MM-dd HH:mm",16); createP.add(tfSTTime);
        btnCreateShowtime=new JButton("新增場次");
        btnCreateShowtime.addActionListener(e->{
            try {
                MovieItem mi=(MovieItem)cbSTMovies.getSelectedItem();
                TheaterItem ti=(TheaterItem)cbSTTheaters.getSelectedItem();
                LocalDateTime t=LocalDateTime.parse(tfSTTime.getText().trim(),DTF);
                AdminService.createShowtime(mi.id, ti.id, t, BigDecimal.ZERO);
                JOptionPane.showMessageDialog(this,"✅ 場次已新增");
                loadShowtimesInto(cbDeleteShowtime);
                loadShowtimesInto(cbUpdateShowtime);
                loadShowtimesInto(cbBookingShowtimes);
            } catch(Exception ex){
                JOptionPane.showMessageDialog(this,"❌ "+ex.getMessage(),
                        "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        createP.add(btnCreateShowtime);
        p.add(createP, BorderLayout.NORTH);

        // 刪除 & 修改區
        JPanel mid=new JPanel(new GridLayout(2,1,10,10));

        // 刪除場次
        JPanel delP=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        delP.add(new JLabel("場次:"));
        cbDeleteShowtime=new JComboBox<>();
        loadShowtimesInto(cbDeleteShowtime);
        delP.add(cbDeleteShowtime);
        btnDeleteShowtime=new JButton("刪除場次");
        btnDeleteShowtime.addActionListener(e->{
            try {
                AdminService.deleteShowtime(
                        ((AdminService.ShowtimeInfo)cbDeleteShowtime.getSelectedItem()).id
                );
                JOptionPane.showMessageDialog(this,"✅ 場次已刪除");
                loadShowtimesInto(cbDeleteShowtime);
                loadShowtimesInto(cbUpdateShowtime);
                loadShowtimesInto(cbBookingShowtimes);
            } catch(Exception ex){
                JOptionPane.showMessageDialog(this,"❌ "+ex.getMessage(),
                        "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        delP.add(btnDeleteShowtime);
        mid.add(delP);

        // 修改場次時間
        JPanel updP=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        updP.add(new JLabel("場次:"));
        cbUpdateShowtime=new JComboBox<>();
        loadShowtimesInto(cbUpdateShowtime);
        updP.add(cbUpdateShowtime);
        updP.add(new JLabel("新時間:"));
        tfUpdateSTTime=new JTextField("yyyy-MM-dd HH:mm",16);
        updP.add(tfUpdateSTTime);
        btnUpdateShowtime=new JButton("更新時間");
        btnUpdateShowtime.addActionListener(e->{
            try {
                int sid=((AdminService.ShowtimeInfo)cbUpdateShowtime.getSelectedItem()).id;
                LocalDateTime t=LocalDateTime.parse(tfUpdateSTTime.getText().trim(),DTF);
                AdminService.updateShowtimeTime(sid, t);
                JOptionPane.showMessageDialog(this,"✅ 時間已更新");
                loadShowtimesInto(cbDeleteShowtime);
                loadShowtimesInto(cbUpdateShowtime);
                loadShowtimesInto(cbBookingShowtimes);
            } catch(Exception ex){
                JOptionPane.showMessageDialog(this,"❌ "+ex.getMessage(),
                        "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        updP.add(btnUpdateShowtime);
        mid.add(updP);

        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    // 載入場次列表到 JComboBox
    private void loadShowtimesInto(JComboBox<AdminService.ShowtimeInfo> cb) {
        cb.removeAllItems();
        try {
            for (var si : AdminService.getAllShowtimes()) {
                cb.addItem(si);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,"無法載入場次："+e.getMessage(),
                    "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 顯示訂單
    private void refreshBookings() {
        taBookings.setText("");
        var si = (AdminService.ShowtimeInfo)cbBookingShowtimes.getSelectedItem();
        if (si==null) return;
        try {
            List<AdminService.BookingRecord> recs = AdminService.getShowtimeBookings(si.id);
            for (var r : recs) {
                taBookings.append(
                        String.format("訂單:%d 會員:%s 座位:%s%n",
                                r.bookingId, r.userEmail, r.seatNumber));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,"查詢失敗："+e.getMessage(),
                    "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 載入影廳
    private void loadTheaters() {
        cbMovieTheaters.removeAllItems();
        cbSTTheaters.removeAllItems();
        String sql="SELECT uid,hall_type FROM theaters";
        try (Connection conn=DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC","root","Jaron471");
             Statement st=conn.createStatement();
             ResultSet rs=st.executeQuery(sql)) {
            while(rs.next()){
                TheaterItem t=new TheaterItem(rs.getInt("uid"), rs.getString("hall_type"));
                cbMovieTheaters.addItem(t);
                cbSTTheaters.addItem(t);
            }
        } catch(SQLException e){
            JOptionPane.showMessageDialog(this,"無法載入影廳："+e.getMessage(),
                    "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 載入電影列表
    private void loadMoviesDelete() {
        cbMoviesDelete.removeAllItems();
        loadMoviesInto(cbSTMovies);
        String sql="SELECT uid,title FROM movies";
        try (Connection conn=DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC","root","Jaron471");
             Statement st=conn.createStatement();
             ResultSet rs=st.executeQuery(sql)) {
            while(rs.next()){
                MovieItem m=new MovieItem(rs.getInt("uid"), rs.getString("title"));
                cbMoviesDelete.addItem(m);
            }
        } catch(SQLException e){
            JOptionPane.showMessageDialog(this,"無法載入電影："+e.getMessage(),
                    "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMoviesInto(JComboBox<MovieItem> cb) {
        cb.removeAllItems();
        String sql="SELECT uid,title FROM movies";
        try (Connection conn=DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/movie_booking?useSSL=false&serverTimezone=UTC","root","Jaron471");
             Statement st=conn.createStatement();
             ResultSet rs=st.executeQuery(sql)) {
            while(rs.next()){
                cb.addItem(new MovieItem(rs.getInt("uid"), rs.getString("title")));
            }
        } catch(SQLException e){
            JOptionPane.showMessageDialog(this,"無法載入電影："+e.getMessage(),
                    "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    // DTO for JComboBox
    private static class TheaterItem {
        final int id; final String hallType;
        TheaterItem(int id,String ht){this.id=id;this.hallType=ht;}
        @Override public String toString(){return id + ": " + hallType;}
    }
    private static class MovieItem {
        final int id; final String title;
        MovieItem(int id,String t){this.id=id;this.title=t;}
        @Override public String toString(){return id + ": " + title;}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminUI().setVisible(true));
    }
}
