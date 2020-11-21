package org.kutsuki.matchaserver.rest;

import java.io.File;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class AbstractRest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRest.class);
    private static final String EMSP = "&emsp;";
    private static final String EXCEPTION_SUBJECT = "Exception Thrown";
    private static final String LINE_BREAK = "<br/>";

    @Value("${email.matcha}")
    private String matcha;

    @Value("${email.home}")
    private String home;

    @Autowired
    private JavaMailSender javaMailSender;

    // email
    public void email(String bcc, String subject, String htmlBody) {
	email(matcha, bcc, subject, htmlBody, null, false);
    }

    // email with attachments
    public void emailAttachment(String bcc, String subject, String htmlBody, List<String> attachments) {
	email(matcha, bcc, subject, htmlBody, attachments, false);
    }

    // emailHome
    public void email(String subject, String htmlBody) {
	email(matcha, null, subject, htmlBody, null, false);
    }

    // email
    public void email(String from, String bcc, String subject, String htmlBody, List<String> attachments,
	    boolean noRetry) {
	try {
	    MimeMessage msg = javaMailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(msg, true);
	    helper.setFrom(from);
	    helper.setTo(home);
	    helper.setSubject(subject);
	    helper.setText(htmlBody, true);

	    if (bcc != null) {
		helper.setBcc(StringUtils.split(bcc, ','));
	    }

	    if (attachments != null) {
		for (String path : attachments) {
		    File file = new File(path);
		    helper.addAttachment(file.getName(), new FileSystemResource(file));
		}
	    }

	    javaMailSender.send(msg);
	} catch (MessagingException e) {
	    LOGGER.error("Error trying to Email: " + htmlBody, e);
	}
    }

    // emailException
    public void emailException(String message, Throwable e) {
	StringBuilder sb = new StringBuilder();
	sb.append(message);
	sb.append(LINE_BREAK);
	sb.append(LINE_BREAK);
	sb.append(e.getClass().getName());
	sb.append(':').append(StringUtils.SPACE);
	sb.append(e.getMessage());
	sb.append(LINE_BREAK);

	for (StackTraceElement element : e.getStackTrace()) {
	    sb.append(EMSP);
	    sb.append(element.toString());
	    sb.append(LINE_BREAK);
	}

	email(EXCEPTION_SUBJECT, sb.toString());
    }

    // getLinkBreak
    public String getLineBreak() {
	return LINE_BREAK;
    }
}
