package com.axelor.message.listener;

import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.axelor.message.service.registry.MailMessageActionRegister;
import com.google.inject.Inject;
import jakarta.annotation.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailMessageServerStartListener {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  protected final MailMessageActionRegister mailMessageActionRegister;

  @Inject
  public MailMessageServerStartListener(MailMessageActionRegister mailMessageActionRegister) {
    this.mailMessageActionRegister = mailMessageActionRegister;
  }

  public void onStartup(@Priority(-1) @Observes StartupEvent event) {
    LOG.debug("Starting MAIL MESSAGE REGISTER ACTION");
    mailMessageActionRegister.registerAction();
  }
}
