package com.axelor.message.service;

import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.registry.MailMessageActionRegister;
import com.google.inject.Inject;

public class MailMessageActionServiceImpl implements MailMessageActionService {
  protected final MessageRepository messageRepository;

  @Inject
  public MailMessageActionServiceImpl(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  @Override
  public Message executePostMailMessageActions(Message message) {
    for (Class<? extends MailMessageAction> mailMessageActionClass :
        MailMessageActionRegister.getInstance().getMailActionClasses()) {
      message = Beans.get(mailMessageActionClass).postMailGenerationAction(message);
    }
    return messageRepository.save(message);
  }
}
