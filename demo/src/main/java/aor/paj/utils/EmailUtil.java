package aor.paj.utils;

import jakarta.mail.Session;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {

    private static final String username = "jjoaommorais86@gmail.com";
    private static final String password = "spsn ihhe ysco sfgn";

    public static void sendEmail(String to, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        System.out.println("Sending email to " + to + " with subject " + subject + " and content " + content);

        Session session = Session.getDefaultInstance(props,
                new jakarta.mail.Authenticator() {
                    protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new jakarta.mail.PasswordAuthentication(username, password);
                    }
                });

        try {
            System.out.println("Sending email...");
            Message message = new MimeMessage(session);
            System.out.println("Message created");
            message.setFrom(new InternetAddress(username));
            System.out.println("From set");
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            System.out.println("Recipients set");
            message.setSubject(subject);
            System.out.println("Subject set");

            // Set the email content to HTML
            message.setContent(content, "text/html; charset=utf-8");


            System.out.println("Content set");
            try{
                Transport.send(message);
                System.out.println("Email sent");
            } catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }


        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    public static void sendVerificationEmail(String to, String userName, String verificationLink) {
        String subject = "Account Verification";
        String content = "<h1>Hello, " + userName + "!</h1>" +
                "<p>To verify your account, click the link below:</p>" +
                "<p><a href=\"" + verificationLink + "\">Verify Account</a></p>" +
                "<p>This link is valid for 1 hour.</p>" +
                "<p>If you didn't register, please ignore this email.</p>";

        sendEmail(to, subject, content);
    }

    public static void sendPasswordResetEmail(String to, String userName, String resetLink) {
        String subject = "Password Reset";
        String content = "<h1>Hello, " + userName + "!</h1>" +
                "<p>To reset your password, click the link below:</p>" +
                "<p><a href=\"" + resetLink + "\">Reset Password</a></p>" +
                "<p>This link is valid for 1 hour.</p>" +
                "<p>If you didn't request a password reset, please ignore this email.</p>";

        sendEmail(to, subject, content);
    }
}