package org.kutsuki.matchaserver.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document
public abstract class AbstractDateDocument extends AbstractDocument {
    @Transient
    @JsonIgnore
    private transient ZonedDateTime zonedDateTime;

    @Transient
    @JsonIgnore
    private transient String yyyymmdd;

    private Date date;

    // YYYY-MM-DD
    public String getYYYYMMDD() {
	if (yyyymmdd == null && date != null) {
	    yyyymmdd = StringUtils.substringBefore(getZonedDateTime().toString(), Character.toString('T'));
	}

	return yyyymmdd;
    }

    // getZonedDateTime
    public ZonedDateTime getZonedDateTime() {
	if (zonedDateTime == null && date != null) {
	    zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	return zonedDateTime;
    }

    // setZonedDateTime
    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
	this.zonedDateTime = zonedDateTime;
	this.setDate(Date.from(zonedDateTime.toInstant()));
    }

    public Date getDate() {
	return date;
    }

    public void setDate(Date date) {
	this.date = date;
    }
}
