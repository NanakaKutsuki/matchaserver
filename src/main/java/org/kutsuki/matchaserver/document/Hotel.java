package org.kutsuki.matchaserver.document;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document
public class Hotel extends AbstractDocument implements Comparable<Hotel> {
    @Transient
    @JsonIgnore
    private int retires;

    @Transient
    @JsonIgnore
    private ZonedDateTime nextRuntime;

    private boolean active;
    private String link;
    private String cityId;
    private String latitude;
    private String longitude;
    private String name;
    private String pathId;
    private String trivagoId;
    private String cpt;

    @Override
    public int compareTo(Hotel rhs) {
	return getId().compareTo(rhs.getId());
    }

    @Override
    public boolean equals(Object obj) {
	boolean equals = false;

	if (obj == null || obj.getClass() != getClass()) {
	    equals = false;
	} else if (obj == this) {
	    equals = true;
	} else {
	    Hotel rhs = (Hotel) obj;
	    EqualsBuilder eb = new EqualsBuilder();
	    eb.append(getName(), rhs.getName());
	    eb.append(getCityId(), rhs.getCityId());
	    eb.append(getLink(), rhs.getLink());
	    eb.append(getLatitude(), rhs.getLatitude());
	    eb.append(getLongitude(), rhs.getLongitude());
	    eb.append(isActive(), rhs.isActive());
	    eb.append(getId(), rhs.getId());
	    eb.append(getPathId(), rhs.getPathId());
	    eb.append(getTrivagoId(), rhs.getTrivagoId());
	    eb.append(getCPT(), rhs.getCPT());
	    eb.append(getRetries(), rhs.getRetries());
	    eb.append(getNextRuntime(), rhs.getNextRuntime());
	    equals = eb.isEquals();
	}

	return equals;
    }

    @Override
    public int hashCode() {
	HashCodeBuilder hcb = new HashCodeBuilder();
	hcb.append(getName());
	hcb.append(getCityId());
	hcb.append(getLink());
	hcb.append(isActive());
	hcb.append(getId());
	hcb.append(getLatitude());
	hcb.append(getLongitude());
	hcb.append(getPathId());
	hcb.append(getCPT());
	hcb.append(getTrivagoId());
	hcb.append(getNextRuntime());
	hcb.append(getRetries());
	return hcb.toHashCode();
    }

    public boolean isActive() {
	return active;
    }

    public void setActive(boolean active) {
	this.active = active;
    }

    public String getLink() {
	return link;
    }

    public void setLink(String link) {
	this.link = link;
    }

    public String getCityId() {
	return cityId;
    }

    public void setCityId(String cityId) {
	this.cityId = cityId;
    }

    public String getCPT() {
	return cpt;
    }

    public void setCPT(String cpt) {
	this.cpt = cpt;
    }

    public String getLatitude() {
	return latitude;
    }

    public void setLatitude(String latitude) {
	this.latitude = latitude;
    }

    public String getLongitude() {
	return longitude;
    }

    public void setLongitude(String longitude) {
	this.longitude = longitude;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getPathId() {
	return pathId;
    }

    public void setPathId(String pathId) {
	this.pathId = pathId;
    }

    public int getRetries() {
	return retires;
    }

    public void setRetries(int retries) {
	this.retires = retries;
    }

    public String getTrivagoId() {
	return trivagoId;
    }

    public void setTrivagoId(String trivagoId) {
	this.trivagoId = trivagoId;
    }

    public ZonedDateTime getNextRuntime() {
	return nextRuntime;
    }

    public void setNextRuntime(ZonedDateTime nextRuntime) {
	this.nextRuntime = nextRuntime;
    }
}
