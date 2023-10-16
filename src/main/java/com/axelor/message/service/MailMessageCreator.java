package com.axelor.message.service;

import com.axelor.mail.db.MailMessage;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface MailMessageCreator {

  /**
   * Creates a new MailMessage object with the given parameters.
   *
   * @param userId the ID of the user who authored the message
   * @param body the body of the message
   * @param subject the subject of the message
   * @param type the type of the message
   * @return the newly created MailMessage object
   */
  MailMessage create(Long userId, String body, String subject, String type);

  /**
   * Creates a new MailMessage object with the given parameters and applies extra configurations to
   * it.
   *
   * @param userId the ID of the user who authored the message
   * @param body the body of the message
   * @param subject the subject of the message
   * @param type the type of the message
   * @param extraConfigs a Consumer that applies extra configurations to the MailMessage object
   * @return the newly created MailMessage object
   */
  MailMessage create(
      Long userId, String body, String subject, String type, Consumer<MailMessage> extraConfigs);

  /**
   * Creates a new MailMessage object with the given parameters and persists it asynchronously.
   *
   * @param userId the ID of the user who authored the message
   * @param body the body of the message
   * @param subject the subject of the message
   * @param type the type of the message
   * @return a Future object representing the asynchronous persistence operation
   */
  Future<MailMessage> persist(Long userId, String body, String subject, String type);

  /**
   * Creates a new MailMessage object with the given parameters, applies extra configurations to it,
   * and persists it asynchronously.
   *
   * @param userId the ID of the user who authored the message
   * @param body the body of the message
   * @param subject the subject of the message
   * @param type the type of the message
   * @param extraConfigs a Consumer that applies extra configurations to the MailMessage object
   * @return a Future object representing the asynchronous persistence operation
   */
  Future<MailMessage> persist(
      Long userId, String body, String subject, String type, Consumer<MailMessage> extraConfigs);
}
