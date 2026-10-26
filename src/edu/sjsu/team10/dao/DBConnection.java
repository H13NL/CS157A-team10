package edu.sjsu.team10.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
	
	// change testproject into your database name
    private static final String URL = "jdbc:mysql://localhost:3306/testproject" +  "?useSSL=false" + "&allowPublicKeyRetrieval=true" + "&serverTimezone=UTC" + "&forceConnectionTimeZoneToSession=true";
    private static final String USER = "root";
	// change to your database password (I set mine as a environment variable). 
    private static final String PASSWORD = System.getenv("WHATS_MENU_DB_PASSWORD");

    // register the MySQL driver from WEB-INF/lib (Tomcat doesn't always auto-load it)
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
