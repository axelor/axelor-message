package com.axelor.message.mail;

import com.axelor.mail.db.MailMessage;
import com.axelor.message.service.MailMessageCreator;
import com.axelor.meta.loader.LoaderHelper;
import com.axelor.utils.junit.BaseTest;
import jakarta.inject.Inject;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MailMessageCreatorTest extends BaseTest {

  protected final LoaderHelper loaderHelper;
  protected final MailMessageCreator mailMessageCreator;

  @Inject
  public MailMessageCreatorTest(LoaderHelper loaderHelper, MailMessageCreator mailMessageCreator) {
    this.loaderHelper = loaderHelper;
    this.mailMessageCreator = mailMessageCreator;
  }

  @BeforeEach
  void setUp() {
    loaderHelper.importCsv("data/users-input.xml");
  }

  @Test
  void create_simple() throws ExecutionException, InterruptedException {

    MailMessage message = mailMessageCreator.persist(1L, "body", "subject", "type").get();

    Assertions.assertNotNull(message);
    Assertions.assertNotNull(message.getId());
    Assertions.assertEquals("body", message.getBody());
    Assertions.assertEquals("subject", message.getSubject());
    Assertions.assertEquals("type", message.getType());
    Assertions.assertNotNull(message.getAuthor());
    Assertions.assertEquals(1L, message.getAuthor().getId());
  }

  @Test
  void create_noData() throws ExecutionException, InterruptedException {

    MailMessage message = mailMessageCreator.persist(null, null, null, null).get();

    Assertions.assertNotNull(message);
    Assertions.assertNotNull(message.getId());
    Assertions.assertNull(message.getBody());
    Assertions.assertNull(message.getSubject());
    Assertions.assertNull(message.getType());
    Assertions.assertNull(message.getAuthor());
  }

  @Test
  void create_withExtraConfigs() throws ExecutionException, InterruptedException {

    MailMessage message =
        mailMessageCreator
            .persist(
                1L,
                "body",
                "subject",
                "type",
                mailMessage -> {
                  mailMessage.setRelatedModel("com.axelor.meta.db.MetaFile");
                  mailMessage.setRelatedId(1L);
                })
            .get();

    Assertions.assertNotNull(message);
    Assertions.assertNotNull(message.getId());
    Assertions.assertEquals("body", message.getBody());
    Assertions.assertEquals("subject", message.getSubject());
    Assertions.assertEquals("type", message.getType());
    Assertions.assertNotNull(message.getAuthor());
    Assertions.assertEquals(1L, message.getAuthor().getId());
    Assertions.assertEquals("com.axelor.meta.db.MetaFile", message.getRelatedModel());
    Assertions.assertEquals(1L, message.getRelatedId());
  }
}
