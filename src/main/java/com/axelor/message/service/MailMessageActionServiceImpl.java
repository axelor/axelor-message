package com.axelor.message.service;

import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.registry.MailMessageActionRegister;
import com.google.inject.Inject;

public class MailMessageActionServiceImpl implements MailMessageActionService {
  protected final MessageRepository messageRepository;
  protected final MailMessageActionRegister mailMessageActionRegister;

  @Inject
  public MailMessageActionServiceImpl(
      MessageRepository messageRepository, MailMessageActionRegister mailMessageActionRegister) {
    this.messageRepository = messageRepository;
    this.mailMessageActionRegister = mailMessageActionRegister;
  }

  @Override
  public Message executePostMailMessageActions(Message message) {
    for (Class<? extends MailMessageAction> mailMessageActionClass :
        mailMessageActionRegister.getMailActionClasses()) {
      message = Beans.get(mailMessageActionClass).postMailGenerationAction(message);
    }
    return messageRepository.save(message);
  }
}
