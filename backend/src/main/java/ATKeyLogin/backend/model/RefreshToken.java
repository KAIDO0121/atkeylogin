package ATKeyLogin.backend.model;

import java.io.Serializable;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("RefreshToken")
public class RefreshToken implements Serializable{

	// private Instant expiryDate;

	private String token;

	private String userId;

	private String email;

	public RefreshToken() {

	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

}
