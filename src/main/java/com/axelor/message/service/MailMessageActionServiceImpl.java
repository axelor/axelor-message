package com.axelor.message.service;

import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.registry.MailMessageActionRegister;

public class MailMessageActionServiceImpl implements MailMessageActionService {

  @Override
  public Message executePostMailMessageActions(Message message) {
    /*
    Saving message in case of adding some attachment file actions
     */
    message = JPA.save(message);
    for (Class<? extends MailMessageAction> mailMessageActionClass :
        MailMessageActionRegister.getInstance().getMailActionClasses()) {
      message = Beans.get(mailMessageActionClass).postMailGenerationAction(message);
    }

    return message;
  }
}
