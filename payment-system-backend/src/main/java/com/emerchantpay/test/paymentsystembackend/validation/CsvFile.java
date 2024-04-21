package com.emerchantpay.test.paymentsystembackend.validation;

import static com.emerchantpay.test.paymentsystembackend.validation.CsvFileValidator.MAX_SIZE_IN_MB;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.TYPE_PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CsvFileValidator.class)
public @interface CsvFile {

  String message() default
      "File size is should be greater than 0 and less than " + MAX_SIZE_IN_MB + " MB!";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
