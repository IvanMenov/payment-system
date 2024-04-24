package com.emerchantpay.test.paymentsystembackend.controllers.v1;

import com.emerchantpay.test.paymentsystembackend.exceptions.MerchantNotActivatedException;
import com.emerchantpay.test.paymentsystembackend.exceptions.NoReferenceIdException;
import com.emerchantpay.test.paymentsystembackend.exceptions.TranctionAlreadySubmittedException;
import com.opencsv.exceptions.CsvException;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsController {

  @ExceptionHandler(value = ExpiredJwtException.class)
  public ResponseEntity<Object> handleExpiredJwtToken(ExpiredJwtException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT token expired!");
  }

  @ExceptionHandler(
      value = {
        NumberFormatException.class,
        IllegalArgumentException.class,
        NullPointerException.class,
      })
  public ResponseEntity<Object> handleBaseInputException(Exception exception) {
    return ResponseEntity.badRequest().body(String.format(exception.getMessage()));
  }

  @ExceptionHandler(value = {IOException.class, CsvException.class})
  public ResponseEntity<Object> handleImportException(Exception exception) {
    return ResponseEntity.internalServerError()
        .body(
            String.format(
                "Problems importing principals from file.Cause:%s", exception.getMessage()));
  }

  @ExceptionHandler(
      value = {
        MerchantNotActivatedException.class,
        TranctionAlreadySubmittedException.class,
        NoReferenceIdException.class,
        RuntimeException.class,
        Exception.class
      })
  public ResponseEntity<Object> handleInputValidationException(Exception exception) {
    return ResponseEntity.internalServerError().body(exception.getMessage());
  }
}
