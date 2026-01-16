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

import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.TemplateContext;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.db.repo.TemplateRepository;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.Context;
import com.axelor.text.GroovyTemplates;
import com.axelor.text.StringTemplates;
import com.axelor.text.Templates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMessageServiceImpl implements TemplateMessageService {

  private static final String RECIPIENT_SEPARATOR = ";|,";
  private static final char TEMPLATE_DELIMITER = '$';

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private Model modelObject;

  protected final MessageService messageService;
  protected final MailAccountService mailAccountService;
  protected final TemplateContextService templateContextService;

  protected final MessageActionService messageActionService;
  protected final MessageRepository messageRepository;
  protected final TemplateRepository templateRepository;
  protected final EmailAddressRepository emailAddressRepository;
  protected final GroovyTemplates groovyTemplates;

  @Inject
  public TemplateMessageServiceImpl(
      EmailAddressRepository emailAddressRepository,
      GroovyTemplates groovyTemplates,
      MessageRepository messageRepository,
      TemplateRepository templateRepository,
      MailAccountService mailAccountService,
      MessageService messageService,
      TemplateContextService templateContextService,
      MessageActionService messageActionService) {
    this.emailAddressRepository = emailAddressRepository;
    this.templateRepository = templateRepository;
    this.messageRepository = messageRepository;
    this.messageService = messageService;
    this.mailAccountService = mailAccountService;
    this.templateContextService = templateContextService;
    this.messageActionService = messageActionService;
    this.groovyTemplates = groovyTemplates;
  }

  @Override
  public Message generateMessage(Model model, Template template) throws ClassNotFoundException {
    return generateMessage(model, template, false);
  }

  @Override
  public Message generateMessage(Model model, Template template, boolean isTemporaryMessage)
      throws ClassNotFoundException {

    this.modelObject = model;
    Class<?> klass = EntityHelper.getEntityClass(model);
    boolean isJson = Boolean.TRUE.equals(template.getIsJson());
    return generateMessage(
        model.getId() == null ? 0L : model.getId(),
        isJson ? ((MetaJsonModel) model).getName() : klass.getCanonicalName(),
        isJson ? ((MetaJsonModel) model).getName() : klass.getSimpleName(),
        template,
        isTemporaryMessage);
  }

  @Override
  public Message generateMessage(Long objectId, String model, String tag, Template template)
      throws ClassNotFoundException {
    return generateMessage(objectId, model, tag, template, false);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message generateMessage(
      Long objectId, String model, String tag, Template template, boolean isForTemporaryEmail)
      throws ClassNotFoundException {
    Templates templates;
    Map<String, Object> templatesContext = Maps.newHashMap();

    // Extract Templates implementation based on the template's engine - FACTORY DESIGN PATTERN
    if (template.getTemplateEngineSelect() == TemplateRepository.TEMPLATE_ENGINE_GROOVY_TEMPLATE) {
      templates = groovyTemplates;
    } else {
      templates = new StringTemplates(TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);
    }

    Object modelObj =
        Boolean.TRUE.equals(template.getIsJson())
            ? template.getMetaJsonModel()
            : template.getMetaModel();

    if (modelObj != null) {
      handleModelObjectTemplatesContext(objectId, model, tag, template, modelObj, templatesContext);
    }

    log.debug(
        "model : {}\ntag : {}\nobject id : {}\ntemplate : {}", model, tag, objectId, template);

    Message message =
        generateMessage(
            model, objectId, template, templates, templatesContext, isForTemporaryEmail);

    if (!isForTemporaryEmail) {
      log.debug("Saving message with meta files");
      message = saveMessageWithMetaFiles(template, message, templates, templatesContext);
      log.debug("Execute Post mail message actions");
      message = messageActionService.executePostMessageActions(message);
    }

    return message;
  }

  protected Message saveMessageWithMetaFiles(
      Template template,
      Message message,
      Templates templates,
      Map<String, Object> templatesContext) {
    message.setTemplate(templateRepository.find(template.getId()));
    message = messageRepository.save(message);
    messageService.attachMetaFiles(message, getMetaFiles(template, templates, templatesContext));
    return message;
  }

  protected void handleModelObjectTemplatesContext(
      Long objectId,
      String model,
      String tag,
      Template template,
      Object modelObj,
      Map<String, Object> templatesContext)
      throws ClassNotFoundException {
    String modelName =
        Boolean.TRUE.equals(template.getIsJson())
            ? ((MetaJsonModel) modelObj).getName()
            : ((MetaModel) modelObj).getFullName();
    if (!model.equals(modelName)) {
      throw new IllegalStateException(
          String.format(
              I18n.get(MessageExceptionMessage.INVALID_MODEL_TEMPLATE_EMAIL), modelName, model));
    }

    initAndComputeTemplatesContext(objectId, model, tag, template, templatesContext);
  }

  protected Map<String, Object> initAndComputeTemplatesContextForUnSavedObject(
      String tag, Template template, Map<String, Object> templatesContext) {
    Class<?> klass = EntityHelper.getEntityClass(this.modelObject);
    Context context = new Context(Mapper.toMap(this.modelObject), klass);

    templatesContext.put(tag, context.asType(klass));
    if (template.getTemplateContextList() != null) {
      for (TemplateContext templateContext : template.getTemplateContextList()) {
        Object result = templateContextService.computeTemplateContext(templateContext, context);
        templatesContext.put(templateContext.getName(), result);
      }
    }
    return templatesContext;
  }

  protected Map<String, Object> initAndComputeTemplatesContext(
      Long objectId,
      String model,
      String tag,
      Template template,
      Map<String, Object> templatesContext)
      throws ClassNotFoundException {
    if (objectId != 0L) {
      // initialize template context with object record
      initMaker(objectId, model, tag, template.getIsJson(), templatesContext);
      // evaluate template context list
      computeTemplateContexts(
          template.getTemplateContextList(),
          objectId,
          model,
          template.getIsJson(),
          templatesContext);
    } else {
      initAndComputeTemplatesContextForUnSavedObject(tag, template, templatesContext);
    }

    return templatesContext;
  }

  /**
   * This method is used to generate a message from the template's content and assign the email
   * address to the message
   */
  protected Message generateMessage(
      String model,
      Long objectId,
      Template template,
      Templates templates,
      Map<String, Object> templatesContext,
      boolean isForTemporaryEmail) {

    String content = "";
    String subject = "";
    String replyToRecipients = "";
    String toRecipients = "";
    String ccRecipients = "";
    String bccRecipients = "";
    String addressBlock = "";
    int mediaTypeSelect;
    String signature = "";

    if (!Strings.isNullOrEmpty(template.getContent())) {
      content = templates.fromText(template.getContent()).make(templatesContext).render();
    }

    if (!Strings.isNullOrEmpty(template.getAddressBlock())) {
      addressBlock = templates.fromText(template.getAddressBlock()).make(templatesContext).render();
    }

    if (!Strings.isNullOrEmpty(template.getSubject())) {
      subject = templates.fromText(template.getSubject()).make(templatesContext).render();
      log.debug("Subject ::: {}", subject);
    }

    if (!Strings.isNullOrEmpty(template.getReplyToRecipients())) {
      replyToRecipients =
          templates.fromText(template.getReplyToRecipients()).make(templatesContext).render();
      log.debug("Reply to ::: {}", replyToRecipients);
    }

    if (template.getToRecipients() != null) {
      toRecipients = templates.fromText(template.getToRecipients()).make(templatesContext).render();
      log.debug("To ::: {}", toRecipients);
    }

    if (template.getCcRecipients() != null) {
      ccRecipients = templates.fromText(template.getCcRecipients()).make(templatesContext).render();
      log.debug("CC ::: {}", ccRecipients);
    }

    if (template.getBccRecipients() != null) {
      bccRecipients =
          templates.fromText(template.getBccRecipients()).make(templatesContext).render();
      log.debug("BCC ::: {}", bccRecipients);
    }

    mediaTypeSelect = this.getMediaTypeSelect(template);
    log.debug("Media ::: {}", mediaTypeSelect);

    if (template.getSignature() != null) {
      signature = templates.fromText(template.getSignature()).make(templatesContext).render();
      log.debug("Signature ::: {}", signature);
    }

    EmailAccount mailAccount =
        template.getMailAccount() != null ? template.getMailAccount() : getMailAccount();
    EmailAddress fromAddress = null;

    if (mailAccount != null) {
      fromAddress = getMailAddress(mailAccount.getFromAddress());
    } else {
      IllegalStateException illegalStateException =
          new IllegalStateException(I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_6));
      log.error(illegalStateException.getMessage(), illegalStateException);
    }

    return messageService.createMessage(
        model,
        Math.toIntExact(objectId),
        subject,
        content,
        fromAddress,
        getEmailAddresses(replyToRecipients),
        getEmailAddresses(toRecipients),
        getEmailAddresses(ccRecipients),
        getEmailAddresses(bccRecipients),
        null,
        addressBlock,
        mediaTypeSelect,
        mailAccount,
        signature,
        isForTemporaryEmail);
  }

  @Override
  public Message generateAndSendMessage(Model model, Template template)
      throws IOException, ClassNotFoundException {

    Message message = this.generateMessage(model, template);
    messageService.sendMessage(message);

    return message;
  }

  @Override
  public Message generateAndSendTemporaryMessage(Model model, Template template)
      throws MessagingException, IOException, ClassNotFoundException {

    Message message = this.generateMessage(model, template, true);
    messageService.sendMessage(message, true);

    return message;
  }

  @Override
  public Set<MetaFile> getMetaFiles(Template template) {
    List<DMSFile> metaAttachments =
        Query.of(DMSFile.class)
            .filter(
                "self.relatedId = ?1 AND self.relatedModel = ?2",
                template.getId(),
                EntityHelper.getEntityClass(template).getName())
            .fetch();
    Set<MetaFile> metaFiles = Sets.newHashSet();
    for (DMSFile metaAttachment : metaAttachments) {
      if (Boolean.FALSE.equals(metaAttachment.getIsDirectory())) {
        metaFiles.add(metaAttachment.getMetaFile());
      }
    }

    log.debug("Metafile to attach: {}", metaFiles);
    return metaFiles;
  }

  @Override
  public Set<MetaFile> getMetaFiles(
      Template template, Templates templates, Map<String, Object> templatesContext) {
    return getMetaFiles(template);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Object> initMaker(
      long objectId, String model, String tag, boolean isJson, Map<String, Object> templatesContext)
      throws ClassNotFoundException {

    if (isJson) {
      templatesContext.put(tag, JPA.find(MetaJsonRecord.class, objectId));
      return templatesContext;
    }

    Class<? extends Model> myClass = (Class<? extends Model>) Class.forName(model);
    templatesContext.put(tag, JPA.find(myClass, objectId));

    return templatesContext;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> computeTemplateContexts(
      List<TemplateContext> templateContextList,
      long objectId,
      String model,
      boolean isJson,
      Map<String, Object> templatesContext)
      throws ClassNotFoundException {

    if (templateContextList == null) {
      return templatesContext;
    }

    Context context;
    if (isJson) {
      context = new Context(objectId, MetaJsonRecord.class);
    } else {
      Class<? extends Model> myClass = (Class<? extends Model>) Class.forName(model);
      context = new Context(objectId, myClass);
    }

    for (TemplateContext templateContext : templateContextList) {
      Object result = templateContextService.computeTemplateContext(templateContext, context);
      templatesContext.put(templateContext.getName(), result);
    }

    return templatesContext;
  }

  /**
   * Return a list of email addresses from the {@code recipients} string.
   *
   * @param recipients
   * @return
   */
  protected List<EmailAddress> getEmailAddresses(String recipients) {

    if (Strings.isNullOrEmpty(recipients)) {
      return Collections.emptyList();
    }

    return Splitter.onPattern(RECIPIENT_SEPARATOR)
        .trimResults()
        .omitEmptyStrings()
        .splitToList(recipients)
        .stream()
        .map(this::getMailAddress)
        .toList();
  }

  /**
   * Get {@link EmailAddress} by {@code recipient} string or create it if it's not found.
   *
   * @param recipient
   * @return
   */
  protected EmailAddress getMailAddress(String recipient) {

    if (Strings.isNullOrEmpty(recipient)) {
      return null;
    }

    EmailAddress emailAddress = emailAddressRepository.findByAddress(recipient);

    if (emailAddress == null) {
      Map<String, Object> values = new HashMap<>();
      values.put("address", recipient);
      emailAddress = emailAddressRepository.create(values);
    }

    return emailAddress;
  }

  protected Integer getMediaTypeSelect(Template template) {
    return template.getMediaTypeSelect();
  }

  protected EmailAccount getMailAccount() {
    return mailAccountService.getDefaultSender();
  }
}
