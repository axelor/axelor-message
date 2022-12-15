package com.axelor.message.service;

import com.axelor.message.db.Template;
import java.util.Map;

public interface GenerateMessageService {
  Map<String, Object> generateMessage(long objectId, String model, String tag, Template template)
      throws ClassNotFoundException;
}
