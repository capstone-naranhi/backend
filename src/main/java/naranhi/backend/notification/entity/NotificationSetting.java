package naranhi.backend.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import naranhi.backend.global.entity.BaseEntity;
import naranhi.backend.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "notification_setting")
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long notificationSettingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "is_received_safety_notification", nullable = false)
    private boolean isReceivedSafetyNotification;

    @Column(name = "is_received_device_notification", nullable = false)
    private boolean isReceivedDeviceNotification;

    @Column(name = "is_received_report_notification", nullable = false)
    private boolean isReceivedReportNotification;

    @Column(name = "is_received_general_notification", nullable = false)
    private boolean isReceivedGeneralNotification;

    @Column(name = "is_interference_active", nullable = false)
    private boolean isInterferenceActive;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "interference_end_time")
    private LocalTime interferenceEndTime;
}
