package org.kutsuki.model;

public class LocationModel extends AbstractModel implements Comparable<LocationModel> {
    private static final long serialVersionUID = 6787126613305482388L;

    private String location;
    private String email;

    @Override
    public int compareTo(LocationModel rhs) {
	return getLocation().compareTo(rhs.getLocation());
    }

    public String getLocation() {
	return location;
    }

    public void setLocation(String location) {
	this.location = location;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }
}
