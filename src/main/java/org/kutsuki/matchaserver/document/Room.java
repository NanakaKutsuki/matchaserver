package org.kutsuki.matchaserver.document;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Room extends AbstractDateDocument implements Comparable<Room> {
    private String hotelId;
    private String cityId;
    private String hotelName;
    private String cityName;
    private BigDecimal rate;
    private boolean soldOut;

    public int compareTo(Room rhs) {
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

    public String getCityName() {
	return cityName;
    }

    public void setCityName(String cityName) {
	this.cityName = cityName;
    }

    public String getHotelId() {
	return hotelId;
    }

    public void setHotelId(String hotelId) {
	this.hotelId = hotelId;
    }

    public String getCityId() {
	return cityId;
    }

    public void setCityId(String cityId) {
	this.cityId = cityId;
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
