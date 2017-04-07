package it.polimi.moscowmule.neighborhoodsecurity.utilities;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.polimi.moscowmule.neighborhoodsecurity.user.User;

public class DatabaseUsers {

	public static int createUser(User u) {
		System.out.println("[DB] Beginning user creation "+u);
		Connection connection;
		try {
			connection = Database.getConnection();
			System.out.println("[DB] Connection established");
			PreparedStatement createStmt = connection.prepareStatement("INSERT INTO gsx95369n3oh2zo6.users (USERNAME, EMAIL, CREATED) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS);
			createStmt.clearParameters();
			createStmt.setString(1, u.getUsername());
			createStmt.setString(2, u.getEmail());
			createStmt.setDate(3, new Date(System.currentTimeMillis()));
			System.out.println("[DB] Statement prepared for "+u);
			createStmt.executeUpdate();
			System.out.println("[DB] Update executed for "+u);
			ResultSet results = createStmt.getGeneratedKeys();
			int id = -1;
			if (results.next()) {
				id = results.getInt(1);
				System.out.println("[DB] User "+u+" id is "+id);
			}else{
				return -1;
			}
						
			System.out.println("[DB] Beginning user authorization creation for "+u);
			PreparedStatement createStmt2 = connection.prepareStatement("INSERT INTO gsx95369n3oh2zo6.authorization (USERID, SUPERUSER, TOKEN) VALUES (?,?,?)");
			System.out.println("[DB] Authorization statement prepared for "+u);
			createStmt2.clearParameters();
			createStmt2.setInt(1, id);
			createStmt2.setInt(2, 0);
			createStmt2.setString(3, "");
			createStmt2.executeUpdate();
			System.out.println("[DB] Completed creation of "+u);
			connection.close();
			return id;
		} catch (URISyntaxException | SQLException e) {
			System.err.println(e.getMessage());
			return -1;
		}
	}

	public static User getById(int id) {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement("SELECT * FROM gsx95369n3oh2zo6.users WHERE ID == ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
			ResultSet results = getStmt.executeQuery();
			User u = null;
			if (results.next()) {
				u = new User();
				u.setId(results.getInt(1));
				u.setUsername(results.getString(2));
				u.setEmail(results.getString(3));
				u.setCreated(results.getDate(4));
			}

			connection.close();
			return u;
		} catch (URISyntaxException | SQLException e) {
			return null;
		}
	}
	
	public static User getByEmail(String email){
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement("SELECT * FROM gsx95369n3oh2zo6.users WHERE EMAIL == ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setString(1, email);
			ResultSet results = getStmt.executeQuery();
			User u = null;
			if (results.next()) {
				u = new User();
				u.setId(results.getInt(1));
				u.setUsername(results.getString(2));
				u.setEmail(results.getString(3));
				u.setCreated(results.getDate(4));
			}

			connection.close();
			return u;
		} catch (URISyntaxException | SQLException e) {
			return null;
		}
	}

	public static boolean removeUser(int id) {
		Connection connection;
		try{
			connection = Database.getConnection();
			PreparedStatement delStmt = connection.prepareStatement("DELETE FROM gsx95369n3oh2zo6.users WHERE ID == ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			delStmt.clearParameters();
			delStmt.setInt(1, id);
			delStmt.executeUpdate();
			connection.close();
		} catch (URISyntaxException | SQLException e) {
			return false;
		}
		return true;
	}

	public static boolean createLogin(int id, String password) {
		Connection connection;
		try{
			connection = Database.getConnection();
			PreparedStatement createStmt = connection.prepareStatement("INSERT INTO gsx95369n3oh2zo6.secret (IDUSER, PASSWORD) VALUES (?,?)");
			createStmt.clearParameters();
			createStmt.setInt(1, id);
			createStmt.setString(2, password);
			createStmt.executeUpdate();
		} catch(SQLException | URISyntaxException e){
			return false;
		} 
		return true;
	}

}
