package ATKeyLogin.backend.service;
import org.springframework.stereotype.Service;

import ATKeyLogin.backend.dao.LicenseStateDAO;
import ATKeyLogin.backend.dao.LicenseStateDAO.LicenseAndUser;
import ATKeyLogin.backend.dao.LicenseStateDAO.LicenseStateInfo;
import ATKeyLogin.backend.dao.LicenseStateLogDAO;
import ATKeyLogin.backend.model.*;
import ATKeyLogin.backend.model.exception.BusinessLogicException;

import org.springframework.context.annotation.Lazy;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class LicenseStateService {
    private static final Logger log = LoggerFactory.getLogger(LicenseStateService.class);

    private LicenseStateDAO licenseStateRepository;


    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private DeviceService deviceService;

    @Autowired
    private LicenseStateLogDAO licenseStateLogRepository;

    public LicenseStateService(
        LicenseStateDAO licenseStateRepository
    ) {
        this.licenseStateRepository = licenseStateRepository;
    }

    public Optional<LicenseAndUser> getLicenseAndUserByEmail(String email) {
        return licenseStateRepository.getLicenseAndUserByEmail(email);
    }

    public Optional<LicenseAndUser> getLicenseAndUserByUId(long userId) {
        return licenseStateRepository.getLicenseAndUserByUId(userId);
    }

    public Optional<LicenseStateInfo> getLicenseStateInfoByUserId(long userId) {
        return licenseStateRepository.getLicenseStateInfoByUserId(userId);
    }

    public void updateLicenseStateByUserId(Long userId) {
        licenseStateRepository.updateLicenseStateByUserId(userId);
    }

    public Optional<LicenseStateInfo> getLicenseStateInfoByEmail(String email) {
        return licenseStateRepository.getLicenseStateInfoByEmail(email);
    }


    public int adminUpdateLicense(String licenseCode, Long maxDuration, Long createdAt, Long activatedAt) {
        return licenseStateRepository.adminUpdateLicense(licenseCode, maxDuration, createdAt, activatedAt);
    }

    public int updateRetailLicense(String licenseCode, Long maxDuration, Long activatedAt, int state) {
        return licenseStateRepository.updateRetailLicense(licenseCode, maxDuration, activatedAt, state);
    }

    public int deleteByShopifyOrderId(Long shopifyOrderId) {
        return licenseStateRepository.deleteByShopifyOrderId(shopifyOrderId);
    }
    


    // public int updateCompanionedTimes(int cnt, long licenseStateId) {
    //     return licenseStateRepository.updateCompanionedTimes(cnt, licenseStateId);
    // }

    public LicenseStateInfo windowsloginLicenseValidation(Optional<LicenseStateInfo> info) throws BusinessLogicException{

        if (!info.isPresent()) {
            throw new BusinessLogicException("assertion_companion_licenseNotFound");
        }
        
        Long duration = info.get().getMaxDuration();

        if (duration * 86400 + info.get().getActivatedAt() < System.currentTimeMillis() / 1000L) {
            throw new BusinessLogicException("assertion_companion_licenseExpired");
        }

        if (!info.get().getState().equals(LicenseStateEnum.TRIAL.valueOf()) &&
                !info.get().getState().equals(LicenseStateEnum.ACTIVE.valueOf())) {
            throw new BusinessLogicException("assertion_companion_licenseNotActivated");
        }

        return info.get();

    }

    public LicenseStateInfo companionLicenseValidation(Optional<LicenseStateInfo> info) throws BusinessLogicException{

        if (!info.isPresent()) {
            throw new BusinessLogicException("assertion_companion_licenseNotFound");
        }

        if (info.get().getCompanionedTimes() + 1 > info.get().getMaxCompanionCounts()) {
            throw new BusinessLogicException("assertion_companion_maxCompanionCountsExceeded");
        }

        Long duration = info.get().getMaxDuration();

        if (duration * 86400 + info.get().getActivatedAt() < System.currentTimeMillis() / 1000L) {
            throw new BusinessLogicException("assertion_companion_licenseExpired");
        }

        if (!info.get().getState().equals(LicenseStateEnum.TRIAL.valueOf()) &&
                !info.get().getState().equals(LicenseStateEnum.ACTIVE.valueOf())) {
            throw new BusinessLogicException("assertion_companion_licenseNotActivated");
        }

        return info.get();

    }

    public void generateLicenseStates(List<LicenseCode> codes, OEM oem, Long maxDuration, int maxCompanionCounts, Long ...shopifyOrderId) {
        Long shop = shopifyOrderId.length > 0 ? shopifyOrderId[0] : null;
        List<LicenseState> stateList = new ArrayList<>();
        List<LicenseStateLog> logList = new ArrayList<>();
        for(LicenseCode code: codes) {
            Date date = new Date();
            LicenseState state = new LicenseState(
                    System.currentTimeMillis() / 1000L,
                    code,
                    oem,
                    maxDuration,
                    maxCompanionCounts,
                    LicenseStateEnum.INACTIVE,
                    shop);
            stateList.add(state);
            LicenseStateLog log = new LicenseStateLog(
                    System.currentTimeMillis() / 1000L,
                    (long) 0,
                    maxDuration,
                    maxCompanionCounts,
                    oem,
                    LicenseStateEnum.INACTIVE,
                    state);
            logList.add(log);
        }
        licenseStateRepository.saveAllAndFlush(stateList);
        licenseStateLogRepository.saveAllAndFlush(logList);
    }

    public void activateLicense(LicenseState licenseState, Long time, LicenseStateEnum newState) {
        licenseState.setActivatedAt(time);
        licenseState.setState(newState);
        LicenseState saveState = licenseStateRepository.saveAndFlush(licenseState);
        saveNewLog(saveState, time, newState);
    }

    public void changeState(LicenseState licenseState, Long time, LicenseStateEnum newState) {
        licenseState.setState(newState);
        LicenseState saveState = licenseStateRepository.saveAndFlush(licenseState);
        saveNewLog(saveState, time, newState);
    }

    private void saveNewLog(LicenseState licenseState, Long createAt, LicenseStateEnum newState) {
        LicenseStateLog log = new LicenseStateLog(
                createAt,
                licenseState.getActivatedAt(),
                licenseState.getMaxDuration(),
                licenseState.getMaxCompanionCounts(),
                licenseState.getOEM(),
                newState,
                licenseState);
        licenseStateLogRepository.saveAndFlush(log);
    }
}
