package naranhi.backend.log.repository;

import naranhi.backend.log.document.SafetyEventLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SafetyEventLogRepository extends MongoRepository<SafetyEventLog, String> {
}
