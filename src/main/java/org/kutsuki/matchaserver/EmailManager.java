package org.kutsuki.matchaserver;

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

public class EmailManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailManager.class);

    public static final String HOME = "nanakakutsuki@gmail.com";

    private static final int RETRY = 5;
    private static final String EXCEPTION_SUBJECT = "Exception Thrown";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String MIDORI = "Green";
    private static final String PORT = "587";
    private static final String SIGNATURE = "<br/><br/>--<br/>Sentinel";
    private static final String SMTP = "smtp.1and1.com";
    private static final String TEXT_HTML = "text/html";
    private static final String USERNAME = "matcha.green@kutsuki.org";
    private static final String USERNAME_SENTINEL = "noreply@sentinel-corp.com";

    // email
    public static boolean email(String subject, String body) {
	return email(HOME, subject, body, null);
    }

    // email
    public static boolean email(String to, String subject, String body, List<String> attachments) {
	return email(USERNAME, to, subject, body, null);
    }

    // email
    public static boolean emailSentinel(String to, String subject, String body) {
	return email(USERNAME_SENTINEL, to, subject, body, null);
    }

    // email
    public static boolean email(final String user, String to, String subject, String body, List<String> attachments) {
	Properties props = new Properties();
	props.put(MAIL_SMTP_AUTH, Boolean.TRUE);
	props.put(MAIL_SMTP_STARTTLS_ENABLE, Boolean.TRUE);
	props.put(MAIL_SMTP_HOST, SMTP);
	props.put(MAIL_SMTP_PORT, PORT);

	Session session = Session.getInstance(props, new Authenticator() {
	    protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user,
			MIDORI + Integer.toString(0) + Integer.toString(0) + Character.toString(')'));
	    }
	});

	Message message = null;
	try {
	    message = new MimeMessage(session);
	    message.setFrom(new InternetAddress(user));
	    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	    message.setSubject(subject);

	    MimeBodyPart mbp1 = new MimeBodyPart();
	    StringBuilder sb = new StringBuilder();
	    sb.append(body);
	    sb.append(SIGNATURE);

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
	} catch (Exception e) {
	    LOGGER.error("Failed to create message: " + to + StringUtils.SPACE + subject, e);
	}

	return sendMessage(message);
    }

    // emailException
    public static boolean emailException(String message, Throwable e) {
	StringBuilder sb = new StringBuilder();
	sb.append(message);
	sb.append(System.lineSeparator());
	sb.append(System.lineSeparator());
	sb.append(e.getClass().getName());
	sb.append(':').append(StringUtils.SPACE);
	sb.append(e.getMessage());
	sb.append(System.lineSeparator());

	for (StackTraceElement element : e.getStackTrace()) {
	    sb.append('\t');
	    sb.append(element.toString());
	    sb.append(System.lineSeparator());
	}

	return email(HOME, EXCEPTION_SUBJECT, sb.toString(), null);
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
