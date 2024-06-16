package ATKeyLogin.backend.domain.Attestation.Results;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttestationResponseDTO {

    @NotBlank(message = "attestation_result_attestationObjectRequired")
    private String attestationObject;

    @NotBlank(message = "attestation_result_clientDataJSONRequired")
    private String clientDataJSON;
}
