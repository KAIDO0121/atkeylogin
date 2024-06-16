package ATKeyLogin.backend.domain;

import lombok.Data;
import java.util.*;


@Data
public class FidoKeyInfoRes {

    private Map<String, Object> key_info;

    public FidoKeyInfoRes(Map<String, Object> keyInfo) {
        this.key_info = keyInfo;
        this.key_info.put("keyId", keyInfo.get("credential_id"));
        this.key_info.remove("credential_id");
        this.key_info.put("create_time", (Long) keyInfo.get("create_time") / 1000);
        this.key_info.put("update_time", (Long) keyInfo.get("update_time") / 1000);
    }
}
