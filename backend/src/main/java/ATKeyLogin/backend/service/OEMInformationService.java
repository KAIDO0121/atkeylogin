package ATKeyLogin.backend.service;

import ATKeyLogin.backend.dao.OEMDAO;
import ATKeyLogin.backend.model.OEM;
import com.authentrend.iso3166.CountryCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class OEMInformationService {
    public static final String RETAIL_OEM_ID = "00000001";

    @Autowired
    private CountryService countryService;

    @Autowired
    private OEMDAO oemRepository;

    private boolean redisVerification = false;

    public HashMap<String, Object> GetOemData() {
        // access data from database
        // TODO: maybe we can access from redis cache
        HashMap<String, Object> oemInfo = new HashMap<>();
        HashMap<String, CountryCode> codes = new HashMap<>();
        List<OEM> oems = oemRepository.findAll();
        for (OEM oem: oems) {
            String alpha3Code = oem.getOEMId().substring(1, 4);
            String alpha2Code = "";
            if (codes.containsKey(alpha3Code)) {
                alpha2Code = codes.get(alpha3Code).getAlpha2();
            } else {
                CountryCode code = countryService.findCountryCode(alpha3Code);
                if (code != null && alpha3Code!= null) {
                    codes.put(alpha3Code, code);
                    alpha2Code = code.getAlpha2();
                }
                
            }

            ArrayList<String> companies = new ArrayList<>();
            if (alpha2Code.length() > 1) {
                if (oemInfo.containsKey(alpha2Code)) {
                companies = (ArrayList<String>) oemInfo.get(alpha2Code);
                } else {
                    companies = new ArrayList<>();
                    oemInfo.put(alpha2Code, companies);
                }

            }
            
            companies.add(oem.getBusinessName());
        }
        return oemInfo;
    }

    public OEM createNewOEM(String countryCode, String companyName) throws Exception {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException("admin_oem_invalidCountryCode");
        } else if (countryCode.length() != 2 && countryCode.length() != 3) {
            throw new IllegalArgumentException("admin_oem_invalidCountryCode");
        }
        if (companyName == null || companyName.isBlank()) {
            throw  new IllegalArgumentException("admin_oem_invalidCompanyName");
        }

        CountryCode code = countryService.findCountryCode(countryCode);
        if (code == null) {
            throw new IllegalArgumentException("admin_oem_invalidCountryCode");
        }

        // TODO: redis get data size and save to redis
        int countryIndex = 1;
        String oemIdPrefix = String.format("0%s", code.getAlpha3());
        Example<OEM> searchExample = oemIdSearchExample(oemIdPrefix);
        List<OEM> oems = oemRepository.findAll(searchExample);
        if (!oems.isEmpty()) {
            int lastIndex = oems.size() - 1;
            countryIndex = Integer.parseInt(oems.get(lastIndex).getOEMId().substring(4), 16) + 1;
        }

        String oemId = String.format("0%s%04X", code.getAlpha3(), countryIndex);
        OEM oem = createOEM(oemId, companyName);
//        System.out.println("create OEM: " + oem);

        return oem;
    }

    public OEM SearchByCountryAndName(String countryCode, String companyName) throws Exception {
        if (countryCode == null || countryCode.isBlank()) {
            throw new IllegalArgumentException("admin_oem_invalidCountryCode");
        } else if (countryCode.length() != 2 && countryCode.length() != 3) {
            throw new IllegalArgumentException("admin_oem_invalidCountryCode");
        }
        if (companyName == null || companyName.isBlank()) {
            throw  new IllegalArgumentException("admin_oem_invalidCompanyCode");
        }

        // TODO: if redis not find, return null
        // redisDataInit();

        CountryCode code = countryService.findCountryCode(countryCode);
        if (code == null) {
            throw new IllegalArgumentException("admin_oem_invalidCompanyCode");
        }
        String oemIdPrefix = String.format("0%s", code.getAlpha3());

        Example<OEM> example = oemIdAndCompanyNameSearchExample(oemIdPrefix, companyName);
        OEM oem = null;
        List<OEM> oemList = oemRepository.findAll(example);
//        System.out.printf("[DEBUG] oem size %d\n", oemList.size());
        if (oemList.size() > 0) {
            oem = oemList.get(0);
        }

        return oem;
    }

    public OEM SearchOrCreateOEM(String countryCode, String companyName) throws Exception {
        OEM oem;
        oem = SearchByCountryAndName(countryCode, companyName);
        if (oem == null) {
            oem = createNewOEM(countryCode, companyName);
        }

        return oem;
    }

    public OEM GetRetail() {
        OEM oem = oemRepository.findByOEMId(RETAIL_OEM_ID);
        if (oem == null) {
//            System.out.println("not find retail oem, create one");
            oem = createOEM(RETAIL_OEM_ID, "Retail");
        } /*else {
            System.out.println("find retail oem");
        }*/

        return oem;
    }

    public OEM GetOemById(String oemId) {
        return oemRepository.findByOEMId(oemId);
    }

    private void redisDataInit() {
        if (redisVerification) return;

        // TODO: handle data in redis
        List<OEM> oems = oemRepository.findAll();
        HashMap<String, String> ccMap = new HashMap<>();
        HashMap<String, ArrayList> companyMap = new HashMap<>();
        for(OEM oem: oems) {
            String alpha3Code = oem.getOEMId().substring(1, 4);
            String alpha2Code;
            if (ccMap.containsKey(alpha3Code)) {
                alpha2Code = ccMap.get(alpha3Code);
            } else {
                CountryCode code = countryService.findCountryCode(alpha3Code);
                alpha2Code = code.getAlpha2();
                ccMap.put(alpha3Code, alpha2Code);
            }

            ArrayList<String> companyList;
            if (companyMap.containsKey(alpha2Code)) {
                companyList = companyMap.get(alpha2Code);
            } else {
                companyList = new ArrayList<>();
                companyMap.put(alpha2Code, companyList);
            }

            companyList.add(oem.getBusinessName());
        }

        // TODO: save to redis

        redisVerification = true;
    }

    private Example<OEM> oemIdSearchExample(String oemIdPrefix) {
        OEM oem = new OEM(oemIdPrefix);
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withMatcher("OEMId", ExampleMatcher.GenericPropertyMatchers.startsWith());
        return Example.of(oem, matcher);
    }

    private Example<OEM> oemIdAndCompanyNameSearchExample(String oemIdPrefix, String companyName) {
        OEM oem = new OEM(oemIdPrefix, companyName);
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withMatcher("OEMId", ExampleMatcher.GenericPropertyMatchers.startsWith())
            .withMatcher("businessName", ExampleMatcher.GenericPropertyMatchers.ignoreCase());
        return Example.of(oem, matcher);
    }

    private OEM createOEM(String oemId, String companyName) {
        Date date = new Date();
        OEM oemData = new OEM(oemId, Long.valueOf(date.getTime()), companyName);
        OEM dataRecord = oemRepository.saveAndFlush(oemData);
        return dataRecord;
    }
}
