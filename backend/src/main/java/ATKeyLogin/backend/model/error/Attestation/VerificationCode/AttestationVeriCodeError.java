package ATKeyLogin.backend.model.error.Attestation.VerificationCode;
import java.util.*;

import lombok.Data;

@Data
public class AttestationVeriCodeError {
    static final Map<String, String> keyToMsg ;

    static {
        Map<String, String> m = new HashMap<String, String>();
        m.put("attestation_veri_emailRequired", "The email address is required.");
        m.put("attestation_veri_emailInvalid", "The email address is invalid.");

        m.put("attestation_veri_veriCodeTooSmall", "veriCode should not be less than 100000");
        m.put("attestation_veri_veriCodeTooBig", "veriCode should not be greater than 999999");
        
        
        keyToMsg = Collections.unmodifiableMap(m);
    }
}
