// ShowtimeItem.java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShowtimeItem {
    private final int id;
    private final String title;
    private final LocalDateTime time;
    private final String hallType;

    private static final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ShowtimeItem(int id, String title, LocalDateTime time, String hallType) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.hallType = hallType;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getTime() { return time; }
    public String getHallType() { return hallType; }

    @Override
    public String toString() {
        // ComboBox 內顯示：<ID>: <電影名稱> | yyyy-MM-dd HH:mm | <大廳/小廳>
        return String.format("%d: %s ｜ %s ｜ %s",
                id, title, time.format(fmt), hallType);
    }
}
