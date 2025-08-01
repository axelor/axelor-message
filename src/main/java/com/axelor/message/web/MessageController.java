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
package com.axelor.message.web;

import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.message.service.MessageService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ExceptionHelper;
import com.axelor.utils.helpers.ModelHelper;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class MessageController {

  public void sendMessage(ActionRequest request, ActionResponse response) {
    try {
      Message message = request.getContext().asType(Message.class);
      message = Beans.get(MessageRepository.class).find(message.getId());
      if (message.getStatusSelect() == MessageRepository.STATUS_DRAFT) {
        Beans.get(MessageService.class)
            .sendMessage(Beans.get(MessageRepository.class).find(message.getId()));
        response.setReload(true);
        response.setInfo(I18n.get(MessageExceptionMessage.MESSAGE_4));
      } else {
        response.setError(I18n.get(MessageExceptionMessage.MESSAGE_7));
      }
    } catch (Exception e) {
      ExceptionHelper.error(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void sendMessages(ActionRequest request, ActionResponse response) {
    try {
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");

      if (idList == null) {
        ExceptionHelper.error(
            response, I18n.get(MessageExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
        return;
      }
      MessageService messageService = Beans.get(MessageService.class);
      ModelHelper.apply(Message.class, idList, messageService::sendMessage);
      response.setInfo(
          String.format(
              I18n.get(MessageExceptionMessage.MESSAGES_SEND_IN_PROGRESS), idList.size()));
      response.setReload(true);
    } catch (Exception e) {
      ExceptionHelper.error(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void regenerateMessages(ActionRequest request, ActionResponse response) {
    try {
      List<Integer> idList = (List<Integer>) request.getContext().get("_ids");

      if (idList == null) {
        ExceptionHelper.error(
            response, I18n.get(MessageExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
        return;
      }
      MessageService messageService = Beans.get(MessageService.class);
      int error = ModelHelper.apply(Message.class, idList, messageService::regenerateMessage);
      response.setInfo(
          String.format(
              I18n.get(MessageExceptionMessage.MESSAGES_REGENERATED),
              idList.size() - error,
              error));
      response.setReload(true);
    } catch (Exception e) {
      ExceptionHelper.error(response, e);
    }
  }

  public void setContextValues(ActionRequest request, ActionResponse response) {
    try {
      response.setValues(request.getContext().get("_message"));
    } catch (Exception e) {
      ExceptionHelper.error(response, e);
    }
  }
}
