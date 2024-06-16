package ATKeyLogin.backend.controller;

import ATKeyLogin.backend.domain.ActivateDTO;
import ATKeyLogin.backend.model.exception.IllegalUsingLicenseException;
import ATKeyLogin.backend.service.CountryService;
import ATKeyLogin.backend.service.LicenseService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static ATKeyLogin.backend.domain.Response.ResponseHandler.*;

@RestController
@RequestMapping("/api")
public class LicenseController {
    private static final Logger logger = LoggerFactory.getLogger(LicenseController.class);

    @Autowired
    private LicenseService licenseService;

    // For Test

    // @PreAuthorize("hasRole('USER')")
    @PutMapping(path = "/license/{userId}/activate", produces = "application/json; charset=utf-8")
    public ResponseEntity<?> ActivateLicenseCode(@PathVariable("userId") long userId, @Valid @NotNull @RequestBody ActivateDTO requestBody) {
        try {
            Date time = new Date();
            HashMap<String, Object> licenseState = licenseService.ActivateLicenseCode(userId, requestBody.getLicenseCode(), time);
            HashMap<String, Object> m = new HashMap<>();
            m.put("license", licenseState);
            return getSuccessRes(m);
        } catch (IllegalArgumentException e) {
            return getFailRes(HttpStatus.BAD_REQUEST, e.getMessage(), "failed");
        } catch (IllegalUsingLicenseException e) {
            logger.warn(String.format("user[%d] is activating license code (%s) but code had been used by user[%d]", userId, requestBody.getLicenseCode(), e.getActiveUser()));
            return getFailRes(HttpStatus.BAD_REQUEST, e.getMessage(), "failed");
        }
    }
}
