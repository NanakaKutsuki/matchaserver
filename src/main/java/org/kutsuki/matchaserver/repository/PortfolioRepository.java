package org.kutsuki.matchaserver.repository;

import org.kutsuki.matchaserver.document.Position;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PortfolioRepository extends MongoRepository<Position, String> {
}
