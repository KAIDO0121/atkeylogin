package ATKeyLogin.backend.domain.Assertion.Results;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WindowsLoginRequestDTO extends AssertionRequestDTO{
    
}
