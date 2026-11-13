package com.hospital.dao;
import com.hospital.model.*;
import com.hospital.util.DBConnection;
import java.sql.*;

public class UserDAO {
  public long count() throws SQLException { try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM users");ResultSet r=p.executeQuery()){r.next();return r.getLong(1);} }
  public User findByUsername(String username) throws SQLException {
    String sql="SELECT user_id,username,password_hash,role,doctor_id,active FROM users WHERE username=?";
    try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)){p.setString(1,username); try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}
  }
  public long create(String username,String hash,Role role,Long doctorId) throws SQLException {
    String sql="INSERT INTO users(username,password_hash,role,doctor_id) VALUES(?,?,?,?)";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){p.setString(1,username);p.setString(2,hash);p.setString(3,role.name());if(doctorId==null)p.setNull(4,Types.BIGINT);else p.setLong(4,doctorId);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getLong(1);}}
  }
  public void changePassword(long userId,String hash) throws SQLException { try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")){p.setString(1,hash);p.setLong(2,userId);p.executeUpdate();} }
  public void setActive(long userId,boolean active) throws SQLException {try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET active=? WHERE user_id=?")){p.setBoolean(1,active);p.setLong(2,userId);if(p.executeUpdate()==0)throw new SQLException("User not found.");}}
  private User map(ResultSet r)throws SQLException{long d=r.getLong("doctor_id");Long doctor=r.wasNull()?null:d;return new User(r.getLong("user_id"),r.getString("username"),r.getString("password_hash"),Role.valueOf(r.getString("role")),doctor,r.getBoolean("active"));}
}
