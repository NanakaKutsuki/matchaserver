package org.kutsuki.matchaserver.model;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractDateTimeModel extends AbstractModel {
    private transient static final long serialVersionUID = -739432577468998363L;

    private transient ZonedDateTime zonedDateTime;
    private transient String yyyymmdd;

    private String dateTime;

    // YYYY-MM-DD
    public String getYYYYMMDD() {
	if (yyyymmdd == null && dateTime != null) {
	    yyyymmdd = StringUtils.substringBefore(dateTime, Character.toString('T'));
	}

	return yyyymmdd;
    }

    // getZonedDateTime
    public ZonedDateTime getZonedDateTime() {
	if (zonedDateTime == null && dateTime != null) {
	    zonedDateTime = ZonedDateTime.parse(dateTime);
	}

	return zonedDateTime;
    }

    // setZonedDateTime
    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
	this.zonedDateTime = zonedDateTime;
	this.setDateTime(StringUtils.substringBefore(zonedDateTime.toString(), Character.toString('[')));
    }

    public String getDateTime() {
	return dateTime;
    }

    public void setDateTime(String dateTime) {
	this.dateTime = dateTime;
    }
}
