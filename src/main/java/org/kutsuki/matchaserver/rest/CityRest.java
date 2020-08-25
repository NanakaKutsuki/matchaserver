package org.kutsuki.matchaserver.rest;

import java.util.Collections;
import java.util.List;

import org.kutsuki.matchaserver.document.City;
import org.kutsuki.matchaserver.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CityRest {
    @Autowired
    private CityRepository repository;

    @GetMapping("/rest/city/getAll")
    public List<City> getAll() {
	List<City> cityList = repository.findAll();
	Collections.sort(cityList);
	return cityList;
    }
}