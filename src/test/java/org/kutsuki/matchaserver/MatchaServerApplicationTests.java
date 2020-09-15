package org.kutsuki.matchaserver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.matchaserver.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MatchaServerApplicationTests {
    @Autowired
    private RoomRepository repository;

    @Test
    public void contextLoads() {
//	List<Room> roomList = new ArrayList<Room>();
//	for (Room room : repository.findAll()) {
//	    ZonedDateTime zdt = room.getZonedDateTime().withZoneSameInstant(ZoneId.of("America/Denver"));
//	    if (zdt.getHour() == 0 && zdt.getSecond() == 0) {
//		room.setZonedDateTime(zdt.withSecond(1));
//		System.out.println(room.getHotelName() + " " + room.getZonedDateTime());
//		roomList.add(room);
//	    }
//	}
//
//	System.out.println("Saving: " + roomList.size());
//
//	repository.saveAll(roomList);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }

    // @Test
    public void sendLeaderboards() {
	LeaderboardParser parser = new LeaderboardParser();
	Assertions.assertTrue(parser.send(), "Failed to send Leaderboards!");
    }
}
