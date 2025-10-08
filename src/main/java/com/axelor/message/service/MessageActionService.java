package com.axelor.message.service;

import com.axelor.message.db.Message;

public interface MessageActionService {

  Message executePostMessageActions(Message message);
}
