package it.polimi.moscowmule.neighborhoodsecurity.authentication;

public class Authenticator {
	public static int getUserId(String authToken){
		return 1;
	}

	public static boolean isSuperuser(int userId) {
		return false;
	}
}
