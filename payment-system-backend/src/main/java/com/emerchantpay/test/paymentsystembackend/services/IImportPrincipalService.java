package com.emerchantpay.test.paymentsystembackend.services;

import org.springframework.web.multipart.MultipartFile;

public interface IImportPrincipalService {

  public void importPrincipalsFromCsv(MultipartFile file);
}
