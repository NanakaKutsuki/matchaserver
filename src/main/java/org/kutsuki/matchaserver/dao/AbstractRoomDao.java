package org.kutsuki.matchaserver.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.model.scraper.HotelModel;
import org.kutsuki.matchaserver.model.scraper.LocationModel;
import org.kutsuki.matchaserver.model.scraper.RoomModel;
import org.kutsuki.matchaserver.model.scraper.ScraperModel;

public abstract class AbstractRoomDao extends AbstractDao<RoomModel> {
    protected static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();
    protected static final NumberFormat PERCENT = NumberFormat.getPercentInstance();

    private static final String DATE_TIME = "dateTime";
    private static final String HOTEL_ID = "hotelId";
    private static final String LOCATION_ID = "locationId";
    private static final String SOLD_OUT = "soldOut";
    private static final String SOLD_OUT_TITLE = " SOLD OUT!";

    public abstract BigDecimal getAlert();

    public abstract RoomModel getLatestRoom(HotelModel hotelModel);

    public abstract void emailRateAlert(String to, RoomModel room, BigDecimal prev);

    public abstract void emailSoldOutAlert(String to, RoomModel room);

    public abstract void emailInventoryAlert(String to, RoomModel room, BigDecimal prev);

    public AbstractRoomDao() {
	PERCENT.setMinimumFractionDigits(2);
	PERCENT.setMaximumFractionDigits(2);
    }

    @Override
    public Class<RoomModel> getClazz() {
	return RoomModel.class;
    }

    // getRooms
    public List<ScraperModel> getRooms(String lid, String start) {
	List<ScraperModel> eventList = new ArrayList<ScraperModel>();

	try {
	    LocalDate ld = LocalDate.parse(start);
	    ZonedDateTime endDateTime = ZonedDateTime.of(ld, LocalTime.MAX, getMST());
	    ZonedDateTime startDateTime = endDateTime.minusDays(10);

	    RangeQueryBuilder range = QueryBuilders.rangeQuery(DATE_TIME).from(toDate(startDateTime))
		    .to(toDate(endDateTime));
	    TermQueryBuilder term = QueryBuilders.termQuery(LOCATION_ID, lid);

	    // get most recent rates
	    List<RoomModel> roomList = search(QueryBuilders.boolQuery().must(range).must(term));
	    roomList = filterMostRecent(roomList);

	    TreeMap<LocalDate, TreeMap<String, ScraperModel>> dateScraperMap = new TreeMap<LocalDate, TreeMap<String, ScraperModel>>(
		    Collections.reverseOrder());
	    for (RoomModel room : roomList) {
		ScraperModel event = new ScraperModel();

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

		event.setHid(room.getHotelId());
		event.setDate(room.getZonedDateTime().toLocalDate().toString());
		event.setName(room.getHotelName());
		event.setRate(sb.toString());

		TreeMap<String, ScraperModel> scraperMap = dateScraperMap.get(room.getZonedDateTime().toLocalDate());
		if (scraperMap == null) {
		    scraperMap = new TreeMap<String, ScraperModel>();
		}

		scraperMap.put(event.getName(), event);
		dateScraperMap.put(room.getZonedDateTime().toLocalDate(), scraperMap);
	    }

	    for (TreeMap<String, ScraperModel> scraperMap : dateScraperMap.values()) {
		for (ScraperModel model : scraperMap.values()) {
		    eventList.add(model);
		}
	    }
	} catch (DateTimeParseException e) {
	    EmailManager.emailException("Unable to parse start date while getRooms", e);
	}

	return eventList;
    }

    // getRoomDetail
    public List<RoomModel> getRoomDetail(String hid, String start) {
	LocalDate ld = LocalDate.parse(start);
	ZonedDateTime startDateTime = ZonedDateTime.of(ld, LocalTime.MIN, getMST());
	ZonedDateTime endDateTime = ZonedDateTime.of(ld, LocalTime.MAX, getMST());

	RangeQueryBuilder range = QueryBuilders.rangeQuery(DATE_TIME).from(toDate(startDateTime))
		.to(toDate(endDateTime));
	TermQueryBuilder term = QueryBuilders.termQuery(HOTEL_ID, hid);

	// get rates
	List<RoomModel> roomList = search(QueryBuilders.boolQuery().must(range).must(term));
	Collections.sort(roomList);
	return roomList;
    }

    // filterMostRecent
    public List<RoomModel> filterMostRecent(List<RoomModel> roomList) {
	List<RoomModel> filteredRoomList = new ArrayList<RoomModel>();
	Map<String, Map<String, RoomModel>> dateRoomMap = new HashMap<String, Map<String, RoomModel>>();

	// start filtering
	for (RoomModel room : roomList) {
	    String yyyymmdd = room.getYYYYMMDD();

	    Map<String, RoomModel> roomMap = dateRoomMap.get(yyyymmdd);
	    if (roomMap == null) {
		dateRoomMap.put(yyyymmdd, new HashMap<String, RoomModel>());
		dateRoomMap.get(yyyymmdd).put(room.getHotelId(), room);
	    } else {
		RoomModel rate = roomMap.get(room.getHotelId());

		if (rate == null || room.getZonedDateTime().isAfter(rate.getZonedDateTime())) {
		    roomMap.put(room.getHotelId(), room);
		}
	    }
	}

	// add remaining to the filter list
	for (Entry<String, Map<String, RoomModel>> entry : dateRoomMap.entrySet()) {
	    for (RoomModel room : entry.getValue().values()) {
		filteredRoomList.add(room);
	    }
	}

	Collections.sort(filteredRoomList);
	return filteredRoomList;
    }

    public List<RoomModel> getRoomsByDateRange(String lid, ZonedDateTime start, ZonedDateTime end, boolean latest,
	    boolean soldOutOnly) {
	RangeQueryBuilder range = QueryBuilders.rangeQuery(DATE_TIME).from(toDate(start)).to(toDate(end));
	TermQueryBuilder locationTerm = QueryBuilders.termQuery(LOCATION_ID, lid);
	BoolQueryBuilder query = QueryBuilders.boolQuery().must(range).must(locationTerm);

	if (soldOutOnly) {
	    TermQueryBuilder soldOutTerm = QueryBuilders.termQuery(SOLD_OUT, true);
	    query.must(soldOutTerm);
	}

	List<RoomModel> roomList = search(query);

	Map<String, Map<String, RoomModel>> dateHotelRoomMap = new HashMap<String, Map<String, RoomModel>>();
	ZonedDateTime startOfDay = startOfDay(start.plusDays(1));
	for (RoomModel model : roomList) {
	    if (model.getZonedDateTime().isAfter(startOfDay)) {
		Map<String, RoomModel> hotelRoomMap = dateHotelRoomMap.get(model.getYYYYMMDD());
		if (hotelRoomMap != null) {
		    RoomModel prevModel = hotelRoomMap.get(model.getHotelId());
		    if (prevModel == null || (latest && model.getZonedDateTime().isAfter(prevModel.getZonedDateTime()))
			    || (!latest && model.getZonedDateTime().isBefore(prevModel.getZonedDateTime()))) {
			hotelRoomMap.put(model.getHotelId(), model);
		    }
		} else {
		    dateHotelRoomMap.put(model.getYYYYMMDD(), new HashMap<String, RoomModel>());
		    dateHotelRoomMap.get(model.getYYYYMMDD()).put(model.getHotelId(), model);
		}
	    }
	}

	roomList.clear();
	for (Map<String, RoomModel> hotelRoomMap : dateHotelRoomMap.values()) {
	    for (RoomModel model : hotelRoomMap.values()) {
		roomList.add(model);
	    }
	}

	Collections.sort(roomList);
	return roomList;
    }

    public void indexRoom(BigDecimal rate, HotelModel hotelModel, LocationModel locationModel) {
	ZonedDateTime begin = now().withHour(9).withMinute(0).withSecond(0).withNano(0);
	ZonedDateTime end = now().withHour(23).withMinute(55).withSecond(0).withNano(0);

	if (hotelModel != null && now().isAfter(begin) && now().isBefore(end)) {
	    RoomModel roomModel = new RoomModel();
	    roomModel.setHotelId(hotelModel.getId());
	    roomModel.setLocationId(hotelModel.getLocationId());
	    roomModel.setHotelName(hotelModel.getName());
	    roomModel.setLocationName(locationModel.getLocation());

	    if (rate != null) {
		roomModel.setRate(rate);
	    } else {
		roomModel.setRate(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
		roomModel.setSoldOut(true);
	    }

	    // set date time
	    if (hotelModel.getNextRuntime().isAfter(now())) {
		// set to midnight
		roomModel.setZonedDateTime(startOfDay(hotelModel.getNextRuntime().withHour(0)));
	    } else {
		roomModel.setZonedDateTime(now().withMinute(0).withSecond(0).withNano(0));

		RoomModel prev = getLatestRoom(hotelModel);
		if (prev != null) {
		    if (roomModel.isSoldOut()) {
			// currently full
			setPrevRate(roomModel, prev.getRate());

			// alert if it wasn't already full
			if (!prev.isSoldOut()) {
			    emailSoldOutAlert(locationModel.getEmail(), roomModel);
			}
		    } else if (!roomModel.isSoldOut() && prev.isSoldOut()) {
			// previously full and no longer full
			emailInventoryAlert(locationModel.getEmail(), roomModel, prev.getRate());
		    } else if (prev.getRate().compareTo(BigDecimal.ZERO) == 1
			    && roomModel.getRate().compareTo(BigDecimal.ZERO) == 1
			    && (roomModel.getRate().compareTo(prev.getRate().add(getAlert())) == 1
				    || roomModel.getRate().compareTo(prev.getRate().subtract(getAlert())) == -1)) {
			emailRateAlert(locationModel.getEmail(), roomModel, prev.getRate());
		    }
		} else {
		    if (roomModel.isSoldOut()) {
			emailSoldOutAlert(locationModel.getEmail(), roomModel);
		    }
		}
	    }

	    // index room
	    index(roomModel);
	    DaoManager.HOTEL.complete(hotelModel.getId());
	}
    }

    public void setPrevRate(RoomModel roomModel, BigDecimal prevRate) {
	roomModel.setRate(prevRate);
    }

    public String toDate(ZonedDateTime zdt) {
	return StringUtils.substringBefore(zdt.toString(), Character.toString('['));
    }

    public ZonedDateTime startOfDay(ZonedDateTime zdt) {
	return zdt.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
}
