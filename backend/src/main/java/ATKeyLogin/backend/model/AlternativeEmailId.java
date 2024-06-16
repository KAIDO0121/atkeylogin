package ATKeyLogin.backend.model;

import java.io.Serializable;
import lombok.*;

import jakarta.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AlternativeEmailId implements Serializable {
    private User user;
    private String email;


    public AlternativeEmailId(String email) {
        this.email = email;
    }

}