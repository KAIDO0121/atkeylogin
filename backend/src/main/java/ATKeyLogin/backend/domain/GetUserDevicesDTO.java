package ATKeyLogin.backend.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetUserDevicesDTO {
    
    @Min(value = 0L, message = "user_getDevice_userIdInvalid")
    private long userId;
}
