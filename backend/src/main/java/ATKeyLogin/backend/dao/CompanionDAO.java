package ATKeyLogin.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ATKeyLogin.backend.model.Companion;
import ATKeyLogin.backend.model.Device;


@Repository
@Transactional
public interface CompanionDAO extends JpaRepository<Companion, Long> {

    boolean existsByDeviceAndKeyId(Device device, String keyId);

    @Modifying
    @Query(nativeQuery = true, value = "delete from companions where id = ?1")
    int delByCompanionId(long companionId);

    @Modifying
    @Query(nativeQuery = true, value = "delete from companions where key_id = ?1")
    int delByKeyId(String keyId);

    @Query(nativeQuery = true,
    value = "SELECT user_sid_on_device as userSidOnDevice,"
    + " created_at as companionedDate, user_name_on_device as userNameOnDevice from companions"
    + " WHERE device_id = ?1 AND key_id = ?2"
    )
    Optional<CompanionedDevice> findByDeviceIdAndKeyId(String deviceId, String keyId);

    public static interface CompanionedDevice {

        String getUserSidOnDevice();
        String getUserNameOnDevice();
        Long getCompanionedDate();
    }
}
