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

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.exception.MessageExceptionMessage;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import org.slf4j.LoggerFactory;

@Singleton
public class MailAccountController {

  public void validateSmtpAccount(ActionRequest request, ActionResponse response) {
    try {
      EmailAccount account = request.getContext().asType(EmailAccount.class);
      Beans.get(MailAccountService.class).checkMailAccountConfiguration(account);

      response.setValue("isValid", Boolean.TRUE);
      response.setValue("change", Boolean.FALSE);
      response.setValue("newPassword", null);
      response.setInfo(I18n.get(MessageExceptionMessage.MAIL_ACCOUNT_3));

    } catch (Exception e) {
      LoggerFactory.getLogger(MailAccountController.class).error(e.getMessage(), e);
      response.setValue("isValid", Boolean.FALSE);
    }
  }

  public void checkDefaultMailAccount(ActionRequest request, ActionResponse response) {
    try {
      EmailAccount account = request.getContext().asType(EmailAccount.class);
      Beans.get(MailAccountService.class).checkDefaultMailAccount(account);
    } catch (Exception e) {
      LoggerFactory.getLogger(MailAccountController.class).error(e.getMessage(), e);
      response.setAttr("isDefault", "value", false);
      response.setInfo(e.getMessage());
    }
  }

  public void fetchEmails(ActionRequest request, ActionResponse response) {
    try {
      EmailAccount account = request.getContext().asType(EmailAccount.class);
      account = Beans.get(EmailAccountRepository.class).find(account.getId());

      int totalFetched = Beans.get(MailAccountService.class).fetchEmails(account, true);

      response.setInfo(I18n.get(String.format("Total email fetched: %s", totalFetched)));
    } catch (Exception e) {
      LoggerFactory.getLogger(MailAccountController.class).error(e.getMessage(), e);
      response.setException(e);
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
      LoggerFactory.getLogger(MailAccountController.class).error(e.getMessage(), e);
      response.setException(e);
    }
  }
}
