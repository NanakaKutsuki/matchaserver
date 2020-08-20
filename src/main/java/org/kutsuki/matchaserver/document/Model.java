package org.kutsuki.matchaserver.document;

public class Model extends AbstractDocument {
    private String location;
    private String email;

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
