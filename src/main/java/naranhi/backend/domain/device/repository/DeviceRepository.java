package naranhi.backend.domain.device.repository;

import naranhi.backend.domain.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}
