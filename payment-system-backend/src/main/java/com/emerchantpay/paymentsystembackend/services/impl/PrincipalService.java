package com.emerchantpay.paymentsystembackend.services.impl;

import com.emerchantpay.paymentsystembackend.model.*;
import com.emerchantpay.paymentsystembackend.repositories.PrincipalRepository;
import com.emerchantpay.paymentsystembackend.repositories.TransactionRepository;
import com.emerchantpay.paymentsystembackend.services.IPrincipalService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrincipalService implements IPrincipalService<Principal> {

    @Autowired
    private PrincipalRepository principalRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * @param merchant <p>creates Principal if it doesn't already exist, otherwise updates it
     */
    @Override
    public Principal createOrUpdatePrincipal(Principal merchant) {
        return principalRepository.save(merchant);
    }

    /**
     * finds all Principal with that have PrincipalType.MERCHANT
     *
     * @return List<Principal>
     */
    @Override
    public List<Principal> findAllMerchants() {
        return principalRepository.findAllByPrincipalType(PrincipalType.MERCHANT);
    }


    /**
     * @param id delete merchant by id by first if there are no transactions related to that merchant
     *           otherwise the method would throw a RuntimeException exception
     */
    @Override
    @Transactional
    public void deleteMerchantById(long id) {
        if (transactionRepository.hasTransactionForMerchant(id) > 0) {
            throw new RuntimeException(
                    "Cannot delete merchant, because there are related transactions with him/her.");
        }
        principalRepository.deleteById(id);
    }

    /**
     * @param merchant check if merchant is of PrincipalType.MERCHANT and has
     *                 Principal.Status.INACTIVE
     * @return boolean
     */
    @Override
    public boolean isMerchantInactive(Principal merchant) {
        Optional<Principal> optionalPrincipal =
                principalRepository.findByIdAndPrincipalType(
                        merchant.getId(), PrincipalType.MERCHANT.toString());
        if (optionalPrincipal.isPresent()) {
            return optionalPrincipal
                    .get()
                    .getPrincipalStatus()
                    .getStatusValue()
                    .equals(Principal.Status.INACTIVE.getStatusValue());
        }
        return false;
    }

    /**
     * @param id finds Principal by id
     * @return Optional<Principal>
     */
    @Override
    public Optional<Principal> findPrincipalById(long id) {
        return principalRepository.findPrincipalById(id);
    }

    @Override
    public Optional<Principal> findPrincipalByIdLoadBatchOfTransactions(long id, int limit, int offset) {
        List<Tuple> tuples =
                principalRepository.findPrincipalByIdWithBatchOfTransactions(id, offset, limit);
        if (tuples.isEmpty()) {
            return Optional.empty();
        }
        Principal principal = Principal.builder()
                .id(tuples.get(0).get("id", Long.class))
                .email(tuples.get(0).get("email", String.class))
                .totalTransactionSum(tuples.get(0).get("total_transaction_sum", BigDecimal.class))
                .countTransactions(tuples.get(0).get("count_transactions", Long.class))
                .build();

        tuples.forEach(tuple -> {
            if(tuple.get("uuid", String.class) == null){
                return;
            }
            Transaction.TransactionBuilder transactionBuilder = Transaction.builder()
                    .uuid(tuple.get("uuid", String.class))
                    .amount(tuple.get("amount", Double.class))
                    .customerEmail(tuple.get("customer_email", String.class))
                    .customerPhone(tuple.get("customer_phone", String.class))
                    .referenceTransactionUUID(tuple.get("reference_transaction_uuid", String.class))
                    .timestamp(tuple.get("timestamp", Long.class));

            if (tuple.get("transaction_type", String.class) != null) {
                transactionBuilder.transactionType(TransactionType.valueOf(tuple.get("transaction_type", String.class)));
            }
            if (tuple.get("transaction_status", String.class) != null) {
                transactionBuilder.transactionStatus(Transaction.Status.valueOf(tuple.get("transaction_status", String.class)));

            }
            principal.addTransaction(transactionBuilder.build());
        });

        return Optional.of(principal);

    }

    @Override
    public Optional<Principal> findPrincipalByEmail(String email) {
        return principalRepository.findByEmail(email);
    }
}
