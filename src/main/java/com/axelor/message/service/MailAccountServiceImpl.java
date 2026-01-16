/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2026 Axelor (<http://axelor.com>).
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

import com.axelor.i18n.I18n;
import com.axelor.mail.ImapAccount;
import com.axelor.mail.MailAccount;
import com.axelor.mail.MailConstants;
import com.axelor.mail.MailParser;
import com.axelor.mail.MailReader;
import com.axelor.mail.Pop3Account;
import com.axelor.mail.SmtpAccount;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.helpers.ExceptionHelper;
import com.axelor.utils.helpers.date.LocalDateTimeHelper;
import com.axelor.utils.service.CipherService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.activation.DataSource;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailAccountServiceImpl implements MailAccountService {

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected static final int CHECK_CONF_TIMEOUT = 5000;
  protected final EmailAccountRepository emailAccountRepo;
  protected final CipherService cipherService;
  protected final EmailAddressRepository emailAddressRepo;
  protected final MessageRepository messageRepo;
  protected final MetaFiles metaFiles;

  @Inject
  public MailAccountServiceImpl(
      EmailAccountRepository emailAccountRepo,
      CipherService cipherService,
      EmailAddressRepository emailAddressRepo,
      MessageRepository messageRepo,
      MetaFiles metaFiles) {
    this.emailAccountRepo = emailAccountRepo;
    this.cipherService = cipherService;
    this.emailAddressRepo = emailAddressRepo;
    this.messageRepo = messageRepo;
    this.metaFiles = metaFiles;
  }

  @Override
  public void checkDefaultMailAccount(EmailAccount emailAccount) {

    if (!Boolean.TRUE.equals(emailAccount.getIsDefault())) {
      return;
    }

    String query = "self.isDefault = true";
    List<Object> params = Lists.newArrayList();
    if (emailAccount.getId() != null) {
      query += " AND self.id != ?1";
      params.add(emailAccount.getId());
    }

    Integer serverTypeSelect = emailAccount.getServerTypeSelect();
    if (serverTypeSelect == EmailAccountRepository.SERVER_TYPE_SMTP) {
      query += " AND self.serverTypeSelect = " + EmailAccountRepository.SERVER_TYPE_SMTP + " ";
    } else if (serverTypeSelect == EmailAccountRepository.SERVER_TYPE_IMAP
        || serverTypeSelect == EmailAccountRepository.SERVER_TYPE_POP) {
      query +=
          " AND (self.serverTypeSelect = "
              + EmailAccountRepository.SERVER_TYPE_IMAP
              + " OR "
              + "self.serverTypeSelect = "
              + EmailAccountRepository.SERVER_TYPE_POP
              + ") ";
    }

    long count = emailAccountRepo.all().filter(query, params.toArray()).count();
    if (count > 0) {
      throw new IllegalStateException(I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_5));
    }
  }

  @Override
  public EmailAccount getDefaultSender() {

    return emailAccountRepo
        .all()
        .filter(
            "self.isDefault = true AND self.serverTypeSelect = ?1",
            EmailAccountRepository.SERVER_TYPE_SMTP)
        .fetchOne();
  }

  @Override
  public EmailAccount getDefaultReader() {

    return emailAccountRepo
        .all()
        .filter(
            "self.isDefault = true "
                + "AND (self.serverTypeSelect = ?1 OR self.serverTypeSelect = ?2)",
            EmailAccountRepository.SERVER_TYPE_IMAP,
            EmailAccountRepository.SERVER_TYPE_POP)
        .fetchOne();
  }

  @Override
  @Transactional(ignore = Exception.class)
  public void checkMailAccountConfiguration(EmailAccount emailAccount) throws MessagingException {
    try {
      com.axelor.mail.MailAccount account = getMailAccount(emailAccount);
      Session session = account.getSession();

      if (emailAccount.getServerTypeSelect().equals(EmailAccountRepository.SERVER_TYPE_SMTP)) {
        Transport transport = session.getTransport(getProtocol(emailAccount));
        transport.connect(
            emailAccount.getHost(),
            emailAccount.getPort(),
            emailAccount.getLogin(),
            emailAccount.getPassword());
        transport.close();
      } else {
        session.getStore().connect();
      }
      emailAccount.setIsValid(true);
    } catch (AuthenticationFailedException e) {
      emailAccount.setIsValid(false);
      throw new IllegalStateException(I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_1), e);
    } catch (NoSuchProviderException e) {
      emailAccount.setIsValid(false);
      throw new IllegalStateException(I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_2), e);
    } finally {
      emailAccountRepo.save(emailAccount);
    }
  }

  @Override
  public com.axelor.mail.MailAccount getMailAccount(EmailAccount mailAccount) {

    Integer serverType = mailAccount.getServerTypeSelect();

    String port = mailAccount.getPort() <= 0 ? null : mailAccount.getPort().toString();

    MailAccount account =
        switch (serverType) {
          case EmailAccountRepository.SERVER_TYPE_SMTP ->
              new SmtpAccount(
                  mailAccount.getHost(),
                  port,
                  mailAccount.getLogin(),
                  getDecryptPassword(mailAccount.getPassword()),
                  getSecurity(mailAccount));

          case EmailAccountRepository.SERVER_TYPE_IMAP ->
              new ImapAccount(
                  mailAccount.getHost(),
                  mailAccount.getPort().toString(),
                  mailAccount.getLogin(),
                  getDecryptPassword(mailAccount.getPassword()),
                  getSecurity(mailAccount));

          default ->
              new Pop3Account(
                  mailAccount.getHost(),
                  mailAccount.getPort().toString(),
                  mailAccount.getLogin(),
                  getDecryptPassword(mailAccount.getPassword()),
                  getSecurity(mailAccount));
        };

    Properties props = account.getSession().getProperties();
    if (mailAccount.getFromAddress() != null && !"".equals(mailAccount.getFromAddress())) {
      props.setProperty("mail.smtp.from", mailAccount.getFromAddress());
    }
    if (mailAccount.getFromName() != null && !"".equals(mailAccount.getFromName())) {
      props.setProperty("mail.smtp.from.personal", mailAccount.getFromName());
    }
    account.setConnectionTimeout(CHECK_CONF_TIMEOUT);

    return account;
  }

  public String getSecurity(EmailAccount emailAccount) {

    if (emailAccount.getSecuritySelect() == EmailAccountRepository.SECURITY_SSL) {
      return MailConstants.CHANNEL_SSL;
    } else if (emailAccount.getSecuritySelect() == EmailAccountRepository.SECURITY_STARTTLS) {
      return MailConstants.CHANNEL_STARTTLS;
    } else {
      return null;
    }
  }

  public String getProtocol(EmailAccount emailAccount) {

    return switch (emailAccount.getServerTypeSelect()) {
      case EmailAccountRepository.SERVER_TYPE_SMTP -> "smtp";
      case EmailAccountRepository.SERVER_TYPE_IMAP -> {
        if (emailAccount.getSecuritySelect() == EmailAccountRepository.SECURITY_SSL) {
          yield MailConstants.PROTOCOL_IMAPS;
        }
        yield MailConstants.PROTOCOL_IMAP;
      }
      case EmailAccountRepository.SERVER_TYPE_POP -> MailConstants.PROTOCOL_POP3;
      default -> "";
    };
  }

  public String getSignature(EmailAccount emailAccount) {

    if (emailAccount != null && emailAccount.getSignature() != null) {
      return "\n " + emailAccount.getSignature();
    }
    return "";
  }

  @Override
  public int fetchEmails(EmailAccount emailAccount, boolean unseenOnly)
      throws MessagingException, IOException {

    if (emailAccount == null) {
      return 0;
    }

    String host = emailAccount.getHost();
    Integer port = emailAccount.getPort();
    String login = emailAccount.getLogin();
    String password = emailAccount.getPassword();

    log.debug("Fetching emails from host: {}, port: {}, login: {} ", host, port, login);

    MailAccount account =
        emailAccount.getServerTypeSelect().equals(EmailAccountRepository.SERVER_TYPE_IMAP)
            ? new ImapAccount(host, port.toString(), login, password, getSecurity(emailAccount))
            : new Pop3Account(host, port.toString(), login, password, getSecurity(emailAccount));

    MailReader reader = new MailReader(account);
    final Store store = reader.getStore();
    final Folder inbox = store.getFolder("INBOX");

    // open as READ_WRITE to mark messages as seen
    inbox.open(Folder.READ_WRITE);

    // find all unseen messages
    final FetchProfile profile = new FetchProfile();
    jakarta.mail.Message[] messages;
    if (unseenOnly) {
      final FlagTerm unseen = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
      messages = inbox.search(unseen);
    } else {
      messages = inbox.getMessages();
    }

    profile.add(FetchProfile.Item.ENVELOPE);

    // actually fetch the messages
    inbox.fetch(messages, profile);
    log.debug("Total emails unseen: {}", messages.length);

    int count = 0;
    for (jakarta.mail.Message message : messages) {
      if (message instanceof MimeMessage mimeMessage) {
        MailParser parser = new MailParser(mimeMessage);
        parser.parse();
        createMessage(emailAccount, parser, message.getSentDate());
        count++;
      }
    }

    log.debug("Total emails fetched: {}", count);

    return count;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Message createMessage(EmailAccount mailAccount, MailParser parser, Date date)
      throws MessagingException {

    Message message = new Message();

    message.setMailAccount(mailAccount);
    message.setTypeSelect(MessageRepository.TYPE_SENT);
    message.setMediaTypeSelect(MessageRepository.MEDIA_TYPE_EMAIL);

    message.setFromEmailAddress(getEmailAddress(parser.getFrom()));
    message.setCcEmailAddressSet(getEmailAddressSet(parser.getCc()));
    message.setBccEmailAddressSet(getEmailAddressSet(parser.getBcc()));
    message.setToEmailAddressSet(getEmailAddressSet(parser.getTo()));
    message.addReplyToEmailAddressSetItem(getEmailAddress(parser.getReplyTo()));

    message.setContent(parser.getHtml());
    message.setSubject(parser.getSubject());
    message.setSentDateT(LocalDateTimeHelper.toLocalDateT(date));

    message = messageRepo.save(message);

    List<DataSource> attachments = parser.getAttachments();
    addAttachments(message, attachments);

    return message;
  }

  private EmailAddress getEmailAddress(InternetAddress address) {

    EmailAddress emailAddress;
    emailAddress = emailAddressRepo.findByAddress(address.getAddress());
    if (emailAddress == null) {
      emailAddress = createEmailAddress(address.getAddress());
    }

    return emailAddress;
  }

  @Transactional(rollbackOn = Exception.class)
  private EmailAddress createEmailAddress(String address) {
    EmailAddress emailAddress = new EmailAddress();
    emailAddress.setAddress(address);
    return emailAddressRepo.save(emailAddress);
  }

  private Set<EmailAddress> getEmailAddressSet(List<InternetAddress> addresses) {

    Set<EmailAddress> addressSet = new HashSet<>();

    if (addresses == null) {
      return addressSet;
    }

    for (InternetAddress address : addresses) {

      EmailAddress emailAddress = getEmailAddress(address);

      addressSet.add(emailAddress);
    }

    return addressSet;
  }

  private void addAttachments(Message message, List<DataSource> attachments) {

    if (attachments == null) {
      return;
    }

    for (DataSource source : attachments) {
      try {
        InputStream stream = source.getInputStream();
        metaFiles.attach(stream, source.getName(), message);
      } catch (IOException e) {
        ExceptionHelper.error(e);
      }
    }
  }

  @Override
  public String getEncryptPassword(String password) {

    return cipherService.encrypt(password);
  }

  @Override
  public String getDecryptPassword(String password) {

    return cipherService.decrypt(password);
  }
}
