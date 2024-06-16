package ATKeyLogin.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.security.InvalidParameterException;

import ATKeyLogin.backend.dao.RescueTempDAO;
import ATKeyLogin.backend.dao.TempDAO;
import ATKeyLogin.backend.dao.UserDAO;
import ATKeyLogin.backend.dao.UserDAO.UserWithoutLicense;

import org.springframework.http.*;
import ATKeyLogin.backend.domain.UpdateKeyNameDTO;
import ATKeyLogin.backend.domain.FidoKeyInfoRes;
// import ATKeyLogin.backend.dao.UserDAO.CompanionInfos;
import ATKeyLogin.backend.domain.RescueTempDTO;
import ATKeyLogin.backend.domain.RescueVeriCodeDTO;
import ATKeyLogin.backend.domain.TempDTO;
import ATKeyLogin.backend.domain.TempVeriCodeDTO;
import ATKeyLogin.backend.domain.Request.AuthFiRequest;
import ATKeyLogin.backend.model.Device;
import ATKeyLogin.backend.model.User;
import ATKeyLogin.backend.model.exception.AuthFiServiceException;
import ATKeyLogin.backend.model.exception.BusinessLogicException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    // @Autowired
    // private Validator validator;

    @Autowired
    private RescueTempDAO rescueDAO;

    @Autowired
    private TempDAO tempDAO;    

    @Value("${AuthFi.stage.uri}")
    private String AuthfiURI;

    @Value("${AuthFi.apiPath}") 
    private String AuthFiApiPath;

    @Autowired
    private MailService mailService;

    private UserDAO userRepository;

    public UserService(
        UserDAO userRepository
    ) {
        this.userRepository = userRepository;
    }

    // public List<Optional<CompanionInfos>> getCompanionInfosByUserId(long userId) {
    //     return userRepository.getCompanionInfosByUserId(userId);
    // }
    
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteUser(userId);
    }


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserWithoutLicense> findUsersWithoutLicense() {
        return userRepository.findUsersWithoutLicense();
    }

    public int updateUserLastLogin(String email) {
        return userRepository.updateUserLastLogin(System.currentTimeMillis() / 1000L, email);
    }

    public int updateAuthFiUserId(String authFiUserId, String email) {
        return userRepository.updateAuthFiUserId(authFiUserId, email);
    }
    
    public User createUser(User user) {
        user.setCreatedAt(System.currentTimeMillis() / 1000L);
        user.setLastLogin(System.currentTimeMillis() / 1000L);

        return userRepository.saveAndFlush(user);
    }

    public String deleteAuthenticator(String authFiUserId, String keyId) throws AuthFiServiceException, IOException {
        RestTemplate restTemplate = new RestTemplate();
        String uri = String.format("%s/api/v1/users/%s/keys/%s", AuthfiURI, authFiUserId, keyId);

        HttpEntity<?> req = new AuthFiRequest(new HashMap<>(), AuthFiApiPath).getRequest();
        Map <String, Object> res = new HashMap<>();

        try {
            Object resBody = restTemplate.exchange(uri, HttpMethod.DELETE, req, Object.class).getBody();

            ObjectMapper mapObject = new ObjectMapper();

            res = mapObject.convertValue(resBody, Map.class);

            res.remove("code");
            int deleteKeys = (int) res.get("deleteKeys");

            if (deleteKeys < 1) {
                throw new IOException("user_authenticator_invalidKeyId");
            }
            return keyId;

        } catch (HttpClientErrorException e) {
            log.debug("e = {}", e.getResponseBodyAs(Map.class));
            Integer c = (Integer) e.getResponseBodyAs(Map.class).get("code");
            String err = String.format("authFiError_%d", c);
            throw new AuthFiServiceException(err);
        }

    }

    public Map updateAuthenticatorName(String authFiUserId, String keyId, UpdateKeyNameDTO updateKeyNameDTO) throws AuthFiServiceException {
        RestTemplate restTemplate = new RestTemplate();
        String uri = String.format("%s/api/v1/users/%s/keys/%s", AuthfiURI, authFiUserId, keyId);

        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > params = mapObject.convertValue(updateKeyNameDTO, Map.class);

        HttpEntity<?> req = new AuthFiRequest(params, AuthFiApiPath).getRequest();

        Map <String, Object> res = new HashMap<>();

        try {
            Object resBody = restTemplate.exchange(uri, HttpMethod.PUT, req, Object.class).getBody();

            res = mapObject.convertValue(resBody, Map.class);

            res.remove("code");
            return (Map) res.get("key_info");

        } catch (HttpClientErrorException e) {
            log.debug("e = {}", e.getResponseBodyAs(Map.class));
            Integer c = (Integer) e.getResponseBodyAs(Map.class).get("code");
            String err = String.format("authFiError_%d", c);
            throw new AuthFiServiceException(err);
        }

    }

    public List<Map> getAuthenticatorList(String authFiUserId) throws AuthFiServiceException {
        RestTemplate restTemplate = new RestTemplate();
        String uri = String.format("%s/api/v1/users/%s/keys", AuthfiURI, authFiUserId);

        HttpEntity<?> req = new AuthFiRequest(new HashMap<>(), AuthFiApiPath).getRequest();

        ObjectMapper mapObject = new ObjectMapper();

        Map <String, Object> res = new HashMap<>();

        try {
            Object resBody = restTemplate.exchange(uri, HttpMethod.GET, req, Object.class).getBody();

            res = mapObject.convertValue(resBody, Map.class);

            res.remove("code");
            List<Map> keyList = (List<Map>) res.get("keys");

            return keyList.stream()
            .map(k -> new FidoKeyInfoRes(k).getKey_info())
            .collect(Collectors.toList());

        } catch (HttpClientErrorException e) {
            log.debug("e = {}", e.getResponseBodyAs(Map.class));
            Integer c = (Integer) e.getResponseBodyAs(Map.class).get("code");
            String err = String.format("authFiError_%d", c);
            throw new AuthFiServiceException(err);
        }
    
    }
    
    public String addRescueTempUser(RescueTempDTO RescueTempDTO) throws IOException { // dto = domain

        String addedTempEmail = "";

        Optional<User> u = findByEmail(RescueTempDTO.getEmail());
        if (!u.isPresent()) {
            throw new InvalidParameterException("operation_rescuePlan_userNotFound");
        }
            rescueDAO.createRescueTemp(RescueTempDTO);
            try {
                addedTempEmail = mailService.sendRescueEmail(RescueTempDTO.getEmail(), RescueTempDTO.getVeriCode(), RescueTempDTO.getLanguage());
            } catch (IOException e) {
                log.debug("Send email failed: {}", e.getMessage());
                throw e;
            }
            
            addedTempEmail = RescueTempDTO.getEmail();
        
        
        return addedTempEmail;
    }

    public Map<String, Object> getUsersWithoutLicense() throws IOException {
        List<UserWithoutLicense> users = userRepository.findUsersWithoutLicense();
        Map<String, Object> r = new HashMap<>();
        r.put("users", users);

        return r;
    }

    public long validateRescue(RescueVeriCodeDTO RescueVeriCodeDTO) throws Exception{
        
        Map<String, Object> createdT = rescueDAO.getRescueTemp(RescueVeriCodeDTO);
        
        if (createdT.size() < 1 || 
        !Objects.equals(createdT.get("email"), RescueVeriCodeDTO.getEmail()) || 
        !Objects.equals(createdT.get("veriCode"), RescueVeriCodeDTO.getVeriCode())
        ) {
            throw new InvalidParameterException("operation_veri_emailOrVeriCodeInvalid");
        }
        Optional<User> u = findByEmail(RescueVeriCodeDTO.getEmail());
        if (!u.isPresent()) {
            throw new InvalidParameterException("operation_veri_userNotFound");
        }
        rescueDAO.removeRescueTemp(u.get().getEmail());
        return u.get().getId();
    }

    public String addTempUser(TempDTO userTempDTO) throws IOException { // dto = domain

        String addedTempEmail = "";

        Optional<User> u = findByEmail(userTempDTO.getEmail());
        if (u.isPresent()) {
            throw new InvalidParameterException("attestation_veriCode_userAlreadyExist");
        }
        // if (!dao.checkDuplicate(userTempDTO)) {
            tempDAO.createUserTemp(userTempDTO);
            try {
                addedTempEmail = mailService.sendSignUpEmail(userTempDTO.getEmail(), userTempDTO.getVeriCode(), userTempDTO.getLanguage());
            } catch (IOException e) {
                log.debug("Send email failed: {}", e.getMessage());
                throw e;
            }
            
            addedTempEmail = userTempDTO.getEmail();
        // }
        
        return addedTempEmail;
    }

    public User validateTemp(TempVeriCodeDTO TempVeriCodeDTO) throws Exception{

        Map<String, Object> createdT = tempDAO.getUserTemp(TempVeriCodeDTO);
        if (createdT.size() < 1 || 
        !Objects.equals(createdT.get("email"), TempVeriCodeDTO.getEmail()) || 
        !Objects.equals(createdT.get("veriCode"), TempVeriCodeDTO.getVeriCode())
        ) {
            throw new InvalidParameterException("attestation_veriCode_emailOrVeriCodeInvalid");
        }
        Optional<User> u = findByEmail(TempVeriCodeDTO.getEmail());
        if (u.isPresent()) {
            throw new InvalidParameterException("attestation_veriCode_userAlreadyExist");
        }
        tempDAO.removeUserTemp(TempVeriCodeDTO.getEmail());
        User newUser = new User((String) createdT.get("email"), (String) createdT.get("authorities"));
        
        // TODO check if the affected row >  0 ?
        User created = createUser(newUser);

        return created;

    }

    
    
}

