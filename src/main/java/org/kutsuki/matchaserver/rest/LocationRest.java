package org.kutsuki.matchaserver.rest;

import java.util.Collections;
import java.util.List;

import org.kutsuki.matchaserver.dao.DaoManager;
import org.kutsuki.matchaserver.model.LocationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationRest {
    @GetMapping("/rest/location/getAll")
    public List<LocationModel> getAll() {
	List<LocationModel> locationList = DaoManager.LOCATION.getAll();
	Collections.sort(locationList);
	return locationList;
    }
}