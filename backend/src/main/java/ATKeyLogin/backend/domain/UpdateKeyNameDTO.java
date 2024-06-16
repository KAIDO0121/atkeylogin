package ATKeyLogin.backend.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern.Flag;
import lombok.Data;


@Data
public class UpdateKeyNameDTO {
    @NotEmpty(message = "user_authenticator_nameRequired")
    private String name;
}
