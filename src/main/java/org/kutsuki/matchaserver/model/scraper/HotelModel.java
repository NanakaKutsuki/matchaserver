package org.kutsuki.matchaserver.model.scraper;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kutsuki.matchaserver.model.AbstractModel;

public class HotelModel extends AbstractModel implements Comparable<HotelModel> {
    private static final long serialVersionUID = 6057107340374642484L;

    private transient int retires;
    private transient ZonedDateTime nextRuntime;

    private boolean active;
    private String link;
    private String locationId;
    private String latitude;
    private String longitude;
    private String name;
    private String pathId;
    private String trivagoId;
    private String cpt;

    @Override
    public int compareTo(HotelModel rhs) {
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
	    HotelModel rhs = (HotelModel) obj;
	    EqualsBuilder eb = new EqualsBuilder();
	    eb.append(getName(), rhs.getName());
	    eb.append(getLocationId(), rhs.getLocationId());
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
	hcb.append(getLocationId());
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

    public String getLocationId() {
	return locationId;
    }

    public void setLocationId(String locationId) {
	this.locationId = locationId;
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
