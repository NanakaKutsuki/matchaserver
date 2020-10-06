package org.kutsuki.matchaserver.repository;

import java.util.Date;
import java.util.List;

import org.kutsuki.matchaserver.document.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoomRepository extends MongoRepository<Room, String> {
    public List<Room> findAllByCityIdAndDateBetween(String cityId, Date start, Date end);

    public List<Room> findAllByCityIdAndSoldOutAndDateBetween(String cityId, boolean soldOut, Date start, Date end);

    public List<Room> findAllByHotelIdAndDateBetween(String hotelId, Date start, Date end);
}
