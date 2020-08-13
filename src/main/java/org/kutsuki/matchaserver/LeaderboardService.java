package org.kutsuki.matchaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.model.LeaderboardModel;

public class LeaderboardService {
    private static final String LEADERBOARDS = " Leaderboards";
    private static final String YOU = "You";
    private static final String AVERAGE = "Company Average";
    private static final String WORKED_THIS_MONTH = "Worked This Month";
    private static final String OFF_THIS_MONTH = "Off This Month";
    private static final String WORKED_THIS_YEAR = "Worked This Year";
    private static final String WORKABLE_THIS_YEAR = "Workable This Year";
    private static final String OFF_THIS_YEAR = "Off This Year";
    private static final String TOTAL_DAYS_OFF = "Total Days Off";
    private static final String PERCENTAGE_WORKED = "Percentage Worked";
    private static final String FEDERAL_HOLIDAYS = "Federal Holidays";
    private static final String RANK = "Rank";
    private static final String HOURS = " hours";
    private static final String DAYS = " days";
    private static final String NOT_APPLICABLE = "n/a";
    private static final String OUT_OF = "Out of ";

    private static final String TABLE = "<table style=\"border: 1px solid black;border-collapse: collapse;\">";
    private static final String TABLE_END = "</table><br/></br>";
    private static final String TD = "<td style=\"border: 1px solid #ddd;padding: 8px;\">";
    private static final String TD_END = "</td>";
    private static final String TH = "<th style=\"border: 1px solid #ddd;padding: 8px;\">";
    private static final String TH_END = "</th>";
    private static final String TR = "<tr>";
    private static final String TR_END = "</tr>";

    private static final String ND = "nd";
    private static final String RD = "rd";
    private static final String ST = "st";
    private static final String NTH = "th";

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DTF_PARSE = DateTimeFormatter.ofPattern("M/d/yyyy");
    private int holidays;
    private List<LocalDate> holidayList;
    private LocalDate endDate;

    public LeaderboardService() {
	this.holidayList = new ArrayList<LocalDate>();
	this.holidayList.add(LocalDate.of(2020, 1, 1));
	this.holidayList.add(LocalDate.of(2020, 1, 20));
	this.holidayList.add(LocalDate.of(2020, 2, 17));
	this.holidayList.add(LocalDate.of(2020, 5, 25));
	this.holidayList.add(LocalDate.of(2020, 7, 3));
	this.holidayList.add(LocalDate.of(2020, 9, 7));
	this.holidayList.add(LocalDate.of(2020, 10, 12));
	this.holidayList.add(LocalDate.of(2020, 11, 11));
	this.holidayList.add(LocalDate.of(2020, 11, 26));
	this.holidayList.add(LocalDate.of(2020, 12, 25));

	if (LocalDate.now().minusWeeks(1).getYear() != holidayList.get(0).getYear()) {
	    throw new IllegalArgumentException("Holiday List needs to be updated! " + holidayList.get(0).getYear());
	}
    }

    public void send() {
	File file = new File("C:/Invoices - TimeOffCsv.csv");
	List<LeaderboardModel> modelList = new ArrayList<LeaderboardModel>();

	try (BufferedReader br = new BufferedReader(new FileReader(file));) {
	    // skip first line
	    String line = br.readLine();

	    // parse average from second line
	    line = br.readLine();
	    LeaderboardModel average = parseLine(line, true);
	    String subject = DTF.format(endDate) + LEADERBOARDS;

	    while ((line = br.readLine()) != null) {
		if (!StringUtils.startsWith(line, Character.toString(','))) {
		    modelList.add(parseLine(line, false));
		}
	    }

	    Collections.sort(modelList);

	    Random random = new Random();
	    int sendHome = random.nextInt(modelList.size());

	    for (int i = 0; i < modelList.size(); i++) {
		LeaderboardModel model = modelList.get(i);
		StringBuilder sb = new StringBuilder();
		sb.append(TABLE);
		sb.append(TR);
		sb.append(TH).append(DTF.format(endDate)).append(TH_END);
		sb.append(TH).append(YOU).append(TH_END);
		sb.append(TH).append(AVERAGE).append(TH_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(WORKED_THIS_MONTH).append(TD_END);
		sb.append(TD).append(model.getWorkedThisMonth()).append(HOURS).append(TD_END);
		sb.append(TD).append(average.getWorkedThisMonth()).append(HOURS).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(OFF_THIS_MONTH).append(TD_END);
		sb.append(TD).append(model.getOffThisMonth()).append(HOURS).append(TD_END);
		sb.append(TD).append(average.getOffThisMonth()).append(HOURS).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(WORKED_THIS_YEAR).append(TD_END);
		sb.append(TD).append(model.getWorkedThisYear()).append(HOURS).append(TD_END);
		sb.append(TD).append(average.getWorkedThisYear()).append(HOURS).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(WORKABLE_THIS_YEAR).append(TD_END);
		sb.append(TD).append(model.getWorkableThisYear()).append(HOURS).append(TD_END);
		sb.append(TD).append(NOT_APPLICABLE).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(OFF_THIS_YEAR).append(TD_END);
		sb.append(TD).append(model.getOffThisYear()).append(HOURS).append(TD_END);
		sb.append(TD).append(average.getOffThisYear()).append(HOURS).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(TOTAL_DAYS_OFF).append(TD_END);
		sb.append(TD).append(model.getTotalDaysOff()).append(DAYS).append(TD_END);
		sb.append(TD).append(average.getTotalDaysOff()).append(DAYS).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(PERCENTAGE_WORKED).append(TD_END);
		sb.append(TD).append(model.getPercentWorked()).append(TD_END);
		sb.append(TD).append(average.getPercentWorked()).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(FEDERAL_HOLIDAYS).append(TD_END);
		sb.append(TD).append(holidays).append(DAYS).append(TD_END);
		sb.append(TD).append(OUT_OF).append(holidayList.size()).append(TD_END);
		sb.append(TR_END);
		sb.append(TR);
		sb.append(TD).append(RANK).append(TD_END);
		sb.append(TD).append(ordinal(i + 1)).append(TD_END);
		sb.append(TD).append(OUT_OF).append(modelList.size()).append(TD_END);
		sb.append(TR_END);
		sb.append(TABLE_END);

		System.out.println("mailto: " + model.getEmail());
		EmailManager.emailSentinel(model.getEmail(), subject, sb.toString());

		if (i == sendHome) {
		    EmailManager.emailSentinel(EmailManager.HOME, model.getEmail(), sb.toString());
		}
	    }
	} catch (IOException e) {
	    EmailManager.emailException("Error while reading from File!", e);
	}
    }

    private LeaderboardModel parseLine(String line, boolean average) throws DateTimeParseException {
	String[] s = StringUtils.split(line, ',');

	if (average) {
	    endDate = LocalDate.parse(s[9], DTF_PARSE);

	    if (LocalDate.now().minusWeeks(1).isAfter(endDate)) {
		throw new IllegalArgumentException("Parsed end date is too old! " + endDate);
	    }

	    Iterator<LocalDate> itr = holidayList.iterator();
	    LocalDate date = itr.next();
	    while (date.isBefore(endDate)) {
		holidays++;

		if (itr.hasNext()) {
		    date = itr.next();
		} else {
		    date = LocalDate.MAX;
		}
	    }
	}

	return new LeaderboardModel(s, average);
    }

    private String ordinal(int i) {
	String[] suffixes = new String[] { NTH, ST, ND, RD, NTH, NTH, NTH, NTH, NTH, NTH };
	switch (i % 100) {
	case 11:
	case 12:
	case 13:
	    return i + NTH;
	default:
	    return i + suffixes[i % 10];

	}
    }

    public static void main(String[] args) {
	LeaderboardService rest = new LeaderboardService();
	rest.send();
    }
}
