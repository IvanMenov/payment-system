package com.emerchantpay.test.paymentsystembackend.validation;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.springframework.web.multipart.MultipartFile;

public class CsvFileValidator implements ConstraintValidator<CsvFile, MultipartFile> {
  static final double MAX_SIZE_IN_MB = 10.0;
  private CSVParser parser;

  @Override
  public void initialize(CsvFile constraintAnnotation) {
    parser = new CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build();
  }

  @Override
  public boolean isValid(
      MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
    if (!file.isEmpty()) {
      double currentFileSizeInMb = (double) file.getSize() / (1024 * 1024);
      if (currentFileSizeInMb > MAX_SIZE_IN_MB) {
        return false;
      }
      try (Reader reader = new InputStreamReader(file.getInputStream());
          CSVReader csvReader =
              new CSVReaderBuilder(reader).withSkipLines(0).withCSVParser(parser).build(); ) {
        return csvReader.verifyReader() && csvReader.peek() != null;
      } catch (IOException e) {
        return false;
      }
    }
    return false;
  }
}
