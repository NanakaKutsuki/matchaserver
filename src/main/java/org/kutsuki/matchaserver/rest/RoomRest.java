package org.kutsuki.matchaserver.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.dao.DaoManager;
import org.kutsuki.matchaserver.model.HotelModel;
import org.kutsuki.matchaserver.model.LocationModel;
import org.kutsuki.matchaserver.model.RoomModel;
import org.kutsuki.matchaserver.model.ScraperModel;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomRest {
    @GetMapping("/rest/room/getRooms")
    public List<ScraperModel> getRooms(@RequestParam("lid") String lid, @RequestParam("start") String start) {
	return DaoManager.ROOM.getRooms(lid, start);
    }

    @GetMapping("/rest/room/getRoomDetail")
    public List<RoomModel> getRoomsOverTime(@RequestParam("hid") String hid, @RequestParam("start") String start) {
	return DaoManager.ROOM.getRoomDetail(hid, start);
    }

    @GetMapping("/rest/room/indexRoom")
    public ResponseEntity<String> indexRoom(@RequestParam("rate") String rate, @RequestParam("href") String href) {
	// find hotel
	HotelModel hotelModel = DaoManager.HOTEL.getByLink(href);

	// find location
	LocationModel locationModel = DaoManager.LOCATION.getById(hotelModel.getLocationId());

	try {
	    DaoManager.ROOM.indexRoom(new BigDecimal(StringUtils.remove(rate, '$')), hotelModel, locationModel);
	} catch (NumberFormatException e) {
	    String error = "Error parsing price: " + rate + " for: " + hotelModel.getName() + " at "
		    + hotelModel.getLink();
	    EmailManager.emailException(error, e);
	}

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/room/soldOut")
    public ResponseEntity<String> soldOut(@RequestParam("href") String href) {
	// find hotel
	HotelModel hotelModel = DaoManager.HOTEL.getByLink(href);

	// find location
	LocationModel locationModel = DaoManager.LOCATION.getById(hotelModel.getLocationId());

	// indexing Sold Out
	DaoManager.ROOM.indexRoom(null, hotelModel, locationModel);

	// return finished
	return ResponseEntity.ok().build();
    }

    @Scheduled(cron = "0 30 20 * * SUN")
    public void weeklySummary() {
	for (LocationModel model : DaoManager.LOCATION.getAll()) {
	    String subject = model.getLocation() + " Weekly Summary";
	    EmailManager.email(model.getEmail(), subject, DaoManager.ROOM.getWeeklySummary(model.getId()));
	}
    }

    @Scheduled(cron = "0 0 11-21 * * *")
    public void checkLastRuntime() {
	if (DaoManager.HOTEL.now().isAfter(DaoManager.HOTEL.getLastRuntime().plusHours(1).plusMinutes(30))) {
	    EmailManager.emailHome("Check Scraper Box", "Last Runtime: " + DaoManager.HOTEL.getLastRuntime());
	}
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void eveningReport() {
	// EST
	LocalDate now = LocalDate.now();

	for (LocationModel model : DaoManager.LOCATION.getAll()) {
	    StringBuilder subject = new StringBuilder();
	    subject.append("Tomorrow in ");
	    subject.append(model.getLocation());
	    subject.append(StringUtils.SPACE);
	    subject.append('(');
	    subject.append(now.getMonthValue());
	    subject.append('/');
	    subject.append(now.getDayOfMonth());
	    subject.append(')');
	    EmailManager.email(model.getEmail(), subject.toString(), DaoManager.ROOM.getEveningSummary(model.getId()));
	}
    }
}