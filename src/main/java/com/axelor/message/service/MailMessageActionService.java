package com.axelor.message.service;

import com.axelor.db.Model;
import com.axelor.mail.db.MailMessage;

public interface MailMessageActionService {
  MailMessage executePreMailMessageActions(MailMessage message, Model relatedRecord);
}
