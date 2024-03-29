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
package com.axelor.message.module;

import com.axelor.app.AxelorModule;
import com.axelor.mail.service.MailServiceImpl;
import com.axelor.message.db.repo.MessageManagementRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.listener.MailMessageServerStartListener;
import com.axelor.message.service.AppSettingsMessageService;
import com.axelor.message.service.AppSettingsMessageServiceImpl;
import com.axelor.message.service.GenerateMessageService;
import com.axelor.message.service.GenerateMessageServiceImpl;
import com.axelor.message.service.MailAccountService;
import com.axelor.message.service.MailAccountServiceImpl;
import com.axelor.message.service.MailMessageActionService;
import com.axelor.message.service.MailMessageActionServiceImpl;
import com.axelor.message.service.MailMessageCreator;
import com.axelor.message.service.MailMessageCreatorImpl;
import com.axelor.message.service.MailMessageService;
import com.axelor.message.service.MailMessageServiceImpl;
import com.axelor.message.service.MailServiceMessageImpl;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.MessageServiceImpl;
import com.axelor.message.service.SendMailQueueService;
import com.axelor.message.service.TemplateMessageService;
import com.axelor.message.service.TemplateMessageServiceImpl;
import com.axelor.message.service.TemplateService;
import com.axelor.message.service.registry.MailMessageActionRegister;
import com.axelor.utils.service.AppSettingsServiceImpl;
import com.google.inject.Singleton;

public class MessageModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(TemplateMessageService.class).to(TemplateMessageServiceImpl.class);
    bind(MessageService.class).to(MessageServiceImpl.class);
    bind(MessageRepository.class).to(MessageManagementRepository.class);
    bind(MailAccountService.class).to(MailAccountServiceImpl.class);
    bind(MailServiceImpl.class).to(MailServiceMessageImpl.class);
    bind(MailMessageService.class).to(MailMessageServiceImpl.class);
    bind(GenerateMessageService.class).to(GenerateMessageServiceImpl.class);
    bind(AppSettingsMessageService.class).to(AppSettingsMessageServiceImpl.class);
    bind(AppSettingsServiceImpl.class).to(AppSettingsMessageServiceImpl.class);
    bind(MailMessageCreator.class).to(MailMessageCreatorImpl.class);
    bind(MailMessageActionService.class).to(MailMessageActionServiceImpl.class);
    bind(MailMessageActionRegister.class).in(Singleton.class);
    // needed to use event notification methods
    bind(SendMailQueueService.class);
    bind(TemplateService.class);
    // needed to scan classes that implements the MailMessageAction in the startup
    bind(MailMessageServerStartListener.class);
  }
}
