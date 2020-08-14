package org.kutsuki.matchaserver.dao;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.model.HotelModel;

public class HotelDao extends AbstractDao<HotelModel> {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_RETRIES = 8;
    private static final String ACTIVE = "active";
    private static final String HEADER = "https://www.trivago.com/?";
    private static final String ARRIVE = "aDateRange[arr]=";
    private static final String DEPART = "&aDateRange[dep]=";
    private static final String RANGE = "&aPriceRange[from]=0&aPriceRange[to]=0";
    private static final String PATH_ID = "&iPathId=";
    private static final String LAT = "&aGeoCode[lat]=";
    private static final String LON = "&aGeoCode[lng]=";
    private static final String GEO_DISTANCE_ITEM = "&iGeoDistanceItem=";
    private static final String CPT = "&aCategoryRange=0,1,2,3,4,5&aOverallLiking=1,2,3,4,5&sOrderBy=relevance%20desc&bTopDealsOnly=false&iRoomType=7&cpt=";
    private static final String FOOTER = "&iIncludeAll=0&iViewType=0&bIsSeoPage=false&bIsSitemap=false&";
    private static final String LSB_HTML = "%5B";
    private static final String RSB_HTML = "%5D";
    private static final String LOCATION_ID = "locationId";
    private static final String TRIVAGO_ID = "trivagoId";
    private static final String TYPE = "hotel";

    private boolean restart;
    private List<String> hotelList;
    private Map<String, HotelModel> unfinishedMap;
    private ZonedDateTime lastCompleted;
    private ZonedDateTime lastRuntime;
    private ZonedDateTime nextRefresh;

    public HotelDao() {
	this.hotelList = new ArrayList<String>();
	this.lastCompleted = now();
	this.lastRuntime = now();
	this.restart = false;
	this.unfinishedMap = new ConcurrentHashMap<String, HotelModel>();
	setNextRefresh();
    }

    public static void main(String[] args) {
    }

    // getByLocationId
    public List<HotelModel> getByLocationId(String lid) {
	return search(QueryBuilders.termQuery(LOCATION_ID, lid));
    }

    // getByLink
    public HotelModel getByLink(String href) {
	HotelModel model = null;

	String link = StringUtils.replaceAll(href, LSB_HTML, Character.toString('['));
	link = StringUtils.replaceAll(link, RSB_HTML, Character.toString(']'));

	String trivagoId = StringUtils.substringBetween(link, GEO_DISTANCE_ITEM, Character.toString('&'));
	String date = StringUtils.substringBetween(link, ARRIVE, Character.toString('&'));

	if (StringUtils.isNotBlank(trivagoId) && StringUtils.isNotBlank(date)) {
	    List<HotelModel> searchList = search(QueryBuilders.termQuery(TRIVAGO_ID, trivagoId));
	    if (!searchList.isEmpty()) {
		model = searchList.get(0);

		try {
		    LocalDate ld = LocalDate.parse(date, DTF);
		    ZonedDateTime zdt = ZonedDateTime.of(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 0, 0, 0,
			    0, getMST());
		    model.setNextRuntime(zdt);
		} catch (DateTimeParseException e) {
		    EmailManager.emailException("Exception thrown while parsing: " + date, e);
		}
	    }
	}

	if (model == null) {
	    EmailManager.emailHome("Unable to Find Hotel!", link);
	}

	return model;
    }

    // getNextHotel
    public String getNextHotel() {
	HotelModel nextHotel = null;
	ZonedDateTime now = now();

	if (now.isAfter(nextRefresh) && now.getHour() >= 9 && now.getHour() <= 22) {
	    if (!unfinishedMap.isEmpty()) {
		StringBuilder sb = new StringBuilder();
		sb.append("Check Scraper!!!");
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());

		for (HotelModel model : unfinishedMap.values()) {
		    sb.append(model.getName());
		    sb.append(' ');
		    sb.append(model.getId()).append(System.lineSeparator());
		}

		EmailManager.emailHome(now() + " Unfinished Hotels!", sb.toString());
	    }

	    hotelList.clear();
	    unfinishedMap.clear();

	    for (HotelModel model : search(QueryBuilders.termQuery(ACTIVE, true))) {
		if (now.getHour() == 22) {
		    model.setNextRuntime(now.plusDays(1));
		} else {
		    model.setNextRuntime(now);
		}

		model.setRetries(0);

		hotelList.add(model.getId());
		unfinishedMap.put(model.getId(), model);
	    }

	    Collections.shuffle(hotelList);
	    setNextRefresh();
	    lastCompleted = now();
	}

	if (hotelList.isEmpty() && now().isAfter(lastCompleted.plusMinutes(1))) {
	    for (HotelModel model : unfinishedMap.values()) {
		if (model.getRetries() < MAX_RETRIES) {
		    model.setRetries(model.getRetries() + 1);
		    hotelList.add(model.getId());
		}
	    }

	    lastCompleted = now();
	}

	if (!hotelList.isEmpty()) {
	    nextHotel = unfinishedMap.get(hotelList.remove(0));
	}

	lastRuntime = now;
	return getLink(nextHotel);
    }

    // complete
    public void complete(String hotelId) {
	unfinishedMap.remove(hotelId);
	lastCompleted = now();
    }

    // getLastRuntime
    public ZonedDateTime getLastRuntime() {
	return lastRuntime;
    }

    public String getLink(HotelModel model) {
	String link = null;

	if (model != null) {
	    StringBuilder sb = new StringBuilder();

	    sb.append(HEADER);
	    sb.append(ARRIVE);
	    sb.append(DTF.format(model.getNextRuntime()));
	    sb.append(DEPART);
	    sb.append(DTF.format(model.getNextRuntime().plusDays(1)));
	    sb.append(RANGE);

	    if (model.getPathId() != null) {
		sb.append(PATH_ID);
		sb.append(model.getPathId());
	    }

	    if (model.getLatitude() != null) {
		sb.append(LAT);
		sb.append(model.getLatitude());
	    }

	    if (model.getLongitude() != null) {
		sb.append(LON);
		sb.append(model.getLongitude());
	    }

	    sb.append(GEO_DISTANCE_ITEM);
	    sb.append(model.getTrivagoId());
	    // iGeoDistanceLimit here
	    sb.append(CPT);
	    sb.append(model.getCPT());
	    sb.append(FOOTER);
	    link = sb.toString();
	}

	return link;
    }

    // getNextRefresh
    public ZonedDateTime getNextRefresh() {
	return nextRefresh;
    }

    // getStatus
    public List<HotelModel> getStatus() {
	List<HotelModel> statusList = new ArrayList<HotelModel>();

	for (String id : hotelList) {
	    HotelModel model = unfinishedMap.get(id);
	    statusList.add(model);
	}

	return statusList;
    }

    // getUnfinished
    public List<HotelModel> getUnfinished() {
	return new ArrayList<HotelModel>(unfinishedMap.values());
    }

    // isRestart
    public boolean isRestart() {
	if (now().toLocalTime().isAfter(lastRuntime.toLocalTime().plusMinutes(3))) {
	    Iterator<HotelModel> itr = unfinishedMap.values().iterator();
	    while (!restart && itr.hasNext()) {
		if (itr.next().getRetries() < MAX_RETRIES) {
		    restart = true;
		}
	    }
	}

	boolean result = restart;

	if (restart) {
	    this.restart = false;
	}

	return result;
    }

    // reloadHotel
    public void reloadHotel(String hid) {
	HotelModel model = getById(hid);

	if (model != null) {
	    model.setNextRuntime(now());
	    model.setRetries(0);

	    if (!hotelList.contains(model.getId())) {
		hotelList.add(model.getId());
		unfinishedMap.put(model.getId(), model);
	    }

	    Collections.shuffle(hotelList);
	}
    }

    // reloadHotels
    public void reloadHotels(String lid) {
	TermQueryBuilder active = QueryBuilders.termQuery(ACTIVE, true);
	BoolQueryBuilder query = QueryBuilders.boolQuery().must(active);

	if (StringUtils.isNotBlank(lid)) {
	    TermQueryBuilder location = QueryBuilders.termQuery(LOCATION_ID, lid);
	    query = query.must(location);
	}

	for (HotelModel model : search(query)) {
	    model.setNextRuntime(now());
	    model.setRetries(0);

	    if (!hotelList.contains(model.getId())) {
		hotelList.add(model.getId());
		unfinishedMap.put(model.getId(), model);
	    }
	}

	Collections.shuffle(hotelList);
    }

    // setRestart
    public void setRestart(boolean restart) {
	this.restart = restart;
    }

    // setNextRefresh
    private void setNextRefresh() {
	this.nextRefresh = now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }

    @Override
    public Class<HotelModel> getClazz() {
	return HotelModel.class;
    }

    @Override
    public String getType() {
	return TYPE;
    }
}
