package com.hospital.model;
import java.math.BigDecimal;
public record Bill(long id, long appointmentId, long patientId, BigDecimal subtotal, BigDecimal discount, BigDecimal total, BigDecimal paidAmount, String status) {
  public BigDecimal balance() { return total.subtract(paidAmount); }
}
