/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.message.service;

import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.JpaSupport;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.mail.MailBuilder;
import com.axelor.mail.MailSender;
import com.axelor.mail.SmtpAccount;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.MultiRelated;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.db.repo.MultiRelatedRepository;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.utils.helpers.ExceptionHelper;
import com.axelor.utils.helpers.json.JsonHelper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import javax.persistence.LockModeType;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.groovy.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageServiceImpl extends JpaSupport implements MessageService {

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final MetaAttachmentRepository metaAttachmentRepository;
  protected final MessageRepository messageRepository;
  protected final SendMailQueueService sendMailQueueService;
  protected final AppSettingsMessageService appSettingsMessageService;

  @Inject
  public MessageServiceImpl(
      MetaAttachmentRepository metaAttachmentRepository,
      MessageRepository messageRepository,
      SendMailQueueService sendMailQueueService,
      AppSettingsMessageService appSettingsMessageService) {
    this.metaAttachmentRepository = metaAttachmentRepository;
    this.messageRepository = messageRepository;
    this.sendMailQueueService = sendMailQueueService;
    this.appSettingsMessageService = appSettingsMessageService;
  }

  @Override
  @Transactional
  public Message createMessage(
      String model,
      long id,
      String subject,
      String content,
      EmailAddress fromEmailAddress,
      List<EmailAddress> replyToEmailAddressList,
      List<EmailAddress> toEmailAddressList,
      List<EmailAddress> ccEmailAddressList,
      List<EmailAddress> bccEmailAddressList,
      Set<MetaFile> metaFiles,
      String addressBlock,
      int mediaTypeSelect,
      EmailAccount emailAccount,
      String signature) {

    emailAccount =
        emailAccount != null
            ? Beans.get(EmailAccountRepository.class).find(emailAccount.getId())
            : emailAccount;
    Message message =
        createMessage(
            content,
            fromEmailAddress,
            false,
            MessageRepository.STATUS_DRAFT,
            subject,
            MessageRepository.TYPE_SENT,
            replyToEmailAddressList,
            toEmailAddressList,
            ccEmailAddressList,
            bccEmailAddressList,
            addressBlock,
            mediaTypeSelect,
            emailAccount,
            signature);
    addMessageRelatedTo(message, model, id);

    messageRepository.save(message);

    attachMetaFiles(message, metaFiles);

    return message;
  }

  @Override
  public Message createMessage(
      String model,
      long id,
      String subject,
      String content,
      EmailAddress fromEmailAddress,
      List<EmailAddress> replyToEmailAddressList,
      List<EmailAddress> toEmailAddressList,
      List<EmailAddress> ccEmailAddressList,
      List<EmailAddress> bccEmailAddressList,
      Set<MetaFile> metaFiles,
      String addressBlock,
      int mediaTypeSelect,
      EmailAccount emailAccount,
      String signature,
      Boolean isForTemporaryMessage) {

    if (!isForTemporaryMessage) {
      return createMessage(
          model,
          id,
          subject,
          content,
          fromEmailAddress,
          replyToEmailAddressList,
          toEmailAddressList,
          ccEmailAddressList,
          bccEmailAddressList,
          metaFiles,
          addressBlock,
          mediaTypeSelect,
          emailAccount,
          signature);
    }

    emailAccount =
        emailAccount != null
            ? Beans.get(EmailAccountRepository.class).find(emailAccount.getId())
            : emailAccount;
    Message message =
        createMessage(
            content,
            fromEmailAddress,
            false,
            MessageRepository.STATUS_DRAFT,
            subject,
            MessageRepository.TYPE_SENT,
            replyToEmailAddressList,
            toEmailAddressList,
            ccEmailAddressList,
            bccEmailAddressList,
            addressBlock,
            mediaTypeSelect,
            emailAccount,
            signature);
    addMessageRelatedTo(message, model, id);

    return message;
  }

  @Override
  @Transactional
  public void attachMetaFiles(Message message, Set<MetaFile> metaFiles) {

    Preconditions.checkNotNull(message.getId());

    if (metaFiles == null || metaFiles.isEmpty()) {
      return;
    }

    log.debug("Add metafiles to object {}:{}", Message.class.getName(), message.getId());

    for (MetaFile metaFile : metaFiles) {
      Beans.get(MetaFiles.class).attach(metaFile, metaFile.getFileName(), message);
    }
  }

  protected Message createMessage(
      String content,
      EmailAddress fromEmailAddress,
      boolean sentByEmail,
      int statusSelect,
      String subject,
      int typeSelect,
      List<EmailAddress> replyToEmailAddressList,
      List<EmailAddress> toEmailAddressList,
      List<EmailAddress> ccEmailAddressList,
      List<EmailAddress> bccEmailAddressList,
      String addressBlock,
      int mediaTypeSelect,
      EmailAccount emailAccount,
      String signature) {

    Set<EmailAddress> replyToEmailAddressSet = Sets.newHashSet();
    Set<EmailAddress> bccEmailAddressSet = Sets.newHashSet();
    Set<EmailAddress> toEmailAddressSet = Sets.newHashSet();
    Set<EmailAddress> ccEmailAddressSet = Sets.newHashSet();

    if (mediaTypeSelect == MessageRepository.MEDIA_TYPE_EMAIL) {
      if (replyToEmailAddressList != null) {
        replyToEmailAddressSet.addAll(replyToEmailAddressList);
      }
      if (bccEmailAddressList != null) {
        bccEmailAddressSet.addAll(bccEmailAddressList);
      }
      if (toEmailAddressList != null) {
        toEmailAddressSet.addAll(toEmailAddressList);
      }
      if (ccEmailAddressList != null) {
        ccEmailAddressSet.addAll(ccEmailAddressList);
      }
    }

    if (!Strings.isNullOrEmpty(signature)) {
      content += "<p></p><p></p>" + signature;
    } else if (emailAccount != null) {
      content += "<p></p><p></p>" + Beans.get(MailAccountService.class).getSignature(emailAccount);
    }

    return new Message(
        typeSelect,
        subject,
        content,
        statusSelect,
        mediaTypeSelect,
        addressBlock,
        fromEmailAddress,
        replyToEmailAddressSet,
        toEmailAddressSet,
        ccEmailAddressSet,
        bccEmailAddressSet,
        sentByEmail,
        emailAccount);
  }

  @Override
  public Message sendMessage(Message message) throws IOException {
    try {
      sendMessage(message, false);
    } catch (MessagingException e) {
      ExceptionHelper.trace(e);
    }
    return message;
  }

  @Override
  public Message sendMessage(Message message, Boolean isTemporaryEmail)
      throws MessagingException, IOException {

    if (!isTemporaryEmail) {
      if (message.getMediaTypeSelect() == MessageRepository.MEDIA_TYPE_MAIL) {
        message = sendByMail(message);
      } else if (message.getMediaTypeSelect() == MessageRepository.MEDIA_TYPE_EMAIL) {
        message = sendByEmail(message);
      } else if (message.getMediaTypeSelect() == MessageRepository.MEDIA_TYPE_CHAT) {
        message = sendToUser(message);
      } else if (message.getMediaTypeSelect() == MessageRepository.MEDIA_TYPE_SMS) {
        message = sendSMS(message);
      }
    } else {
      if (message.getMediaTypeSelect() != MessageRepository.MEDIA_TYPE_EMAIL) {
        throw new IllegalStateException(
            I18n.get(MessageExceptionMessage.TEMPORARY_EMAIL_MEDIA_TYPE_ERROR));
      }
      message = sendByEmail(message, isTemporaryEmail);
    }

    return message;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendToUser(Message message) {

    if (message.getRecipientUser() == null) {
      return message;
    }

    message.setSenderUser(AuthUtils.getUser());
    log.debug("Sent internal message to user ::: {}", message.getRecipientUser());

    message.setStatusSelect(MessageRepository.STATUS_SENT);
    message.setSentByEmail(false);
    message.setSentDateT(LocalDateTime.now());
    return messageRepository.save(message);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendByMail(Message message) {

    log.debug("Sent mail");
    message.setStatusSelect(MessageRepository.STATUS_SENT);
    message.setSentByEmail(false);
    message.setSentDateT(LocalDateTime.now());
    return messageRepository.save(message);
  }

  @Override
  public Message sendByEmail(Message message) throws MessagingException {
    return sendByEmail(message, false);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendByEmail(Message message, Boolean isTemporaryEmail) throws MessagingException {

    EmailAccount mailAccount = message.getMailAccount();

    if (mailAccount == null) {
      return message;
    }

    log.debug("Sending email...");
    MailAccountService mailAccountService = Beans.get(MailAccountService.class);
    com.axelor.mail.MailAccount account =
        new SmtpAccount(
            mailAccount.getHost(),
            mailAccount.getPort().toString(),
            mailAccount.getLogin(),
            mailAccountService.getDecryptPassword(mailAccount.getPassword()),
            mailAccountService.getSecurity(mailAccount));

    List<String> replytoRecipients = this.getEmailAddresses(message.getReplyToEmailAddressSet());
    List<String> toRecipients = this.getEmailAddresses(message.getToEmailAddressSet());
    List<String> ccRecipients = this.getEmailAddresses(message.getCcEmailAddressSet());
    List<String> bccRecipients = this.getEmailAddresses(message.getBccEmailAddressSet());

    if (toRecipients.isEmpty() && ccRecipients.isEmpty() && bccRecipients.isEmpty()) {
      throw new IllegalStateException(I18n.get(MessageExceptionMessage.MESSAGE_6));
    }

    MailSender sender = new MailSender(account);
    MailBuilder mailBuilder = sender.compose();

    mailBuilder.subject(message.getSubject());

    if (!Strings.isNullOrEmpty(mailAccount.getFromAddress())) {
      String fromAddress = mailAccount.getFromAddress();
      if (!Strings.isNullOrEmpty(mailAccount.getFromName())) {
        String fromName;
        try {
          fromName = MimeUtility.encodeText(mailAccount.getFromName(), "UTF-8", "B");
        } catch (UnsupportedEncodingException e) {
          fromName = mailAccount.getFromName();
        }
        fromAddress = String.format("%s <%s>", fromName, mailAccount.getFromAddress());
      } else if (message.getFromEmailAddress() != null) {
        if (!Strings.isNullOrEmpty(message.getFromEmailAddress().getAddress())) {
          log.debug(
              "Override from :::  {}", this.getFullEmailAddress(message.getFromEmailAddress()));
          mailBuilder.from(this.getFullEmailAddress(message.getFromEmailAddress()));
        } else {
          throw new IllegalStateException(I18n.get(MessageExceptionMessage.MESSAGE_5));
        }
      }
      mailBuilder.from(fromAddress);
    }
    if (replytoRecipients != null && !replytoRecipients.isEmpty()) {
      mailBuilder.replyTo(Joiner.on(",").join(replytoRecipients));
    }
    if (!toRecipients.isEmpty()) {
      mailBuilder.to(Joiner.on(",").join(toRecipients));
    }
    if (ccRecipients != null && !ccRecipients.isEmpty()) {
      mailBuilder.cc(Joiner.on(",").join(ccRecipients));
    }
    if (bccRecipients != null && !bccRecipients.isEmpty()) {
      mailBuilder.bcc(Joiner.on(",").join(bccRecipients));
    }
    if (!Strings.isNullOrEmpty(message.getContent())) {
      mailBuilder.html(message.getContent());
    } else {
      mailBuilder.html("");
    }

    if (!Boolean.TRUE.equals(isTemporaryEmail)) {
      for (MetaAttachment metaAttachment : getMetaAttachments(message)) {
        MetaFile metaFile = metaAttachment.getMetaFile();
        mailBuilder.attach(metaFile.getFileName(), MetaFiles.getPath(metaFile).toString());
      }

      getEntityManager().flush();
      getEntityManager().lock(message, LockModeType.PESSIMISTIC_WRITE);
      // send email using a separate process to avoid thread blocking
      sendMailQueueService.submitMailJob(mailBuilder, message);

    } else {

      // Sending email(message) which is not saved.
      // No separate thread or JPA persistence lock required
      try {
        mailBuilder.send();
      } catch (IOException e) {
        log.error("Exception when sending email: {}", e.getMessage(), e);
      }

      log.debug("Email sent.");
    }

    return message;
  }

  @Override
  @SuppressWarnings("deprecation")
  @Transactional(rollbackOn = {Exception.class})
  public Message sendSMS(Message message) throws IOException {

    if (message.getMailAccount() == null
        || Strings.isNullOrEmpty(message.getMailAccount().getBrevoApiKey())) {
      return message;
    }
    if (Strings.isNullOrEmpty(message.getToMobilePhone())) {
      throw new IllegalStateException(
          I18n.get(MessageExceptionMessage.SMS_ERROR_MISSING_MOBILE_NUMBER));
    }

    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.connectTimeout(30, TimeUnit.SECONDS);
    builder.readTimeout(30, TimeUnit.SECONDS);
    builder.writeTimeout(30, TimeUnit.SECONDS);
    OkHttpClient client = new OkHttpClient(builder);
    MediaType mediaType = MediaType.parse("application/json");

    var map =
        Maps.of(
            "sender",
            getSender(message),
            "recipient",
            message.getToMobilePhone(),
            "content",
            message.getContent().replaceAll("<\\/*\\w+>", ""),
            "type",
            "transactional");

    String datas = JsonHelper.toJson(map);

    RequestBody body = RequestBody.create(mediaType, datas);
    Request request =
        new Request.Builder()
            .url(appSettingsMessageService.sendingblueUrlSendsms())
            .header("api-key", message.getMailAccount().getBrevoApiKey())
            .post(body)
            .build();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() || response.code() != 201) {
        throw new IllegalStateException(
            String.format("%d %s", response.code(), response.message()));
      }
    }

    message.setStatusSelect(MessageRepository.STATUS_SENT);
    message.setSentDateT(LocalDateTime.now());

    message.setSentByEmail(false);
    return message;
  }

  protected String getSender(Message message) {
    return message.getSenderUser().getCode();
  }

  public Set<MetaAttachment> getMetaAttachments(Message message) {

    Query<MetaAttachment> query =
        metaAttachmentRepository
            .all()
            .filter(
                "self.objectId = ?1 AND self.objectName = ?2",
                message.getId(),
                Message.class.getName());
    return Sets.newHashSet(query.fetch());
  }

  public List<String> getEmailAddresses(Set<EmailAddress> emailAddressSet) {

    List<String> recipients = Lists.newArrayList();
    if (emailAddressSet != null) {
      for (EmailAddress emailAddress : emailAddressSet) {

        if (Strings.isNullOrEmpty(emailAddress.getAddress())) {
          continue;
        }
        recipients.add(this.getFullEmailAddress(emailAddress));
      }
    }

    return recipients;
  }

  @Override
  public String printMessage(Message message) {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = {Exception.class})
  public Message regenerateMessage(Message message)
      throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    Preconditions.checkNotNull(
        message.getTemplate(),
        I18n.get("Cannot regenerate message without template associated to message."));

    if (ObjectUtils.isEmpty(message.getMultiRelatedList())) {
      throw new IllegalStateException(I18n.get("Cannot regenerate message without related model."));
    }

    MultiRelated multiRelated = message.getMultiRelatedList().get(0);

    Model model =
        JPA.all((Class<? extends Model>) Class.forName(multiRelated.getRelatedToSelect()))
            .filter("self.id = ?", multiRelated.getRelatedToSelectId())
            .fetchOne();
    Message newMessage =
        Beans.get(TemplateMessageService.class).generateMessage(model, message.getTemplate());
    newMessage.setMultiRelatedList(message.getMultiRelatedList());
    message.setArchived(true);
    return newMessage;
  }

  @Override
  public String getFullEmailAddress(EmailAddress emailAddress) {
    return emailAddress.getAddress();
  }

  @Override
  public void addMessageRelatedTo(Message message, String relatedToSelect, Long relatedToSelectId) {

    MultiRelated multiRelated =
        Beans.get(MultiRelatedRepository.class)
            .all()
            .filter(
                "self.message  = :message AND self.relatedToSelect = :relatedToSelect AND self.relatedToSelectId = :relatedToSelectId")
            .bind("message", message)
            .bind("relatedToSelect", relatedToSelect)
            .bind("relatedToSelectId", relatedToSelectId)
            .fetchOne();
    if (multiRelated != null) {
      return;
    }

    multiRelated = new MultiRelated();
    multiRelated.setRelatedToSelect(relatedToSelect);
    multiRelated.setRelatedToSelectId(relatedToSelectId);
    message.addMultiRelatedListItem(multiRelated);
  }
}
