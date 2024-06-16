package ATKeyLogin.backend.service;

import ATKeyLogin.backend.controller.AttestationController;
import ATKeyLogin.backend.dao.CompanionDAO;
import ATKeyLogin.backend.dao.CompanionDAO.CompanionedDevice;
import ATKeyLogin.backend.dao.DeviceDAO;
import ATKeyLogin.backend.dao.DeviceDAO.DeviceInfosForWebClient;
import ATKeyLogin.backend.model.Companion;
import ATKeyLogin.backend.model.Device;
import ATKeyLogin.backend.model.LicenseStateEnum;
import ATKeyLogin.backend.model.User;
import ATKeyLogin.backend.model.exception.BusinessLogicException;
import com.maxmind.db.Reader;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class CompanionService {

  private static final Logger log = LoggerFactory.getLogger(
    CompanionService.class
  );

  private CompanionDAO companionRepository;

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private GeoService geoService;

  @Autowired
  private LicenseStateService licenseStateService;

  public CompanionService(CompanionDAO companionRepository) {
    this.companionRepository = companionRepository;
  }

  public int delByCompanionId(long companionId) {
    return companionRepository.delByCompanionId(companionId);
  }

  public int delByKeyId(String keyId) {
    return companionRepository.delByKeyId(keyId);
  }

  public Optional<CompanionedDevice> findByDeviceIdAndKeyId(
    String deviceId,
    String keyId
  ) {
    return companionRepository.findByDeviceIdAndKeyId(deviceId, keyId);
  }

  public Companion createCompanion(Companion companion)
    throws BusinessLogicException {
    companion.setCreatedAt(System.currentTimeMillis() / 1000L);
    Companion c = null;
    try {
      c = companionRepository.saveAndFlush(companion);
    } catch (Exception e) {
      log.debug("createCompanion error = {}", e.getMessage());
      throw new BusinessLogicException(
        "assertion_companion_deviceAlreadyCompanioned"
      );
    }
    return c;
  }

  public Future<String> getLocation(InetAddress ipAddress) {
    CompletableFuture<String> completableFuture = new CompletableFuture<>();
    String location = "";
    try {
      geoService.init();
      CityResponse ci = geoService.client.city(ipAddress);
      CountryResponse co = geoService.client.country(ipAddress);

      Country country = co.getCountry();
      City city = ci.getCity();

      location = city.getName();
      if (city.getName() == null) {
        location = country.getName();
      }
    } catch (Exception e1) {
      log.debug("geolocation api service error = {}", e1.getMessage());

      try {
        Resource countryDB = new ClassPathResource("GeoLite2-Country.mmdb");
        Resource cityDB = new ClassPathResource("GeoLite2-City.mmdb");

        InputStream countryStream = countryDB.getInputStream();
        InputStream cityStream = cityDB.getInputStream();

        DatabaseReader countryReader = new DatabaseReader.Builder(countryStream)
          .fileMode(Reader.FileMode.MEMORY)
          .build();
        DatabaseReader cityReader = new DatabaseReader.Builder(cityStream)
          .fileMode(Reader.FileMode.MEMORY)
          .build();

        CountryResponse co = countryReader.country(ipAddress);
        CityResponse ci = cityReader.city(ipAddress);

        Country country = co.getCountry();
        City city = ci.getCity();

        location = city.getName();

        if (city.getName() == null) {
          location = country.getName();
        }
      } catch (Exception e2) {
        log.debug("geolocation db error = {}", e2.getMessage());
      }
    } finally {
      if (location == null || location.length() < 1) {
        location = "";
      }
    }
    completableFuture.complete(location);
    return completableFuture;
  }

  public String addCompanion(
    String device_UID,
    String deviceName,
    String userNameOnDevice,
    String email,
    String keyId,
    String userSID,
    InetAddress ipAddress
  )
    throws BusinessLogicException, InterruptedException, ExecutionException, IOException, GeoIp2Exception{
    Future<Map> res = deviceService.addDevice(
      device_UID,
      deviceName,
      email,
      keyId,
      ipAddress
    );

    Device device = (Device) res.get().get("device");
    if (companionRepository.existsByDeviceAndKeyId(device, keyId)) {
      throw new BusinessLogicException(
        "assertion_companion_deviceAlreadyCompanioned"
      );
    }

    Future<String> location = getLocation(ipAddress);

    Companion newCompanion = new Companion(keyId);
    newCompanion.setUserNameOnDevice(userNameOnDevice);
    newCompanion.setUserSidOnDevice(userSID);
    newCompanion.setDevice(device);
    newCompanion.setUser((User) res.get().get("user"));
    newCompanion.setLocation(location.get());
    Companion created = createCompanion(newCompanion);

    // licenseStateService.updateCompanionedTimes(1, (Long) res.get().get("lsid"));

    return location.get();
  }
}
