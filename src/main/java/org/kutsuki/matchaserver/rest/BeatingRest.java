package org.kutsuki.matchaserver.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.beating.BeatingBiggest;
import org.kutsuki.matchaserver.beating.BeatingCount;
import org.kutsuki.matchaserver.beating.BeatingResult;
import org.kutsuki.matchaserver.document.Beating;
import org.kutsuki.matchaserver.repository.BeatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class BeatingRest extends AbstractRest {
    private static final BigDecimal FANCY = new BigDecimal(65);
    private static final BigDecimal HUNDRED = new BigDecimal(100);

    @Autowired
    private BeatingRepository repository;

    private BeatingBiggest biggest;
    private List<Beating> beatingList;
    private List<String> loseList;
    private List<String> placeList;
    private List<String> playerList;
    private List<String> winList;
    private int maxWinStreak;
    private int maxLoseStreak;
    private int nextId;

    // constructor
    public BeatingRest() {
	this.placeList = new ArrayList<String>();
	this.playerList = new ArrayList<String>();
    }

    @PostConstruct
    public void postConstruct() {
	reloadCache(true);
    }

    @GetMapping("/rest/beating/addEvent")
    public ResponseEntity<String> addBeating(@RequestParam("date") String date, @RequestParam("place") String place,
	    @RequestParam("loser") String loser, @RequestParam("total") String total,
	    @RequestParam("playerMap") String playerMap) {
	if (StringUtils.isNotBlank(date) && StringUtils.isNotBlank(place) && StringUtils.isNotBlank(loser)
		&& StringUtils.isNotBlank(loser) && StringUtils.isNotBlank(total)
		&& StringUtils.isNotBlank(playerMap)) {
	    try {
		Beating beating = new Beating();
		beating.setId(Integer.toString(nextId));
		beating.setDate(Date.from(LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		beating.setPlace(place);
		beating.setLoser(loser);

		int totalCards = 0;
		Map<String, Integer> map = new HashMap<String, Integer>();
		String[] split = StringUtils.split(playerMap, ',');
		for (String s : split) {
		    String[] split2 = StringUtils.split(s, ':');
		    String player = split2[0];
		    int cards = Integer.parseInt(split2[1]);
		    totalCards += cards;
		    map.put(player, cards);
		}
		beating.setPlayerMap(map);

		BigDecimal bd = new BigDecimal(total);
		beating.setTotal(bd);

		BigDecimal costPerPerson = bd.divide(BigDecimal.valueOf(totalCards), 2, RoundingMode.HALF_UP);
		beating.setFancy(costPerPerson.compareTo(FANCY) == 1);

		repository.insert(beating);
		beatingList.add(beating);
		reloadCache(false);
	    } catch (DateTimeParseException | NumberFormatException e) {
		emailException("Error Adding Beating", e);
	    }
	}

	return ResponseEntity.ok().build();
    }

    @GetMapping("/rest/beating/getAll")
    public List<Beating> getAll() {
	return beatingList;
    }

    @GetMapping("/rest/beating/getBeatings")
    public BeatingResult getBeatings(@RequestParam("fancy") String fancyFilter, @RequestParam("start") String start,
	    @RequestParam("end") String end) {
	// convert inputs
	boolean fancy = Boolean.parseBoolean(fancyFilter);
	LocalDate startDate = LocalDate.MIN;
	if (StringUtils.isNotBlank(start)) {
	    startDate = LocalDate.parse(start);
	}

	LocalDate endDate = LocalDate.MAX;
	if (StringUtils.isNotBlank(end)) {
	    endDate = LocalDate.parse(end);
	}

	// init variables
	BigDecimal avgCost = BigDecimal.ZERO;
	BigDecimal avgMeal = BigDecimal.ZERO;
	BigDecimal totalCostPerPerson = BigDecimal.ZERO;
	BigDecimal totalSpent = BigDecimal.ZERO;
	int numEvents = 0;
	Map<String, BigDecimal> expectedMap = new HashMap<String, BigDecimal>();
	Map<String, BigDecimal> valueMap = new HashMap<String, BigDecimal>();
	Map<String, BigDecimal> winPctMap = new HashMap<String, BigDecimal>();
	Map<String, Map<String, BigDecimal>> mayorMap = new TreeMap<String, Map<String, BigDecimal>>();
	Map<String, Map<String, BigDecimal>> versusMap = new TreeMap<String, Map<String, BigDecimal>>();
	Map<String, Integer> lossesMap = new HashMap<String, Integer>();
	Map<String, Integer> placeMap = new HashMap<String, Integer>();
	Map<String, Integer> playedMap = new HashMap<String, Integer>();
	Map<String, Integer> streakMap = new HashMap<String, Integer>();
	this.biggest = new BeatingBiggest(null, null, BigDecimal.ZERO);
	this.loseList = new ArrayList<String>();
	this.maxLoseStreak = 0;
	this.maxWinStreak = 0;
	this.winList = new ArrayList<String>();

	// go through each event
	for (Beating beating : beatingList) {
	    if (dateFilter(beating.getZonedDateTime().toLocalDate(), startDate, endDate)) {
		// get total cards played
		BigDecimal totalCards = getTotalCards(beating);

		// calc value per card
		BigDecimal costPerCard = beating.getTotal().divide(totalCards, 4, RoundingMode.HALF_UP);

		// fancy filter, no cost per person greater than 65.
		if (!(fancy && beating.isFancy())) {
		    // add total cost per person
		    totalCostPerPerson = totalCostPerPerson.add(costPerCard);

		    // add total spent
		    totalSpent = totalSpent.add(beating.getTotal());

		    // go through each player
		    for (Entry<String, Integer> e : beating.getPlayerMap().entrySet()) {
			String player = e.getKey();
			int cards = e.getValue();

			calcExpected(expectedMap, player, beating.getLoser(), cards, totalCards);
			calcMayor(mayorMap, beating.getPlace(), player, cards, costPerCard);
			calcPlayed(playedMap, player, cards);
			calcStreak(streakMap, player, beating.getLoser());
			calcValue(valueMap, player, cards, costPerCard);
			calcVersus(versusMap, beating.getPlayerMap(), player, beating.getLoser(), cards, costPerCard);
		    }

		    // subtract value from loser
		    mayorMap.get(beating.getPlace()).put(beating.getLoser(),
			    getMapBD(mayorMap.get(beating.getPlace()), beating.getLoser())
				    .subtract(beating.getTotal()));
		    valueMap.put(beating.getLoser(),
			    getMapBD(valueMap, beating.getLoser()).subtract(beating.getTotal()));

		    // add loss
		    lossesMap.put(beating.getLoser(), getMapInt(lossesMap, beating.getLoser()) + 1);

		    // add place map
		    placeMap.put(beating.getPlace(), getMapInt(placeMap, beating.getPlace()) + 1);

		    // calc biggest beating
		    calcBiggest(beating);

		    // increment number of events
		    numEvents++;
		}
	    }
	}

	// calcAvgs
	if (numEvents > 0) {
	    avgCost = totalCostPerPerson.divide(BigDecimal.valueOf(numEvents), 2, RoundingMode.HALF_UP);
	    avgMeal = totalSpent.divide(BigDecimal.valueOf(numEvents), 2, RoundingMode.HALF_UP);
	}

	// calc win percentage
	calcWinPct(winPctMap, playedMap, lossesMap);

	// set results
	BeatingResult result = new BeatingResult();
	result.setAvgCost(avgCost);
	result.setAvgMeal(avgMeal);
	result.setBiggest(biggest);
	result.setMostVisited(calcMostVisited(placeMap));
	result.setLosingStreak(combineList(loseList, maxLoseStreak));
	result.setTotalSpent(totalSpent);
	result.setWinningStreak(combineList(winList, maxWinStreak));
	result.setExpectedList(expectedMap);
	result.setMayorList(mayorMap, placeMap);
	result.setPlayedList(playedMap);
	result.setStreakList(streakMap);
	result.setValueList(valueMap);
	result.setVersusList(versusMap);
	result.setWinPctList(winPctMap, playedMap);
	return result;
    }

    @CrossOrigin(origins = "https://beatings.sentinel-corp.com")
    @GetMapping("/rest/beating/getPlaces")
    public List<String> getPlaces() {
	return placeList;
    }

    @CrossOrigin(origins = "https://beatings.sentinel-corp.com")
    @GetMapping("/rest/beating/getPlayers")
    public List<String> getPlayers() {
	return playerList;
    }

    @CrossOrigin(origins = "https://beatings.sentinel-corp.com")
    @GetMapping("/rest/beating/reloadCache")
    public ResponseEntity<String> reloadCache() {
	reloadCache(true);
	return ResponseEntity.ok().build();
    }

    // combine List of players into one
    private BeatingCount combineList(List<String> playerList, int count) {
	Collections.sort(playerList);

	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < playerList.size(); i++) {
	    if (i != 0) {
		sb.append(',');
		sb.append(StringUtils.SPACE);
	    }

	    sb.append(playerList.get(i));
	}

	return new BeatingCount(sb.toString(), count);
    }

    // calcBiggestBeating
    private void calcBiggest(Beating beating) {
	if (beating.getTotal().compareTo(biggest.getValue()) == 1) {
	    this.biggest = new BeatingBiggest(beating.getLoser(), beating.getPlace(), beating.getTotal());
	}
    }

    // calcExpected
    private void calcExpected(Map<String, BigDecimal> expectedMap, String player, String loser, int cards,
	    BigDecimal totalCards) {
	BigDecimal expected = getMapBD(expectedMap, player);
	expected = expected.add(BigDecimal.valueOf(cards).divide(totalCards, 4, RoundingMode.HALF_UP));

	if (player.equals(loser)) {
	    expected = expected.subtract(BigDecimal.ONE);
	}

	expectedMap.put(player, expected);
    }

    // calcMayor
    private void calcMayor(Map<String, Map<String, BigDecimal>> mayorMap, String place, String player, int cards,
	    BigDecimal costPerCard) {
	Map<String, BigDecimal> valueMap = getMapTree(mayorMap, place);
	calcValue(valueMap, player, cards, costPerCard);
	mayorMap.put(place, valueMap);
    }

    // calcMostVisited
    private BeatingCount calcMostVisited(Map<String, Integer> placeMap) {
	BeatingCount mostVisited = null;
	int maxVisited = 0;
	for (Entry<String, Integer> e : placeMap.entrySet()) {
	    if (e.getValue() > maxVisited) {
		mostVisited = new BeatingCount(e.getKey(), e.getValue());
		maxVisited = e.getValue();
	    }
	}

	return mostVisited;
    }

    // calcPlayed
    private void calcPlayed(Map<String, Integer> playedMap, String player, int cards) {
	Integer played = getMapInt(playedMap, player);
	playedMap.put(player, played + cards);
    }

    // calcStreak
    private void calcStreak(Map<String, Integer> streakMap, String player, String loser) {
	Integer streak = getMapInt(streakMap, player);

	if (player.equals(loser)) {
	    if (streak < 0) {
		streak -= 1;
	    } else {
		streak = -1;
	    }

	    if (streak < maxLoseStreak) {
		loseList.clear();
		loseList.add(player);
		maxLoseStreak = streak;
	    } else if (streak == maxLoseStreak && !loseList.contains(player)) {
		loseList.add(player);
	    }
	} else {
	    if (streak > 0) {
		streak += 1;
	    } else {
		streak = 1;
	    }

	    if (streak > maxWinStreak) {
		winList.clear();
		winList.add(player);
		maxWinStreak = streak;
	    } else if (streak == maxWinStreak && !winList.contains(player)) {
		winList.add(player);
	    }
	}

	streakMap.put(player, streak);
    }

    // calcValue
    private void calcValue(Map<String, BigDecimal> valueMap, String player, int cards, BigDecimal costPerCard) {
	BigDecimal playerValue = getMapBD(valueMap, player);
	valueMap.put(player, playerValue.add(costPerCard.multiply(BigDecimal.valueOf(cards))));
    }

    // calcVersus
    private void calcVersus(Map<String, Map<String, BigDecimal>> versusMap, Map<String, Integer> playerMap,
	    String player, String loser, int cards, BigDecimal costPerCard) {
	Map<String, BigDecimal> valueMap = getMapTree(versusMap, player);

	if (player.equals(loser)) {
	    for (Entry<String, Integer> e : playerMap.entrySet()) {
		if (!e.getKey().equals(loser)) {
		    calcValue(valueMap, e.getKey(), e.getValue(), costPerCard.negate());
		}
	    }

	    versusMap.put(player, valueMap);
	} else {
	    calcValue(valueMap, loser, cards, costPerCard);
	    versusMap.put(player, valueMap);
	}
    }

    // calcWinPercentage
    private void calcWinPct(Map<String, BigDecimal> winPctMap, Map<String, Integer> playedMap,
	    Map<String, Integer> lossesMap) {
	for (String player : playedMap.keySet()) {
	    BigDecimal played = BigDecimal.valueOf(getMapInt(playedMap, player));
	    BigDecimal losses = BigDecimal.valueOf(getMapInt(lossesMap, player));
	    BigDecimal pct = BigDecimal.ONE.subtract(losses.divide(played, 4, RoundingMode.HALF_UP)).multiply(HUNDRED);
	    winPctMap.put(player, pct);
	}
    }

    // dateFilter
    private boolean dateFilter(LocalDate date, LocalDate startDate, LocalDate endDate) {
	return (date.isEqual(startDate) || date.isAfter(startDate))
		&& (date.isEqual(endDate) || date.isBefore(endDate));
    }

    // get BigDecimal from map
    private BigDecimal getMapBD(Map<String, BigDecimal> map, String key) {
	BigDecimal result = map.get(key);
	if (result == null) {
	    result = BigDecimal.ZERO;
	}

	return result;
    }

    // get Integer from map
    private Integer getMapInt(Map<String, Integer> map, String key) {
	Integer result = map.get(key);
	if (result == null) {
	    result = 0;
	}

	return result;
    }

    // get TreeMap from map
    private Map<String, BigDecimal> getMapTree(Map<String, Map<String, BigDecimal>> map, String key) {
	Map<String, BigDecimal> result = map.get(key);
	if (result == null) {
	    result = new TreeMap<String, BigDecimal>();
	}

	return result;
    }

    // getTotalCardsPlayed
    private BigDecimal getTotalCards(Beating beating) {
	int cards = 0;
	for (int i : beating.getPlayerMap().values()) {
	    cards += i;
	}

	return BigDecimal.valueOf(cards);
    }

    // reloadCache
    private void reloadCache(boolean full) {
	if (full) {
	    this.beatingList = repository.findAll();
	}

	Collections.sort(beatingList);

	this.nextId = beatingList.size() + 1;

	this.placeList.clear();
	this.playerList.clear();
	for (Beating beating : beatingList) {
	    if (!placeList.contains(beating.getPlace())) {
		placeList.add(beating.getPlace());
	    }

	    for (String player : beating.getPlayerMap().keySet()) {
		if (!playerList.contains(player)) {
		    playerList.add(player);
		}
	    }
	}

	Collections.sort(placeList);
	Collections.sort(playerList);
    }
}