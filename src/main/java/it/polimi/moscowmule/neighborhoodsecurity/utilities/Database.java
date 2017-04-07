package it.polimi.moscowmule.neighborhoodsecurity.utilities;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	/**
	 * Creates a connection to the JawsDB SQL database
	 * 
	 * @return the connection
	 * @throws URISyntaxException
	 * @throws SQLException
	 */
	static Connection getConnection() throws URISyntaxException, SQLException {
		URI jdbUri = new URI(System.getenv("JAWSDB_URL"));

		String username = jdbUri.getUserInfo().split(":")[0];
		String password = jdbUri.getUserInfo().split(":")[1];
		String port = String.valueOf(jdbUri.getPort());
		String jdbUrl = "jdbc:mysql://" + jdbUri.getHost() + ":" + port + jdbUri.getPath();

		return DriverManager.getConnection(jdbUrl, username, password);
	}
}
