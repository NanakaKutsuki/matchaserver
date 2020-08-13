package org.kutsuki.matchaserver.beating;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BeatingBiggest {
    private String player;
    private String place;
    private BigDecimal value;

    public BeatingBiggest(String player, String place, BigDecimal value) {
	this.player = player;
	this.place = place;
	this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public String getPlayer() {
	return player;
    }

    public String getPlace() {
	return place;
    }

    public BigDecimal getValue() {
	return value;
    }
}
