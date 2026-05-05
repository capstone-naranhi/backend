package naranhi.backend.domain.device.repository;

import naranhi.backend.domain.device.entity.MemberDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDeviceRepository extends JpaRepository<MemberDevice, Long> {

    boolean existsByMemberIdAndDeviceId(Long memberId, Long deviceId);
}
