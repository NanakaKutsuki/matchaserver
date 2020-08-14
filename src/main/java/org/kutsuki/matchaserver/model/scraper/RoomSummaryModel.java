package org.kutsuki.matchaserver.model.scraper;

import java.math.BigDecimal;

import org.kutsuki.matchaserver.model.AbstractDateTimeModel;

public class RoomSummaryModel extends AbstractDateTimeModel implements Comparable<RoomSummaryModel> {
    private static final transient long serialVersionUID = -7055663053579028478L;

    private String hotelName;
    private BigDecimal startingRate;
    private BigDecimal endingRate;
    private boolean soldOut;

    public RoomSummaryModel(RoomModel start, RoomModel end) {
	this.hotelName = start.getHotelName();
	this.startingRate = start.getRate();
	this.endingRate = end.getRate();
	this.soldOut = start.isSoldOut() || end.isSoldOut();
	this.setZonedDateTime(end.getZonedDateTime());
    }

    public int compareTo(RoomSummaryModel rhs) {
	int result = getYYYYMMDD().compareTo(rhs.getYYYYMMDD());

	// if equal, use the hotel name
	if (result == 0) {
	    result = rhs.getEndingRate().compareTo(getEndingRate());

	    if (result == 0) {
		result = getHotelName().compareTo(rhs.getHotelName());
	    }
	}

	return result;
    }

    public String getHotelName() {
	return hotelName;
    }

    public BigDecimal getStartingRate() {
	return startingRate;
    }

    public BigDecimal getEndingRate() {
	return endingRate;
    }

    public boolean isSoldOut() {
	return soldOut;
    }
}
