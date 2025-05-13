import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SeatSelectionUI extends JFrame {

    private final Set<String> selectedSeats = new HashSet<>();

    public SeatSelectionUI(String hallType) {
        setTitle("🎫 選擇座位 - " + hallType);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel seatPanel = new JPanel(new GridLayout(hallType.equals("大廳") ? 13 : 9, hallType.equals("大廳") ? 38 : 16, 2, 2));
        seatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createSeats(seatPanel, hallType);

        JButton confirmBtn = new JButton("確認訂票");
        confirmBtn.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請選擇至少一個座位");
            } else {
                JOptionPane.showMessageDialog(this, "您選擇了: " + selectedSeats);
                // 呼叫後端服務，例如 BookingService.bookTickets(...)
                dispose();
            }
        });

        add(new JScrollPane(seatPanel), BorderLayout.CENTER);
        add(confirmBtn, BorderLayout.SOUTH);
    }

    private void createSeats(JPanel panel, String hallType) {
        char[] rows = hallType.equals("大廳") ? "ABCDEFGHIJKLM".toCharArray() : "ABCDEFGHI".toCharArray();
        int cols = hallType.equals("大廳") ? 38 : 16;

        for (char row : rows) {
            for (int col = 1; col <= cols; col++) {
                String seatLabel = row + String.valueOf(col);
                JButton seatBtn = new JButton(seatLabel);
                seatBtn.setBackground(Color.LIGHT_GRAY);
                seatBtn.addActionListener(e -> {
                    if (selectedSeats.contains(seatLabel)) {
                        selectedSeats.remove(seatLabel);
                        seatBtn.setBackground(Color.LIGHT_GRAY);
                    } else {
                        selectedSeats.add(seatLabel);
                        seatBtn.setBackground(Color.GREEN);
                    }
                });
                panel.add(seatBtn);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SeatSelectionUI("大廳").setVisible(true));
    }
}
