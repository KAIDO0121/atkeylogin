package ATKeyLogin.backend.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GenerateOEMLicenseDTO {
    // country name from API request
    @Size(min = 2, max = 3, message = "admin_licenses_invalidCountryCode")
    private String country;

    // company name from API request
    @Size(min = 3, message = "admin_licenses_invalidCompanyName")
    private String company;

    // max device-account companion number from API request
    @Min(value = 1, message = "admin_licenses_invalidCompanion")
    private int companion;

    // license active year from API request
    @Min(value = 1, message = "admin_licenses_invalidDuration")
    private int duration;

    // how many license code to generate from API request
    @Min(value = 1, message = "admin_licenses_invalidAmount")
    private int amount;

    // if real contract was made, put in so there is information to trace back.
    @NotNull(message = "admin_licenses_invalidContract")
    private String contract;
}
