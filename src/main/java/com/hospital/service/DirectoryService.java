package com.hospital.service;
import com.hospital.dao.*;import com.hospital.model.*;import com.hospital.util.*;import java.math.BigDecimal;import java.sql.*;import java.util.*;
public class DirectoryService {
 private final DepartmentDAO departments=new DepartmentDAO();private final DoctorDAO doctors=new DoctorDAO();private final AuditDAO audit=new AuditDAO();
 public long addDepartment(User u,String name,String description)throws SQLException{Access.require(u,Role.ADMIN);long id=departments.create(InputValidator.required(name,"Department name"),description);audit.record(u.id(),"DEPARTMENT_CREATED","DEPARTMENT",id,"Department added");return id;}
 public List<String> departments(User u)throws SQLException{Access.require(u,Role.ADMIN,Role.RECEPTIONIST,Role.DOCTOR);return departments.list();}
 public void deactivateDepartment(User u,long id)throws SQLException{Access.require(u,Role.ADMIN);departments.deactivate(id);audit.record(u.id(),"DEPARTMENT_DEACTIVATED","DEPARTMENT",id,"Department deactivated");}
 public long addDoctor(User u,String name,String specialty,long dept,String phone,BigDecimal fee)throws SQLException{Access.require(u,Role.ADMIN);long id=doctors.create(name,specialty,dept,InputValidator.phone(phone),fee);audit.record(u.id(),"DOCTOR_CREATED","DOCTOR",id,"Doctor added");return id;}
 public List<String> doctors(User u,String filter)throws SQLException{Access.require(u,Role.ADMIN,Role.RECEPTIONIST,Role.DOCTOR);return doctors.list(filter);}
 public void doctorActive(User u,long id,boolean active)throws SQLException{Access.require(u,Role.ADMIN);doctors.setActive(id,active);audit.record(u.id(),"DOCTOR_STATUS_CHANGED","DOCTOR",id,"active="+active);}
 public void availability(User u,long id,int day,Time from,Time to,int slot)throws SQLException{Access.require(u,Role.ADMIN);if(day<1||day>7||slot<5||slot>240||!from.before(to))throw new IllegalArgumentException("Invalid working hours or slot size.");doctors.setAvailability(id,day,from,to,slot);audit.record(u.id(),"DOCTOR_AVAILABILITY_SET","DOCTOR",id,"Availability configured");}
}
