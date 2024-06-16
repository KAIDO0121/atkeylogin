package ATKeyLogin.backend.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeLicenseStateDTO(
    @NotBlank(message = "license_activate_invalidLicenseCode") String licenseCode, 
    @NotNull(message = "license_activate_invalidState") Boolean state) {
}
