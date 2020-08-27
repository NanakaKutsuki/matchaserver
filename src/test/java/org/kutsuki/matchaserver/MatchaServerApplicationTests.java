package org.kutsuki.matchaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.matchaserver.document.Document;
import org.kutsuki.matchaserver.document.Room;
import org.kutsuki.matchaserver.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class MatchaServerApplicationTests {
    @Autowired
    private RoomRepository repository;

    @Test
    public void contextLoads() {
	System.out.println("Finding All");
	List<Room> roomList = repository.findAll();
	System.out.println("Found All of them");

	for (Room room : roomList) {
	    if (room.getZonedDateTime().withZoneSameInstant(ZoneId.of("America/Denver")).getHour() == 0) {
		System.out.println(room.getZonedDateTime().withZoneSameInstant(ZoneId.of("America/Denver")));
	    }
	}

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }

    // @Test
    public void fixMidnightRooms() {
	System.out.println("Finding All");
	List<Room> roomList = repository.findAll();
	System.out.println("Found All of them");

	List<Room> updateList = new ArrayList<Room>();
	for (Room room : roomList) {
	    if (room.getZonedDateTime().withZoneSameInstant(ZoneId.of("America/Denver")).getHour() == 0) {
		room.getDate().setSeconds(1);
		updateList.add(room);
	    }
	}

	System.out.println("Saving... " + updateList.size());
	repository.saveAll(updateList);
    }

    // @Test
    public void loadRooms() {
	try (BufferedReader br = new BufferedReader(
		new FileReader(new File("C:\\Users\\MatchaGreen\\Desktop\\rooms.json")))) {
	    String line = "";
	    List<Room> roomList = new ArrayList<Room>();

	    System.out.println("Parsing...");

	    while ((line = br.readLine()) != null) {
		ObjectMapper mapper = new ObjectMapper();
		Document d = mapper.readValue(line, Document.class);

		ZonedDateTime zdt = ZonedDateTime.parse(d.getDateTime());

		Room room = new Room();
		room.setCityId(d.getLocationId());
		room.setCityName(d.getLocationName());
		room.setDate(Date.from(zdt.toInstant()));
		room.setHotelId(d.getHotelId());
		room.setHotelName(d.getHotelName());
		room.setRate(d.getRate());
		room.setSoldOut(d.isSoldOut());
		roomList.add(room);
	    }

	    System.out.println("Parsed: " + roomList.size());
	    System.out.println("Now saving to MongoDB...");

	    repository.saveAll(roomList);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
