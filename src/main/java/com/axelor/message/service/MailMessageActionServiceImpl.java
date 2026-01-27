package com.axelor.message.service;

import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.message.service.registry.MailMessageActionRegister;
import jakarta.inject.Inject;

public class MailMessageActionServiceImpl implements MailMessageActionService {
  protected final MailMessageActionRegister mailMessageActionRegister;

  @Inject
  public MailMessageActionServiceImpl(MailMessageActionRegister mailMessageActionRegister) {
    this.mailMessageActionRegister = mailMessageActionRegister;
  }

  @Override
  public MailMessage executePreMailMessageActions(MailMessage mailMessage, Model relatedRecord) {
    for (Class<? extends MailMessageAction> mailMessageActionClass :
        mailMessageActionRegister.getMailActionClasses()) {
      mailMessage = Beans.get(mailMessageActionClass).preSendAction(mailMessage, relatedRecord);
    }
    return mailMessage;
  }
}
