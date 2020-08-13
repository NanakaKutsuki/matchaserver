package org.kutsuki.matchaserver.beating;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class BeatingMayor implements Comparable<BeatingMayor> {
    private String place;
    private int visited;
    private BeatingValue mayor;
    private BeatingValue deputy;
    private BeatingValue drain;

    public BeatingMayor(String place, int visited, List<String> mayors, BigDecimal mayorLost, List<String> deputies,
	    BigDecimal deputyLost, List<String> drains, BigDecimal drainWon) {
	this.place = place;
	this.visited = visited;
	this.mayor = new BeatingValue(combineList(mayors), mayorLost, 0);
	this.drain = new BeatingValue(combineList(drains), drainWon, 0);

	if (!deputies.isEmpty()) {
	    this.deputy = new BeatingValue(combineList(deputies), deputyLost, 0);
	}
    }

    @Override
    public int compareTo(BeatingMayor rhs) {
	return getPlace().compareTo(rhs.getPlace());
    }

    public String getPlace() {
	return place;
    }

    public int getVisited() {
	return visited;
    }

    public BeatingValue getMayor() {
	return mayor;
    }

    public BeatingValue getDeputy() {
	return deputy;
    }

    public BeatingValue getDrain() {
	return drain;
    }

    private String combineList(List<String> playerList) {
	Collections.sort(playerList);

	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < playerList.size(); i++) {
	    if (i != 0) {
		sb.append(',');
		sb.append(StringUtils.SPACE);
	    }

	    sb.append(playerList.get(i));
	}

	return sb.toString();
    }
}
