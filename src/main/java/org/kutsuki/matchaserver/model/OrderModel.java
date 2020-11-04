package org.kutsuki.matchaserver.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;

public class OrderModel {
    private BigDecimal price;
    private List<PositionModel> positionList;
    private String spread;

    public OrderModel(String spread, BigDecimal price) {
	this.positionList = new ArrayList<PositionModel>();
	this.price = price;
	this.spread = spread;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(getSymbol());
	sb.append(StringUtils.SPACE);
	sb.append(getSpread());
	sb.append(StringUtils.SPACE);
	sb.append(price);
	sb.append(EmailManager.NEW_LINE);

	for (PositionModel position : positionList) {
	    sb.append(position);
	    sb.append(EmailManager.NEW_LINE);
	}

	return sb.toString();
    }

    public void addPosition(PositionModel model) {
	positionList.add(model);
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
