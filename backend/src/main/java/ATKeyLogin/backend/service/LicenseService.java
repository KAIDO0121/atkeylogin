package ATKeyLogin.backend.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import javax.swing.JButton;

import ATKeyLogin.backend.dao.LicenseCodeDAO;
import ATKeyLogin.backend.dao.LicenseCodeDAO.Company;
import ATKeyLogin.backend.dao.LicenseCodeDAO.OrderDetail;
import ATKeyLogin.backend.dao.LicenseCodeDAO.LicenseDetail;
import ATKeyLogin.backend.dao.LicenseCodeDAO.LicenseAndUser;
import ATKeyLogin.backend.dao.OrderDAO;
import ATKeyLogin.backend.domain.ChangeLicenseStateDTO;
import ATKeyLogin.backend.domain.GenerateOEMLicenseDTO;
import ATKeyLogin.backend.model.exception.IllegalUsingLicenseException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import ATKeyLogin.backend.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.validator.routines.EmailValidator;

import com.authentrend.atkey.login.licensecode.ActivationLicenseCode;
import com.authentrend.atkey.login.licensecode.CodeVerifier;
import com.authentrend.iso3166.CountryCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;

@Service
public class LicenseService {
    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    private static final String RETAIL_ORDER_ID_PREFIX = "000RET";
    private static final long DAYS_OF_ONE_YEAR = 365;
    private static final long TRIAL_DAYS_LIMIT = 604800;

    @Autowired
    private CountryService countryService;

    @Autowired
    private LicenseStateService licenseStateService;

    @Autowired
    private OEMInformationService oemService;

    @Autowired
    private UserService userService;

    @Autowired
    private LicenseCodeDAO licenseCodeRepository;

    @Autowired
    private OrderDAO orderRepository;

    public Map <String, Object> getOEMHierarchyAndUsers(String licenseCode, 
    String email, Long startDate, 
    Long endDate, String contract, 
    String countryCode, String businessName) {
        
        List<Map> companies = new ArrayList<>();

        Map<String, List> cmap = new HashMap<>();

        List<LicenseDetail> users = new ArrayList<>();
        
        Map < String, Object > res = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();

        if (licenseCode != null) {
            if (licenseCode.isBlank()) {
                throw new IllegalArgumentException("admin_OEMsAndUsers_invalidLicenseCode");
            }

            Company c = licenseCodeRepository.findCompanyByLicenseCode(licenseCode);
            
            Map < String, Object > companyMap = new HashMap<>();
            companyMap = mapper.convertValue(c, Map.class);
            
            LicenseDetail u = licenseCodeRepository.findUserByLicenseCode(licenseCode);
            
            if (c != null) {
                String oemId = c.getOEMId();
                String country = oemId.substring(1, 4);
                companyMap.put("country", country);
                companyMap.remove("oemid");

                cmap.put(oemId, new ArrayList());
                
                cmap.get(oemId).add(companyMap);

            }

            if (u != null) {
                users.add(u);
            }

            res.put("users", users);
            res.put("companies", cmap);

            return res;
        }

        if (email != null) {

            if (!EmailValidator.getInstance().isValid(email)) {
                throw new IllegalArgumentException("admin_OEMsAndUsers_invalidEmail");
            }

            LicenseDetail u = licenseCodeRepository.findUserByEmail(email);

            if (u != null) {
                users.add(u);
            }

            res.put("users", users);
            res.put("companies", cmap);

            return res;
        }

        if (endDate != null || startDate != null) {
            if (endDate == null || startDate == null) {
                throw new IllegalArgumentException("admin_OEMsAndUsers_invalidDateRange");
            }

            if (endDate <= startDate) {
                throw new IllegalArgumentException("admin_OEMsAndUsers_invalidDateRange");
            }

            List<LicenseDetail> l = licenseCodeRepository.findUsersByExpirationDate(startDate, endDate);

            if (l.size() > 0) {
                users = l;
            }

            res.put("users", users);
            res.put("companies", cmap);

            return res;
        }

        if (contract != null) {
            if (contract.isBlank()) {
                throw new IllegalArgumentException("admin_license_invalidPONumber");
            }
            
            Map<String, List> l2 = new HashMap<>();

            List<Company> c = licenseCodeRepository.findCompanyByContract(contract);

            for (int i = 0; i < c.size(); i++) {
                String oemId = c.get(i).getOEMId();
                String country = oemId.substring(1, 4);

                Map order = mapper.convertValue(c.get(i), Map.class);
                order.remove("oemid");
                order.put("country", country);
                
                if (l2.get(oemId) == null) {
                    l2.put(oemId, new ArrayList());
                }
                l2.get(oemId).add(order);
            }

            if (l2.keySet().size() > 0) {
                cmap = l2;
            }

            res.put("users", users);
            res.put("companies", cmap);

            return res;

        }

        if (businessName != null || countryCode != null) {

            if (businessName != null && businessName.isBlank()) {
                throw new IllegalArgumentException("admin_license_invalidBusinessName");
            }

            if (countryCode != null && (countryCode.length() < 2 || countryCode.length() > 3)) {
                throw new IllegalArgumentException("admin_license_invalidCountryCode");
            }

            Map<String, List> l2 = new HashMap<>();

            List<Company> c = licenseCodeRepository
            .findOEMsOrdersByCountry(
                countryCode, businessName);

            for (int i = 0; i < c.size(); i++) {
                String oemId = c.get(i).getOEMId();
                String country = oemId.substring(1, 4);

                Map order = mapper.convertValue(c.get(i), Map.class);
                order.remove("oemid");
                order.put("country", country);
                
                if (l2.get(oemId) == null) {
                    l2.put(oemId, new ArrayList());
                }
                l2.get(oemId).add(order);
            }

            if (l2.keySet().size() > 0) {
                cmap = l2;
            }

            res.put("users", users);
            res.put("companies", cmap);

            return res;

        }
        
        return res;
    }


    public Map <String, Object> getLicensesAndUsersWithRange(String orderId, int start, int range) {

        List<LicenseAndUser> licensesAndUsers = licenseCodeRepository.getLicensesAndUsersWithRange(orderId, start, range);

        List<LicenseCode> totaLicenseCodes = licenseCodeRepository.findByOrderID(orderId);
        
        Map < String, Object > res = new HashMap<>();

        res.put("licensesAndUsers", licensesAndUsers);
        res.put("start", start);
        res.put("size", licensesAndUsers.size());
        res.put("totaLicenseCodes", totaLicenseCodes.size());

        return res;
    }

    public Map <String, Object> getOrderDetail(String orderId) {
        
        ObjectMapper mapper = new ObjectMapper();

        Optional<OrderDetail> orderDetail = licenseCodeRepository.getOrderDetailByOrderId(orderId);
        
        Map < String, Object > res = new HashMap<>();

        res.put("orderDetail", mapper.convertValue(orderDetail.get(), Map.class));

        return res;
    }

    public String GenerateRetailLicense(LocalDateTime apiTriggerTime, int duration, int companion, Long shopifyOrderId) throws Exception {
        if (companion < 1) {
            throw new IllegalArgumentException("admin_license_invalidCompanion");
        }
        if (duration < 1) {
            throw new IllegalArgumentException("admin_license_invalidDuration");
        }

        String orderId = orderId(RETAIL_ORDER_ID_PREFIX, apiTriggerTime);
        OEM oemFromDb = oemService.GetRetail();
        String OEMId = oemFromDb.getOEMId();

        saveOrder(orderId, oemFromDb, companion, duration, 1, System.currentTimeMillis() / 1000L, "");

        ActivationLicenseCode code = ActivationLicenseCode.generateCode(
                OEMId, apiTriggerTime);
        List<LicenseCode> licenseCodes = new ArrayList<>();
        LicenseCode newLicenseCode = new LicenseCode(code.code(), orderId);
        licenseCodes.add(newLicenseCode);
        List<LicenseCode> licenseCodeEntities = licenseCodeRepository.saveAllAndFlush(licenseCodes);
        licenseStateService.generateLicenseStates(licenseCodeEntities, oemFromDb, Long.valueOf(7), companion, shopifyOrderId);

        return newLicenseCode.getLicenseCode();
    }

    public LicenseCode authenticateBeforeActivation(String licenseCode, Long userId) {
        LicenseCode code = licenseCodeRepository.findByLicenseCode(licenseCode);
        // user can re activate his license 
        // but a user can't activate other people's license
        // a user can't activate another new license either
        if (code == null) {
            throw new IllegalArgumentException("license_activate_licenseCodeNotFound");
        } 
        if (code.getUserId() != null && !code.getUserId().equals(userId)) {
            throw new IllegalUsingLicenseException("license_activate_codeHasBeenActivated", code.getUserId());
        }

        // simple validation
        if (!CodeVerifier.LicenseCodeVerify(licenseCode)) {
            throw new IllegalArgumentException("license_activate_invalidLicenseCode");
        }

        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("license_activate_invalidUserId");
        }

        List<LicenseCode> activeCodes = licenseCodeRepository.findByUserId(userId);

        if (!activeCodes.isEmpty() && !activeCodes.get(0).getLicenseCode().equals(code.getLicenseCode())) {
            throw new IllegalArgumentException("license_activate_userAlreadyHaveActiveCode");
        }


        code.setUserId(userId);

        return code;
    }
    
    @Transactional
    public HashMap<String, Object> ActivateRetailLCByApi(Long userId, String licenseCodeString, Date time) throws IllegalArgumentException, IllegalUsingLicenseException { 
        String licenseCode = convertInputLicenseCode(licenseCodeString);
        
        LicenseCode code = authenticateBeforeActivation(licenseCode, userId);

        if (code.getLicenseState().getActivatedAt() != null) {
            throw new IllegalArgumentException("license_activate_userReActivateCode");
        }

        if (code.getLicenseState().getState() != LicenseStateEnum.INACTIVE.valueOf()) {
            throw new IllegalArgumentException("license_activate_invalidActivation");
        }
        
        licenseCodeRepository.saveAndFlush(code);

        long utcTime = System.currentTimeMillis() / 1000L;
        LicenseState licenseState = code.getLicenseState();
        LicenseStateEnum newState = LicenseStateEnum.TRIAL;
        
        licenseStateService.activateLicense(licenseState, utcTime, newState);

        HashMap<String, Object> map = new HashMap<>();
        map.put("license_code", ActivationLicenseCode.convertToPrint(code.getLicenseCode()));
        map.put("created_at", licenseState.getCreatedAt());
        map.put("activated_at", utcTime);
        map.put("max_duration", licenseState.getMaxDuration());
        // map.put("companioned_times", licenseState.getCompanionedTimes());
        map.put("OEM_id", licenseState.getOEM().getOEMId());
        map.put("state", newState.valueOf());

        return map;
        
    }

    @Transactional
    public HashMap<String, Object> ActivateRetailLCByEvent(Long userId, String licenseCodeString, Date time, Long duration) throws IllegalArgumentException, IllegalUsingLicenseException { 
        String licenseCode = convertInputLicenseCode(licenseCodeString);
        LicenseCode code = authenticateBeforeActivation(licenseCode, userId);
        
        
        if (duration < 365L && (code.getLicenseState().getState() != LicenseStateEnum.INACTIVE.valueOf() || code.getLicenseState().getState() != LicenseStateEnum.TRIAL.valueOf())) {
            throw new IllegalArgumentException("license_activate_invalidLicenseState");
        }

        
        licenseCodeRepository.saveAndFlush(code);

        long utcTime = System.currentTimeMillis() / 1000L;
        LicenseState licenseState = code.getLicenseState();
        LicenseStateEnum newState = LicenseStateEnum.ACTIVE;
        
        
        licenseStateService.updateRetailLicense(licenseCode, duration, utcTime, newState.valueOf());

        HashMap<String, Object> map = new HashMap<>();
        map.put("license_code", ActivationLicenseCode.convertToPrint(code.getLicenseCode()));
        map.put("created_at", licenseState.getCreatedAt());
        map.put("activated_at", utcTime);
        map.put("max_duration", licenseState.getMaxDuration());
        // map.put("companioned_times", licenseState.getCompanionedTimes());
        map.put("OEM_id", licenseState.getOEM().getOEMId());
        map.put("state", newState.valueOf());

        return map;
    }

    @Transactional
    public HashMap<String, Object> ActivateLicenseCode(long userId, String licenseCodeString, Date time) throws IllegalArgumentException, IllegalUsingLicenseException {
        String licenseCode = convertInputLicenseCode(licenseCodeString);
        HashMap<String, Object> map = new HashMap<>();
        // simple validation
        if (!CodeVerifier.LicenseCodeVerify(licenseCode)) {
            throw new IllegalArgumentException("license_activate_invalidLicenseCode");
        }

        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("license_activate_invalidUserId");
        }
        LicenseCode code = licenseCodeRepository.findByLicenseCode(licenseCode);

        if (code.getOrderID().substring(0, 6).equals(RETAIL_ORDER_ID_PREFIX)) {

            return ActivateRetailLCByApi(userId, licenseCodeString, time);
        }

        List<LicenseCode> activeCodes = licenseCodeRepository.findByUserId(userId);
        if (!activeCodes.isEmpty()) {
            throw new IllegalArgumentException("license_activate_invalidUserId");
        }
        
        if (code == null) {
            throw new IllegalArgumentException("license_activate_invalidLicenseCode");
        } else if (code.getUserId() != null || code.getLicenseState().getState() != LicenseStateEnum.INACTIVE.valueOf()) {
            // someone else used this license code
            throw new IllegalUsingLicenseException("license_activate_invalidLicenseCode", code.getUserId());
        }

        code.setUserId(userId);
        licenseCodeRepository.saveAndFlush(code);

        long utcTime = System.currentTimeMillis() / 1000L;
        LicenseState licenseState = code.getLicenseState();
        long maxDuration = licenseState.getMaxDuration();
        licenseStateService.activateLicense(licenseState, utcTime, LicenseStateEnum.ACTIVE);
        
        map.put("license_code", ActivationLicenseCode.convertToPrint(code.getLicenseCode()));
        map.put("created_at", licenseState.getCreatedAt());
        map.put("activated_at", utcTime);
        map.put("max_duration", maxDuration);
        // map.put("companioned_times", licenseState.getCompanionedTimes());
        map.put("OEM_id", licenseState.getOEM().getOEMId());
        map.put("state", LicenseStateEnum.ACTIVE.valueOf());

        return map;
    }


    

    @Transactional
    public void ChangeCodeState(ChangeLicenseStateDTO requestBody, Date time) throws IllegalArgumentException {
        String licenseCode = convertInputLicenseCode(requestBody.licenseCode());

        // simple validation
        if (!CodeVerifier.LicenseCodeVerify(licenseCode)) {
            throw new IllegalArgumentException("admin_license_invalidLicenseCode");
        }

        LicenseCode code = licenseCodeRepository.findByLicenseCode(licenseCode);
        if (code == null) {
            throw new IllegalArgumentException("admin_license_invalidLicenseCode");
        }/* else if (licenseState.getState() == LicenseStateEnum.INACTIVE.valueOf()) {
            throw new IllegalArgumentException("wrong_code");
        }*/
        LicenseState licenseState = code.getLicenseState();
        if (licenseState.getState() == LicenseStateEnum.ACTIVE.valueOf() && requestBody.state()) {
            throw new IllegalArgumentException("admin_license_invalidLicenseCodeState");
        } else if (licenseState.getState() == LicenseStateEnum.SUSPEND.valueOf() && !requestBody.state()) {
            throw new IllegalArgumentException("admin_license_invalidLicenseCodeState");
        }

        long utcTime = time.getTime();
        LicenseStateEnum newState = LicenseStateEnum.SUSPEND;
        if (requestBody.state()) {
            newState = LicenseStateEnum.ACTIVE;
        }

        licenseStateService.changeState(licenseState, utcTime, newState);
    }

    public String GenerateOEMLicense(GenerateOEMLicenseDTO requestBody, LocalDateTime apiTriggerTime) throws Exception {
        // TODO: require login admin
        // simple validation
        String countryName = requestBody.getCountry();
        String companyName = requestBody.getCompany();
        int companion = requestBody.getCompanion();
        int duration = requestBody.getDuration();
        int amount = requestBody.getAmount();

        CountryCode cc = countryService.findCountryCode(countryName);
        if (cc == null) {
            throw new IllegalArgumentException("admin_licenses_invalidCountryCode");
        }
        String countryCodeForTag = cc.getShortNameLowerCase().substring(0, 3).toUpperCase();
        String orderId = orderId(
                String.format("%s%s", countryCodeForTag, companyName.substring(0, 3).toUpperCase()),
                apiTriggerTime);

        OEM oemFromDb = oemService.SearchOrCreateOEM(countryName, companyName);
        String OEMId = oemFromDb.getOEMId();

        saveOrder(orderId, oemFromDb, companion, duration, amount, System.currentTimeMillis() / 1000L, requestBody.getContract().trim());

        List<LicenseCode> licenseCodes = new ArrayList<>();
        ArrayList<ActivationLicenseCode> codes = ActivationLicenseCode.generateCode(
                OEMId, apiTriggerTime, amount);
        for(ActivationLicenseCode atkeyCode : codes) {
            LicenseCode code = new LicenseCode(atkeyCode.code(), orderId);
            licenseCodes.add(code);
        }
        List<LicenseCode> licenseCodeEntities = licenseCodeRepository.saveAllAndFlush(licenseCodes);
        licenseStateService.generateLicenseStates(licenseCodeEntities, oemFromDb, Long.valueOf(duration * DAYS_OF_ONE_YEAR), companion);

        return orderId;
    }

    public String LicenseCodeCsvFile(String orderId) throws Exception {
        if (orderId.startsWith(RETAIL_ORDER_ID_PREFIX)) {
            throw new IllegalArgumentException("admin_licenses_downloadCSVError");
        }

        // SQL injection?
        Order order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            throw new IllegalArgumentException("admin_licenses_orderNotFound");
        }

        List<LicenseCode> codes = licenseCodeRepository.findByOrderID(orderId);
        if (codes == null || codes.isEmpty()) {
            throw new IllegalArgumentException("admin_licenses_licenseNotFound");
        } else if (codes.size() != order.getAmount()) {
            throw new RuntimeException("admin_licenses_licenseCodeSizeNotMatch");
        }

        OEM oem = order.getOem();
        String alpha3 = oem.getOEMId().substring(1, 4);
        CountryCode cc = countryService.findCountryCode(alpha3);
        String orderCreateTime = orderId.substring(6);
        // information part
        StringBuilder csvFileContentBuilder = new StringBuilder();
        csvFileContentBuilder.append("******************,\n");
        csvFileContentBuilder.append(String.format("PO Number,%s\n", order.getContract()));
        csvFileContentBuilder.append(String.format("Create Time,%s\n", orderCreateTime));
        csvFileContentBuilder.append(String.format("Country,%s\n", cc.getShortNameLowerCase()));
        csvFileContentBuilder.append(String.format("Company Name,%s\n", oem.getBusinessName()));
        csvFileContentBuilder.append(String.format("Max. Companion Devices,%d\n", order.getMaxCompanionCounts()));
        csvFileContentBuilder.append(String.format("Validity Period,%d\n", order.getDuration()));
        csvFileContentBuilder.append(String.format("Number of codes,%d\n", order.getAmount()));
        csvFileContentBuilder.append("******************,\n");
        csvFileContentBuilder.append(",\nNo.,License\n");
        // license code part
        for(int idx = 0; idx < codes.size(); idx++) {
            LicenseCode code = codes.get(idx);
            csvFileContentBuilder.append(String.format("%d,%s\n", idx + 1, ActivationLicenseCode.convertToPrint(code.getLicenseCode()) ));
        }
        return csvFileContentBuilder.toString();
    }

    
    private void saveOrder(String orderId, OEM oem, int maxCompanionCounts, int duration, int amount, long create, String contract) {
        Order order = new Order(orderId, oem, maxCompanionCounts, duration, amount, create, contract);
        orderRepository.saveAndFlush(order);
    }

    private long getUTCTime(LocalDateTime localDateTime) {
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }

    private String orderId(String orderPrefix, LocalDateTime apiTriggerTime) {
        return String.format(
                "%s_%04d-%02d-%02d_%02d-%02d-%02d",
                orderPrefix, apiTriggerTime.getYear(), apiTriggerTime.getMonthValue(), apiTriggerTime.getDayOfMonth(),
                apiTriggerTime.getHour(), apiTriggerTime.getMinute(), apiTriggerTime.getSecond()
        );
    }

    private String convertInputLicenseCode(String licenseCode) {
        String[] codeParts = licenseCode.split("-");
        StringBuilder builder = new StringBuilder();
        for (String c : codeParts) {
            builder.append(c);
        }
        return builder.toString();
    }
}
