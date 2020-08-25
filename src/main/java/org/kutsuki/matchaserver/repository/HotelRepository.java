package org.kutsuki.matchaserver.repository;

import java.util.List;

import org.kutsuki.matchaserver.document.Hotel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HotelRepository extends MongoRepository<Hotel, String> {
    public List<Hotel> findAllByActiveTrue();

    public List<Hotel> findAllByCityIdAndActiveTrue(String cityId);

    public Hotel findByTrivagoId(String trivagoId);
}
