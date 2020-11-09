package org.kutsuki.matchaserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.matchaserver.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MatchaServerApplicationTests {
    @Autowired
    private AlertRepository repository;

    @Test
    public void contextLoads() {
	System.out.println(repository.findAll().get(0).getAlert());
	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
