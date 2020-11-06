package org.kutsuki.matchaserver.rest;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.MatchaTracker;
import org.kutsuki.matchaserver.document.Hotel;
import org.kutsuki.matchaserver.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotelRest extends AbstractDateTimeRest {
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_RETRIES = 5;
    private static final String HEADER = "https://www.trivago.com/?";
    public static final String ARRIVE = "aDateRange[arr]=";
    private static final String DEPART = "&aDateRange[dep]=";
    private static final String RANGE = "&aPriceRange[from]=0&aPriceRange[to]=0";
    private static final String PATH_ID = "&iPathId=";
    private static final String LAT = "&aGeoCode[lat]=";
    private static final String LON = "&aGeoCode[lng]=";
    public static final String GEO_DISTANCE_ITEM = "&iGeoDistanceItem=";
    private static final String CPT = "&aCategoryRange=0,1,2,3,4,5&aOverallLiking=1,2,3,4,5&sOrderBy=relevance%20desc&bTopDealsOnly=false&iRoomType=7&cpt=";
    private static final String FOOTER = "&iIncludeAll=0&iViewType=0&bIsSeoPage=false&bIsSitemap=false&";

    private boolean restart;
    private boolean sent;
    private List<String> hotelList;
    private LocalDateTime heartbeat;
    private ZonedDateTime lastCompleted;

    @Autowired
    private HotelRepository repository;

    public HotelRest() {
	this.heartbeat = LocalDateTime.now();
	this.hotelList = new ArrayList<String>();
	this.lastCompleted = now();
	this.restart = false;
	this.sent = false;
    }

    @GetMapping("/rest/hotel/getAll")
    public List<Hotel> getAll() {
	List<Hotel> hotelList = repository.findAll();
	Collections.sort(hotelList);
	return hotelList;
    }

    @GetMapping("/rest/hotel/getHotelById")
    public Hotel getHotelById(@RequestParam(value = "hotelId", defaultValue = "8") String hotelId) {
	Hotel hotel = null;
	Optional<Hotel> option = repository.findById(hotelId);

	if (option.isPresent()) {
	    hotel = option.get();
	}

	return hotel;
    }

    @GetMapping("/rest/hotel/getNextHotel")
    public String getNextHotel() {
	Hotel nextHotel = null;
	ZonedDateTime now = now();

	if (hotelList.isEmpty() && now().isAfter(lastCompleted.plusMinutes(1))) {
	    for (Hotel hotel : MatchaTracker.UNFINISHED_MAP.values()) {
		if (hotel.getRetries() < MAX_RETRIES) {
		    hotel.setRetries(hotel.getRetries() + 1);
		    hotelList.add(hotel.getId());
		}
	    }

	    lastCompleted = now;
	}

	if (!hotelList.isEmpty()) {
	    nextHotel = MatchaTracker.UNFINISHED_MAP.get(hotelList.remove(0));
	}

	MatchaTracker.LAST_RUNTIME = now;
	return getLink(nextHotel);
    }

    @GetMapping("/rest/hotel/getStatus")
    public List<Hotel> getStatus() {
	List<Hotel> statusList = new ArrayList<Hotel>();

	for (String id : hotelList) {
	    Hotel hotel = MatchaTracker.UNFINISHED_MAP.get(id);
	    statusList.add(hotel);
	}

	return statusList;
    }

    @GetMapping("/rest/hotel/getUnfinished")
    public List<Hotel> getUnfinished() {
	return new ArrayList<Hotel>(MatchaTracker.UNFINISHED_MAP.values());
    }

    @GetMapping("/rest/hotel/getLink")
    public String getLink(@RequestParam(value = "hotelId", defaultValue = "8") String hotelId) {
	Hotel hotel = getHotelById(hotelId);

	if (hotel != null) {
	    hotel.setNextRuntime(now());
	}

	return getLink(hotel);
    }

    @GetMapping("/rest/hotel/isRestart")
    public Boolean isRestart() {
	boolean result = restart;

	if (now().toLocalTime().isAfter(MatchaTracker.LAST_RUNTIME.toLocalTime().plusMinutes(3))) {
	    Iterator<Hotel> itr = MatchaTracker.UNFINISHED_MAP.values().iterator();
	    while (!restart && itr.hasNext()) {
		if (itr.next().getRetries() < MAX_RETRIES) {
		    restart = true;
		}
	    }
	}

	if (restart) {
	    this.restart = false;
	}

	heartbeat = LocalDateTime.now();
	sent = false;
	return result;
    }

    @GetMapping("/rest/hotel/reloadHotel")
    public ResponseEntity<String> reloadHotel(@RequestParam(value = "hotelId", required = true) String hotelId) {
	Hotel hotel = getHotelById(hotelId);

	if (hotel != null) {
	    hotel.setNextRuntime(now());
	    hotel.setRetries(0);

	    if (!hotelList.contains(hotel.getId())) {
		hotelList.add(hotel.getId());
		MatchaTracker.UNFINISHED_MAP.put(hotel.getId(), hotel);
	    }

	    Collections.shuffle(hotelList);
	}

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/hotel/reloadHotels")
    public ResponseEntity<String> reloadHotels(@RequestParam(value = "cityId", required = true) String cityId) {
	for (Hotel hotel : repository.findAllByCityIdAndActiveTrue(cityId)) {
	    hotel.setNextRuntime(now());
	    hotel.setRetries(0);

	    if (!hotelList.contains(hotel.getId())) {
		hotelList.add(hotel.getId());
		MatchaTracker.UNFINISHED_MAP.put(hotel.getId(), hotel);
	    }
	}

	Collections.shuffle(hotelList);

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/hotel/restart")
    public ResponseEntity<String> restart() {
	this.restart = true;

	// return finished
	return ResponseEntity.ok().build();
    }

    @Scheduled(cron = "0 0 0,11-23 * * *")
    public void refreshHotels() {
	ZonedDateTime now = now();

	if (!MatchaTracker.UNFINISHED_MAP.isEmpty()) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Check Scraper!!!");
	    sb.append(EmailManager.NEW_LINE);
	    sb.append(EmailManager.NEW_LINE);

	    for (Hotel hotel : MatchaTracker.UNFINISHED_MAP.values()) {
		sb.append(hotel.getName());
		sb.append(StringUtils.SPACE);
		sb.append(hotel.getId());
		sb.append(StringUtils.SPACE);
		sb.append(hotel.getNextRuntime());
		sb.append(EmailManager.NEW_LINE);
	    }

	    EmailManager.emailHome(now() + " Unfinished Hotels!", sb.toString());
	}

	hotelList.clear();
	MatchaTracker.UNFINISHED_MAP.clear();

	for (Hotel hotel : repository.findAllByActiveTrue()) {
	    if (now.getHour() == 22) {
		hotel.setNextRuntime(now.plusDays(1));
	    } else {
		hotel.setNextRuntime(now);
	    }
	    hotel.setRetries(0);

	    hotelList.add(hotel.getId());
	    MatchaTracker.UNFINISHED_MAP.put(hotel.getId(), hotel);
	}

	Collections.shuffle(hotelList);
	lastCompleted = now;
    }

    @Scheduled(cron = "0 0 0,11-23 * * *")
    public void checkLastRuntime() {
	if (now().isAfter(MatchaTracker.LAST_RUNTIME.plusHours(1))) {
	    EmailManager.emailHome("Check Scraper Box", "Last Runtime: " + MatchaTracker.LAST_RUNTIME);
	}
    }

    @Scheduled(cron = "0 * * * * *")
    public void checkScraper() {
	if (!sent && LocalDateTime.now().isAfter(heartbeat.plusMinutes(5))) {
	    EmailManager.emailHome("Check Scraper Box", "Last Heartbeat: " + heartbeat);
	    sent = true;
	}
    }

    // getLink
    private String getLink(Hotel hotel) {
	String link = null;

	if (hotel != null) {
	    StringBuilder sb = new StringBuilder();

	    sb.append(HEADER);
	    sb.append(ARRIVE);
	    sb.append(YYYYMMDD.format(hotel.getNextRuntime()));
	    sb.append(DEPART);
	    sb.append(YYYYMMDD.format(hotel.getNextRuntime().plusDays(1)));
	    sb.append(RANGE);

	    if (hotel.getPathId() != null) {
		sb.append(PATH_ID);
		sb.append(hotel.getPathId());
	    }

	    if (hotel.getLatitude() != null) {
		sb.append(LAT);
		sb.append(hotel.getLatitude());
	    }

	    if (hotel.getLongitude() != null) {
		sb.append(LON);
		sb.append(hotel.getLongitude());
	    }

	    sb.append(GEO_DISTANCE_ITEM);
	    sb.append(hotel.getTrivagoId());
	    // iGeoDistanceLimit here
	    sb.append(CPT);
	    sb.append(hotel.getCPT());
	    sb.append(FOOTER);
	    link = sb.toString();
	}

	return link;
    }
}