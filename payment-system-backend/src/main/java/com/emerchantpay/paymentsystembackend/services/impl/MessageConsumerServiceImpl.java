package com.emerchantpay.paymentsystembackend.services.impl;

import com.emerchantpay.paymentsystembackend.model.Transaction;
import com.emerchantpay.paymentsystembackend.services.IMessageConsumerService;
import com.emerchantpay.paymentsystembackend.services.IPaymentService;
import com.emerchantpay.paymentsystembackend.model.KafkaMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumerServiceImpl implements IMessageConsumerService {
  @Autowired private IPaymentService paymentService;

  @Override
  @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.group-id}")
  public void listen(KafkaMessage message) {
    System.out.println("Consuming message: " + message);
    Transaction transaction = paymentService.initializeTransaction(message.getPrincipal(), message.getPaymentDTO());
    paymentService.commenceTransactionProcess(message.getPrincipal(), message.getPaymentDTO(), transaction);
  }
}
