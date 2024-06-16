package ATKeyLogin.backend.domain.Assertion.Results;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompaionRequestDTO extends AssertionRequestDTO{


    @NotBlank(message = "assertion_companion_userSIDIsRequired")
    private String userSID;

    @NotBlank(message = "assertion_companion_deviceNameIsRequired")
    private String deviceName;

    @NotBlank(message = "assertion_companion_userNameIsRequired")
    private String userNameOnDevice;

}

