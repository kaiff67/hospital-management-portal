package com.hospital;
import com.hospital.dao.UserDAO;import com.hospital.service.AuthService;import com.hospital.ui.HospitalWindow;import com.hospital.util.DBConnection;
import java.sql.Connection;
import javax.swing.SwingUtilities;

public final class Main {
 private Main() { }
 public static void main(String[] args){
  try(Connection ignored=DBConnection.getConnection()){
   bootstrapAdministrator();
  }catch(Exception e){System.err.println("Unable to start. Check MySQL is running, the schema is installed, and DB_URL/DB_USER/DB_PASSWORD are set.");System.err.println("Startup error type: "+e.getClass().getSimpleName()+". Technical details were withheld to avoid exposing configuration.");return;}
  SwingUtilities.invokeLater(HospitalWindow::new);
 }
 private static void bootstrapAdministrator()throws Exception{
  UserDAO users=new UserDAO();if(users.count()!=0)return;
  String name=System.getenv("BOOTSTRAP_ADMIN_USER"),password=System.getenv("BOOTSTRAP_ADMIN_PASSWORD");
  if(name!=null&&!name.isBlank()&&password!=null&&!password.isBlank()){
   new AuthService().registerBootstrap(name,password);
   System.out.println("Initial administrator account created from bootstrap environment variables.");
  }else System.out.println("No users exist. Set BOOTSTRAP_ADMIN_USER and BOOTSTRAP_ADMIN_PASSWORD for first-run administrator creation.");
 }
}
