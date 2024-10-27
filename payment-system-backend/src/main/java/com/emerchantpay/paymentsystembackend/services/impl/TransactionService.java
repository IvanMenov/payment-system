package com.emerchantpay.paymentsystembackend.services.impl;

import com.emerchantpay.paymentsystembackend.model.Transaction;
import com.emerchantpay.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.paymentsystembackend.services.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TransactionService implements ITransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public List<Transaction> findTransactionsForPrincipal(long principalId, int limit, int offset) {
        return List.of();
    }
}
