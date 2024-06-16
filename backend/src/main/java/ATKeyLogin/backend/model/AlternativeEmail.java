package ATKeyLogin.backend.model;

import jakarta.persistence.*;

import lombok.*;
import java.util.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(AlternativeEmailId.class)
@Data
@Table(name = "alternative_emails", uniqueConstraints={
    @UniqueConstraint(columnNames = {"user_id", "email"})
})
public class AlternativeEmail {


	@Id
	@ManyToOne()
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "user_id")
	private User user;

	@Id
	@Column(name = "email")
    private String email;

	public AlternativeEmail (String email) {
        this.email = email;
    }
    
}
