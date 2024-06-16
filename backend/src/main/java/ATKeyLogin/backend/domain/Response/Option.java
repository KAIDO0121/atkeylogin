package ATKeyLogin.backend.domain.Response;

import java.util.*;

import lombok.Data;

@Data
public class Option {

    public int code;

    @Data
    public static class fido_register_request {

        @Data
        private class rp {
            private String name;
            private String id;
        }

        @Data
        private class User {
            private String id;
            private String displayName;
            private String name;
        }

        private String challenge;

        private List<?> excludeCredentials;

        @Data
        private class pubKeyCredParams {
            private String type;
            private int alg;
        }

        private String timeout;

        private Object extensions;

        @Data
        private class authenticatorSelection{
            private String authenticatorAttachment;
            private Boolean requireResidentKey;
            private String userVerification;
        };

        private String attestation;
    }

    
    
}
