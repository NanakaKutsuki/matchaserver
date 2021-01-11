package org.kutsuki.matchaserver;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class MatchaServerApplication {
    private static final String CONFIDENTIAL = "CONFIDENTIAL";
    private static final String CONNECTOR = "org.apache.coyote.http11.Http11NioProtocol";
    private static final String HTTP = "http";
    private static final String MAPPING = "/**";
    private static final String PATTERN = "/*";

    @Value("${local.port}")
    private int port;

    @Value("${server.port}")
    private int redirectPort;

    public static void main(String[] args) {
	SpringApplication.run(MatchaServerApplication.class, args);
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
	TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
	    @Override
	    protected void postProcessContext(Context context) {
		SecurityConstraint securityConstraint = new SecurityConstraint();
		securityConstraint.setUserConstraint(CONFIDENTIAL);
		SecurityCollection collection = new SecurityCollection();
		collection.addPattern(PATTERN);
		securityConstraint.addCollection(collection);
		context.addConstraint(securityConstraint);
	    }
	};

	tomcat.addAdditionalTomcatConnectors(redirectConnector());
	return tomcat;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
	return new WebMvcConfigurer() {
	    @Override
	    public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping(MAPPING);
	    }
	};
    }

    private Connector redirectConnector() {
	Connector connector = new Connector(CONNECTOR);
	connector.setScheme(HTTP);
	connector.setPort(port);
	connector.setSecure(false);
	connector.setRedirectPort(redirectPort);
	return connector;
    }
}
