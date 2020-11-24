package org.kutsuki.matchaserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.matchaserver.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MatchaServerApplicationTests {
    @Autowired
    private HotelRepository repository;

    @Test
    public void contextLoads() {
	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
