package org.kutsuki.matchaserver.model;

import java.math.BigDecimal;

import org.kutsuki.matchaserver.document.AbstractDateDocument;
import org.kutsuki.matchaserver.document.Room;

public class RoomSummaryModel extends AbstractDateDocument implements Comparable<RoomSummaryModel> {
    private String hotelName;
    private BigDecimal startingRate;
    private BigDecimal endingRate;
    private boolean soldOut;

    public RoomSummaryModel(Room start, Room end) {
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
