package org.kutsuki.matchaserver.document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.portfolio.OptionType;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Position extends AbstractDocument implements Comparable<Position> {
    private static final DateTimeFormatter REST_DTF = DateTimeFormatter.ofPattern("MMddyy");
    private static final DateTimeFormatter ORDER_DTF = DateTimeFormatter.ofPattern("d MMM yy");

    private int quantity;
    private String symbol;
    private LocalDate expiry;
    private BigDecimal strike;
    private OptionType type;
    private String fullSymbol;

    @Transient
    @JsonIgnore
    private transient String side;

    public Position(int quantity, String symbol, LocalDate expiry, BigDecimal strike, OptionType type) {
	this.quantity = quantity;
	this.symbol = symbol;
	this.expiry = expiry;
	this.strike = strike;
	this.type = type;

	StringBuilder sb = new StringBuilder();
	sb.append(getSymbol());
	sb.append('_');
	sb.append(REST_DTF.format(getExpiry()));
	sb.append(getType().toString().charAt(0));
	sb.append(getStrike());
	this.fullSymbol = sb.toString();
    }

    @Override
    public int compareTo(Position rhs) {
	int result = getSymbol().compareTo(rhs.getSymbol());

	if (result == 0) {
	    result = getExpiry().compareTo(rhs.getExpiry());

	    if (result == 0) {
		result = getStrike().compareTo(rhs.getStrike());

		if (result == 0) {
		    result = getType().compareTo(rhs.getType());
		}
	    }
	}

	return result;
    }

    @JsonIgnore
    public String getOrder() {
	StringBuilder sb = new StringBuilder();
	sb.append(StringUtils.substringBefore(getSide(), Character.toString('_')));
	sb.append(StringUtils.SPACE);

	if (getQuantity() > 0) {
	    sb.append('+');
	}

	sb.append(getQuantity());
	sb.append(StringUtils.SPACE);
	sb.append(getSymbol());
	sb.append(StringUtils.SPACE);
	sb.append('[');
	sb.append(ORDER_DTF.format(getExpiry()));
	sb.append(']');
	sb.append(StringUtils.SPACE);
	sb.append(getStrike());
	sb.append(StringUtils.SPACE);
	sb.append(getType());

	return sb.toString();
    }

    @JsonIgnore
    public String getStatement() {
	StringBuilder sb = new StringBuilder();
	sb.append(getSymbol());
	sb.append(StringUtils.SPACE);
	sb.append('[');
	sb.append(ORDER_DTF.format(getExpiry()));
	sb.append(']');
	sb.append(StringUtils.SPACE);
	sb.append(getStrike());
	sb.append(StringUtils.SPACE);
	sb.append(getType());
	sb.append(StringUtils.SPACE);

	if (getQuantity() > 0) {
	    sb.append('+');
	}
	sb.append(getQuantity());

	return sb.toString();
    }

    public int getQuantity() {
	return quantity;
    }

    public String getSide() {
	return side;
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

    @JsonIgnore
    public String getFullSymbol() {
	return fullSymbol;
    }

    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }

    public void setSide(String side) {
	this.side = side;
    }
}
