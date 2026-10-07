import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestDateTime {
    public static void main(String[] args) {
        try {
            System.out.println(LocalDateTime.parse("2024-03-01T10:00", DateTimeFormatter.ISO_DATE_TIME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
