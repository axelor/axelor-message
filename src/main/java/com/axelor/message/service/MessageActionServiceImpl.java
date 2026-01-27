package com.axelor.message.service;

import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.registry.MessageActionRegister;
import jakarta.inject.Inject;

public class MessageActionServiceImpl implements MessageActionService {
  protected final MessageRepository messageRepository;
  protected final MessageActionRegister messageActionRegister;

  @Inject
  public MessageActionServiceImpl(
      MessageRepository messageRepository, MessageActionRegister messageActionRegister) {
    this.messageRepository = messageRepository;
    this.messageActionRegister = messageActionRegister;
  }

  @Override
  public Message executePostMessageActions(Message message) {
    for (Class<? extends MessageAction> mailMessageActionClass :
        messageActionRegister.getMessageActions()) {
      message = Beans.get(mailMessageActionClass).postMessageGenerationAction(message);
    }
    return messageRepository.save(message);
  }
}
