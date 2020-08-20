package org.kutsuki.matchaserver.repository;

import java.util.List;

import org.kutsuki.matchaserver.model.scraper.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelRepository extends MongoRepository<Hotel, String> {
    public List<Hotel> findByActive(boolean active);

    public List<Hotel> findByCityIdAndActive(String cityId, boolean active);

    public Hotel findByTrivagoId(String trivagoId);
}
