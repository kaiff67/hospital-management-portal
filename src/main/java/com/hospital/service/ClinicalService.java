package com.hospital.service;
import com.hospital.dao.*;import com.hospital.model.*;import com.hospital.util.Access;import java.sql.SQLException;import java.util.*;
public class ClinicalService {
 private final MedicalRecordDAO records=new MedicalRecordDAO();private final PrescriptionDAO prescriptions=new PrescriptionDAO();private final AuditDAO audit=new AuditDAO();
 public long createRecord(User u,long appointment,String symptoms,String diagnosis,String notes)throws SQLException{Access.require(u,Role.DOCTOR);long id=records.create(appointment,symptoms,diagnosis,notes,u.id());audit.record(u.id(),"MEDICAL_RECORD_CREATED","MEDICAL_RECORD",id,"Clinical record created");return id;}
 public List<String> history(User u,long patient)throws SQLException{Access.require(u,Role.ADMIN,Role.DOCTOR);return records.history(patient,u.role()==Role.DOCTOR?u.doctorId():null);}
 public long prescribe(User u,long record,List<String[]> items)throws SQLException{Access.require(u,Role.DOCTOR);if(u.doctorId()==null)throw new SecurityException("Doctor account is not assigned to a doctor profile.");long id=prescriptions.create(record,items,u.doctorId());audit.record(u.id(),"PRESCRIPTION_CREATED","PRESCRIPTION",id,"Prescription recorded");return id;}
 public List<String> prescriptions(User u,long patient)throws SQLException{Access.require(u,Role.ADMIN,Role.DOCTOR);return prescriptions.list(patient,u.role()==Role.DOCTOR?u.doctorId():null);}
}
