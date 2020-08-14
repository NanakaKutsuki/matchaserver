package org.kutsuki.matchaserver.dao;

import org.kutsuki.matchaserver.model.scraper.LocationModel;

public class LocationDao extends AbstractDao<LocationModel> {
    private static final String TYPE = "location";

    @Override
    public Class<LocationModel> getClazz() {
	return LocationModel.class;
    }

    @Override
    public String getType() {
	return TYPE;
    }
}
