package org.kutsuki.model.beating;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BeatingWinPct implements Comparable<BeatingWinPct> {
    private String player;
    private int played;
    private BigDecimal winPct;

    public BeatingWinPct(String player, Integer played, BigDecimal winPct) {
	this.player = player;
	this.played = played != null ? played : 0;
	this.winPct = winPct.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int compareTo(BeatingWinPct rhs) {
	int result = getWinPct().compareTo(rhs.getWinPct());

	if (result == 0) {
	    result = Integer.compare(getPlayed(), rhs.getPlayed());

	    if (result == 0) {
		result = getPlayer().compareTo(rhs.getPlayer());
	    }
	}

	return result;
    }

    public String getPlayer() {
	return player;
    }

    public int getPlayed() {
	return played;
    }

    public BigDecimal getWinPct() {
	return winPct;
    }
}
