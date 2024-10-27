package com.emerchantpay.paymentsystembackend.services;

import com.emerchantpay.paymentsystembackend.model.KafkaMessage;

public interface IMessageConsumerService {
  void listen(KafkaMessage message);
}
