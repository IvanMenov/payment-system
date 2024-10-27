package com.emerchantpay.paymentsystembackend.services.impl;

import com.emerchantpay.paymentsystembackend.model.KafkaMessage;
import com.emerchantpay.paymentsystembackend.services.IMessageProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducerServiceImpl implements IMessageProducerService {

  @Autowired private KafkaTemplate<String, KafkaMessage> kafkaTemplate;

  @Value("${spring.kafka.topic}")
  private String topicName;

  @Override
  public void sendMessage(KafkaMessage message) {
    System.out.println("Sending message: " + message);
    kafkaTemplate.send(topicName, message);
  }
}
