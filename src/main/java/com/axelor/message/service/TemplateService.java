package com.axelor.message.service;

import com.axelor.db.Model;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.meta.db.MetaModel;
import java.util.Map;

public interface TemplateService {
  void checkTargetReceptor(Template template);

  String processSubject(
      String timeZone, Template template, Model bean, String beanName, Map<String, Object> context);

  String processContent(
      String timeZone, Template template, Model bean, String beanName, Map<String, Object> context);

  Message generateDraftMessage(Template template, MetaModel metaModel, String referenceId)
      throws ClassNotFoundException;
}
