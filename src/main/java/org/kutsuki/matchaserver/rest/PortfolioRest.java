package org.kutsuki.matchaserver.rest;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.document.Position;
import org.kutsuki.matchaserver.model.OptionType;
import org.kutsuki.matchaserver.model.OrderModel;
import org.kutsuki.matchaserver.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioRest {
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
	    .appendPattern("d MMM yy").toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mma");

    private static final String BACKRATIO = "BACKRATIO";
    private static final String BOT = "BOT ";
    private static final String BUY = "BUY ";
    private static final String BUY_TO_CLOSE = "BUY_TO_CLOSE";
    private static final String BUY_TO_OPEN = "BUY_TO_OPEN";
    private static final String BUTTERFLY = "BUTTERFLY";
    private static final String CONDOR = "CONDOR";
    private static final String HUNDRED = "100";
    private static final String IRON_CONDOR = "IRON CONDOR";
    private static final String NEW = "NEW";
    private static final String SELL = "SELL ";
    private static final String SELL_TO_CLOSE = "SELL_TO_CLOSE";
    private static final String SELL_TO_OPEN = "SELL_TO_OPEN";
    private static final String SOLD = "SOLD ";
    private static final String ST_WEEKLY = "ST Weekly";
    private static final String WEEKLYS = "(Weeklys)";
    private static final String VERTICAL = "VERTICAL";
    private static final String UNBALANCED_BUTTERFLY = "~BUTTERFLY";

    @Autowired
    private PortfolioRepository repository;

    @Value("${email.portfolio}")
    private String emailPortfolio;

    @GetMapping("/rest/portfolio/uploadAlert")
    public ResponseEntity<String> uploadAlert(@RequestParam("alert") String alert) {
	try {
	    StringBuilder subject = new StringBuilder();
	    StringBuilder body = new StringBuilder();

	    String escaped = URLDecoder.decode(alert, StandardCharsets.UTF_8.toString());
	    body.append(escaped);

	    if (StringUtils.startsWith(body, Character.toString('#'))) {
		subject.append(StringUtils.substringBefore(escaped, StringUtils.SPACE));

		if (StringUtils.containsIgnoreCase(escaped, NEW)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(NEW);
		}

		boolean first = true;
		body.append(EmailManager.NEW_LINE);
		body.append(EmailManager.NEW_LINE);

		for (OrderModel order : createOrder(escaped)) {
		    if (!first) {
			subject.append(StringUtils.SPACE);
			subject.append('&');

			for (int i = 0; i < 50; i++) {
			    body.append('-');
			}
			body.append(EmailManager.NEW_LINE);
		    }

		    subject.append(StringUtils.SPACE);
		    subject.append(order.getSymbol());
		    subject.append(StringUtils.SPACE);
		    subject.append(order.getSpread());

		    body.append(order);
		    first = false;
		}

		subject.append(StringUtils.SPACE);
		subject.append(LocalTime.now().format(TIME_FORMATTER));
	    } else {
		subject.append(ST_WEEKLY);
		subject.append(StringUtils.SPACE);
		subject.append(LocalTime.now().format(TIME_FORMATTER));
	    }

	    EmailManager.email(emailPortfolio, subject.toString(), body.toString());
	} catch (UnsupportedEncodingException e) {
	    EmailManager.emailException(alert, e);
	}

	// return finished
	return ResponseEntity.ok().build();
    }

    private List<OrderModel> createOrder(String body) {
	List<OrderModel> orderList = new ArrayList<OrderModel>();

	if (StringUtils.contains(body, OptionType.CALL.toString())
		|| StringUtils.contains(body, OptionType.PUT.toString())) {
	    try {
		for (String split : StringUtils.split(body, '&')) {
		    if (StringUtils.contains(split, BOT)) {
			split = StringUtils.substringAfter(split, BOT);
		    } else if (StringUtils.contains(split, BUY)) {
			split = StringUtils.substringAfter(split, BUY);
		    } else if (StringUtils.contains(split, SELL)) {
			split = StringUtils.substringAfter(split, SELL);
		    } else if (StringUtils.contains(split, SOLD)) {
			split = StringUtils.substringAfter(split, SOLD);
		    }

		    String[] split2 = StringUtils.split(split, StringUtils.SPACE);
		    if (StringUtils.contains(split, BACKRATIO)) {
			orderList.add(backratio(split2));
		    } else if (StringUtils.contains(split, IRON_CONDOR)) {
			orderList.add(ironCondor(split2));
		    } else if (StringUtils.contains(split, UNBALANCED_BUTTERFLY)) {
			orderList.add(unbalancedButterfly(split2));
		    } else if (StringUtils.contains(split, VERTICAL)) {
			orderList.add(vertical(split2));
		    } else if (StringUtils.contains(split, BUTTERFLY)) {
			orderList.add(butterfly(split2));
		    } else if (StringUtils.contains(split, CONDOR)) {
			orderList.add(condor(split2));
		    } else {
			orderList.add(single(split2));
		    }
		}
	    } catch (Exception e) {
		EmailManager.emailException("Error parsing text", e);
	    }
	} else {
	    EmailManager.emailHome("Error parsing text into order", body);
	}

	return orderList;
    }

    private String getPortfolio(List<OrderModel> orderList) {
	LocalDate now = LocalDate.now();
	Map<String, Position> portfolioMap = new HashMap<String, Position>();
	for (Position position : repository.findAll()) {
	    if (now.isAfter(position.getExpiry())) {
		repository.delete(position);
	    }

	    portfolioMap.put(position.getFullSymbol(), position);
	}

	for (OrderModel order : orderList) {
	    for (Position orderEntry : order.getPositionList()) {
		Position position = portfolioMap.get(orderEntry.getFullSymbol());
		if (position != null) {
		    position.setQuantity(position.getQuantity() + orderEntry.getQuantity());

		    if (position.getQuantity() == 0) {
			if (orderEntry.getQuantity() > 0) {
			    orderEntry.setSide(BUY_TO_CLOSE);
			}

			portfolioMap.remove(position.getFullSymbol());
			repository.delete(position);
		    } else {
			portfolioMap.put(position.getFullSymbol(), position);
			repository.save(position);
		    }
		} else {
		    portfolioMap.put(orderEntry.getFullSymbol(), orderEntry);
		    repository.save(orderEntry);
		}
	    }
	}

	return null;
    }

    private OrderModel backratio(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	List<BigDecimal> ratioList = parseSlashes(split[1]);
	String symbol = parseSymbol(split[3]);

	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[4], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[5], WEEKLYS)) {
	    i++;
	}

	LocalDate expiry = parseExpiry(split[4 + i], split[5 + i], split[6 + i]);
	List<BigDecimal> strikeList = parseSlashes(split[7 + i]);
	OptionType type = parseType(split[8 + i]);
	BigDecimal price = parsePrice(split[9 + i]);

	OrderModel order = new OrderModel(BACKRATIO, price, split[9 + i]);
	int qty1 = -quantity * ratioList.get(0).intValue();
	order.addPosition(new Position(qty1, symbol, expiry, strikeList.get(0), type));
	int qty2 = quantity * ratioList.get(1).intValue();
	order.addPosition(new Position(qty2, symbol, expiry, strikeList.get(1), type));

	return order;
    }

    private OrderModel butterfly(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[2]);

	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[3], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[4], WEEKLYS)) {
	    i++;
	}

	LocalDate expiry = parseExpiry(split[3 + i], split[4 + i], split[5 + i]);
	List<BigDecimal> strikeList = parseSlashes(split[6 + i]);
	OptionType type = parseType(split[7 + i]);
	BigDecimal price = parsePrice(split[8 + i]);

	OrderModel order = new OrderModel(BUTTERFLY, price, split[8 + i]);
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(0), type));
	int qty2 = -quantity * 2;
	order.addPosition(new Position(qty2, symbol, expiry, strikeList.get(1), type));
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(2), type));

	return order;
    }

    private OrderModel condor(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[2]);

	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[3], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[4], WEEKLYS)) {
	    i++;
	}

	LocalDate expiry = parseExpiry(split[3 + i], split[4 + i], split[5 + i]);
	List<BigDecimal> strikeList = parseSlashes(split[6 + i]);
	OptionType type = parseType(split[7 + i]);
	BigDecimal price = parsePrice(split[8 + i]);

	OrderModel order = new OrderModel(CONDOR, price, split[8 + i]);
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(0), type));
	order.addPosition(new Position(-quantity, symbol, expiry, strikeList.get(1), type));
	order.addPosition(new Position(-quantity, symbol, expiry, strikeList.get(2), type));
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(3), type));

	return order;
    }

    private OrderModel ironCondor(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[3]);

	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[4], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[5], WEEKLYS)) {
	    i++;
	}

	LocalDate expiry = parseExpiry(split[4 + i], split[5 + i], split[6 + i]);
	List<BigDecimal> strikeList = parseSlashes(split[7 + i]);
	List<OptionType> type = parseTypes(split[8 + i]);
	BigDecimal price = parsePrice(split[9 + i]);

	OrderModel order = new OrderModel(IRON_CONDOR, price, split[9 + i]);
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(0), type.get(0)));
	order.addPosition(new Position(-quantity, symbol, expiry, strikeList.get(1), type.get(0)));
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(2), type.get(1)));
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(3), type.get(1)));

	return order;
    }

    private OrderModel single(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[1]);

	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[2], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[3], WEEKLYS)) {
	    i++;
	}

	LocalDate expiry = parseExpiry(split[2 + i], split[3 + i], split[4 + i]);
	BigDecimal strike = parseStrike(split[5 + i]);
	OptionType type = parseType(split[6 + i]);
	BigDecimal price = parsePrice(split[7 + i]);

	OrderModel order = new OrderModel(type.toString(), price, split[7 + i]);
	order.addPosition(new Position(quantity, symbol, expiry, strike, type));

	return order;
    }

    private OrderModel unbalancedButterfly(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	List<BigDecimal> ratioList = parseSlashes(split[1]);
	String symbol = parseSymbol(split[3]);

	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[4], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[5], WEEKLYS)) {
	    i++;
	}

	LocalDate expiry = parseExpiry(split[4 + i], split[5 + i], split[6 + i]);
	List<BigDecimal> strikeList = parseSlashes(split[7 + i]);
	OptionType type = parseType(split[8 + i]);
	BigDecimal price = parsePrice(split[9 + i]);

	OrderModel order = new OrderModel(UNBALANCED_BUTTERFLY, price, split[9 + i]);
	int qty1 = quantity * ratioList.get(0).intValue();
	order.addPosition(new Position(qty1, symbol, expiry, strikeList.get(0), type));
	int qty2 = -quantity * ratioList.get(1).intValue();
	order.addPosition(new Position(qty2, symbol, expiry, strikeList.get(1), type));
	int qty3 = quantity * ratioList.get(2).intValue();
	order.addPosition(new Position(qty3, symbol, expiry, strikeList.get(2), type));

	return order;
    }

    private OrderModel vertical(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[2]);

	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[3], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[4], WEEKLYS)) {
	    i++;
	}

	LocalDate expiry = parseExpiry(split[3 + i], split[4 + i], split[5 + i]);
	List<BigDecimal> strikeList = parseSlashes(split[6 + i]);
	OptionType type = parseType(split[7 + i]);
	BigDecimal price = parsePrice(split[8 + i]);

	OrderModel order = new OrderModel(VERTICAL, price, split[8 + i]);
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(0), type));
	order.addPosition(new Position(-quantity, symbol, expiry, strikeList.get(1), type));

	return order;
    }

    private LocalDate parseExpiry(String day, String month, String year) throws Exception {
	LocalDate exp = null;

	try {
	    String date = day + StringUtils.SPACE + month + StringUtils.SPACE + year;
	    exp = LocalDate.parse(date, FORMATTER);
	} catch (DateTimeParseException e) {
	    throw new Exception("Error parsing Date: " + day + StringUtils.SPACE + month + StringUtils.SPACE + year, e);
	}

	return exp;
    }

    private BigDecimal parsePrice(String val) throws Exception {
	BigDecimal price = null;

	if (!StringUtils.contains(val, '@') || !StringUtils.contains(val, '.')) {
	    throw new Exception("Missing @ or . from price: " + val);
	}

	try {
	    price = new BigDecimal(StringUtils.substring(val, 1, StringUtils.indexOf(val, '.') + 3));
	} catch (NumberFormatException e) {
	    throw new Exception("Error parsing price: " + val, e);
	}

	return price;
    }

    private int parseQuantity(String val) throws Exception {
	int qty = 0;

	try {
	    qty = Integer.parseInt(val);
	} catch (NumberFormatException e) {
	    throw new Exception("Error parsing quantity: " + val, e);
	}

	return qty;
    }

    private List<BigDecimal> parseSlashes(String slashes) throws Exception {
	List<BigDecimal> slashList = new ArrayList<BigDecimal>();

	for (String slash : StringUtils.split(slashes, '/')) {
	    try {
		slashList.add(new BigDecimal(slash).setScale(2));
	    } catch (NumberFormatException e) {
		throw new Exception("Error parsing slashes: " + slashes, e);
	    }
	}

	return slashList;
    }

    private BigDecimal parseStrike(String strike) throws Exception {
	return parseSlashes(strike).get(0);
    }

    private String parseSymbol(String symbol) throws Exception {
	if (StringUtils.length(symbol) > 4 || StringUtils.length(symbol) == 0) {
	    throw new Exception("Error parsing symbol: " + symbol);
	}

	return symbol;
    }

    private OptionType parseType(String type) throws Exception {
	return parseTypes(type).get(0);
    }

    private List<OptionType> parseTypes(String slashes) throws Exception {
	List<OptionType> slashList = new ArrayList<OptionType>();
	for (String split : StringUtils.split(slashes, '/')) {
	    try {
		slashList.add(OptionType.valueOf(split));
	    } catch (IllegalArgumentException e) {
		throw new Exception("Error parsing type: " + split);
	    }
	}

	return slashList;
    }
}