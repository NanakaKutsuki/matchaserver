package org.kutsuki.matchaserver.document;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class City extends AbstractDocument implements Comparable<City> {
    private String city;
    private String email;

    @Override
    public int compareTo(City rhs) {
	return getCity().compareTo(rhs.getCity());
    }

    public String getCity() {
	return city;
    }

    public void setCity(String city) {
	this.city = city;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }
}
