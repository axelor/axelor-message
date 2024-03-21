package com.axelor.message.service;

import com.axelor.message.db.Message;

public interface MailMessageActionService {
  Message executePostMailMessageActions(Message message);
}
