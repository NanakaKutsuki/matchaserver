package org.kutsuki.matchaserver.repository;

import org.kutsuki.matchaserver.model.scraper.City;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CityRepository extends MongoRepository<City, String> {

}
