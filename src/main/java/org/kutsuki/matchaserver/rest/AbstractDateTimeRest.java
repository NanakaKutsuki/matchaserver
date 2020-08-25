package org.kutsuki.matchaserver.rest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

public abstract class AbstractDateTimeRest {
    private static final ZoneId MST = ZoneId.of("America/Denver");

    // now
    public ZonedDateTime now() {
	return ZonedDateTime.now(MST);
    }

    // startOfDay
    public ZonedDateTime startOfDay(ZonedDateTime zdt) {
	return zdt.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    // toDate
    public Date toDate(ZonedDateTime zdt) {
	return Date.from(zdt.toInstant());
    }

    // toZonedDateTime
    public ZonedDateTime toZonedDateTime(String date, LocalTime time) throws DateTimeParseException {
	return ZonedDateTime.of(LocalDate.parse(date), time, MST);
    }
}
