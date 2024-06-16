package ATKeyLogin.backend.domain.Attestation.Options;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttestationParamsDTO {
    @Valid
    @NotNull(message = "attestation_options_paramRequired")
    private AttestationOptionsDTO params;
}
