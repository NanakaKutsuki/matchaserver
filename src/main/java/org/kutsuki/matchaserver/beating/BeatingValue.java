package org.kutsuki.matchaserver.beating;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BeatingValue implements Comparable<BeatingValue> {
    private String player;
    private BigDecimal value;

    public BeatingValue(String player, BigDecimal value, int newScale) {
	this.player = player;
	this.value = value.setScale(newScale, RoundingMode.HALF_UP);
    }

    @Override
    public int compareTo(BeatingValue rhs) {
	int result = getValue().compareTo(rhs.getValue());

	if (result == 0) {
	    result = getPlayer().compareTo(rhs.getPlayer());
	}

	return result;
    }

    public String getPlayer() {
	return player;
    }

    public BigDecimal getValue() {
	return value;
    }
}
