package com.hospital.util;
import org.junit.jupiter.api.Test;import java.math.BigDecimal;import java.time.*;import static org.junit.jupiter.api.Assertions.*;
class InputValidatorTest {
 @Test void rejectsFutureBirthDate(){assertThrows(IllegalArgumentException.class,()->InputValidator.birthDate(LocalDate.now().plusDays(1).toString()));}
 @Test void rejectsBadPhone(){assertThrows(IllegalArgumentException.class,()->InputValidator.phone("abc"));}
 @Test void rejectsNegativeMoney(){assertThrows(IllegalArgumentException.class,()->InputValidator.nonNegativeMoney("-1.00","Fee"));}
 @Test void acceptsValidPhoneAndMoney(){assertEquals("+1 555-0100",InputValidator.phone("+1 555-0100"));assertEquals(new BigDecimal("12.50"),InputValidator.nonNegativeMoney("12.50","Fee"));}
}
