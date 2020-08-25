package org.kutsuki.matchaserver.model;

public class EventModel {
    private String hotelId;
    private String date;
    private String name;
    private String rate;

    public String hotelId() {
	return hotelId;
    }

    public void setHotelId(String hotelId) {
	this.hotelId = hotelId;
    }

    public String getDate() {
	return date;
    }

    public void setDate(String date) {
	this.date = date;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getRate() {
	return rate;
    }

    public void setRate(String rate) {
	this.rate = rate;
    }
}
