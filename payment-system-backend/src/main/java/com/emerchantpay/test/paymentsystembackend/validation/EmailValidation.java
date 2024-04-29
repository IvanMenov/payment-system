package com.emerchantpay.test.paymentsystembackend.validation;

import java.util.regex.Pattern;

public class EmailValidation {
  private static Pattern patternEmail = Pattern.compile("^(.+)@(\\S+)$");

  public static boolean patternMatches(String emailAddress) {
    return patternEmail.matcher(emailAddress).matches();
  }
}
