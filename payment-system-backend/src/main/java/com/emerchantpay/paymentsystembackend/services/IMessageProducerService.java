package com.emerchantpay.paymentsystembackend.services;

import com.emerchantpay.paymentsystembackend.model.KafkaMessage;

public interface IMessageProducerService {

  void sendMessage(KafkaMessage message);
}
