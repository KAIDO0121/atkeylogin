package ATKeyLogin.backend.domain.Attestation.Results;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class FidoSignUpResponseDTO {
    @NotBlank(message = "attestation_result_idRequired")
    private String id;

    @NotBlank(message = "attestation_result_rawIdRequired")
    private String rawId;

    @NotBlank(message = "attestation_result_typeRequired")
    @Pattern(regexp = "^(public-key)$", message = "attestation_result_invalidType")
    private String type;

    @NotNull
    @Valid
    private AttestationResponseDTO response;
}
