package org.kutsuki.matchaserver.dao;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.kutsuki.matchaserver.model.HotelModel;
import org.kutsuki.matchaserver.model.RoomModel;
import org.kutsuki.matchaserver.model.RoomSummaryModel;

public class RoomDao extends AbstractRoomDao {
    private static final BigDecimal ALERT = BigDecimal.TEN;
    private static final DateTimeFormatter MMMM_DD_YYYY = DateTimeFormatter.ofPattern("MMMM dd, YYYY EEEE");

    private static final String DATE_TIME = "dateTime";
    private static final String HOTEL_ID = "hotelId";
    private static final String LOCATION_ID = "locationId";
    private static final String SOLD_OUT_TITLE = "SOLD OUT! ";
    private static final String TYPE = "room";

    // 44, 46, 47, 48
    public static void main(String[] args) {
	HotelModel model = DaoManager.HOTEL.getById("48");
	model.setActive(false);
	DaoManager.HOTEL.index(model);
    }

    @Override
    public void emailRateAlert(String to, RoomModel room, BigDecimal prev) {
	String subject = "Rate Alert: " + room.getHotelName() + " in " + room.getLocationName() + ' '
		+ CURRENCY.format(room.getRate());

	StringBuilder sb = new StringBuilder();
	sb.append(room.getHotelName()).append('\n');
	sb.append(room.getLocationName()).append('\n');
	sb.append("Checking in ").append(MMMM_DD_YYYY.format(room.getZonedDateTime())).append("\n");
	sb.append("Previous Rate: ").append(CURRENCY.format(prev)).append('\n');
	sb.append("Current Rate: ").append(CURRENCY.format(room.getRate()));
	sb.append("Generated: ").append(now());

	DaoManager.EMAIL.email(to, subject, sb.toString());
    }

    @Override
    public void emailSoldOutAlert(String to, RoomModel room) {
	String subject = "Rate Alert: " + room.getHotelName() + " in " + room.getLocationName() + " is SOLD OUT!";

	StringBuilder sb = new StringBuilder();
	sb.append(room.getHotelName()).append('\n');
	sb.append(room.getLocationName()).append('\n');
	sb.append("Checking in ").append(MMMM_DD_YYYY.format(room.getZonedDateTime())).append("\n");
	if (room.getRate().compareTo(BigDecimal.ZERO) > 0) {
	    sb.append("Previous Rate: ").append(CURRENCY.format(room.getRate())).append('\n');
	}
	sb.append("Generated: ").append(now());
	sb.append("SOLD OUT!!!");

	DaoManager.EMAIL.email(to, subject, sb.toString());
    }

    @Override
    public void emailInventoryAlert(String to, RoomModel room, BigDecimal prev) {
	String subject = "Rate Alert: " + room.getHotelName() + " in " + room.getLocationName()
		+ " has ADDED INVENTORY!";

	StringBuilder sb = new StringBuilder();
	sb.append(room.getHotelName()).append('\n');
	sb.append(room.getLocationName()).append('\n');
	sb.append("Checking in ").append(MMMM_DD_YYYY.format(room.getZonedDateTime())).append("\n");
	if (room.getRate().compareTo(BigDecimal.ZERO) > 0) {
	    sb.append("Previous Rate: ").append(CURRENCY.format(prev)).append('\n');
	}
	sb.append("Current Rate: ").append(CURRENCY.format(room.getRate()));
	sb.append("Generated: ").append(now());

	DaoManager.EMAIL.email(to, subject, sb.toString());
    }

    @Override
    public BigDecimal getAlert() {
	return ALERT;
    }

    @Override
    public RoomModel getLatestRoom(HotelModel hotelModel) {
	// midnight MST
	ZonedDateTime from = startOfDay(now());

	TermQueryBuilder hotel = QueryBuilders.termQuery(HOTEL_ID, hotelModel.getId());
	TermQueryBuilder location = QueryBuilders.termQuery(LOCATION_ID, hotelModel.getLocationId());
	RangeQueryBuilder range = QueryBuilders.rangeQuery(DATE_TIME).from(toDate(from)).to(toDate(now()));
	BoolQueryBuilder query = QueryBuilders.boolQuery().must(hotel).must(location).must(range);

	List<RoomModel> roomList = search(query);

	TreeMap<ZonedDateTime, RoomModel> roomMap = new TreeMap<ZonedDateTime, RoomModel>();
	for (RoomModel model : roomList) {
	    if (!roomMap.containsKey(model.getZonedDateTime())) {
		roomMap.put(model.getZonedDateTime(), model);
	    } else {
		// remove duplicate
		delete(model);
	    }
	}

	RoomModel latest = null;
	if (!roomMap.isEmpty()) {
	    latest = roomMap.lastEntry().getValue();
	}

	return latest;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    public String getEveningSummary(String lid) {
	ZonedDateTime startDateTime = startOfDay(now()).plusDays(1);
	ZonedDateTime endDateTime = startOfDay(now()).plusDays(1).withHour(23);

	RangeQueryBuilder range = QueryBuilders.rangeQuery(DATE_TIME).from(toDate(startDateTime))
		.to(toDate(endDateTime));
	TermQueryBuilder term = QueryBuilders.termQuery(LOCATION_ID, lid);
	BoolQueryBuilder query = QueryBuilders.boolQuery().must(term).must(range);

	List<RoomModel> roomList = search(query);
	roomList = filterMostRecent(roomList);

	StringBuilder sb = new StringBuilder();
	sb.append("Rates for Tomorrow: " + MMMM_DD_YYYY.format(startDateTime));
	sb.append(System.lineSeparator());

	for (RoomModel model : roomList) {
	    sb.append(System.lineSeparator());
	    sb.append(CURRENCY.format(model.getRate()));

	    if (model.isSoldOut()) {
		sb.append(' ');
		sb.append(SOLD_OUT_TITLE);
	    } else {
		sb.append(':');
		sb.append(' ');
	    }

	    sb.append(model.getHotelName());
	}

	return sb.toString();
    }

    public String getWeeklySummary(String lid) {
	List<RoomModel> startRoomList = DaoManager.ROOM.getRoomsByDateRange(lid, now().minusYears(1),
		now().minusYears(1).plusMonths(1), false, false);
	List<RoomModel> endRoomList = DaoManager.ROOM.getRoomsByDateRange(lid, now().minusYears(1),
		now().minusYears(1).plusMonths(1), true, false);

	List<RoomSummaryModel> summaryList = new ArrayList<RoomSummaryModel>();
	for (int i = 0; i < startRoomList.size(); i++) {
	    summaryList.add(new RoomSummaryModel(startRoomList.get(i), endRoomList.get(i)));
	}

	StringBuilder sb = new StringBuilder();
	sb.append("Rates from Last Year:");
	sb.append(System.lineSeparator());

	String lastYYYYMMDD = "";
	Collections.sort(summaryList);
	for (RoomSummaryModel model : summaryList) {
	    if (!model.getYYYYMMDD().equals(lastYYYYMMDD)) {
		sb.append(System.lineSeparator());
		sb.append(MMMM_DD_YYYY.format(model.getZonedDateTime()));
		sb.append(System.lineSeparator());
		lastYYYYMMDD = model.getYYYYMMDD();
	    }

	    sb.append(CURRENCY.format(model.getStartingRate()));
	    if (!model.getStartingRate().equals(model.getEndingRate())) {
		sb.append('-');
		sb.append(CURRENCY.format(model.getEndingRate()));
	    }

	    if (model.isSoldOut()) {
		sb.append(' ');
		sb.append(SOLD_OUT_TITLE);
	    } else {
		sb.append(':');
		sb.append(' ');
	    }

	    sb.append(model.getHotelName());
	    sb.append(System.lineSeparator());
	}

	return sb.toString();
    }
}
