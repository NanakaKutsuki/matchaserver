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
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.model.OptionType;
import org.kutsuki.matchaserver.model.OrderModel;
import org.kutsuki.matchaserver.model.PositionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShadowRest {
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
	    .appendPattern("d MMM yy").toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mma");

    private static final String BUTTERFLY = "BUTTERFLY";
    private static final String BOT = "BOT ";
    private static final String BUY = "BUY ";
    private static final String CONDOR = "CONDOR";
    private static final String HUNDRED = "100";
    private static final String IRON_CONDOR = "IRON CONDOR";
    private static final String NEW = "NEW";
    private static final String RATIO = "RATIO";
    private static final String SELL = "SELL ";
    private static final String SHADOW_TRADER = "Shadow Trader";
    private static final String SOLD = "SOLD ";
    private static final String WEEKLYS = "(Weeklys)";
    private static final String VERTICAL = "VERTICAL";
    private static final String UNBALANCED_BUTTERFLY = "~BUTTERFLY";

    @Value("${email.shadow}")
    private String emailShadow;

    @GetMapping("/rest/shadow/uploadText")
    public ResponseEntity<String> uploadText(@RequestParam("text") String text) {
	try {
	    StringBuilder subject = new StringBuilder();
	    StringBuilder body = new StringBuilder();

	    String escapedText = URLDecoder.decode(text, StandardCharsets.UTF_8.name());
	    body.append(escapedText);

	    if (StringUtils.startsWith(body, Character.toString('#'))) {
		subject.append(StringUtils.substringBefore(escapedText, StringUtils.SPACE));

		if (StringUtils.containsIgnoreCase(escapedText, NEW)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(NEW);
		}

		boolean first = true;
		body.append(EmailManager.NEW_LINE);
		body.append(EmailManager.NEW_LINE);

		for (OrderModel order : createOrder(escapedText)) {
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
		subject.append(SHADOW_TRADER);
		subject.append(StringUtils.SPACE);
		subject.append(LocalTime.now().format(TIME_FORMATTER));
	    }

	    EmailManager.email(emailShadow, subject.toString(), body.toString());
	} catch (UnsupportedEncodingException e) {
	    EmailManager.emailException(text, e);
	}

	// return finished
	return ResponseEntity.ok().build();
    }

    private List<OrderModel> createOrder(String body) {
	List<OrderModel> orderList = new ArrayList<OrderModel>();

	if (StringUtils.contains(body, OptionType.CALL.name()) || StringUtils.contains(body, OptionType.PUT.name())) {
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
		    if (StringUtils.contains(split, IRON_CONDOR)) {
			orderList.add(ironCondor(split2));
		    } else if (StringUtils.contains(split, RATIO)) {
			orderList.add(ratio(split2));
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

    private OrderModel butterfly(String[] split) throws Exception {
	// String body = "#402 NEW BOT +2 BUTTERFLY FB 10 JUL 20 245/250/255 CALL @.53
	// ISE & SOLD -8 FB 10 JUL 20 210 PUT @.14 CBOE";
	// String body = "#425 SOLD -1 BUTTERFLY AMZN 100 21 AUG 20 3290/3300/3310 CALL
	// @.87 CBOE Official exit, am holding a couple into close to see what happens
	// but doesn't look good...";
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
	OrderModel order = new OrderModel(BUTTERFLY, price);

	PositionModel position = new PositionModel();
	position.setExpiry(expiry);
	position.setQuantity(quantity);
	position.setStrike(strikeList.get(0));
	position.setSymbol(symbol);
	position.setType(type);
	order.addPosition(position);

	PositionModel position2 = new PositionModel();
	position2.setExpiry(expiry);
	position2.setQuantity(-quantity * 2);
	position2.setStrike(strikeList.get(1));
	position2.setSymbol(symbol);
	position2.setType(type);
	order.addPosition(position2);

	PositionModel position3 = new PositionModel();
	position3.setExpiry(expiry);
	position3.setQuantity(quantity);
	position3.setStrike(strikeList.get(2));
	position3.setSymbol(symbol);
	position3.setType(type);
	order.addPosition(position3);

	return order;
    }

    private OrderModel condor(String[] split) throws Exception {
	// String body = "#441 BOT +2 CONDOR SPX 18 SEP 20 3470/3475/3490/3555 CALL
	// @-.95cr CBOE (2 of potential 4--above .80cr is good)";
	// String body = "#400 SOLD -1 CONDOR AAPL 10 JUL 20 365/370/372.5/377.5 CALL
	// @1.28 CBOE Closing out here. We only have one unit officially and AAPL is
	// breaking to new ATH.";
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
	OrderModel order = new OrderModel(CONDOR, price);

	PositionModel position = new PositionModel();
	position.setExpiry(expiry);
	position.setQuantity(quantity);
	position.setStrike(strikeList.get(0));
	position.setSymbol(symbol);
	position.setType(type);
	order.addPosition(position);

	PositionModel position2 = new PositionModel();
	position2.setExpiry(expiry);
	position2.setQuantity(-quantity);
	position2.setStrike(strikeList.get(1));
	position2.setSymbol(symbol);
	position2.setType(type);
	order.addPosition(position2);

	PositionModel position3 = new PositionModel();
	position3.setExpiry(expiry);
	position3.setQuantity(-quantity);
	position3.setStrike(strikeList.get(2));
	position3.setSymbol(symbol);
	position3.setType(type);
	order.addPosition(position3);

	PositionModel position4 = new PositionModel();
	position4.setExpiry(expiry);
	position4.setQuantity(quantity);
	position4.setStrike(strikeList.get(3));
	position4.setSymbol(symbol);
	position4.setType(type);
	order.addPosition(position4);

	return order;
    }

    private OrderModel ironCondor(String[] split) throws Exception {
	// String body = "#418 NEW SOLD -2 IRON CONDOR FB 7 AUG 20 250/252.5/250/247.5
	// CALL/PUT @2.09 CBOE iron butterfly for staying flat into end of week. good
	// r/r.";
	// String body = "#467 NEW SOLD -1 IRON CONDOR AAPL 100 (Weeklys) 30 OCT 20
	// 110/111/110/109 CALL/PUT @.80 MIAX Iron butterfly to illustrate concept with
	// tiny size and risk.";
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
	OrderModel order = new OrderModel(IRON_CONDOR, price);

	PositionModel position = new PositionModel();
	position.setExpiry(expiry);
	position.setQuantity(quantity);
	position.setStrike(strikeList.get(0));
	position.setSymbol(symbol);
	position.setType(type.get(0));
	order.addPosition(position);

	PositionModel position2 = new PositionModel();
	position2.setExpiry(expiry);
	position2.setQuantity(-quantity);
	position2.setStrike(strikeList.get(1));
	position2.setSymbol(symbol);
	position2.setType(type.get(0));
	order.addPosition(position2);

	PositionModel position3 = new PositionModel();
	position3.setExpiry(expiry);
	position3.setQuantity(quantity);
	position3.setStrike(strikeList.get(2));
	position3.setSymbol(symbol);
	position3.setType(type.get(1));
	order.addPosition(position3);

	PositionModel position4 = new PositionModel();
	position4.setExpiry(expiry);
	position4.setQuantity(-quantity);
	position4.setStrike(strikeList.get(3));
	position4.setSymbol(symbol);
	position4.setType(type.get(1));
	order.addPosition(position4);

	return order;
    }

    private OrderModel ratio(String[] split) throws Exception {
	// String body = "#468 SOLD -2 1/2 BACKRATIO SPX 100 (Weeklys) 2 NOV 20
	// 3230/3210 PUT @.10cr Adding two more here (now it's directional, not just for
	// credit). If margin is an issue, bwb or pass.";
	// String body = "#460 NEW SOLD -6 1/3 BACKRATIO NFLX 23 OCT 20 472.5/460 PUT
	// @.00 CBOE";
	// "#456 BOT +2 1/3 BACKRATIO NFLX 100 16 OCT 20 585/600 CALL @.05db ISE Closing
	// half at close to zero to decrease margin."
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
	OrderModel order = new OrderModel(RATIO, price);

	PositionModel position = new PositionModel();
	position.setExpiry(expiry);
	position.setQuantity(-quantity * ratioList.get(0).intValue());
	position.setStrike(strikeList.get(0));
	position.setSymbol(symbol);
	position.setType(type);
	order.addPosition(position);

	PositionModel position2 = new PositionModel();
	position2.setExpiry(expiry);
	position2.setQuantity(quantity * ratioList.get(1).intValue());
	position2.setStrike(strikeList.get(1));
	position2.setSymbol(symbol);
	position2.setType(type);
	order.addPosition(position2);

	return order;
    }

    private OrderModel single(String[] split) throws Exception {
	// String body = "#414 SOLD -2 GLD 100 (Weeklys) 30 OCT 20 187 CALL @.36";
	// String body = "#414 BOT +4 GLD 30 OCT 20 173 PUT @.10 PHLX I'll roll the dice
	// on the Nov6 for now but this makes sense to close with one day to go and just
	// $2.50 above";
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
	OrderModel order = new OrderModel(type.name(), price);

	PositionModel position = new PositionModel();
	position.setExpiry(expiry);
	position.setQuantity(quantity);
	position.setStrike(strike);
	position.setSymbol(symbol);
	position.setType(type);
	order.addPosition(position);

	return order;
    }

    private OrderModel unbalancedButterfly(String[] split) throws Exception {
	// String body = "#449 WORKING SELL -2 1/3/2 ~BUTTERFLY SPX 2 OCT 20
	// 3420/3440/3480 CALL @.25 cr LMT";
	// String body = "#424 BOT +4 1/3/2 ~BUTTERFLY SPX 100 (Weeklys) 21 AUG 20
	// 3405/3420/3450 CALL @.05db CBOE (PM options, not the AM ones)";
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
	OrderModel order = new OrderModel(UNBALANCED_BUTTERFLY, price);

	PositionModel position = new PositionModel();
	position.setExpiry(expiry);
	position.setQuantity(quantity * ratioList.get(0).intValue());
	position.setStrike(strikeList.get(0));
	position.setSymbol(symbol);
	position.setType(type);
	order.addPosition(position);

	PositionModel position2 = new PositionModel();
	position2.setExpiry(expiry);
	position2.setQuantity(-quantity * ratioList.get(1).intValue());
	position2.setStrike(strikeList.get(1));
	position2.setSymbol(symbol);
	position2.setType(type);
	order.addPosition(position2);

	PositionModel position3 = new PositionModel();
	position3.setExpiry(expiry);
	position3.setQuantity(quantity * ratioList.get(2).intValue());
	position3.setStrike(strikeList.get(2));
	position3.setSymbol(symbol);
	position3.setType(type);
	order.addPosition(position3);

	return order;
    }

    private OrderModel vertical(String[] split) throws Exception {
	// String body = "#64 SOLD -1 VERTICAL CMG 100 (Weeklys) 3 NOV 17 275/265 PUT
	// @5.22 ISE (itm put vertical for credit) -Peter";
	// String body = "#458 BOT +1 VERTICAL SPX 21 OCT 20 3375/3365 PUT @1.45 COBE
	// tightens it up to only $5 of downside risk. Looks more bearish now.";
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
	OrderModel order = new OrderModel(VERTICAL, price);

	PositionModel position = new PositionModel();
	position.setExpiry(expiry);
	position.setQuantity(quantity);
	position.setStrike(strikeList.get(0));
	position.setSymbol(symbol);
	position.setType(type);
	order.addPosition(position);

	PositionModel position2 = new PositionModel();
	position2.setExpiry(expiry);
	position2.setQuantity(-quantity);
	position2.setStrike(strikeList.get(1));
	position2.setSymbol(symbol);
	position2.setType(type);
	order.addPosition(position2);

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