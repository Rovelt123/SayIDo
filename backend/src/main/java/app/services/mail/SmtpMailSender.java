package app.services.mail;

import app.utils.Utils;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class SmtpMailSender implements MailSender {

    private final String host =
            Utils.getPropertyValue("smtp.host", "config.properties");

    private final String port =
            Utils.getPropertyValue("smtp.port", "config.properties");

    private final String emailSender =
            Utils.getPropertyValue("email.sender", "config.properties");

    private final String username = System.getenv("SMTP_USERNAME");
    private final String password = System.getenv("SMTP_PASSWORD");

    // ________________________________________________________

    @Override
    public void sendMail(String recipient, String subject, String body) {

        if (username == null || username.isBlank() || password == null || password.isBlank()) {

            throw new IllegalStateException("SMTP_USERNAME or SMTP_PASSWORD is not set.");
        }

        Session session = Session.getInstance(
                createSmtpProperties(),
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {

                        return new PasswordAuthentication(username, password);
                    }
                }
        );

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailSender));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipient)
            );
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Could not send email via SMTP.", e);
        }
    }

    private Properties createSmtpProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.starttls.required", "true");
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        properties.setProperty("mail.smtp.connectiontimeout", "10000");
        properties.setProperty("mail.smtp.timeout", "20000");
        properties.setProperty("mail.smtp.writetimeout", "20000");
        return properties;
    }
}
