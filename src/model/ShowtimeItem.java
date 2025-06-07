package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import database.DatabaseConnector;

/**
 * DTO：封裝電影場次資訊，用於在下拉選單中顯示與傳遞
 */
public class ShowtimeItem {
    private final int id;
    private final String title;
    private final LocalDateTime time;
    private final String hallType;
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ShowtimeItem(int id, String title, LocalDateTime time, String hallType) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.hallType = hallType;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getHallType() {
        return hallType;
    }

    @Override
    public String toString() {
        // 下拉選單顯示格式：<ID>: <電影名稱> ｜ <時間> ｜ <廳別>
        return String.format("%d: %s ｜ %s ｜ %s", id, title, time.format(fmt), hallType);
    }
}
