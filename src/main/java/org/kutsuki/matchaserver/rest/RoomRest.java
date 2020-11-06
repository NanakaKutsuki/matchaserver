package org.kutsuki.matchaserver.rest;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.MatchaTracker;
import org.kutsuki.matchaserver.document.City;
import org.kutsuki.matchaserver.document.Hotel;
import org.kutsuki.matchaserver.document.Room;
import org.kutsuki.matchaserver.model.EventModel;
import org.kutsuki.matchaserver.model.RoomSummaryModel;
import org.kutsuki.matchaserver.repository.CityRepository;
import org.kutsuki.matchaserver.repository.HotelRepository;
import org.kutsuki.matchaserver.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomRest extends AbstractDateTimeRest {
    private static final BigDecimal ALERT = BigDecimal.TEN;
    private static final DateTimeFormatter MMMM_DD_YYYY = DateTimeFormatter.ofPattern("MMMM dd, YYYY EEEE");
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
    private static final String SOLD_OUT_TITLE = "SOLD OUT! ";

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping("/rest/room/getRooms")
    public List<EventModel> getRooms(@RequestParam("cityId") String cityId, @RequestParam("start") String startDate) {
	List<EventModel> eventList = new ArrayList<EventModel>();

	try {
	    ZonedDateTime endDateTime = toZonedDateTime(startDate, LocalTime.MAX);
	    Date start = toDate(endDateTime.minusDays(10));
	    Date end = toDate(endDateTime);

	    // get most recent rates
	    List<Room> roomList = roomRepository.findAllByCityIdAndDateBetween(cityId, start, end);
	    roomList = filterMostRecent(roomList);

	    TreeMap<LocalDate, TreeMap<String, EventModel>> dateScraperMap = new TreeMap<LocalDate, TreeMap<String, EventModel>>(
		    Collections.reverseOrder());
	    for (Room room : roomList) {
		EventModel event = new EventModel();

		StringBuilder sb = new StringBuilder();
		if (room.isSoldOut()) {
		    sb.append(SOLD_OUT_TITLE);
		    sb.append(StringUtils.SPACE);

		    if (room.getRate().compareTo(BigDecimal.ZERO) == 1) {
			sb.append('(');
			sb.append('$');
			sb.append(room.getRate());
			sb.append(')');
		    }
		} else {
		    sb.append('$');
		    sb.append(room.getRate());
		}

		event.setHotelId(room.getHotelId());
		event.setDate(room.getZonedDateTime().toLocalDate().toString());
		event.setName(room.getHotelName());
		event.setRate(sb.toString());

		TreeMap<String, EventModel> scraperMap = dateScraperMap.get(room.getZonedDateTime().toLocalDate());
		if (scraperMap == null) {
		    scraperMap = new TreeMap<String, EventModel>();
		}

		scraperMap.put(event.getName(), event);
		dateScraperMap.put(room.getZonedDateTime().toLocalDate(), scraperMap);
	    }

	    for (TreeMap<String, EventModel> scraperMap : dateScraperMap.values()) {
		for (EventModel model : scraperMap.values()) {
		    eventList.add(model);
		}
	    }
	} catch (DateTimeParseException e) {
	    EmailManager.emailException("Unable to parse start date while getRooms", e);
	}

	return eventList;
    }

    @GetMapping("/rest/room/getRoomDetail")
    public List<Room> getRoomsDetail(@RequestParam("hotelId") String hotelId, @RequestParam("start") String startDate) {
	Date start = toDate(toZonedDateTime(startDate, LocalTime.MIN));
	Date end = toDate(toZonedDateTime(startDate, LocalTime.MAX));

	// get rates
	List<Room> roomList = roomRepository.findAllByHotelIdAndDateBetween(hotelId, start, end);
	Collections.sort(roomList);
	return roomList;
    }

    @GetMapping("/rest/room/indexRoom")
    public ResponseEntity<String> indexRoom(@RequestParam("rate") String rate, @RequestParam("href") String href) {
	// find hotel
	Hotel hotel = getHotelByLink(href);

	// find city
	City city = cityRepository.findById(hotel.getCityId()).get();

	try {
	    ZonedDateTime begin = now().withHour(9).withMinute(0).withSecond(0).withNano(0);
	    ZonedDateTime end = now().withHour(23).withMinute(55).withSecond(0).withNano(0);

	    if (hotel != null && now().isAfter(begin) && now().isBefore(end)) {
		Room room = new Room();
		room.setHotelId(hotel.getId());
		room.setCityId(hotel.getCityId());
		room.setHotelName(hotel.getName());
		room.setCityName(city.getCity());

		if (rate != null) {
		    room.setRate(new BigDecimal(StringUtils.remove(rate, '$')));
		} else {
		    room.setRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
		    room.setSoldOut(true);
		}

		// set date time
		if (hotel.getNextRuntime().isAfter(now())) {
		    // set to midnight
		    room.setZonedDateTime(startOfDay(hotel.getNextRuntime()).withSecond(1));
		} else {
		    room.setZonedDateTime(now().withMinute(0).withSecond(0).withNano(0));

		    Room prev = getLatestRoom(hotel);
		    if (prev != null) {
			if (room.isSoldOut()) {
			    // currently full
			    room.setRate(prev.getRate());

			    // alert if it wasn't already full
			    if (!prev.isSoldOut()) {
				emailSoldOutAlert(city.getEmail(), room);
			    }
			} else if (!room.isSoldOut() && prev.isSoldOut()) {
			    // previously full and no longer full
			    emailInventoryAlert(city.getEmail(), room, prev.getRate());
			} else if (prev.getRate().compareTo(BigDecimal.ZERO) == 1
				&& room.getRate().compareTo(BigDecimal.ZERO) == 1
				&& (room.getRate().compareTo(prev.getRate().add(ALERT)) == 1
					|| room.getRate().compareTo(prev.getRate().subtract(ALERT)) == -1)) {
			    emailRateAlert(city.getEmail(), room, prev.getRate());
			}
		    } else {
			if (room.isSoldOut()) {
			    emailSoldOutAlert(city.getEmail(), room);
			}
		    }
		}

		// index room
		roomRepository.save(room);
		MatchaTracker.UNFINISHED_MAP.remove(hotel.getId());
		MatchaTracker.LAST_RUNTIME = now();
	    }
	} catch (NumberFormatException e) {
	    String error = "Error parsing price: " + rate + " for: " + hotel.getName() + " at " + hotel.getLink();
	    EmailManager.emailException(error, e);
	}

	// return finished
	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/room/soldOut")
    public ResponseEntity<String> soldOut(@RequestParam("href") String href) {
	// indexing Sold Out
	indexRoom(null, href);

	// return finished
	return ResponseEntity.ok().build();
    }

    @Scheduled(cron = "0 30 20 * * SUN")
    public void weeklySummary() {
	for (City city : cityRepository.findAll()) {
	    String subject = city.getCity() + " Weekly Summary";

	    List<Room> startRoomList = getRoomsByDateRange(city.getId(), now().minusYears(1),
		    now().minusYears(1).plusMonths(1), false, false);
	    List<Room> endRoomList = getRoomsByDateRange(city.getId(), now().minusYears(1),
		    now().minusYears(1).plusMonths(1), true, false);

	    List<RoomSummaryModel> summaryList = new ArrayList<RoomSummaryModel>();
	    for (int i = 0; i < startRoomList.size(); i++) {
		summaryList.add(new RoomSummaryModel(startRoomList.get(i), endRoomList.get(i)));
	    }

	    StringBuilder sb = new StringBuilder();
	    sb.append("Rates from Last Year:");
	    sb.append(EmailManager.NEW_LINE);
	    sb.append(EmailManager.NEW_LINE);

	    String lastYYYYMMDD = "";
	    Collections.sort(summaryList);
	    for (RoomSummaryModel model : summaryList) {
		if (!model.getYYYYMMDD().equals(lastYYYYMMDD)) {
		    sb.append(EmailManager.NEW_LINE);
		    sb.append(MMMM_DD_YYYY.format(model.getZonedDateTime()));
		    sb.append(EmailManager.NEW_LINE);
		    lastYYYYMMDD = model.getYYYYMMDD();
		}

		sb.append(CURRENCY.format(model.getStartingRate()));
		if (!model.getStartingRate().equals(model.getEndingRate())) {
		    sb.append('-');
		    sb.append(CURRENCY.format(model.getEndingRate()));
		}

		if (model.isSoldOut()) {
		    sb.append(StringUtils.SPACE);
		    sb.append(SOLD_OUT_TITLE);
		} else {
		    sb.append(':');
		    sb.append(StringUtils.SPACE);
		}

		sb.append(model.getHotelName());
		sb.append(EmailManager.NEW_LINE);
	    }

	    EmailManager.email(city.getEmail(), subject, sb.toString());
	}
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void eveningReport() {
	// EST
	LocalDate now = LocalDate.now();

	for (City city : cityRepository.findAll()) {
	    ZonedDateTime startDateTime = startOfDay(now()).plusDays(1);
	    Date start = toDate(startDateTime);
	    Date end = toDate(startDateTime.withHour(23));

	    List<Room> roomList = roomRepository.findAllByCityIdAndDateBetween(city.getId(), start, end);
	    roomList = filterMostRecent(roomList);

	    if (!roomList.isEmpty()) {
		StringBuilder subject = new StringBuilder();
		subject.append("Tomorrow in ");
		subject.append(city.getCity());
		subject.append(StringUtils.SPACE);
		subject.append('(');
		subject.append(now.getMonthValue());
		subject.append('/');
		subject.append(now.getDayOfMonth());
		subject.append(')');

		StringBuilder sb = new StringBuilder();
		sb.append("Rates for Tomorrow: " + MMMM_DD_YYYY.format(startDateTime));
		sb.append(EmailManager.NEW_LINE);

		Collections.sort(roomList);
		for (Room room : roomList) {
		    sb.append(EmailManager.NEW_LINE);
		    sb.append(CURRENCY.format(room.getRate()));

		    if (room.isSoldOut()) {
			sb.append(StringUtils.SPACE);
			sb.append(SOLD_OUT_TITLE);
		    } else {
			sb.append(':');
			sb.append(StringUtils.SPACE);
		    }

		    sb.append(room.getHotelName());
		}

		EmailManager.email(city.getEmail(), subject.toString(), sb.toString());
	    }
	}
    }

    // emailRateAlert
    private void emailRateAlert(String to, Room room, BigDecimal prev) {
	String subject = "Rate Alert: " + room.getHotelName() + " in " + room.getCityName() + StringUtils.SPACE
		+ CURRENCY.format(room.getRate());

	StringBuilder sb = new StringBuilder();
	sb.append(room.getHotelName()).append(EmailManager.NEW_LINE);
	sb.append(room.getCityName()).append(EmailManager.NEW_LINE);
	sb.append("Checking in ").append(MMMM_DD_YYYY.format(room.getZonedDateTime())).append(EmailManager.NEW_LINE);
	sb.append("Previous Rate: ").append(CURRENCY.format(prev)).append(EmailManager.NEW_LINE);
	sb.append("Current Rate: ").append(CURRENCY.format(room.getRate()));
	sb.append("Generated: ").append(now());

	EmailManager.email(to, subject, sb.toString());
    }

    // emailSoldOutAlert
    private void emailSoldOutAlert(String to, Room room) {
	String subject = "Rate Alert: " + room.getHotelName() + " in " + room.getCityName() + " is SOLD OUT!";

	StringBuilder sb = new StringBuilder();
	sb.append(room.getHotelName()).append(EmailManager.NEW_LINE);
	sb.append(room.getCityName()).append(EmailManager.NEW_LINE);
	sb.append("Checking in ").append(MMMM_DD_YYYY.format(room.getZonedDateTime())).append(EmailManager.NEW_LINE);
	if (room.getRate().compareTo(BigDecimal.ZERO) > 0) {
	    sb.append("Previous Rate: ").append(CURRENCY.format(room.getRate())).append(EmailManager.NEW_LINE);
	}
	sb.append("Generated: ").append(now());
	sb.append("SOLD OUT!!!");

	EmailManager.email(to, subject, sb.toString());
    }

    // emailInventoryAlert
    private void emailInventoryAlert(String to, Room room, BigDecimal prev) {
	String subject = "Rate Alert: " + room.getHotelName() + " in " + room.getCityName() + " has ADDED INVENTORY!";

	StringBuilder sb = new StringBuilder();
	sb.append(room.getHotelName()).append(EmailManager.NEW_LINE);
	sb.append(room.getCityName()).append(EmailManager.NEW_LINE);
	sb.append("Checking in ").append(MMMM_DD_YYYY.format(room.getZonedDateTime())).append(EmailManager.NEW_LINE);
	if (room.getRate().compareTo(BigDecimal.ZERO) > 0) {
	    sb.append("Previous Rate: ").append(CURRENCY.format(prev)).append(EmailManager.NEW_LINE);
	}
	sb.append("Current Rate: ").append(CURRENCY.format(room.getRate()));
	sb.append("Generated: ").append(now());

	EmailManager.email(to, subject, sb.toString());
    }

    // getHotelByLink
    private Hotel getHotelByLink(String href) {
	Hotel hotel = null;

	try {
	    String link = URLDecoder.decode(href, StandardCharsets.UTF_8.toString());

	    String trivagoId = StringUtils.substringBetween(link, HotelRest.GEO_DISTANCE_ITEM, Character.toString('&'));
	    String date = StringUtils.substringBetween(link, HotelRest.ARRIVE, Character.toString('&'));

	    if (StringUtils.isNotBlank(trivagoId) && StringUtils.isNotBlank(date)) {
		hotel = hotelRepository.findByTrivagoId(trivagoId);
		if (hotel != null) {
		    ZonedDateTime zdt = toZonedDateTime(date, LocalTime.MIN);
		    hotel.setNextRuntime(zdt);
		}
	    }

	    if (hotel == null) {
		EmailManager.emailHome("Unable to Find Hotel!", link);
	    }
	} catch (DateTimeParseException | UnsupportedEncodingException e) {
	    EmailManager.emailException("Exception thrown while parsing: " + href, e);
	}

	return hotel;
    }

    // getLatestRoom
    private Room getLatestRoom(Hotel hotel) {
	// midnight MST
	Date start = toDate(startOfDay(now()));
	Date end = toDate(now());

	TreeMap<ZonedDateTime, Room> roomMap = new TreeMap<ZonedDateTime, Room>();
	List<Room> roomList = roomRepository.findAllByHotelIdAndDateBetween(hotel.getId(), start, end);

	for (Room room : roomList) {
	    if (!roomMap.containsKey(room.getZonedDateTime())) {
		roomMap.put(room.getZonedDateTime(), room);
	    } else {
		// remove duplicate
		roomRepository.delete(room);
	    }
	}

	Room latest = null;
	if (!roomMap.isEmpty()) {
	    latest = roomMap.lastEntry().getValue();
	}

	return latest;
    }

    // getRoomsByDateRange
    private List<Room> getRoomsByDateRange(String cityId, ZonedDateTime startDateTime, ZonedDateTime endDateTime,
	    boolean latest, boolean soldOutOnly) {
	Date start = toDate(startDateTime);
	Date end = toDate(endDateTime);

	List<Room> roomList = roomRepository.findAllByCityIdAndSoldOutAndDateBetween(cityId, soldOutOnly, start, end);

	Map<String, Map<String, Room>> dateHotelRoomMap = new HashMap<String, Map<String, Room>>();
	ZonedDateTime startOfDay = startOfDay(startDateTime.plusDays(1));
	for (Room room : roomList) {
	    if (room.getZonedDateTime().isAfter(startOfDay)) {
		Map<String, Room> hotelRoomMap = dateHotelRoomMap.get(room.getYYYYMMDD());
		if (hotelRoomMap != null) {
		    Room prevRoom = hotelRoomMap.get(room.getHotelId());
		    if (prevRoom == null || (latest && room.getZonedDateTime().isAfter(prevRoom.getZonedDateTime()))
			    || (!latest && room.getZonedDateTime().isBefore(prevRoom.getZonedDateTime()))) {
			hotelRoomMap.put(room.getHotelId(), room);
		    }
		} else {
		    dateHotelRoomMap.put(room.getYYYYMMDD(), new HashMap<String, Room>());
		    dateHotelRoomMap.get(room.getYYYYMMDD()).put(room.getHotelId(), room);
		}
	    }
	}

	roomList.clear();
	for (Map<String, Room> hotelRoomMap : dateHotelRoomMap.values()) {
	    for (Room room : hotelRoomMap.values()) {
		roomList.add(room);
	    }
	}

	Collections.sort(roomList);
	return roomList;
    }

    // filterMostRecent
    private List<Room> filterMostRecent(List<Room> roomList) {
	List<Room> filteredRoomList = new ArrayList<Room>();
	Map<String, Map<String, Room>> dateRoomMap = new HashMap<String, Map<String, Room>>();

	// start filtering
	for (Room room : roomList) {
	    String yyyymmdd = room.getYYYYMMDD();

	    Map<String, Room> roomMap = dateRoomMap.get(yyyymmdd);
	    if (roomMap == null) {
		dateRoomMap.put(yyyymmdd, new HashMap<String, Room>());
		dateRoomMap.get(yyyymmdd).put(room.getHotelId(), room);
	    } else {
		Room rate = roomMap.get(room.getHotelId());

		if (rate == null || room.getZonedDateTime().isAfter(rate.getZonedDateTime())) {
		    roomMap.put(room.getHotelId(), room);
		}
	    }
	}

	// add remaining to the filter list
	for (Entry<String, Map<String, Room>> entry : dateRoomMap.entrySet()) {
	    for (Room room : entry.getValue().values()) {
		filteredRoomList.add(room);
	    }
	}

	Collections.sort(filteredRoomList);
	return filteredRoomList;
    }
}