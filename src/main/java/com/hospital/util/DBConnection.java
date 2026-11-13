package com.hospital.util;
import java.sql.*;

/** Reads connection settings from the process environment; no credentials are stored in source. */
public final class DBConnection {
  private DBConnection() { }
  public static Connection getConnection() throws SQLException {
    String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/hospital_db?serverTimezone=UTC");
    String user = System.getenv("DB_USER"), password = System.getenv("DB_PASSWORD");
    if (user == null || password == null) throw new SQLException("Set DB_USER and DB_PASSWORD before starting the application.");
    return DriverManager.getConnection(url, user, password);
  }
}
