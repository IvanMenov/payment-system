package com.emerchantpay.test.paymentsystembackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class CsvFileValidator implements ConstraintValidator<CsvFile, MultipartFile> {
  static final long MAX_SIZE_IN_MB = 10;
  static final long MAX_SIZE = MAX_SIZE_IN_MB * 1024 * 1024;

  @Override
  public boolean isValid(
      MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
    return file.getSize() <= MAX_SIZE && !file.isEmpty();
  }
}
