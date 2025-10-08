package com.axelor.message.service;

import com.axelor.message.db.Message;

/**
 * @deprecated Replaced by {@link MessageAction}
 */
@Deprecated(since = "3.3", forRemoval = true)
public interface MailMessageAction {
  Message postMailGenerationAction(Message mailMessage);
}
