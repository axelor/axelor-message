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
package com.axelor.message.job;

import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.repo.EmailAccountRepository;
import com.axelor.message.service.MailAccountService;
import com.axelor.utils.helpers.ExceptionHelper;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import javax.mail.MessagingException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An example {@link Job} class that prints a some messages to the stderr. */
public class FetchEmailJob implements Job {

  protected final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final MailAccountService mailAccountService;

  protected final EmailAccountRepository mailAccountRepo;

  @Inject
  public FetchEmailJob(
      MailAccountService mailAccountService, EmailAccountRepository mailAccountRepo) {
    this.mailAccountService = mailAccountService;
    this.mailAccountRepo = mailAccountRepo;
  }

  @Override
  public void execute(JobExecutionContext context) {

    List<EmailAccount> mailAccounts =
        mailAccountRepo.all().filter("self.isValid = true and self.serverTypeSelect > 1").fetch();

    log.debug("Total email fetching accounts : {}", mailAccounts.size());
    for (EmailAccount account : mailAccounts) {
      try {
        Integer total = mailAccountService.fetchEmails(account, true);
        log.debug("Email fetched for account: {}, total: {} ", account.getName(), total);
      } catch (MessagingException | IOException e) {
        ExceptionHelper.trace(e);
      }
    }
  }
}
