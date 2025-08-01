/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2025 Axelor (<http://axelor.com>).
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

import com.axelor.db.Model;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.TemplateContext;
import com.axelor.meta.db.MetaFile;
import com.axelor.text.Templates;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;

public interface TemplateMessageService {

  /**
   * This method is used to generate a non-temporary message
   *
   * @param model: Model
   * @param template: Template
   * @return persisted message
   * @throws ClassNotFoundException
   */
  public Message generateMessage(Model model, Template template) throws ClassNotFoundException;

  /**
   * This method is used to store the model object in the service and then generate the message
   * based on the parameters Note : - the modelObject is useful when the model is not persisted yet.
   *
   * @param model: Model
   * @param template: Template
   * @param isTemporaryMessage
   * @return Message
   * @throws ClassNotFoundException
   */
  public Message generateMessage(Model model, Template template, Boolean isTemporaryMessage)
      throws ClassNotFoundException;

  /**
   * This method is used to generate message from corresponding params
   *
   * @param objectId : id of the object record to send in the message
   * @param model : the fullName model ex: "com.axelor.contact.db.Company" (class canonicalName)
   * @param tag : model name ex: "Company" (class simpleName)
   * @param template : associated template with model
   * @return persisted Message
   * @throws ClassNotFoundException
   */
  public Message generateMessage(Long objectId, String model, String tag, Template template)
      throws ClassNotFoundException;

  /**
   * This method is used to compute template context, and use the right template engine to copy
   * content. if the message is not temporary it saves it and attach metafiles
   *
   * @param objectId : id of the object record to send in the message
   * @param model : the fullName model ex: "com.axelor.contact.db.Company" (class canonicalName)
   * @param tag : model name ex: "Company" (class simpleName)
   * @param template : associated template with model
   * @param isForTemporaryMessage
   * @return Message
   * @throws ClassNotFoundException
   */
  public Message generateMessage(
      Long objectId, String model, String tag, Template template, Boolean isForTemporaryMessage)
      throws ClassNotFoundException;

  /**
   * Generate and Send the {@link Message}.
   *
   * @param model : Model
   * @param template : Template
   * @return persisted message
   * @throws MessagingException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public Message generateAndSendMessage(Model model, Template template)
      throws IOException, ClassNotFoundException;

  /**
   * Send the transient {@link Message} generated from the template and model.<br>
   * Note: {@link Template} should be of Email type.
   *
   * @param model
   * @param template
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public Message generateAndSendTemporaryMessage(Model model, Template template)
      throws MessagingException, IOException, ClassNotFoundException;

  /**
   * This method is used to update the set of message metafiles with metaAttachment from the
   * template
   *
   * @param template: Template of message
   * @return Set of metafiles attached to the message
   */
  Set<MetaFile> getMetaFiles(Template template);

  /**
   * This method is used to update the set of message metafiles with metaAttachment from the
   * template
   *
   * @param template: Template of message
   * @param templates: Template Implementation
   * @param templatesContext: template context map
   * @return Set of metafiles attached to the message
   */
  public Set<MetaFile> getMetaFiles(
      Template template, Templates templates, Map<String, Object> templatesContext);

  /**
   * This method is used to insert the tag as key and model instance record as a value in the map
   * templatesContext for the given model objectId. if it's a JSON search, it uses the
   * MetaJsonRecord class, otherwise uses generic class of the model.
   *
   * @param objectId : id of the object record to send in the message
   * @param model : the fullName model ex: "com.axelor.contact.db.Company" (class canonicalName)
   * @param tag : model name ex: "Company" (class simpleName)
   * @param isJson: if the model is a {@link com.axelor.meta.db.MetaJsonModel} or a {@link
   *     com.axelor.meta.db.MetaModel}
   * @param templatesContext: holds key value pairs from template's context list, of each key and
   *     it's evaluated groovy expression
   * @return templatesContext map updated
   * @throws ClassNotFoundException
   */
  public Map<String, Object> initMaker(
      long objectId, String model, String tag, boolean isJson, Map<String, Object> templatesContext)
      throws ClassNotFoundException;

  /**
   * This method is used to loop through templateContext list and uses the context of the object to
   * evaluate the groovy expression using templateContextService
   *
   * @param templateContextList
   * @param objectId
   * @param model
   * @param isJson
   * @param templatesContext
   * @return templatesContext map with new key context and their evaluated values
   * @throws ClassNotFoundException
   */
  public Map<String, Object> computeTemplateContexts(
      List<TemplateContext> templateContextList,
      long objectId,
      String model,
      boolean isJson,
      Map<String, Object> templatesContext)
      throws ClassNotFoundException;
}
