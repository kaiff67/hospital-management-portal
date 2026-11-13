package com.hospital.service;
import org.junit.jupiter.api.Test;import java.math.BigDecimal;import static org.junit.jupiter.api.Assertions.*;
class BillingServiceTest {
 @Test void calculatesDiscountedInvoiceUsingDecimalArithmetic(){assertEquals(new BigDecimal("80.00"),BillingService.calculateTotal(new BigDecimal("100.00"),new BigDecimal("20.00")));}
 @Test void rejectsDiscountLargerThanSubtotal(){assertThrows(IllegalArgumentException.class,()->BillingService.calculateTotal(new BigDecimal("10.00"),new BigDecimal("11.00")));}
}
