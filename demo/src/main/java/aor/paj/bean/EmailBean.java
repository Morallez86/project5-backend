package aor.paj.bean;

import aor.paj.utils.EmailUtil;
import jakarta.inject.Inject;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailBean {

    @Inject
    EmailUtil emailUtil;

    public void sendNewUserEmail(String userEmail) {
        // Set up mail server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "your_mail_host");
        properties.put("mail.smtp.port", "your_mail_port");
        properties.put("mail.smtp.auth", "true"); // if authentication is required
        properties.put("mail.smtp.starttls.enable", "true"); // if using TLS

        // Create a mail session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("your_username", "your_password");
            }
        });

        // Define email parameters
        String subject = "New User Registration";
        String body = "A new user has been registered with the email: " + userEmail;

        // Send email using EmailUtil class
        emailUtil.sendEmail(session, "recipient@example.com", subject, body);
    }
}
