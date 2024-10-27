package com.emerchantpay.paymentsystembackend.services;

import com.emerchantpay.paymentsystembackend.model.Principal;
import com.emerchantpay.paymentsystembackend.model.Transaction;

import java.util.List;

public interface ITransactionService {

    List<Transaction> findTransactionsForPrincipal(long principalId, int limit, int offset);
}
