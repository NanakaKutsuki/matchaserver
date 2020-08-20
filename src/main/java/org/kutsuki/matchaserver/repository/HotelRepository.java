package org.kutsuki.matchaserver.repository;

import java.util.List;

import org.kutsuki.matchaserver.document.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelRepository extends MongoRepository<Hotel, String> {
    public List<Hotel> findAllByActive(boolean active);

    public List<Hotel> findAllByCityIdAndActive(String cityId, boolean active);

    public Hotel findByTrivagoId(String trivagoId);
}
