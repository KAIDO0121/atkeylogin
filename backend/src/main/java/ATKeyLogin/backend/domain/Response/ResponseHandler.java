package ATKeyLogin.backend.domain.Response;

import lombok.Data;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ATKeyLogin.backend.controller.UserController;

import java.io.IOException;
import java.io.InputStream;

@Data
public class ResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);

    public static ResponseEntity<Map> getSuccessRes(Map payloads) {
        payloads.put("errorMessage", "");
        payloads.put("status", "success");

        ResponseEntity<Map> res = new ResponseEntity<>(payloads, HttpStatus.OK);
        return res;
    }

    public static ResponseEntity<Map> getCreateRes(Map<String, Object> payloads) {
        payloads.put("errorMessage", "");
        payloads.put("status", "success");

        return new ResponseEntity<>(payloads, HttpStatus.CREATED);
    }

    public static ResponseEntity<Map> getFailRes(HttpStatus HttpStatus, String errorMessage, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("errorMessage", errorMessage);
        m.put("status", status);
        ObjectMapper mapObject = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        mapObject.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        Resource resource = new ClassPathResource("errormsg.json");

        try {
            InputStream jsonAsStream = resource.getInputStream();

            Map<String, List<Map>> codeMap = mapObject.convertValue(jsonAsStream, Map.class);

            if (codeMap.containsKey(errorMessage)) {
                m.put("code", codeMap.get(errorMessage).get(0).get("value"));
            } else {
                if (errorMessage.startsWith("authFiError_")) {
                    m.put("authfiCode", errorMessage.substring(12));
                    m.put("errorMessage", "authFiError");
                }
                m.put("code", "999");
            }

        } catch (Exception e) {
            log.debug("e = {}", e.getMessage());
        }

        ResponseEntity<Map> res = new ResponseEntity<>(m, HttpStatus);

        return res;

    }
}
