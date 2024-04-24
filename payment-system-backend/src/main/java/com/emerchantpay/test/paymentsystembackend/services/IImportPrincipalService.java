package com.emerchantpay.test.paymentsystembackend.services;

import java.io.InputStream;

public interface IImportPrincipalService {

  void importPrincipalsFromCsv(InputStream stream);
}
