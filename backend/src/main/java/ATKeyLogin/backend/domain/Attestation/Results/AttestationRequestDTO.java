package ATKeyLogin.backend.domain.Attestation.Results;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttestationRequestDTO {
    @NotNull
    @Valid
    private FidoSignUpResponseDTO fido_register_response;
}
