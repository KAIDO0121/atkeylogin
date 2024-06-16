package ATKeyLogin.backend.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CreateOEMDTO {
    @Size(min = 2, max = 3, message = "admin_oem_invalidCountryCode")
    private String countryCode;

    // company name from API request
    @Size(min = 3, message = "admin_add_oem_invalidCompanyName")
    private String companyName;
}
