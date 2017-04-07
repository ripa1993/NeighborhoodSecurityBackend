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
		String username = "qgeughr454j5fu0f";
		String password = "eq7okgrs9g08t28d";
		String jdbUrl = "gk90usy5ik2otcvi.cbetxkdyhwsb.us-east-1.rds.amazonaws.com:3306/gsx95369n3oh2zo6";

		return DriverManager.getConnection(jdbUrl, username, password);
	}
}
