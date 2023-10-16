package com.axelor.message.service;

import com.axelor.auth.db.repo.UserRepository;
import com.axelor.mail.db.MailMessage;
import com.axelor.mail.db.repo.MailMessageRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * This class implements the MailMessageCreator interface and provides methods to create and persist
 * MailMessage objects.
 */
@Singleton
public class MailMessageCreatorImpl implements MailMessageCreator {

  protected final ExecutorService executorService;
  protected final MailMessageRepository mailMessageRepository;
  protected final UserRepository userRepository;

  @Inject
  public MailMessageCreatorImpl(
      MailMessageRepository mailMessageRepository, UserRepository userRepository) {
    this.executorService = Executors.newCachedThreadPool();
    this.mailMessageRepository = mailMessageRepository;
    this.userRepository = userRepository;
  }

  @Override
  public MailMessage create(Long userId, String body, String subject, String type) {
    return create(userId, body, subject, type, null);
  }

  @Override
  public MailMessage create(
      Long userId, String body, String subject, String type, Consumer<MailMessage> extraConfigs) {
    MailMessage message = new MailMessage();

    if (userId != null) {
      message.setAuthor(userRepository.find(userId));
    }
    message.setBody(body);
    message.setSubject(subject);
    message.setType(type);

    if (extraConfigs != null) {
      extraConfigs.accept(message);
    }

    return message;
  }

  @Override
  public Future<MailMessage> persist(Long userId, String body, String subject, String type) {
    return persist(userId, body, subject, type, null);
  }

  @Override
  public Future<MailMessage> persist(
      Long userId, String body, String subject, String type, Consumer<MailMessage> extraConfigs) {
    return executorService.submit(() -> save(create(userId, body, subject, type, extraConfigs)));
  }

  @Transactional(rollbackOn = Exception.class)
  protected MailMessage save(MailMessage mailMessage) {
    return mailMessageRepository.save(mailMessage);
  }
}
