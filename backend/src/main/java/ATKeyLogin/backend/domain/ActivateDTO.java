package ATKeyLogin.backend.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateDTO {
    @NotBlank(message = "invalid_license_code")
    String licenseCode;
}
