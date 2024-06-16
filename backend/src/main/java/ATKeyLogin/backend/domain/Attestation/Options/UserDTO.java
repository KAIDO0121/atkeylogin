package ATKeyLogin.backend.domain.Attestation.Options;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern.Flag;
import lombok.Data;

@Data
public class UserDTO {

    @NotEmpty(message = "attestation_options_userNameRequired")
    @Email(message = "attestation_options_invalidUserName", flags = { Flag.CASE_INSENSITIVE })
    private String name;

    @NotEmpty(message = "attestation_options_displayNameRequired")
    @Email(message = "attestation_options_invalidDisplayName", flags = { Flag.CASE_INSENSITIVE })
    private String displayName;


}
