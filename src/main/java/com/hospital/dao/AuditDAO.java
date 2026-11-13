package com.hospital.dao;
import com.hospital.util.DBConnection;import java.sql.*;
public class AuditDAO {
 public void record(Long user,String action,String type,Long id,String details){String q="INSERT INTO audit_logs(user_id,action,entity_type,entity_id,details) VALUES(?,?,?,?,?)";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){if(user==null)p.setNull(1,Types.BIGINT);else p.setLong(1,user);p.setString(2,action);p.setString(3,type);if(id==null)p.setNull(4,Types.BIGINT);else p.setLong(4,id);p.setString(5,details);p.executeUpdate();}catch(SQLException e){System.err.println("Audit event could not be recorded: "+e.getSQLState());}}
}
