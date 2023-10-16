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
   * Generate message from Model and Template.
   *
   * @param model
   * @param template
   * @return
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IOException
   */
  public Message generateMessage(Model model, Template template) throws ClassNotFoundException;

  /**
   * Generate message from Model and Template.
   *
   * <p>If @param isTemporaryMessage is {@code True}, generated message will be transient.
   *
   * <p>
   *
   * @param model
   * @param template
   * @param isTemporaryMessage
   * @return
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IOException
   */
  public Message generateMessage(Model model, Template template, Boolean isTemporaryMessage)
      throws ClassNotFoundException;

  /**
   * Generate message.
   *
   * @param objectId
   * @param model
   * @param tag
   * @param template
   * @return
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IOException
   */
  public Message generateMessage(Long objectId, String model, String tag, Template template)
      throws ClassNotFoundException;

  /**
   * Generate message.
   *
   * <p>If @param isTemporaryMessage is {@code True}, generated message will be transient.
   *
   * <p>
   *
   * @param objectId
   * @param model
   * @param tag
   * @param template
   * @param isForTemporaryMessage
   * @return
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws IOException
   */
  public Message generateMessage(
      Long objectId, String model, String tag, Template template, Boolean isForTemporaryMessage)
      throws ClassNotFoundException;

  /**
   * Generate and Send the {@link Message}.
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

  public Set<MetaFile> getMetaFiles(
      Template template, Templates templates, Map<String, Object> templatesContext);

  public Map<String, Object> initMaker(
      long objectId, String model, String tag, boolean isJson, Map<String, Object> templatesContext)
      throws ClassNotFoundException;

  public Map<String, Object> computeTemplateContexts(
      List<TemplateContext> templateContextList,
      long objectId,
      String model,
      boolean isJson,
      Map<String, Object> templatesContext)
      throws ClassNotFoundException;
}
