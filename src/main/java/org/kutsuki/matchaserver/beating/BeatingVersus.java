package org.kutsuki.matchaserver.beating;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BeatingVersus implements Comparable<BeatingVersus> {
    private String player;
    private List<BeatingValue> enemyList;

    public BeatingVersus(String player) {
	this.player = player;
	this.enemyList = new ArrayList<BeatingValue>();
    }

    @Override
    public int compareTo(BeatingVersus rhs) {
	return getPlayer().compareTo(rhs.getPlayer());
    }

    public void addEnemy(String enemy, BigDecimal value) {
	this.enemyList.add(new BeatingValue(enemy, value, 0));
    }

    public String getPlayer() {
	return player;
    }

    public List<BeatingValue> getEnemyList() {
	return enemyList;
    }
}
