package org.kutsuki.matchaserver.repository;

import org.kutsuki.matchaserver.document.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertRepository extends MongoRepository<Alert, String> {
}
