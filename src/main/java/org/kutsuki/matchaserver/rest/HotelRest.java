package org.kutsuki.matchaserver.rest;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.dao.DaoManager;
import org.kutsuki.matchaserver.model.scraper.HotelModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotelRest {
    @GetMapping("/rest/hotel/getHotel")
    public HotelModel getHotel(@RequestParam("hotelId") String hotelId) {
	return DaoManager.HOTEL.getById(hotelId);
    }

    @GetMapping("/rest/hotel/getNextHotel")
    public String getNextHotel() {
	return DaoManager.HOTEL.getNextHotel();
    }

    @GetMapping("/rest/hotel/getNextRefresh")
    public String getNextRefresh() {
	return DaoManager.HOTEL.getNextRefresh().toLocalDateTime().toString();
    }

    @GetMapping("/rest/hotel/getStatus")
    public List<HotelModel> getStatus() {
	return DaoManager.HOTEL.getStatus();
    }

    @GetMapping("/rest/hotel/getUnfinished")
    public List<HotelModel> getUnfinished() {
	return DaoManager.HOTEL.getUnfinished();
    }

    @GetMapping("/rest/hotel/getLink")
    public String getLink(@RequestParam("hotelId") String hotelId) {
	if (StringUtils.isBlank(hotelId)) {
	    hotelId = Integer.toString(8);
	}

	HotelModel model = DaoManager.HOTEL.getById(hotelId);
	model.setNextRuntime(DaoManager.HOTEL.now());
	return DaoManager.HOTEL.getLink(model);
    }

    @GetMapping("/rest/hotel/getAll")
    public List<HotelModel> getAll() {
	List<HotelModel> hotelList = DaoManager.HOTEL.getAll();
	Collections.sort(hotelList);
	return hotelList;
    }

    @GetMapping("/rest/hotel/isRestart")
    public Boolean isRestart() {
	return DaoManager.HOTEL.isRestart();
    }

    @GetMapping("/rest/hotel/reloadHotel")
    public ResponseEntity<String> reloadHotel(@RequestParam("hid") String hid) {
	DaoManager.HOTEL.reloadHotel(hid);

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/hotel/reloadHotels")
    public ResponseEntity<String> reloadHotels(@RequestParam("lid") String lid) {
	DaoManager.HOTEL.reloadHotels(lid);

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/hotel/restart")
    public ResponseEntity<String> restart() {
	DaoManager.HOTEL.setRestart(true);

	// return finished
	return ResponseEntity.ok().build();
    }
}