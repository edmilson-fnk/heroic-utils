package heroic;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static heroic.Constants.*;

public class EmailSender {

    public static void sendMail(String filePath) {
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USER, EMAIL_PASS);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(EMAIL_USER));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(HEROIC_EMAIL));
            message.setSubject("Presença Woe-Woc");

            Multipart multipart = new MimeMultipart();
            MimeBodyPart attachmentPart = new MimeBodyPart();
            MimeBodyPart textPart = new MimeBodyPart();

            try {
                attachmentPart.attachFile(new File(filePath));
                textPart.setText("Arquivo anexado");
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(attachmentPart);
            } catch (IOException e) {
                e.printStackTrace();
            }

            message.setContent(multipart);

            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        } finally {
            new File(filePath).delete();
        }
    }

}
