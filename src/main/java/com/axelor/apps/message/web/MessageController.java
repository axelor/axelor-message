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
package com.axelor.apps.message.web;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.MessageExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.tool.ModelTool;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageController {

  public void sendMessage(ActionRequest request, ActionResponse response) {
    Message message = request.getContext().asType(Message.class);

    try {
      Beans.get(MessageService.class)
          .sendMessage(Beans.get(MessageRepository.class).find(message.getId()));
      response.setReload(true);
      response.setInfo(I18n.get(MessageExceptionMessage.MESSAGE_4));
    } catch (Exception e) {
      LoggerFactory.getLogger(MessageController.class).error(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public void sendMessages(ActionRequest request, ActionResponse response) {
    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
    try {
      if (idList == null) {
        throw new IllegalStateException(
            I18n.get(MessageExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
      }
      ModelTool.apply(
          Message.class,
          idList,
          model -> Beans.get(MessageService.class).sendMessage((Message) model));
      response.setInfo(
          String.format(
              I18n.get(MessageExceptionMessage.MESSAGES_SEND_IN_PROGRESS), idList.size()));
      response.setReload(true);
    } catch (Exception e) {
      LoggerFactory.getLogger(MessageController.class).error(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public void regenerateMessages(ActionRequest request, ActionResponse response) {
    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
    try {
      if (idList == null) {
        throw new IllegalStateException(
            I18n.get(MessageExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
      }
      int error =
          ModelTool.apply(
              Message.class,
              idList,
              model -> Beans.get(MessageService.class).regenerateMessage((Message) model));
      response.setInfo(
          String.format(
              I18n.get(MessageExceptionMessage.MESSAGES_REGENERATED),
              idList.size() - error,
              error));
      response.setReload(true);
    } catch (Exception e) {
      LoggerFactory.getLogger(MessageController.class).error(e.getMessage(), e);
    }
  }

  public void setContextValues(ActionRequest request, ActionResponse response) {
    response.setValues(request.getContext().get("_message"));
  }
}
