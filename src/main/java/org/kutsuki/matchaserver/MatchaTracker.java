package org.kutsuki.matchaserver;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.kutsuki.matchaserver.document.Hotel;

public class MatchaTracker {
    public static ConcurrentHashMap<String, Hotel> UNFINISHED_MAP = new ConcurrentHashMap<String, Hotel>();
    public static ZonedDateTime LAST_RUNTIME = ZonedDateTime.now();
    public static ZonedDateTime LAST_CHROME_REPORT = ZonedDateTime.now();

    private MatchaTracker() {
	// private constructor
    }
}
