package ATKeyLogin.backend.domain.Assertion.Results;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssertionRequestDTO {
    @NotNull
    @Valid
    private FidoLoginResponseDTO fido_login_response;

    public AssertionRequestDTO(FidoLoginResponseDTO fido_login_response) {
        this.fido_login_response = fido_login_response;
    }
}

