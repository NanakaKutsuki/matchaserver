package org.kutsuki.matchaserver.leaderboard;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kutsuki.matchaserver.EmailManager;

public class LeaderboardModel implements Comparable<LeaderboardModel> {
    private BigDecimal workedThisMonth;
    private BigDecimal offThisMonth;
    private BigDecimal workedThisYear;
    private BigDecimal workableThisYear;
    private BigDecimal offThisYear;
    private int totalDaysOff;
    private String percentWorked;
    private String email;

    public LeaderboardModel(String[] s, boolean average) {
	try {
	    if (average) {
		this.workedThisMonth = new BigDecimal(s[1]).setScale(2, RoundingMode.HALF_UP);
		this.offThisMonth = new BigDecimal(s[2]).setScale(2, RoundingMode.HALF_UP);
		this.workedThisYear = new BigDecimal(s[3]).setScale(2, RoundingMode.HALF_UP);
		this.workableThisYear = new BigDecimal(s[4]).setScale(2, RoundingMode.HALF_UP);
		this.offThisYear = new BigDecimal(s[5]).setScale(2, RoundingMode.HALF_UP);
		this.totalDaysOff = Integer.parseInt(s[6]);
		this.percentWorked = s[7];
	    } else {
		this.workedThisMonth = new BigDecimal(s[2]).setScale(2, RoundingMode.HALF_UP);
		this.offThisMonth = new BigDecimal(s[3]).setScale(2, RoundingMode.HALF_UP);
		this.workedThisYear = new BigDecimal(s[4]).setScale(2, RoundingMode.HALF_UP);
		this.workableThisYear = new BigDecimal(s[5]).setScale(2, RoundingMode.HALF_UP);
		this.offThisYear = new BigDecimal(s[6]).setScale(2, RoundingMode.HALF_UP);
		this.totalDaysOff = Integer.parseInt(s[7]);
		this.percentWorked = s[8];
		this.email = s[10];
	    }
	} catch (NumberFormatException e) {
	    EmailManager.emailException("Error while parsing LeaderboardModel", e);
	}
    }

    @Override
    public int compareTo(LeaderboardModel rhs) {
	return rhs.getOffThisYear().compareTo(getOffThisYear());
    }

    @Override
    public boolean equals(Object obj) {
	boolean equals = false;

	if (obj == null || obj.getClass() != getClass()) {
	    equals = false;
	} else if (obj == this) {
	    equals = true;
	} else {
	    LeaderboardModel rhs = (LeaderboardModel) obj;
	    EqualsBuilder eb = new EqualsBuilder();
	    eb.append(getWorkedThisMonth(), rhs.getWorkedThisMonth());
	    eb.append(getOffThisMonth(), rhs.getOffThisMonth());
	    eb.append(getWorkedThisYear(), rhs.getWorkedThisYear());
	    eb.append(getWorkableThisYear(), rhs.getWorkableThisYear());
	    eb.append(getOffThisYear(), rhs.getOffThisYear());
	    eb.append(getTotalDaysOff(), rhs.getTotalDaysOff());
	    eb.append(getPercentWorked(), rhs.getPercentWorked());
	    eb.append(getEmail(), rhs.getEmail());
	    equals = eb.isEquals();
	}

	return equals;
    }

    @Override
    public int hashCode() {
	HashCodeBuilder hcb = new HashCodeBuilder();
	hcb.append(getWorkedThisMonth());
	hcb.append(getOffThisMonth());
	hcb.append(getWorkedThisYear());
	hcb.append(getWorkableThisYear());
	hcb.append(getOffThisYear());
	hcb.append(getTotalDaysOff());
	hcb.append(getPercentWorked());
	hcb.append(getEmail());
	return hcb.toHashCode();
    }

    public BigDecimal getWorkedThisMonth() {
	return workedThisMonth;
    }

    public BigDecimal getOffThisMonth() {
	return offThisMonth;
    }

    public BigDecimal getWorkedThisYear() {
	return workedThisYear;
    }

    public BigDecimal getWorkableThisYear() {
	return workableThisYear;
    }

    public BigDecimal getOffThisYear() {
	return offThisYear;
    }

    public int getTotalDaysOff() {
	return totalDaysOff;
    }

    public String getPercentWorked() {
	return percentWorked;
    }

    public String getEmail() {
	return email;
    }
}
