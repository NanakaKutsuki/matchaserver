package org.kutsuki.matchaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//TODO
// fix ssl
// convert to mongodb
// change back to 2.3.2.RELEASE
// undo the test cases
@SpringBootApplication
public class MatchaserverApplication {
    public static void main(String[] args) {
	SpringApplication.run(MatchaserverApplication.class, args);
    }
}
