package com.hospital.service;
import com.hospital.dao.*;import com.hospital.model.*;import org.mindrot.jbcrypt.BCrypt;import java.sql.SQLException;
public class AuthService {
 private final UserDAO dao=new UserDAO();private final AuditDAO audit=new AuditDAO();
 public User login(String username,String password)throws SQLException{User u=dao.findByUsername(username);if(u==null||!u.active()||!BCrypt.checkpw(password,u.passwordHash()))throw new SecurityException("Invalid username or password.");audit.record(u.id(),"LOGIN","USER",u.id(),"Successful login");return u;}
 public long register(User actor,String username,String password,Role role,Long doctor)throws SQLException{com.hospital.util.Access.require(actor,Role.ADMIN);if(password==null||password.length()<12)throw new IllegalArgumentException("Password must contain at least 12 characters.");long id=dao.create(username,BCrypt.hashpw(password,BCrypt.gensalt(12)),role,doctor);audit.record(actor.id(),"USER_REGISTERED","USER",id,"Role="+role);return id;}
 public void changePassword(User actor,String oldPassword,String newPassword)throws SQLException{if(!BCrypt.checkpw(oldPassword,actor.passwordHash()))throw new SecurityException("Current password is incorrect.");if(newPassword.length()<12)throw new IllegalArgumentException("Password must contain at least 12 characters.");dao.changePassword(actor.id(),BCrypt.hashpw(newPassword,BCrypt.gensalt(12)));audit.record(actor.id(),"PASSWORD_CHANGED","USER",actor.id(),"Password updated");}
 public void setActive(User actor,long id,boolean active)throws SQLException{com.hospital.util.Access.require(actor,Role.ADMIN);dao.setActive(id,active);audit.record(actor.id(),active?"USER_ACTIVATED":"USER_DEACTIVATED","USER",id,"Status changed");}
 /** Called only when the database has no users; credentials come from one-time environment settings. */
 public long registerBootstrap(String username,String password)throws SQLException{if(password.length()<12)throw new IllegalArgumentException("Bootstrap administrator password must contain at least 12 characters.");return dao.create(username,BCrypt.hashpw(password,BCrypt.gensalt(12)),Role.ADMIN,null);}
}
