package com.axelor.message.listener;

import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.message.service.registry.MailMessageActionRegister;
import javax.annotation.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStartListener {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public void onStartup(@Priority(-1) @Observes StartupEvent event) {
    LOG.debug("Starting MAIL MESSAGE REGISTER ACTION");
    MailMessageActionRegister.getInstance().registerAction();
  }
}
