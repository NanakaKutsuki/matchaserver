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
//	String text = "#414 SOLD -2 GLD 100 (Weeklys) 30 OCT 20 187 CALL @.36";
//	ShadowRest rest = new ShadowRest();
//	rest.uploadText(text);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
