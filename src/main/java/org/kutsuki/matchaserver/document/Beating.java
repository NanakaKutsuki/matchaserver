package org.kutsuki.matchaserver.document;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

//{"date":"2016-07-21","place":"Bon Chon","loser":"Chris Baird","total":50.25,"fancy":false,"playerMap":{"Jamie Duncan":1,"Paul Boudra":1,"Jay Owen":1,"Chris Baird":1}}
@Document
public class Beating extends AbstractDateDocument implements Comparable<Beating> {
    private String place;
    private String loser;
    private BigDecimal total;
    private boolean fancy;
    private Map<String, Integer> playerMap;

    @Override
    public int compareTo(Beating rhs) {
	int result = getZonedDateTime().toLocalDate().compareTo(rhs.getZonedDateTime().toLocalDate());

	if (result == 0) {
	    result = Integer.compare(Integer.parseInt(getId()), Integer.parseInt(rhs.getId()));
	}

	return result;
    }

    @Override
    public boolean equals(Object obj) {
	boolean equals = false;

	if (obj == null || obj.getClass() != getClass()) {
	    equals = false;
	} else if (obj == this) {
	    equals = true;
	} else {
	    Beating rhs = (Beating) obj;
	    EqualsBuilder eb = new EqualsBuilder();
	    eb.append(getDate(), rhs.getDate());
	    eb.append(getPlace(), rhs.getPlace());
	    eb.append(getLoser(), rhs.getLoser());
	    eb.append(getTotal(), rhs.getTotal());
	    eb.append(isFancy(), rhs.isFancy());
	    eb.append(getPlayerMap(), rhs.getPlayerMap());
	    equals = eb.isEquals();
	}

	return equals;
    }

    @Override
    public int hashCode() {
	HashCodeBuilder hcb = new HashCodeBuilder();
	hcb.append(getDate());
	hcb.append(getPlace());
	hcb.append(getLoser());
	hcb.append(getTotal());
	hcb.append(isFancy());
	hcb.append(getPlayerMap());
	return hcb.toHashCode();
    }

    public String getPlace() {
	return place;
    }

    public void setPlace(String place) {
	this.place = place;
    }

    public boolean isFancy() {
	return fancy;
    }

    public void setFancy(boolean fancy) {
	this.fancy = fancy;
    }

    public String getLoser() {
	return loser;
    }

    public void setLoser(String loser) {
	this.loser = loser;
    }

    public BigDecimal getTotal() {
	return total;
    }

    public void setTotal(BigDecimal total) {
	this.total = total;
    }

    public Map<String, Integer> getPlayerMap() {
	return playerMap;
    }

    public void setPlayerMap(Map<String, Integer> playerMap) {
	this.playerMap = playerMap;
    }
}
