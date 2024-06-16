package ATKeyLogin.backend.domain.Attestation.Options;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AttestationOptionsDTO {
    @Valid
    @NotNull(message = "attestation_options_paramRequired")
    private UserDTO user;

    @Valid
    @NotNull(message = "attestation_options_selectionRequired")
    private AttestationAuthenticatorSelectionDTO authenticatorSelection;

    @Pattern(regexp = "^(direct|indirect|none)$", message = "attestation_options_invalidAttestation")
    @NotBlank(message = "attestation_options_attestationRequired")
    private String attestation;
}
