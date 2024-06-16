package ATKeyLogin.backend.model;

import java.io.Serializable;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("UserTemp")
public class UserTemp implements Serializable{

	private String email;

	private String authorities;

	private int veriCode;

	public UserTemp(String email, String authorities) {
		this.email = email;
		this.authorities = authorities;
	}

    public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

    public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public String getAuthorities() {
		return authorities;
	}

	public void setVeriCode(int veriCode) {
		this.veriCode = veriCode;
	}

	public int getVeriCode() {
		return veriCode;
	}

}
