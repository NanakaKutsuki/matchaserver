package org.kutsuki.matchaserver.portfolio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.matchaserver.EmailManager;
import org.kutsuki.matchaserver.document.Position;
import org.kutsuki.matchaserver.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortfolioManager {
    private static final String BUY_TO_CLOSE = "BUY_TO_CLOSE";
    private static final String BUY_TO_OPEN = "BUY_TO_OPEN";
    private static final String SELL_TO_CLOSE = "SELL_TO_CLOSE";
    private static final String SELL_TO_OPEN = "SELL_TO_OPEN";

    private List<Position> deleteList;
    private List<Position> saveList;
    private Map<String, Position> portfolioMap;

    @Autowired
    private PortfolioRepository repository;

    @PostConstruct
    public void postConstruct() {
	this.deleteList = new ArrayList<Position>();
	this.saveList = new ArrayList<Position>();
	this.portfolioMap = new HashMap<String, Position>();

	for (Position position : repository.findAll()) {
	    this.portfolioMap.put(position.getFullSymbol(), position);
	}
    }

    public void clear() {
	deleteList.clear();
	saveList.clear();
    }

    public String getPortfolio(List<OrderModel> orderList, boolean working) {
	LocalDate now = LocalDate.now();
	for (Position position : portfolioMap.values()) {
	    if (now.isAfter(position.getExpiry())) {
		deleteList.add(position);
	    }
	}

	for (Position position : deleteList) {
	    portfolioMap.remove(position.getFullSymbol());
	}

	for (OrderModel order : orderList) {
	    for (Position orderEntry : order.getPositionList()) {
		Position position = portfolioMap.get(orderEntry.getFullSymbol());
		if (position != null) {
		    int qty = position.getQuantity() + orderEntry.getQuantity();

		    if (qty == 0) {
			if (orderEntry.getQuantity() > 0) {
			    orderEntry.setSide(BUY_TO_CLOSE);
			} else {
			    orderEntry.setSide(SELL_TO_CLOSE);
			}
		    } else {
			if (orderEntry.getQuantity() > 0) {
			    orderEntry.setSide(BUY_TO_OPEN);
			} else {
			    orderEntry.setSide(SELL_TO_OPEN);
			}
		    }

		    if (!working) {
			position.setQuantity(qty);
			portfolioMap.put(position.getFullSymbol(), position);
			saveList.add(position);
		    }
		} else {
		    if (orderEntry.getQuantity() > 0) {
			orderEntry.setSide(BUY_TO_OPEN);
		    } else {
			orderEntry.setSide(SELL_TO_OPEN);
		    }

		    if (!working) {
			portfolioMap.put(orderEntry.getFullSymbol(), orderEntry);
			saveList.add(orderEntry);
		    }
		}
	    }
	}

	StringBuilder sb = new StringBuilder();
	sb.append(EmailManager.NEW_LINE);
	sb.append(EmailManager.NEW_LINE);
	sb.append("<b>[PORTFOLIO]</b>");

	List<Position> portfolio = new ArrayList<Position>(portfolioMap.values());
	Collections.sort(portfolio);
	String symbol = StringUtils.EMPTY;
	for (Position position : portfolio) {
	    if (!symbol.equals(position.getSymbol())) {
		symbol = position.getSymbol();
		sb.append(EmailManager.NEW_LINE);
	    }

	    sb.append(position.getStatement());
	    sb.append(EmailManager.NEW_LINE);
	}

	return sb.toString();
    }

    public void reloadCache() {
	portfolioMap.clear();

	for (Position position : repository.findAll()) {
	    portfolioMap.put(position.getFullSymbol(), position);
	}
    }

    public void resolveRepo() {
	repository.deleteAll(deleteList);
	repository.saveAll(saveList);
    }
}
