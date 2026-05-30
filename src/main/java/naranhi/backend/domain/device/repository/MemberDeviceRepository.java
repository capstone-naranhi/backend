package naranhi.backend.domain.device.repository;

import java.util.List;
import naranhi.backend.domain.device.entity.MemberDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberDeviceRepository extends JpaRepository<MemberDevice, Long> {

    boolean existsByMemberIdAndDeviceId(Long memberId, Long deviceId);

    @Query("SELECT md FROM MemberDevice md JOIN FETCH md.device WHERE md.member.id = :memberId")
    List<MemberDevice> findAllWithDeviceByMemberId(@Param("memberId") Long memberId);
}
