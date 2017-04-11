package it.polimi.moscowmule.neighborhoodsecurity.authentication;

import javax.xml.bind.annotation.XmlRootElement;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.ProjectConstants;

@XmlRootElement
public class AuthToken {
	private String authToken;
	private int userId;
	private String username;
	private String userUrl;

	public AuthToken() {

	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
		this.userUrl = ProjectConstants.USERS_BASE_URL + "/" + userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserUrl() {
		return userUrl;
	}

	public void setUserUrl(String userUrl) {
		this.userUrl = userUrl;
	}

}
