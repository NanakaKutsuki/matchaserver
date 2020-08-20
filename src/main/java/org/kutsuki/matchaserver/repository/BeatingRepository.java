package org.kutsuki.matchaserver.repository;

import org.kutsuki.matchaserver.model.beating.Beating;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BeatingRepository extends MongoRepository<Beating, String> {

}
