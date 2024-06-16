package ATKeyLogin.backend.domain.Assertion.Results;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FidoLoginResponseDTO {

    @NotBlank(message = "assertion_result_authenticator_idMissing")
    private String id;

    @NotBlank(message = "assertion_result_authenticator_rawIdMissing")
    private String rawId;

    @NotBlank(message = "assertion_result_authenticator_typeMissing")
    @Pattern(regexp = "^(public-key)$", message = "assertion_result_authenticator_invalidType")
    private String type;

    @NotNull
    @Valid
    private AssertionResponseDTO response;
}
