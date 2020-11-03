package org.kutsuki.matchaserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.matchaserver.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MatchaServerApplicationTests {
    @Autowired
    private CityRepository repository;

    @Test
    public void contextLoads() {
	String txt2 = "#468 SOLD -2 1/2 BACKRATIO SPX 100 (Weeklys) 2 NOV 20 3230/3210 PUT @.10cr  Adding two more here (now it's directional, not just for credit).  If margin is an issue, bwb or pass.";
	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
