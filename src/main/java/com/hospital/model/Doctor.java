package com.hospital.model;
import java.math.BigDecimal;
public record Doctor(long id, String fullName, String specialization, long departmentId, String phone, BigDecimal consultationFee, boolean active) { }
