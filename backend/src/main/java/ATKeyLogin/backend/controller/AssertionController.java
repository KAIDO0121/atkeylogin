package ATKeyLogin.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RestController;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import ATKeyLogin.backend.domain.Assertion.Options.AssertionParamsDTO;
import ATKeyLogin.backend.domain.Assertion.Results.AssertionRequestDTO;
import ATKeyLogin.backend.domain.Assertion.Results.CompaionRequestDTO;
import ATKeyLogin.backend.domain.Assertion.Results.WindowsLoginRequestDTO;
import ATKeyLogin.backend.domain.Response.ResponseHandler;
import ATKeyLogin.backend.model.Companion;
import ATKeyLogin.backend.dao.CompanionDAO.CompanionedDevice;
import ATKeyLogin.backend.model.Device;
import ATKeyLogin.backend.model.RefreshToken;
import ATKeyLogin.backend.model.exception.AuthFiServiceException;
import ATKeyLogin.backend.model.exception.BusinessLogicException;
import ATKeyLogin.backend.service.UserService;
import ATKeyLogin.backend.service.DeviceService;

import ATKeyLogin.backend.service.AuthFiService;
import ATKeyLogin.backend.service.CompanionService;
import ATKeyLogin.backend.service.JWTService;
import ATKeyLogin.backend.service.LicenseStateService;
import ATKeyLogin.backend.service.UserDetailsImpl;
import ATKeyLogin.backend.service.RefreshTokenService;


@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api")
@RestController
public class AssertionController {
    private static final Logger log = LoggerFactory.getLogger(AssertionController.class);


    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CompanionService companionService;

    @Autowired
    private AuthFiService authFiService;

    @Autowired
    private UserService userService;

    @Autowired
    private LicenseStateService licenseStateService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtUtils;  

    @PostMapping(path = "/assertion/options" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getOptionsForLogin(@Valid @RequestBody AssertionParamsDTO assertionParamsDTO) throws AuthFiServiceException{
        Map res = authFiService.assertionOpt(assertionParamsDTO);
        return ResponseHandler.getSuccessRes(res);
    }

    @PostMapping(path = "/assertion/result/authenticator" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> signInToWebClient(@Valid @RequestBody AssertionRequestDTO assertionRequestDTO) throws AuthFiServiceException{
        
        Map res = authFiService.assertionRes(assertionRequestDTO);
        Map user = (Map) res.get("user");

        Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(user.get("name"), user.get("name")));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        String jwt = jwtUtils.generateJwtToken(userDetails);
        
        // ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId(), userDetails.getEmail());
        log.debug("userid = {}", userDetails.getId());
        licenseStateService.updateLicenseStateByUserId(userDetails.getId());        

        res.put("refreshToken", (String)refreshToken.getToken());
        
        res.put("accessToken", jwt);

        res.put("authorities", userDetails.getAuthorities().iterator().next().getAuthority());

        res.put("userId", userDetails.getId());

        return ResponseHandler.getSuccessRes(res);
    }

    @PostMapping(path = "/assertion/result/companion" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> companion(@Valid @RequestBody CompaionRequestDTO compaionRequestDTO, 
    @RequestHeader("x-forwarded-for") String ips,
    @RequestHeader("x-device-uid") String device_UID, HttpServletRequest request) 
    throws BusinessLogicException, UnknownHostException, 
IOException, GeoIp2Exception, InterruptedException, ExecutionException, AuthFiServiceException {
        AssertionRequestDTO assertionRequestDTO = new AssertionRequestDTO(compaionRequestDTO.getFido_login_response());
        Map<String, Map<String, Object>> res = authFiService.assertionRes(assertionRequestDTO);
        
        List<String> ipList = Arrays.asList(ips.split(","));
        InetAddress ipAddress = InetAddress.getByName(ipList.get(0));

        String location = companionService.addCompanion(device_UID, compaionRequestDTO.getDeviceName(), 
        compaionRequestDTO.getUserNameOnDevice(), 
        (String) res.get("user").get("name"), 
        (String) res.get("key_info").get("keyId"),
        compaionRequestDTO.getUserSID(), ipAddress);

        Map < String, Object > deviceMap = new HashMap<>();
        deviceMap.put("location", location);
        deviceMap.put("keyName", res.get("key_info").get("name"));

        // String location = companionService.addCompanion(device_UID, compaionRequestDTO.getDeviceName(), 
        // compaionRequestDTO.getUserNameOnDevice(), 
        // "sean.liao@authentrend.com", 
        // "key005",
        // compaionRequestDTO.getUserSID(), InetAddress.getByName("1.3.0.1"));

        // Map < String, Object > deviceMap = new HashMap<>();
        // deviceMap.put("location", location);
        // deviceMap.put("keyName", "key012");
        
        return ResponseHandler.getSuccessRes(deviceMap);
    }

    @PostMapping(path = "/assertion/result/windowslogin" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> windowsClientLogin(@Valid @RequestBody WindowsLoginRequestDTO windowsLoginRequestDTO, @RequestHeader("x-device-uid") String device_UID) throws BusinessLogicException, AuthFiServiceException{
        
        AssertionRequestDTO assertionRequestDTO = new AssertionRequestDTO(windowsLoginRequestDTO.getFido_login_response());

        Map <String, Object> m = deviceService.validateCompanion(device_UID, windowsLoginRequestDTO.getFido_login_response().getId(), assertionRequestDTO);
        
        // Map res = new HashMap<>();
        // Map keyInfo = new HashMap<>();
        // keyInfo.put("keyId", "key002");

        // res.put("user", "sean.liao@authentrend.com");
        // res.put("keyInfo", keyInfo);
        Map<String, Map<String, Object>> res = (Map) m.get("res");
        CompanionedDevice companionedDevice = (CompanionedDevice) m.get("device");

        // CompanionedDevice device = deviceService.windowsLogin(device_UID, (String) keyInfo.get("keyId"), (String) res.get("user"));
        deviceService.windowsLogin(device_UID, (String) res.get("user").get("name"));

        Map <String, Object> d = new HashMap<>();
        d.put("userSid", companionedDevice.getUserSidOnDevice());
        d.put("userNameOnDevice", companionedDevice.getUserNameOnDevice());

        
        return ResponseHandler.getSuccessRes(d);
    }
}
