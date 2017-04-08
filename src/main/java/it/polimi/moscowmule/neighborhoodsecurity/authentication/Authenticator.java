package it.polimi.moscowmule.neighborhoodsecurity.authentication;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.database.Database;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.AuthorizationDBException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.NoUserFoundException;

public class Authenticator {
	public static int getUserId(String authToken) throws AuthorizationDBException, NoUserFoundException{
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement("SELECT ID FROM gsx95369n3oh2zo6.authorization WHERE TOKEN = ?",ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setString(1, authToken);
			ResultSet result = getStmt.executeQuery();
			if(result.next()){
				return result.getInt(1);
			} else {
				// no match
				throw new NoUserFoundException();
			}

		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new AuthorizationDBException("ERROR when finding user id from token", e);
		}
	}

	public static boolean isSuperuser(int id) throws NoUserFoundException, AuthorizationDBException {
		Connection connection;
		try{
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement("SELECT SUPERUSER FROM gsx95369n3oh2zo6.authorization WHERE ID = ?",ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
			ResultSet result = getStmt.executeQuery();
			if(result.next()){
				if (result.getInt(1)>0){
					return true;
				} else {
					return false;
				}
			} else {
				throw new NoUserFoundException();
			}
		} catch(ClassNotFoundException | URISyntaxException | SQLException e){
			throw new AuthorizationDBException("ERROR when finding if user is superuser", e);
		}
	}
}
