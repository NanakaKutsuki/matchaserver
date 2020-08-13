package org.kutsuki.dao;

import org.kutsuki.model.beating.EventModel;

public class EventDao extends AbstractDao<EventModel> {
    private static final String TYPE = "event";
    private static final String INDEX = "lunch";

    @Override
    public Class<EventModel> getClazz() {
	return EventModel.class;
    }

    @Override
    public String getIndex() {
	return INDEX;
    }

    @Override
    public String getType() {
	return TYPE;
    }
}
