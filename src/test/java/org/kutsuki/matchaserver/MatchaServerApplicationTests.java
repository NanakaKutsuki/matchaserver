package org.kutsuki.matchaserver;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kutsuki.matchaserver.dao.DaoManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
//import org.junit.jupiter.api.Test;

// remove spring runner when upgrading, change @Test to jupiter
@RunWith(SpringRunner.class)
@SpringBootTest
public class MatchaServerApplicationTests {
    @Test
    public void contextLoads() {
	assertTrue("Unable to connect to Hotel index", DaoManager.HOTEL.count() > 0);
    }
}
