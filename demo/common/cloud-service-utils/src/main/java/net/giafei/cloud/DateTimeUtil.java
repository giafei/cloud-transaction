package net.giafei.cloud;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    public static LocalDateTime fromUnixTimestamp(long second) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(second), ZoneId.systemDefault());
    }

    public static LocalDateTime fromUnixTimestampMS(long ms) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
    }

    public static long toUnixTimestampMS(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long toUnixTimestamp(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public final static DateTimeFormatter standard = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public final static DateTimeFormatter standardMS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static String toString(LocalDateTime time) {
        return time.format(standard);
    }

    public static String toString(LocalDateTime time, String format) {
        return time.format(DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime parse(String time) {
        return LocalDateTime.parse(time, standard);
    }

    public static LocalDateTime parse(String time, String format) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(format));
    }
}
