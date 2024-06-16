package ATKeyLogin.backend.domain.Request;

import org.springframework.http.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthFiRequest {

    private final Map body;

    private HttpHeaders headers;

    
    // private String AuthFiApiPath;   

    public AuthFiRequest(Map body, String AuthFiApiPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        headers.add("AT-X-KEY", AuthFiApiPath);
        this.headers = headers;
        this.body = body;
    }

    public HttpEntity<?> getRequest() {
        HttpEntity<?> req = new HttpEntity<>(body, headers);
        return req;
    }
}
