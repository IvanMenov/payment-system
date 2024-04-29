package com.emerchantpay.test.paymentsystembackend.validation;

import java.util.regex.Pattern;

public class PhoneValidation {
  private static Pattern patternPhone = Pattern.compile("^\\d{10}$");

  public static boolean patternMatches(String emailAddress) {
    return patternPhone.matcher(emailAddress).matches();
  }
}
