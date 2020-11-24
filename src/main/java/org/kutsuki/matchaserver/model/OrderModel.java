package org.kutsuki.matchaserver.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.document.Position;

public class OrderModel {
    private BigDecimal priceBD;
    private List<Position> positionList;
    private String price;
    private String spread;

    public OrderModel(String spread, BigDecimal priceBD, String price) {
	this.positionList = new ArrayList<Position>();
	this.price = price;
	this.priceBD = priceBD;
	this.spread = spread;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	return sb.toString();
    }

    public void addPosition(Position model) {
	positionList.add(model);
    }

    public List<Position> getPositionList() {
	return positionList;
    }

    public BigDecimal getPriceBD() {
	return priceBD;
    }

    public String getPrice() {
	return price;
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
