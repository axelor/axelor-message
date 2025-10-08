package com.axelor.message.service;

import com.axelor.message.db.Message;

/**
 * @deprecated Replaced by {@link MessageActionService}
 */
@Deprecated(since = "3.3", forRemoval = true)
public interface MailMessageActionService {
  Message executePostMailMessageActions(Message message);
}
