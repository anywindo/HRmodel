package service.email;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        // In a real application, this would use JavaMailSender or an external API like SendGrid.
        System.out.println("========== MOCK EMAIL ==========");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body:");
        System.out.println(body);
        System.out.println("================================");
    }
}
