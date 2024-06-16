package ATKeyLogin.backend.domain;

import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateLicenseDTO {
    @Min(value = 1, message = "admin_licenseState_invalidMaxDuration")
    Long maxDuration;

    @Min(value = 973838636, message = "admin_licenseState_invalidCreateAt")
    Long createdAt;
    
    @Min(value = 973838636, message = "admin_licenseState_invalidActivateAt")
    Long activatedAt;
}
