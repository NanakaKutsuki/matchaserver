package org.kutsuki.model;

public class EventModel {
    private String id;
    private String title;
    private String color;
    private String backgroundColor;
    private String start;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getColor() {
	return color;
    }

    public void setColor(String color) {
	this.color = color;
    }

    public String getBackgroundColor() {
	return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
	this.backgroundColor = backgroundColor;
    }

    public String getStart() {
	return start;
    }

    public void setStart(String start) {
	this.start = start;
    }
}
