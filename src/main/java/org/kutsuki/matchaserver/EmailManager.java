package org.kutsuki.matchaserver;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailManager.class);

    private static final int RETRY = 5;
    private static final String EXCEPTION_SUBJECT = "Exception Thrown";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String NEW_LINE = "<br/>";
    private static final String PORT = "587";
    private static final String SMTP = "smtp.ionos.com";
    private static final String TEXT_HTML = "text/html";

    private static String fromMatcha;
    private static String password;
    private static String toHome;

    @Autowired
    private EmailManager(@Value("${email.home}") String home, @Value("${email.matcha}") String matcha,
	    @Value("${email.password}") String password) {
	EmailManager.fromMatcha = matcha;
	EmailManager.password = password;
	EmailManager.toHome = home;
    }

    // email
    public static boolean email(String to, String subject, String body) {
	return email(fromMatcha, to, subject, body, null);
    }

    // email with attachments
    public static boolean emailAttachment(String to, String subject, String body, List<String> attachments) {
	return email(fromMatcha, to, subject, body, attachments);
    }

    // email Home
    public static boolean emailHome(String subject, String body) {
	return email(fromMatcha, toHome, subject, body, null);
    }

    // email
    public static boolean email(final String userName, String to, String subject, String body,
	    List<String> attachments) {
	Properties props = new Properties();
	props.put(MAIL_SMTP_AUTH, Boolean.TRUE);
	props.put(MAIL_SMTP_STARTTLS_ENABLE, Boolean.TRUE);
	props.put(MAIL_SMTP_HOST, SMTP);
	props.put(MAIL_SMTP_PORT, PORT);

	Session session = Session.getInstance(props, new Authenticator() {
	    protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	    }
	});

	Message message = null;
	try {
	    message = new MimeMessage(session);
	    message.setFrom(new InternetAddress(userName));
	    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	    message.setSubject(subject);

	    MimeBodyPart mbp1 = new MimeBodyPart();
	    StringBuilder sb = new StringBuilder();
	    sb.append(body);

	    mbp1.setContent(sb.toString(), TEXT_HTML);

	    Multipart mp = new MimeMultipart();
	    mp.addBodyPart(mbp1);

	    if (attachments != null) {
		for (String attachment : attachments) {
		    MimeBodyPart mbp = new MimeBodyPart();
		    mbp.attachFile(attachment);
		    mp.addBodyPart(mbp);
		}
	    }

	    message.setContent(mp);
	} catch (MessagingException | IOException e) {
	    LOGGER.error("Failed to create message: " + to + StringUtils.SPACE + subject, e);
	}

	return sendMessage(message);
    }

    // emailException
    public static boolean emailException(String message, Throwable e) {
	StringBuilder sb = new StringBuilder();
	sb.append(message);
	sb.append(NEW_LINE);
	sb.append(NEW_LINE);
	sb.append(e.getClass().getName());
	sb.append(':').append(StringUtils.SPACE);
	sb.append(e.getMessage());
	sb.append(NEW_LINE);

	for (StackTraceElement element : e.getStackTrace()) {
	    sb.append(element.toString());
	    sb.append(NEW_LINE);
	}

	return emailHome(EXCEPTION_SUBJECT, sb.toString());
    }

    // sendMessage
    private static boolean sendMessage(Message message) {
	boolean success = false;

	if (message != null) {
	    int i = 0;
	    while (i < RETRY && !success) {
		try {
		    Transport.send(message);
		    success = true;
		} catch (MessagingException e) {
		    LOGGER.error("Failed to send: " + (i + 1) + " tries.", e);

		    try {
			// sleep 1 second and then retry
			Thread.sleep(1000);
		    } catch (InterruptedException ie) {
			// ignore
		    }
		}

		i++;
	    }
	}

	return success;
    }
}
