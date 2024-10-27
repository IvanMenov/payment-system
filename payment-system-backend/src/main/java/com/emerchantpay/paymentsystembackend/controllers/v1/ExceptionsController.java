package com.emerchantpay.paymentsystembackend.controllers.v1;

import com.emerchantpay.paymentsystembackend.exceptions.MerchantNotActivatedException;
import com.emerchantpay.paymentsystembackend.exceptions.NoReferenceIdException;
import com.emerchantpay.paymentsystembackend.exceptions.TranctionAlreadySubmittedException;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsController {

  @ExceptionHandler(
      value = {
        NumberFormatException.class,
        IllegalArgumentException.class,
        NullPointerException.class,
      })
  public ResponseEntity<?> handleBaseInputException(Exception exception) {
    return ResponseEntity.badRequest().body(String.format(exception.getMessage()));
  }

  @ExceptionHandler(value = {IOException.class})
  public ResponseEntity<?> handleImportException(Exception exception) {
    return ResponseEntity.internalServerError()
        .body(
            String.format(
                "Problems importing principals from file.Cause:%s", exception.getMessage()));
  }

  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  public ResponseEntity<?> handleInputValidation(MethodArgumentNotValidException exception) {
    return ResponseEntity.internalServerError()
        .body(String.format("Invalid input parameters! ", exception.getBody().getDetail()));
  }

  @ExceptionHandler(
      value = {
        MerchantNotActivatedException.class,
        TranctionAlreadySubmittedException.class,
        NoReferenceIdException.class,
        RuntimeException.class,
        Exception.class
      })
  public ResponseEntity<?> handleInputValidationException(Exception exception) {
    return ResponseEntity.internalServerError().body(exception.getMessage());
  }
}
