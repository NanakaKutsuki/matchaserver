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
import org.springframework.beans.factory.annotation.Value;

public class EmailManager {
    private Logger logger = LoggerFactory.getLogger(EmailManager.class);

    private static final int RETRY = 5;
    private static final String EXCEPTION_SUBJECT = "Exception Thrown";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String PORT = "587";
    private static final String SIGNATURE = "<br/><br/>--<br/>Sentinel";
    private static final String SMTP = "smtp.ionos.com";
    private static final String TEXT_HTML = "text/html";

    @Value("${email.home}")
    private String home;

    @Value("${email.matcha}")
    private String matchaUser;

    @Value("${email.sentinel}")
    private String sentinelUser;

    @Value("${email.pass}")
    private String pass;

    // email
    public boolean email(String to, String subject, String body) {
	return email(matchaUser, to, subject, body, null);
    }

    // email with attachments
    public boolean emailAttachment(String to, String subject, String body, List<String> attachments) {
	return email(matchaUser, to, subject, body, attachments);
    }

    // email Home
    public boolean emailHome(String subject, String body) {
	return email(matchaUser, home, subject, body, null);
    }

    // email Sentinel
    public boolean emailSentinel(String to, String subject, String body) {
	return email(sentinelUser, to, subject, body, null);
    }

    // email
    public boolean email(final String user, String to, String subject, String body, List<String> attachments) {
	Properties props = new Properties();
	props.put(MAIL_SMTP_AUTH, Boolean.TRUE);
	props.put(MAIL_SMTP_STARTTLS_ENABLE, Boolean.TRUE);
	props.put(MAIL_SMTP_HOST, SMTP);
	props.put(MAIL_SMTP_PORT, PORT);

	Session session = Session.getInstance(props, new Authenticator() {
	    protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, pass);
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
	    logger.error("Failed to create message: " + to + StringUtils.SPACE + subject, e);
	}

	return sendMessage(message);
    }

    // emailException
    public boolean emailException(String message, Throwable e) {
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

	return emailHome(EXCEPTION_SUBJECT, sb.toString());
    }

    // sendMessage
    private boolean sendMessage(Message message) {
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
