package com.axelor.apps.message.service;

import com.axelor.apps.message.db.Template;
import java.io.IOException;
import java.util.Map;

public interface GenerateMessageService {
  Map<String, Object> generateMessage(long objectId, String model, String tag, Template template)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException;
}
