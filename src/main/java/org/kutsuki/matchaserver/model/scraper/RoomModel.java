package org.kutsuki.matchaserver.model.scraper;

import java.math.BigDecimal;

import org.kutsuki.matchaserver.model.AbstractDateTimeModel;

public class RoomModel extends AbstractDateTimeModel implements Comparable<RoomModel> {
    private static final transient long serialVersionUID = -2628173653322721464L;

    private String hotelId;
    private String locationId;
    private String hotelName;
    private String locationName;
    private BigDecimal rate;
    private boolean soldOut;

    public int compareTo(RoomModel rhs) {
	int result = getYYYYMMDD().compareTo(rhs.getYYYYMMDD());

	// if equal, use the hotel name
	if (result == 0) {
	    if (getHotelName() != null && rhs.getHotelName() != null) {
		result = getHotelName().compareTo(rhs.getHotelName());
	    } else {
		result = Integer.compare(Integer.parseInt(getHotelId()), Integer.parseInt(rhs.getHotelId()));
	    }

	    // if hotel is the same, further sort by time
	    if (result == 0) {
		result = getZonedDateTime().compareTo(rhs.getZonedDateTime());
	    }
	}

	return result;
    }

    public String getHotelName() {
	return hotelName;
    }

    public void setHotelName(String hotelName) {
	this.hotelName = hotelName;
    }

    public String getLocationName() {
	return locationName;
    }

    public void setLocationName(String locationName) {
	this.locationName = locationName;
    }

    public String getHotelId() {
	return hotelId;
    }

    public void setHotelId(String hotelId) {
	this.hotelId = hotelId;
    }

    public String getLocationId() {
	return locationId;
    }

    public void setLocationId(String locationId) {
	this.locationId = locationId;
    }

    public BigDecimal getRate() {
	return rate;
    }

    public void setRate(BigDecimal rate) {
	this.rate = rate;
    }

    public boolean isSoldOut() {
	return soldOut;
    }

    public void setSoldOut(boolean soldOut) {
	this.soldOut = soldOut;
    }
}
