package org.kutsuki.matchaserver.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShadowRest {
    @Value("${email.shadow}")
    private String emailShadow;

    @GetMapping("/rest/shadow/uploadText")
    public ResponseEntity<String> uploadText(@RequestParam("text") String text) {
	try {
	    String body = URLDecoder.decode(text, StandardCharsets.UTF_8.name());
	    String subject = StringUtils.substringBefore(body, StringUtils.SPACE);
	    EmailManager.email(emailShadow, subject, body);

	    EmailManager.emailHome(subject, body);
	} catch (UnsupportedEncodingException e) {
	    EmailManager.emailException(text, e);
	}

	// return finished
	return ResponseEntity.ok().build();
    }
}