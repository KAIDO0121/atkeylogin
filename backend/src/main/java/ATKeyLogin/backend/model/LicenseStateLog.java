package ATKeyLogin.backend.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name = "licenseStateLogs")
public class LicenseStateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    // @Column(name = "license_code", nullable = false)
    // String license_code;
    
    // the ts that this log has been created
    @Column(name = "created_at", updatable=false)
    private Long createdAt;

    @Column(name = "activated_at")
    private Long activatedAt;

    @Column(name = "max_duration")
    private Long maxDuration;

    // @Column(name = "companioned_times", nullable = false)
    // private int companionedTimes;

    @Column(name = "max_companion_counts", nullable = false)
    private int maxCompanionCounts;

    @ManyToOne()
    @JoinColumn(name = "OEM_id", referencedColumnName = "OEM_id")
    private OEM OEM;

    @Column(name = "state", nullable = false)
    private int state;

    @ManyToOne()
    @JoinColumn(name = "license_code", referencedColumnName = "license_code")
    private LicenseState licenseState;

    public LicenseStateLog(Long createdAt, Long activatedAt, Long maxDuration, int maxCompanionCounts, OEM oem, LicenseStateEnum state, LicenseState licenseState) {
        this.createdAt = createdAt;
        this.activatedAt = activatedAt;
        this.maxDuration = maxDuration;
        this.maxCompanionCounts = maxCompanionCounts;
        OEM = oem;
        this.state = state.valueOf();
        this.licenseState = licenseState;
    }
}
