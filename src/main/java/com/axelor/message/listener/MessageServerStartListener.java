package com.axelor.message.listener;

import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.message.service.registry.MessageActionRegister;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import javax.annotation.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageServerStartListener {

  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected final MessageActionRegister messageActionRegister;

  @Inject
  public MessageServerStartListener(MessageActionRegister messageActionRegister) {
    this.messageActionRegister = messageActionRegister;
  }

  public void onStartup(@Priority(-1) @Observes StartupEvent event) {
    LOG.debug("Starting MAIL MESSAGE REGISTER ACTION");
    messageActionRegister.registerAction();
  }
}
