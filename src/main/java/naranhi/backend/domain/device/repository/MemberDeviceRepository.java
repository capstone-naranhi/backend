package naranhi.backend.domain.device.repository;

import naranhi.backend.domain.device.entity.MemberDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberDeviceRepository extends JpaRepository<MemberDevice, Long> {

    boolean existsByMemberIdAndDeviceId(Long memberId, Long deviceId);

    @Query("SELECT md FROM MemberDevice md JOIN FETCH md.device WHERE md.member.id = :memberId")
    List<MemberDevice> findAllWithDeviceByMemberId(@Param("memberId") Long memberId);
}
