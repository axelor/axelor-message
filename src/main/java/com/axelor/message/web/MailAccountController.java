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
package com.axelor.message.web;

import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.message.service.MailAccountService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.ExceptionHelper;
import com.axelor.utils.helpers.context.adapters.Processor;
import com.google.inject.Singleton;

@Singleton
public class MailAccountController {

  public void validateSmtpAccount(ActionRequest request, ActionResponse response) {
    try {
      EmailAccount account =
          Beans.get(Processor.class).process(EmailAccount.class, request.getContext());
      Beans.get(MailAccountService.class).checkMailAccountConfiguration(account);
      response.setReload(true);
      response.setInfo(I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_3));
    } catch (Exception e) {
      ExceptionHelper.trace(response, e);
    }
  }

  public void checkDefaultMailAccount(ActionRequest request, ActionResponse response) {
    try {
      EmailAccount account = request.getContext().asType(EmailAccount.class);
      Beans.get(MailAccountService.class).checkDefaultMailAccount(account);
    } catch (Exception e) {
      response.setAttr("isDefault", "value", false);
      ExceptionHelper.trace(response, e);
    }
  }

  public void fetchEmails(ActionRequest request, ActionResponse response) {
    try {
      EmailAccount account = request.getContext().asType(EmailAccount.class);
      account = Beans.get(EmailAccountRepository.class).find(account.getId());

      int totalFetched = Beans.get(MailAccountService.class).fetchEmails(account, true);

      response.setInfo(I18n.get(String.format("Total email fetched: %s", totalFetched)));
    } catch (Exception e) {
      ExceptionHelper.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().get("newPassword") != null)
        response.setValue(
            "password",
            Beans.get(MailAccountService.class)
                .getEncryptPassword(request.getContext().get("newPassword").toString()));
    } catch (Exception e) {
      ExceptionHelper.trace(response, e);
    }
  }
}
