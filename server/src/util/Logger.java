package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for the server
 */
public class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void info(String message) {
        log("INFO", message);
    }
    
    public static void error(String message) {
        log("ERROR", message);
    }
    
    public static void error(String message, Throwable throwable) {
        log("ERROR", message + " - " + throwable.getMessage());
        throwable.printStackTrace();
    }
    
    public static void warn(String message) {
        log("WARN", message);
    }
    
    public static void debug(String message) {
        log("DEBUG", message);
    }
    
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println(String.format("[%s] %s: %s", timestamp, level, message));
    }
}
