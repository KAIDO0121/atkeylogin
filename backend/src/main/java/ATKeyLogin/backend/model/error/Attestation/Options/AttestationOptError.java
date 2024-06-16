package ATKeyLogin.backend.model.error.Attestation.Options;
import java.util.*;

import lombok.Data;

@Data
public class AttestationOptError {
    static final Map<String, String> keyToMsg ;

    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("attestation_options_invalidAuthorities", "Invalid authorities");
        m.put("attestation_options_authoritiesRequired", "Authorities is required.");
        m.put("attestation_options_emailRequired", "The email address is required.");
        m.put("attestation_options_invalidEmail", "The email address is invalid.");
        m.put("attestation_options_invalidAuthenticatorAttachment", "AuthenticatorAttachment is invalid");

        m.put("attestation_options_authenticatorAttachmentRequired", "AuthenticatorAttachment is required");
        m.put("attestation_options_RequireResidentKeyError", "RequireResidentKey must set to true");
        m.put("attestation_options_invalidUserVerification", "Invalid userVerification");
        m.put("attestation_options_userVerificationRequired", "UserVerification is required.");

        m.put("attestation_options_authFiReturnError", "AuthFi service error");

        
        //  
        
        keyToMsg = Collections.unmodifiableMap(m);
    }
}
