package naranhi.backend.domain.device.repository.mongo;

import java.util.List;
import naranhi.backend.domain.device.document.DeviceStatusChangeLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceStatusChangeLogRepository extends MongoRepository<DeviceStatusChangeLog, String> {

    List<DeviceStatusChangeLog> findTop3ByDeviceId(Long deviceId, Sort sort);
}
