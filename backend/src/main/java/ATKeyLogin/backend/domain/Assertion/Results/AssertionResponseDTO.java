package ATKeyLogin.backend.domain.Assertion.Results;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssertionResponseDTO {
    @NotBlank(message = "assertion_result_authenticator_authenticatorDataMissing")
    private String authenticatorData;

    @NotBlank(message = "assertion_result_authenticator_signatureMissing")
    private String signature;

    @NotBlank(message = "assertion_result_authenticator_clientDataJSONMissing")
    private String clientDataJSON;

    private String userHandle;
}
