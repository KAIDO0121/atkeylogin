package ATKeyLogin.backend.service;

import ATKeyLogin.backend.controller.AttestationController;
import ATKeyLogin.backend.dao.CompanionDAO.CompanionedDevice;
import ATKeyLogin.backend.dao.DeviceDAO;
import ATKeyLogin.backend.dao.DeviceDAO.DeviceInfosForWebClient;
import ATKeyLogin.backend.dao.LicenseStateDAO.LicenseStateInfo;
import ATKeyLogin.backend.dao.UserDAO;
import ATKeyLogin.backend.domain.Assertion.Results.AssertionRequestDTO;
import ATKeyLogin.backend.model.Device;
import ATKeyLogin.backend.model.LicenseStateEnum;
import ATKeyLogin.backend.model.User;
import ATKeyLogin.backend.model.exception.AuthFiServiceException;
import ATKeyLogin.backend.model.exception.BusinessLogicException;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private static final Logger log = LoggerFactory.getLogger(
    DeviceService.class
  );

  private DeviceDAO deviceRepository;

  @Autowired
  private UserService userService;

  @Autowired
  @Lazy
  private CompanionService companionService;


  @Autowired
  private AuthFiService authFiService;

  @Autowired
  private LicenseStateService licenseStateService;

  public DeviceService(DeviceDAO deviceRepository) {
    this.deviceRepository = deviceRepository;
  }

  public List<Device> findByDeviceId(String deviceId) {
    return deviceRepository.findByDeviceId(deviceId);
  }

  public List<DeviceInfosForWebClient> findByUserId(long userId) {
    return deviceRepository.findByUserId(userId);
  }

  public int updateDeviceLastLogin(String deviceId) {
    return deviceRepository.updateDeviceLastLogin(
      System.currentTimeMillis() / 1000L,
      deviceId
    );
  }

  // public int updateDeviceLocation(String deviceId, String location) {
  //   return deviceRepository.updateDeviceLocation(location, deviceId);
  // }

  public Device createDevice(Device device) {
    device.setCreatedAt(System.currentTimeMillis() / 1000L);
    return deviceRepository.saveAndFlush(device);
  }

  public int delDeviceByDId(String deviceId) {
    return deviceRepository.delDeviceByDId(deviceId);
  }

  public Future<Map> addDevice(
    String device_UID,
    String deviceName,
    String email,
    String keyId,
    InetAddress ipAddress
  ) throws BusinessLogicException, IOException, GeoIp2Exception {
    CompletableFuture<Map> completableFuture = new CompletableFuture<>();

    Optional<User> user = userService.findByEmail(email);
    if (!user.isPresent()) {
      throw new BusinessLogicException("assertion_companion_userNotFound");
    }

    Map m = new HashMap<>();

    long userId = user.get().getId();
    m.put("user", user.get());

    Optional<LicenseStateInfo> info = licenseStateService.getLicenseStateInfoByUserId(
      userId
    );

    licenseStateService.companionLicenseValidation(info);

    List<Device> dList = deviceRepository.findByDeviceId(device_UID);

    m.put("lsid", info.get().getId());


    if (dList.size() > 0) {
      m.put("device", dList.get(0));

      // deviceRepository.updateDeviceLocation(location, device_UID);

      completableFuture.complete(m);
    } else {

      Device newDevice = new Device(device_UID);
      newDevice.setDeviceName(deviceName);
      try {
        newDevice = createDevice(newDevice);
      } catch (Exception e) {
        log.debug("create device error = {}", e.getMessage());
        throw e;
      }

      m.put("device", newDevice);
      completableFuture.complete(m);
    }

    return completableFuture;
  }

  public Map validateCompanion(
    String device_UID,
    String keyId,
    AssertionRequestDTO assertionRequestDTO
  ) throws BusinessLogicException, AuthFiServiceException {
    Optional<CompanionedDevice> companionedDevice = companionService.findByDeviceIdAndKeyId(
      device_UID,
      keyId
    );

    if (!companionedDevice.isPresent()) {
      throw new BusinessLogicException(
        "assertion_windowsLogin_deviceNotCompanioned"
      );
    }
    Map<String, Map<String, Object>> res = new HashMap<>();

    try {
      res = authFiService.assertionRes(assertionRequestDTO);
    } catch (Exception e) {
      throw new AuthFiServiceException(
        "assertion_authFiService_authFiServiceError"
      );
    }

    Map<String, Object> r = new HashMap<>();
    r.put("device", companionedDevice.get());
    r.put("res", res);

    return r;
  }

  public void windowsLogin(String device_UID, String email)
    throws BusinessLogicException {
    Optional<LicenseStateInfo> info = licenseStateService.getLicenseStateInfoByEmail(
      email
    );

    licenseStateService.windowsloginLicenseValidation(info);

    deviceRepository.updateDeviceLastLogin(
      System.currentTimeMillis() / 1000L,
      device_UID
    );
  }
}
