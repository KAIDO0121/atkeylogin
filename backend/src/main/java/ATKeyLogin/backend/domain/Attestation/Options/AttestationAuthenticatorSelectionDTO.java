package ATKeyLogin.backend.domain.Attestation.Options;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AttestationAuthenticatorSelectionDTO {
    
    @Pattern(regexp = "^(cross-platform|platform)$", message = "attestation_options_invalidAuthenticatorAttachment")
    @NotEmpty(message = "attestation_options_invalidAuthenticatorAttachment")
    private String authenticatorAttachment;

    @AssertTrue(message = "attestation_options_RequireResidentKeyError")
    private Boolean requireResidentKey;

    @Pattern(regexp = "^(preferred|required|discouraged)$", message = "attestation_options_invalidUserVerification")
    @NotEmpty(message = "attestation_options_userVerificationRequired")
    private String userVerification;

    // @Pattern(regexp = "^(preferred|required|discouraged)$", message = "Invalid residentKey")
    // @NotEmpty(message = "ResidentKey is required.")
    // private String residentKey;

}
