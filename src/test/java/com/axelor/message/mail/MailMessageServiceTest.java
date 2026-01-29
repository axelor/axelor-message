package com.axelor.message.mail;

import com.axelor.auth.db.User;
import com.axelor.mail.db.MailFlags;
import com.axelor.mail.db.MailMessage;
import com.axelor.message.service.MailMessageCreator;
import com.axelor.message.service.MailMessageService;
import com.axelor.meta.loader.LoaderHelper;
import com.axelor.utils.junit.BaseTest;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MailMessageServiceTest extends BaseTest {

  protected final MailMessageService mailMessageService;
  protected final MailMessageCreator mailMessageCreator;
  protected final LoaderHelper loaderHelper;

  @Inject
  public MailMessageServiceTest(
      MailMessageService mailMessageService,
      MailMessageCreator mailMessageCreator,
      LoaderHelper loaderHelper) {
    this.mailMessageService = mailMessageService;
    this.mailMessageCreator = mailMessageCreator;
    this.loaderHelper = loaderHelper;
  }

  @BeforeEach
  void setUp() {
    loaderHelper.importCsv("data/users-input.xml");
  }

  @AfterEach
  @Transactional(rollbackOn = Exception.class)
  void tearDown() {
    all(MailFlags.class).delete();
    all(MailMessage.class).delete();
  }

  @Test
  void sendNotification_simple() throws InterruptedException {
    User user = all(User.class).fetchOne();
    mailMessageService.sendNotification(user, "subject", "body");
    Thread.sleep(1000);
    MailMessage message = all(MailMessage.class).fetchOne();
    Assertions.assertNotNull(message);
    Assertions.assertNotNull(message.getId());
    Assertions.assertEquals(user, message.getAuthor());
    Assertions.assertEquals("subject", message.getSubject());
    Assertions.assertEquals("body", message.getBody());
    Assertions.assertTrue(CollectionUtils.isNotEmpty(message.getFlags()));
  }
}
