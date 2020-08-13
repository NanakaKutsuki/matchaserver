package org.kutsuki.beating;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BeatingResult {
    private BeatingBiggest biggest;
    private BeatingCount losingStreak;
    private BeatingCount mostVisited;
    private BeatingCount winningStreak;
    private BigDecimal avgCost;
    private BigDecimal avgMeal;
    private BigDecimal totalSpent;
    private List<BeatingValue> expectedList;
    private List<BeatingMayor> mayorList;
    private List<BeatingCount> playedList;
    private List<BeatingCount> streakList;
    private List<BeatingValue> valueList;
    private List<BeatingVersus> versusList;
    private List<BeatingWinPct> winPctList;

    public BigDecimal getAvgCost() {
	return avgCost;
    }

    public BigDecimal getAvgMeal() {
	return avgMeal;
    }

    public BeatingBiggest getBiggest() {
	return biggest;
    }

    public List<BeatingValue> getExpectedList() {
	return expectedList;
    }

    public BeatingCount getLosingStreak() {
	return losingStreak;
    }

    public List<BeatingMayor> getMayorList() {
	return mayorList;
    }

    public BeatingCount getMostVisited() {
	return mostVisited;
    }

    public List<BeatingCount> getPlayedList() {
	return playedList;
    }

    public List<BeatingCount> getStreakList() {
	return streakList;
    }

    public BigDecimal getTotalSpent() {
	return totalSpent;
    }

    public List<BeatingValue> getValueList() {
	return valueList;
    }

    public List<BeatingVersus> getVersusList() {
	return versusList;
    }

    public BeatingCount getWinningStreak() {
	return winningStreak;
    }

    public List<BeatingWinPct> getWinPctList() {
	return winPctList;
    }

    public void setAvgCost(BigDecimal avgCost) {
	this.avgCost = avgCost;
    }

    public void setAvgMeal(BigDecimal avgMeal) {
	this.avgMeal = avgMeal;
    }

    public void setBiggest(BeatingBiggest biggest) {
	this.biggest = biggest;
    }

    public void setExpectedList(Map<String, BigDecimal> expectedMap) {
	this.expectedList = mapToListByValue(expectedMap, 2);
    }

    public void setLosingStreak(BeatingCount losingStreak) {
	this.losingStreak = losingStreak;
    }

    public void setMayorList(Map<String, Map<String, BigDecimal>> mayorMap, Map<String, Integer> placeMap) {
	mayorList = new ArrayList<BeatingMayor>();
	for (Entry<String, Map<String, BigDecimal>> e : mayorMap.entrySet()) {
	    // convert map into list
	    List<BeatingValue> valueList = new ArrayList<BeatingValue>();
	    for (Entry<String, BigDecimal> e2 : e.getValue().entrySet()) {
		valueList.add(new BeatingValue(e2.getKey(), e2.getValue(), 0));
	    }
	    Collections.sort(valueList);

	    // find mayor
	    int i = 0;
	    List<String> mayors = new ArrayList<String>();
	    BigDecimal mayorLost = BigDecimal.ZERO;
	    BigDecimal thisValue = valueList.get(i).getValue();
	    if (thisValue.compareTo(BigDecimal.ZERO) == -1) {
		mayors.add(valueList.get(i).getPlayer());
		mayorLost = thisValue;
		i++;

		BigDecimal nextValue = valueList.get(i).getValue();
		while (nextValue.compareTo(BigDecimal.ZERO) == -1 && nextValue.equals(mayorLost)) {
		    mayors.add(valueList.get(i).getPlayer());

		    i++;
		    nextValue = valueList.get(i).getValue();
		}
	    }

	    // find deputy
	    List<String> deputies = new ArrayList<String>();
	    BigDecimal deputyLost = BigDecimal.ZERO;
	    thisValue = valueList.get(i).getValue();
	    if (thisValue.compareTo(BigDecimal.ZERO) == -1) {
		deputies.add(valueList.get(i).getPlayer());
		deputyLost = thisValue;
		i++;

		BigDecimal nextValue = valueList.get(i).getValue();
		while (nextValue.compareTo(BigDecimal.ZERO) == -1 && nextValue.equals(deputyLost)) {
		    deputies.add(valueList.get(i).getPlayer());

		    i++;
		    nextValue = valueList.get(i).getValue();
		}
	    }

	    // find drain
	    i = valueList.size() - 1;
	    List<String> drains = new ArrayList<String>();
	    BigDecimal drainWon = BigDecimal.ZERO;
	    thisValue = valueList.get(i).getValue();
	    if (thisValue.compareTo(BigDecimal.ZERO) == 1) {
		drains.add(valueList.get(i).getPlayer());
		drainWon = thisValue;
		i--;

		BigDecimal nextValue = valueList.get(i).getValue();
		while (nextValue.compareTo(BigDecimal.ZERO) == 1 && nextValue.equals(drainWon)) {
		    drains.add(valueList.get(i).getPlayer());

		    i--;
		    nextValue = valueList.get(i).getValue();
		}
	    }

	    BeatingMayor mayor = new BeatingMayor(e.getKey(), placeMap.get(e.getKey()), mayors, mayorLost, deputies,
		    deputyLost, drains, drainWon);
	    mayorList.add(mayor);
	}

	Collections.sort(mayorList);
    }

    public void setMostVisited(BeatingCount mostVisited) {
	this.mostVisited = mostVisited;
    }

    public void setPlayedList(Map<String, Integer> playedMap) {
	this.playedList = mapToListByCount(playedMap);
    }

    public void setStreakList(Map<String, Integer> streakMap) {
	this.streakList = mapToListByCount(streakMap);
    }

    public void setTotalSpent(BigDecimal totalSpent) {
	this.totalSpent = totalSpent;
    }

    public void setValueList(Map<String, BigDecimal> valueMap) {
	this.valueList = mapToListByValue(valueMap, 2);
    }

    public void setVersusList(Map<String, Map<String, BigDecimal>> versusMap) {
	this.versusList = new ArrayList<BeatingVersus>();
	for (Entry<String, Map<String, BigDecimal>> e : versusMap.entrySet()) {
	    BeatingVersus versus = new BeatingVersus(e.getKey());

	    for (Entry<String, BigDecimal> e2 : e.getValue().entrySet()) {
		versus.addEnemy(e2.getKey(), e2.getValue());
	    }

	    versusList.add(versus);
	}

	Collections.sort(versusList);
    }

    public void setWinningStreak(BeatingCount winningStreak) {
	this.winningStreak = winningStreak;
    }

    public void setWinPctList(Map<String, BigDecimal> winPctMap, Map<String, Integer> playedMap) {
	this.winPctList = new ArrayList<BeatingWinPct>();
	for (Entry<String, BigDecimal> e : winPctMap.entrySet()) {
	    winPctList.add(new BeatingWinPct(e.getKey(), playedMap.get(e.getKey()), e.getValue()));
	}

	Collections.sort(winPctList);
    }

    // convert Map to List and sort by count
    private List<BeatingCount> mapToListByCount(Map<String, Integer> map) {
	List<BeatingCount> list = new ArrayList<BeatingCount>();
	for (Entry<String, Integer> e : map.entrySet()) {
	    list.add(new BeatingCount(e.getKey(), e.getValue()));
	}

	Collections.sort(list);
	return list;
    }

    // convert Map to List and sort by value
    private List<BeatingValue> mapToListByValue(Map<String, BigDecimal> map, int newScale) {
	List<BeatingValue> list = new ArrayList<BeatingValue>();
	for (Entry<String, BigDecimal> e : map.entrySet()) {
	    list.add(new BeatingValue(e.getKey(), e.getValue(), newScale));
	}

	Collections.sort(list);
	return list;
    }
}
