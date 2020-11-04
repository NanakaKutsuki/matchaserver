package org.kutsuki.matchaserver.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

public class PositionModel {
    private static final String BUY = "BUY";
    private static final String SELL = "SELL";

    private int quantity;
    private String symbol;
    private LocalDate expiry;
    private BigDecimal strike;
    private OptionType type;

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	if (getQuantity() > 0) {
	    sb.append(BUY);
	    sb.append(StringUtils.SPACE);
	    sb.append('+');
	} else {
	    sb.append(SELL);
	    sb.append(StringUtils.SPACE);
	}

	sb.append(getQuantity());
	sb.append(StringUtils.SPACE);
	sb.append(getSymbol());
	sb.append(StringUtils.SPACE);
	sb.append(getExpiry());
	sb.append(StringUtils.SPACE);
	sb.append(getStrike());
	sb.append(StringUtils.SPACE);
	sb.append(getType());

	return sb.toString();
    }

    public int getQuantity() {
	return quantity;
    }

    public String getSymbol() {
	return symbol;
    }

    public LocalDate getExpiry() {
	return expiry;
    }

    public BigDecimal getStrike() {
	return strike;
    }

    public OptionType getType() {
	return type;
    }

    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }

    public void setSymbol(String symbol) {
	this.symbol = symbol;
    }

    public void setExpiry(LocalDate expiry) {
	this.expiry = expiry;
    }

    public void setStrike(BigDecimal strike) {
	this.strike = strike;
    }

    public void setType(OptionType type) {
	this.type = type;
    }
}
