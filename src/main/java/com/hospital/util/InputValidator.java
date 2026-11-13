package com.hospital.util;
import java.math.BigDecimal;
import java.time.*;
import java.util.regex.Pattern;

public final class InputValidator {
  private static final Pattern PHONE = Pattern.compile("^[+0-9() .-]{7,25}$");
  private InputValidator() { }
  public static String required(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required."); return value.trim(); }
  public static String phone(String value) { String v=required(value,"Phone number"); if(!PHONE.matcher(v).matches()) throw new IllegalArgumentException("Enter a valid phone number."); return v; }
  public static LocalDate birthDate(String value) { LocalDate date=LocalDate.parse(value); if(date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Date of birth cannot be in the future."); return date; }
  public static BigDecimal nonNegativeMoney(String value, String field) { BigDecimal n=new BigDecimal(value); if(n.scale()>2 || n.signum()<0) throw new IllegalArgumentException(field+" must be non-negative with at most two decimals."); return n; }
  public static void appointmentTime(LocalDateTime start, LocalDateTime end) { if(!end.isAfter(start) || start.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Appointment must have a future start and a later end."); }
}
