package ATKeyLogin.backend.service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import ATKeyLogin.backend.domain.Attestation.Options.AttestationParamsDTO;
import ATKeyLogin.backend.domain.Attestation.Results.AttestationRequestDTO;
import ATKeyLogin.backend.controller.AttestationController;
import ATKeyLogin.backend.domain.FidoKeyInfoRes;
import ATKeyLogin.backend.domain.Assertion.Options.AssertionParamsDTO;
import ATKeyLogin.backend.domain.Assertion.Results.AssertionRequestDTO;
import ATKeyLogin.backend.domain.Request.AuthFiRequest;
import ATKeyLogin.backend.model.exception.AuthFiServiceException;
import ATKeyLogin.backend.model.exception.BusinessLogicException;

import java.io.IOException;
import java.util.*;

@Service
public class AuthFiService {
    private static final Logger log = LoggerFactory.getLogger(AttestationController.class);

    @Value("${AuthFi.stage.uri}")
    private String AuthfiURI;

    @Value("${AuthFi.apiPath}") 
    private String AuthFiApiPath;

    @Autowired
    private UserService userService;


    public Map attestationOpt(AttestationParamsDTO attestationParamsDTO) throws AuthFiServiceException{
        RestTemplate restTemplate = new RestTemplate();
        String uri = String.format("%s/api/v1/webauthn/registration", AuthfiURI); // or any other uri
       
        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > params = mapObject.convertValue(attestationParamsDTO, Map.class);

        HttpEntity<?> req = new AuthFiRequest(params, AuthFiApiPath).getRequest();
       
        Map <String, Object> res = new HashMap<>();
    
        try {
            Object resBody = restTemplate.postForEntity(uri, req, Object.class).getBody();

            res = mapObject.convertValue(resBody, Map.class);

            Map<String, Map> fido_register_request = (Map) res.get("fido_register_request");

            String uid = (String) fido_register_request.get("user").get("id");

            userService.updateAuthFiUserId(uid, attestationParamsDTO.getParams().getUser().getName());

            res.remove("code");
            return res;

        } catch (HttpClientErrorException e) {
            log.debug("e = {}", e.getResponseBodyAs(Map.class));
            Integer c = (Integer) e.getResponseBodyAs(Map.class).get("code");
            String err = String.format("authFiError_%d", c);
            throw new AuthFiServiceException(err);
        }

    }

    public Map attestationRes(AttestationRequestDTO attestationRequestDTO) throws AuthFiServiceException{
        RestTemplate restTemplate = new RestTemplate();
        String uri = String.format("%s/api/v1/webauthn/registration", AuthfiURI); // or any other uri
       
        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > params = mapObject.convertValue(attestationRequestDTO, Map.class);
        Map <String, Object> res = new HashMap<>();

        HttpEntity<?> req = new AuthFiRequest(params, AuthFiApiPath).getRequest();

        try {
            Object resBody = restTemplate.exchange(uri, HttpMethod.PUT, req, Object.class).getBody();

            res = mapObject.convertValue(resBody, Map.class);
            
            res.replace("key_info", new FidoKeyInfoRes((Map<String,Object>) res.get("key_info")).getKey_info());
            res.remove("code");

            return res;
            
        } catch (HttpClientErrorException e) {
            log.debug("e = {}", e.getResponseBodyAs(Map.class));
            Integer c = (Integer) e.getResponseBodyAs(Map.class).get("code");
            String err = String.format("authFiError_%d", c);
            throw new AuthFiServiceException(err);
        }
       
    
    }

    public Map assertionOpt(AssertionParamsDTO AssertionParamsDTO) throws AuthFiServiceException{
        RestTemplate restTemplate = new RestTemplate();
        String uri = String.format("%s/api/v1/webauthn/login", AuthfiURI); // or any other uri
       
        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > params = mapObject.convertValue(AssertionParamsDTO, Map.class);

        HttpEntity<?> req = new AuthFiRequest(params, AuthFiApiPath).getRequest();
       
        

        Map <String, Object> res = new HashMap<>();

        try {
            Object resBody = restTemplate.postForEntity(uri, req, Object.class).getBody();
            res = mapObject.convertValue(resBody, Map.class);
            res.remove("code");
            return res;

        } catch (HttpClientErrorException e) {
            log.debug("e = {}", e.getResponseBodyAs(Map.class));
            Integer c = (Integer) e.getResponseBodyAs(Map.class).get("code");
            String err = String.format("authFiError_%d", c);
            throw new AuthFiServiceException(err);
        }
    }

    public Map assertionRes(AssertionRequestDTO AssertionRequestDTO) throws AuthFiServiceException{
        RestTemplate restTemplate = new RestTemplate();
        String uri = String.format("%s/api/v1/webauthn/login", AuthfiURI); // or any other uri
       
        ObjectMapper mapObject = new ObjectMapper();
        Map < String, Object > params = mapObject.convertValue(AssertionRequestDTO, Map.class);

        HttpEntity<?> req = new AuthFiRequest(params, AuthFiApiPath).getRequest();

        Map <String, Object> res = new HashMap<>();

        
        try {
    
            Object resBody = restTemplate.exchange(uri, HttpMethod.PUT, req, Object.class).getBody();   
            
            res = mapObject.convertValue(resBody, Map.class);

            Map < String, Object > u = (Map) res.get("user");
            
            userService.updateUserLastLogin((String) u.get("name"));

            res.replace("key_info", new FidoKeyInfoRes( (Map) res.get("key_info")).getKey_info());
            res.remove("code");
            return res;
        } catch (HttpClientErrorException e) {
            log.debug("e = {}", e.getResponseBodyAs(Map.class));
            Integer c = (Integer) e.getResponseBodyAs(Map.class).get("code");
            String err = String.format("authFiError_%d", c);
            throw new AuthFiServiceException(err);
        }


    }

}
