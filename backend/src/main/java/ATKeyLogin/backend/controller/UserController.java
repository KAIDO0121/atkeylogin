package ATKeyLogin.backend.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;


import ATKeyLogin.backend.dao.DeviceDAO.DeviceInfosForWebClient;
import ATKeyLogin.backend.dao.LicenseStateDAO.LicenseAndUser;
import ATKeyLogin.backend.dao.LicenseStateDAO.LicenseStateInfo;
import ATKeyLogin.backend.domain.GetUserDevicesDTO;
import ATKeyLogin.backend.domain.UpdateKeyNameDTO;
import ATKeyLogin.backend.domain.Assertion.Options.AssertionParamsDTO;
import ATKeyLogin.backend.domain.Assertion.Results.AssertionRequestDTO;
import ATKeyLogin.backend.domain.Assertion.Results.CompaionRequestDTO;
import ATKeyLogin.backend.domain.Attestation.Options.AttestationParamsDTO;
import ATKeyLogin.backend.domain.Response.ResponseHandler;
import ATKeyLogin.backend.model.Device;

import ATKeyLogin.backend.model.User;
import ATKeyLogin.backend.model.exception.AuthFiServiceException;
import ATKeyLogin.backend.model.exception.BusinessLogicException;
import ATKeyLogin.backend.service.AuthFiService;
import ATKeyLogin.backend.service.CompanionService;
import ATKeyLogin.backend.service.DeviceService;
import ATKeyLogin.backend.service.LicenseStateService;
import ATKeyLogin.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.maxmind.geoip2.exception.GeoIp2Exception;



@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api")
@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CompanionService companionService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private LicenseStateService licenseStateService;


    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/{userId}/devices" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getUserCompanions(@PathVariable("userId") long userId) {
        
        // jakarta validation doesn't support PathVariable validation
        if (userId < 0) {
            throw new InvalidParameterException("user_devices_invalidUserId");
        }
        List<DeviceInfosForWebClient> dList = deviceService.findByUserId(userId);

        ObjectMapper mapper = new ObjectMapper();
        
        List< Map < String, Object > > l = new ArrayList<>();

        for (int i = 0; i < dList.size(); i++) {
            l.add(mapper.convertValue(dList.get(i), Map.class));
        }

        Map < String, Object > res = new HashMap();
        res.put("deviceInfos", l);

        return ResponseHandler.getSuccessRes(res);
     
    }
    
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(path = "/user/{userId}/device/{companionId}" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> delCompanion(@PathVariable("userId") long userId, @PathVariable("companionId") long companionId) throws BusinessLogicException{
        
        // jakarta validation doesn't support PathVariable validation
        if (userId < 0) {
            throw new InvalidParameterException("user_devices_invalidUserId");
        }
        if (companionId < 0) {
            throw new InvalidParameterException("user_devices_invalidCompanionId");
        }

        int rows = companionService.delByCompanionId(companionId);

        if (rows < 1) {
            throw new InvalidParameterException("user_device_companionNotFound");
        } 
        // else {
        //     Optional<LicenseStateInfo> l = licenseStateService.getLicenseStateInfoByUserId(userId);
        //     if (!l.isPresent()) {
        //         throw new BusinessLogicException("user_device_licenseNotFound");
        //     }
        //     licenseStateService.updateCompanionedTimes(-1, l.get().getId());
        // }
        
        Map < String, Object > res = new HashMap();
        res.put("companionId", companionId);

        return ResponseHandler.getSuccessRes(res);
     
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/{userId}/license" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getLicenseAndUserByUId(@PathVariable("userId") long userId) {
        
        Optional<LicenseAndUser> licenseAndUser = licenseStateService.getLicenseAndUserByUId(userId);
        
        if (!licenseAndUser.isPresent()) {
            throw new InvalidParameterException("user_license_userNotFound");
        }
        
        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > res = mapObject.convertValue(licenseAndUser.get(), Map.class);

        return ResponseHandler.getSuccessRes(res);
     
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/licenseByEmail" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getLicenseAndUserByEmail(HttpServletRequest httpServletRequest) {
        
        String email = (String) httpServletRequest.getAttribute("email");
        
        Optional<LicenseAndUser> licenseAndUser = licenseStateService.getLicenseAndUserByEmail(email);
        
        if (!licenseAndUser.isPresent()) {
            throw new InvalidParameterException("user_license_userNotFound");
        }
        
        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > res = mapObject.convertValue(licenseAndUser.get(), Map.class);

        return ResponseHandler.getSuccessRes(res);
     
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/{userId}/authenticators" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getAuthenticators(@PathVariable("userId") Long userId) throws IOException, AuthFiServiceException{
        
        if (userId < 0) {
            throw new InvalidParameterException("user_devices_invalidUserId");
        }

        Optional<User> user = userService.findById(userId);
        
        if (!user.isPresent()) {
            throw new InvalidParameterException("user_license_userNotFound");
        }

        List<Map> keys = userService.getAuthenticatorList(user.get().getAuthFiUserId());
        Map res = new HashMap<>() ;

        res.put("keysInfo", keys);
        
        return ResponseHandler.getSuccessRes(res);
     
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(path = "/user/{userId}/authenticator/{keyId}" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> deleteKeyById(@PathVariable("userId") Long userId, @PathVariable("keyId") String keyId) throws IOException, AuthFiServiceException{
        
        if (userId < 0) {
            throw new InvalidParameterException("user_devices_invalidUserId");
        }

        if (keyId.isBlank()) {
            throw new InvalidParameterException("user_authenticator_invalidKeyId");
        }

        Optional<User> user = userService.findById(userId);
        
        if (!user.isPresent()) {
            throw new InvalidParameterException("user_license_userNotFound");
        }

        String deleteKeyId = userService.deleteAuthenticator(user.get().getAuthFiUserId(), keyId);

        companionService.delByKeyId(keyId);
        
        Map res = new HashMap<>() ;

        res.put("keyId", deleteKeyId);
        
        return ResponseHandler.getSuccessRes(res);
     
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping(path = "/user/{userId}/authenticator/{keyId}" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> updateKeyName(@Valid @RequestBody UpdateKeyNameDTO updateKeyNameDTO, 
    @PathVariable("userId") Long userId, @PathVariable("keyId") String keyId) throws IOException, AuthFiServiceException{
        
        if (userId < 0) {
            throw new InvalidParameterException("user_devices_invalidUserId");
        }

        if (keyId.isBlank()) {
            throw new InvalidParameterException("user_authenticator_invalidKeyId");
        }

        Optional<User> user = userService.findById(userId);
        
        if (!user.isPresent()) {
            throw new InvalidParameterException("user_license_userNotFound");
        }

        Map keyInfo = userService.updateAuthenticatorName(user.get().getAuthFiUserId(), keyId, updateKeyNameDTO);
        Map res = new HashMap<>() ;

        res.put("keyId", keyInfo.get("credential_id"));
        res.put("name", keyInfo.get("name"));
        
        return ResponseHandler.getSuccessRes(res);
     
    }

    @GetMapping(path = "/currentVersion" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getCurrentVersion() {
        Map res = new HashMap<>() ;
        res.put("version", "v0.2.2");
        return ResponseHandler.getSuccessRes(res);
     
    }

    // @DeleteMapping("/deleteUser/{userId}")
    // public void test(@PathVariable("userId") Long userId) {
    //     userService.deleteUser(userId);
    // }
}
