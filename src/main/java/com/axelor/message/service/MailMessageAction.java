package com.axelor.message.service;

import com.axelor.message.db.Message;

public interface MailMessageAction {
  Message postMailGenerationAction(Long objectId, Message mailMessage);
}
