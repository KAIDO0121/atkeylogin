package ATKeyLogin.backend.service;


import ATKeyLogin.backend.dao.UserDAO;
import ATKeyLogin.backend.model.Device;
import ATKeyLogin.backend.model.LicenseStateEnum;
import ATKeyLogin.backend.model.User;
import ATKeyLogin.backend.model.exception.BusinessLogicException;
import jakarta.annotation.PostConstruct;

import com.maxmind.geoip2.WebServiceClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.security.InvalidParameterException;

import java.util.*;


@Service
public class GeoService {
    private static final Logger log = LoggerFactory.getLogger(GeoService.class);

    @Value("${geo.apiKey}")
    private String geoApiKey;

    @Value("${geo.id}")
    private int geoId;

    public WebServiceClient client;


    @PostConstruct
    public void init() {
        client = new WebServiceClient.Builder(geoId, geoApiKey).build();
        
    }
}
