package org.kutsuki.dao;

import org.kutsuki.model.LocationModel;

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
