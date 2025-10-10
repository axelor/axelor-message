package com.axelor.message.listener;

import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.message.service.registry.MailMessageActionRegister;
import com.axelor.message.service.registry.MessageActionRegister;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import jakarta.annotation.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStartListener {

  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected final MessageActionRegister messageActionRegister;
  protected final MailMessageActionRegister mailMessageActionRegister;

  @Inject
  public ServerStartListener(
      MessageActionRegister messageActionRegister,
      MailMessageActionRegister mailMessageActionRegister) {
    this.messageActionRegister = messageActionRegister;
    this.mailMessageActionRegister = mailMessageActionRegister;
  }

  public void onStartup(@Priority(-1) @Observes StartupEvent event) {
    LOG.debug("Register action for MESSAGE module");
    messageActionRegister.registerAction();
    mailMessageActionRegister.registerAction();
  }
}
