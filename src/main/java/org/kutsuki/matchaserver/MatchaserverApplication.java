package org.kutsuki.matchaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//TODO
// fix ssl
// fix dao
// convert to mongodb
@SpringBootApplication
public class MatchaserverApplication {
    public static void main(String[] args) {
	SpringApplication.run(MatchaserverApplication.class, args);
    }
}
