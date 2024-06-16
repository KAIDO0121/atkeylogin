package ATKeyLogin.backend.domain.Assertion.Options;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AssertionOptionsDTO {
    @Pattern(regexp = "^(preferred|required|discouraged)$", message = "assertion_options_invalidUV")
    @NotEmpty(message = "assertion_options_UVRequired")
    private String userVerification;
}
