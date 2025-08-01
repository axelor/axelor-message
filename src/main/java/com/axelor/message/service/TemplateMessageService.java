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
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TemplateMessageService {

  /**
   * Generate a non-temporary message.
   *
   * @param model: Model
   * @param template: Template
   * @return persisted message
   * @throws ClassNotFoundException
   */
  Message generateMessage(Model model, Template template) throws ClassNotFoundException;

  /**
   * Store the model object in the service and then generate the message based on the parameters.
   *
   * <p>Note:
   *
   * <ul>
   *   <li>the modelObject is useful when the model is not persisted yet.
   * </ul>
   *
   * @param model: Model
   * @param template: Template
   * @param isTemporaryMessage
   * @return Message
   * @throws ClassNotFoundException
   */
  Message generateMessage(Model model, Template template, Boolean isTemporaryMessage)
      throws ClassNotFoundException;

  /**
   * Generate a message from corresponding params.
   *
   * @param objectId : id of the object record to send in the message
   * @param model : the fullName model ex: "com.axelor.contact.db.Company" (class canonicalName)
   * @param tag : model name ex: "Company" (class simpleName)
   * @param template : associated template with a model
   * @return persisted Message
   * @throws ClassNotFoundException
   */
  Message generateMessage(Long objectId, String model, String tag, Template template)
      throws ClassNotFoundException;

  /**
   * Compute the template context and use the right template engine to copy content. If the message
   * is not temporary, save it and attaches metafiles.
   *
   * @param objectId : id of the object record to send in the message
   * @param model : the fullName model ex: "com.axelor.contact.db.Company" (class canonicalName)
   * @param tag : model name ex: "Company" (class simpleName)
   * @param template : associated template with a model
   * @param isForTemporaryMessage
   * @return Message
   * @throws ClassNotFoundException
   */
  Message generateMessage(
      Long objectId, String model, String tag, Template template, Boolean isForTemporaryMessage)
      throws ClassNotFoundException;

  /**
   * Generate and Send the {@link Message}.
   *
   * @param model : Model
   * @param template : Template
   * @return persisted message
   * @throws IOException
   * @throws ClassNotFoundException
   */
  Message generateAndSendMessage(Model model, Template template)
      throws IOException, ClassNotFoundException;

  /**
   * Send the transient {@link Message} generated from the template and model.
   *
   * <p>Note: {@link Template} should be of Email type.
   *
   * @param model
   * @param template
   * @return
   * @throws MessagingException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  Message generateAndSendTemporaryMessage(Model model, Template template)
      throws MessagingException, IOException, ClassNotFoundException;

  /**
   * Update the set of message metafiles with metaAttachment from the template.
   *
   * @param template: Template of a message
   * @return Set of metafiles attached to the message
   */
  Set<MetaFile> getMetaFiles(Template template);

  /**
   * Update the set of message metafiles with metaAttachment from the template.
   *
   * @param template: Template of a message
   * @param templates: Template Implementation
   * @param templatesContext: template context map
   * @return Set of metafiles attached to the message
   */
  Set<MetaFile> getMetaFiles(
      Template template, Templates templates, Map<String, Object> templatesContext);

  /**
   * Insert the tag as a key, and the record of the model instance as a value in the map
   * templatesContext for the given model objectId. If it's a JSON search, it uses the
   * MetaJsonRecord class, otherwise uses generic class of the model.
   *
   * @param objectId : id of the object record to send in the message
   * @param model : the fullName model ex: "com.axelor.contact.db.Company" (class canonicalName)
   * @param tag : model name ex: "Company" (class simpleName)
   * @param isJson: if the model is a {@link com.axelor.meta.db.MetaJsonModel} or a {@link
   *     com.axelor.meta.db.MetaModel}
   * @param templatesContext: holds key value pairs from the template's context list, of each key
   *     and its evaluated groovy expression
   * @return templatesContext map updated
   * @throws ClassNotFoundException
   */
  Map<String, Object> initMaker(
      long objectId, String model, String tag, boolean isJson, Map<String, Object> templatesContext)
      throws ClassNotFoundException;

  /**
   * Loop through the templateContext list and uses the context of the object to evaluate the groovy
   * expression using templateContextService.
   *
   * @param templateContextList
   * @param objectId
   * @param model
   * @param isJson
   * @param templatesContext
   * @return templatesContext map with new key context and their evaluated values
   * @throws ClassNotFoundException
   */
  Map<String, Object> computeTemplateContexts(
      List<TemplateContext> templateContextList,
      long objectId,
      String model,
      boolean isJson,
      Map<String, Object> templatesContext)
      throws ClassNotFoundException;
}
