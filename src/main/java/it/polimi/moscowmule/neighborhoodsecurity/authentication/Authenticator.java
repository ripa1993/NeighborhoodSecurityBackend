package it.polimi.moscowmule.neighborhoodsecurity.authentication;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.ProjectConstants;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.database.Database;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.AuthorizationDBException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.NoUserFoundException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.SecretDBException;

public class Authenticator {
	/**
	 * Retrieves user id given the token
	 * 
	 * @param authToken
	 * @return the user is
	 * @throws AuthorizationDBException
	 * @throws NoUserFoundException
	 *             if the authToken is of no user
	 */
	public static int getUserId(String authToken) throws AuthorizationDBException, NoUserFoundException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement(
					"SELECT ID FROM gsx95369n3oh2zo6.authorization WHERE TOKEN = ? AND ISVALID = 1",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setString(1, authToken);
			ResultSet result = getStmt.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			} else {
				// no match
				throw new NoUserFoundException();
			}

		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new AuthorizationDBException("ERROR when finding user id from token", e);
		}
	}

	/**
	 * Check if an id is superuser
	 * 
	 * @param id
	 *            of the user
	 * @return the status
	 * @throws NoUserFoundException
	 *             if the id is of no user
	 * @throws AuthorizationDBException
	 */
	public static boolean isSuperuser(int id) throws NoUserFoundException, AuthorizationDBException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement(
					"SELECT SUPERUSER FROM gsx95369n3oh2zo6.authorization WHERE ID = ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
			ResultSet result = getStmt.executeQuery();
			if (result.next()) {
				if (result.getInt(1) > 0) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new NoUserFoundException();
			}
		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new AuthorizationDBException("ERROR when finding if user is superuser", e);
		}
	}

	/**
	 * Checks if a username-password pair match a valid user id
	 * 
	 * @param username
	 * @param password
	 * @return the id of the user, if there's a match
	 * @throws NoUserFoundException
	 *             if no match
	 * @throws SecretDBException
	 */
	public static int checkPassword(String username, String password) throws NoUserFoundException, SecretDBException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement(
					"SELECT ID FROM gsx95369n3oh2zo6.users JOIN gsx95369n3oh2zo6.secret WHERE USERNAME = ? AND PASSWORD = ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setString(1, username);
			getStmt.setString(2, password);
			ResultSet result = getStmt.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			} else {
				// wrong pair
				throw new NoUserFoundException();
			}
		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new SecretDBException("ERROR when checking username and password", e);
		}
	}

	/**
	 * Generates a new token for the given user id
	 * 
	 * @param id
	 *            the user id
	 * @return the generated token
	 * @throws AuthorizationDBException
	 */
	public static String generateToken(int id) throws AuthorizationDBException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement updStmt = connection
					.prepareStatement("UPDATE gsx95369n3oh2zo6.authorization SET TOKEN = ?, ISVALID = 1 WHERE ID = ?");
			String token = UUID.randomUUID().toString();
			updStmt.setString(1, token);
			updStmt.setInt(2, id);
			updStmt.executeUpdate();

			return token;

		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new AuthorizationDBException("ERROR when creating token", e);
		}

	}

	/**
	 * Invalidates the token of the user
	 * 
	 * @param id
	 *            of the user
	 * @return true if all is ok
	 * @throws AuthorizationDBException
	 */
	public static boolean invalidateToken(int id) throws AuthorizationDBException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement updStmt = connection
					.prepareStatement("UPDATE gsx95369n3oh2zo6.authorization SET ISVALID = 0 WHERE ID = ?");
			updStmt.setInt(1, id);
			updStmt.executeUpdate();

			return true;

		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new AuthorizationDBException("ERROR when invalidating token", e);
		}
	}

	public static boolean isServiceKeyValid(String serviceKey) {
		if (ProjectConstants.SERVICE_KEYS.contains(serviceKey))
			return true;
		return false;
	}
	
	public static boolean isAuthTokenValid(String authToken){
		try {
			getUserId(authToken);
			return true;
		} catch (AuthorizationDBException | NoUserFoundException e) {
			return false;
		}
	}
}
