package ATKeyLogin.backend.service;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.authentrend.iso3166.CountryCode;

@Service
public class CountryService {
    public ArrayList<CountryCode> findCountryCodesByName(String countryName) {
        return CountryCode.getCodeByName(countryName);
    }

    public CountryCode findCountryCodeByName(String countryName) {
        ArrayList<CountryCode> codes = CountryCode.getCodeByName(countryName);
        if (codes.isEmpty()) return null;
        return codes.get(0);
    }

    public String findCountryNameByCode(String countryCode) {
        CountryCode cc = CountryCode.getByCountryCode(countryCode);

        String countryName = "";
        if (cc != null) {
            countryName = cc.getShortNameLowerCase();
        }
        return countryName;
    }

    public CountryCode findCountryCode(String countryCode) {
        return CountryCode.getByCountryCode(countryCode);
    }
}
