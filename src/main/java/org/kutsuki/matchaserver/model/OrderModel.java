package org.kutsuki.matchaserver.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.document.Position;

public class OrderModel {
    private BigDecimal price;
    private List<Position> positionList;
    private String priceString;
    private String spread;

    public OrderModel(String spread, BigDecimal price, String priceString) {
	this.positionList = new ArrayList<Position>();
	this.price = price;
	this.priceString = priceString;
	this.spread = spread;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(getSymbol());
	sb.append(StringUtils.SPACE);
	sb.append(getSpread());
	sb.append(StringUtils.SPACE);
	sb.append(priceString);
	sb.append(EmailManager.NEW_LINE);

	for (Position position : positionList) {
	    sb.append(position);

	    if (StringUtils.equals(spread, OptionType.CALL.toString())
		    || StringUtils.equals(spread, OptionType.PUT.toString())) {
		sb.append(StringUtils.SPACE);
		sb.append(price);
	    }

	    sb.append(EmailManager.NEW_LINE);
	}

	return sb.toString();
    }

    public void addPosition(Position model) {
	positionList.add(model);
    }

    public List<Position> getPositionList() {
	return positionList;
    }

    public String getSpread() {
	return spread;
    }

    public String getSymbol() {
	String symbol = StringUtils.EMPTY;

	if (positionList.size() > 0) {
	    symbol = positionList.get(0).getSymbol();
	}

	return symbol;
    }
}
