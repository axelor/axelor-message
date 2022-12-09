package com.axelor.apps.message.service;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.MessageExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateMessageServiceImpl implements GenerateMessageService {
  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected final TemplateMessageService templateMessageService;

  @Inject
  public GenerateMessageServiceImpl(TemplateMessageService templateMessageService) {
    this.templateMessageService = templateMessageService;
  }

  public Map<String, Object> generateMessage(
      long objectId, String model, String tag, Template template)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

    LOG.debug("template : {} ", template);
    LOG.debug("object id : {} ", objectId);
    LOG.debug("model : {} ", model);
    LOG.debug("tag : {} ", tag);
    Message message = null;
    if (template != null) {
      message = templateMessageService.generateMessage(objectId, model, tag, template);
    } else {
      message =
          Beans.get(MessageService.class)
              .createMessage(
                  model,
                  Math.toIntExact(objectId),
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  MessageRepository.MEDIA_TYPE_EMAIL,
                  null,
                  null);
    }

    return ActionView.define(I18n.get(MessageExceptionMessage.MESSAGE_3))
        .model(Message.class.getName())
        .add("form", "message-form")
        .param("forceEdit", "true")
        .context("_showRecord", message.getId().toString())
        .map();
  }
}
