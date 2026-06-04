package naranhi.backend.domain.device.repository;

import java.util.List;
import java.util.Optional;
import naranhi.backend.domain.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceSerialNumber(String deviceSerialNumber);

    @Query("""
                SELECT md.member.id
                FROM MemberDevice md
                WHERE md.device.deviceSerialNumber = :serial
            """)
    List<Long> findMemberIdsByDeviceSerialNumber(@Param("serial") String serial);
}
