package naranhi.backend.domain.device.repository.mongo;

import java.util.List;
import naranhi.backend.log.document.DeviceStatusLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceStatusChangeLogRepository extends MongoRepository<DeviceStatusLog, String> {

    List<DeviceStatusLog> findTop3ByDeviceId(Long deviceId, Sort sort);
}
