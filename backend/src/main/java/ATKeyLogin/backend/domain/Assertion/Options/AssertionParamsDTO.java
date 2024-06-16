package ATKeyLogin.backend.domain.Assertion.Options;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssertionParamsDTO {
    @Valid
    @NotNull(message = "assertion_options_invalidAssertionParams")
    private AssertionOptionsDTO params;
}
