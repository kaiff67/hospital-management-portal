package com.hospital.service;
import com.hospital.dao.*;import com.hospital.model.*;import com.hospital.util.*;import java.sql.SQLException;import java.math.BigDecimal;import java.util.*;
public class BillingService {
 private final BillingDAO bills=new BillingDAO();private final PaymentDAO payments=new PaymentDAO();private final ReportDAO reports=new ReportDAO();private final AuditDAO audit=new AuditDAO();
 public long create(User u,long appointment,BigDecimal subtotal,BigDecimal discount,String description)throws SQLException{Access.require(u,Role.ADMIN,Role.BILLING_STAFF);if(subtotal.signum()<0||discount.signum()<0||discount.compareTo(subtotal)>0)throw new IllegalArgumentException("Subtotal and discount are invalid.");long id=bills.create(appointment,subtotal,discount,description,u.id());audit.record(u.id(),"BILL_CREATED","BILL",id,"Invoice generated");return id;}
 public Bill view(User u,long bill)throws SQLException{Access.require(u,Role.ADMIN,Role.BILLING_STAFF);Bill b=bills.find(bill);if(b==null)throw new IllegalArgumentException("Invoice not found.");return b;}
 public List<String> unpaid(User u)throws SQLException{Access.require(u,Role.ADMIN,Role.BILLING_STAFF);return bills.unpaid();}
 public void pay(User u,long bill,BigDecimal amount,PaymentMethod method,String reference)throws SQLException{Access.require(u,Role.ADMIN,Role.BILLING_STAFF);payments.record(bill,amount,method,reference,u.id());audit.record(u.id(),"PAYMENT_RECORDED","BILL",bill,"Payment amount="+amount+", method="+method);}
 public List<String> paymentReport(User u,java.time.LocalDate from,java.time.LocalDate to)throws SQLException{Access.require(u,Role.ADMIN,Role.BILLING_STAFF);if(to.isBefore(from))throw new IllegalArgumentException("End date must be on or after start date.");return reports.billing(from,to);}
 public static BigDecimal calculateTotal(BigDecimal subtotal,BigDecimal discount){if(subtotal.signum()<0||discount.signum()<0||discount.compareTo(subtotal)>0)throw new IllegalArgumentException("Subtotal and discount are invalid.");return subtotal.subtract(discount);}
}
