package org.kutsuki.matchaserver.repository;

import org.kutsuki.matchaserver.document.City;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CityRepository extends MongoRepository<City, String> {
}
