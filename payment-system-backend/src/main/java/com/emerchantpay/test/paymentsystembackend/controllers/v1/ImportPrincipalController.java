package com.emerchantpay.test.paymentsystembackend.controllers.v1;

import com.emerchantpay.test.paymentsystembackend.services.IImportPrincipalService;
import com.emerchantpay.test.paymentsystembackend.validation.CsvFile;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/v1/import")
public class ImportPrincipalController {

  @Autowired private IImportPrincipalService principalService;

  @PostMapping("/principals")
  public ResponseEntity<?> importPrincipals(@RequestParam @NotNull @CsvFile MultipartFile file)
      throws IOException {
    principalService.importPrincipalsFromCsv(file.getInputStream());
    return ResponseEntity.accepted().build();
  }
}
