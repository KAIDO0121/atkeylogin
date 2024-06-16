package ATKeyLogin.backend.model.error.Attestation.RefreshToken;
import java.util.*;

import lombok.Data;

@Data
public class RefreshTokenError {
    static final Map<String, String> keyToMsg ;

    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("attestation_refresh_invalidRefreshToken", "Invalid refresh token");
        m.put("attestation_refresh_refreshTokenRequired", "Refresh token is required");

        keyToMsg = Collections.unmodifiableMap(m);
    }
}
