package org.kutsuki.matchaserver.document;

import java.math.BigDecimal;

public class Document extends AbstractDocument {
    private String hotelId;
    private String locationId;
    private String hotelName;
    private String locationName;
    private BigDecimal rate;
    private boolean soldOut;
    private String dateTime;

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

    public String getHotelName() {
	return hotelName;
    }

    public void setHotelName(String hotelName) {
	this.hotelName = hotelName;
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

    public String getDateTime() {
	return dateTime;
    }

    public void setDateTime(String dateTime) {
	this.dateTime = dateTime;
    }

    public String getLocationName() {
	return locationName;
    }

    public void setLocationName(String locationName) {
	this.locationName = locationName;
    }
}
