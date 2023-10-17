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

import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.mail.MailConstants;
import com.axelor.mail.db.MailFlags;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MailMessageServiceImpl implements MailMessageService {

  protected final MailFollowerRepository mailFollowerRepository;
  protected final MailMessageCreator mailMessageCreator;

  @Inject
  public MailMessageServiceImpl(
      MailFollowerRepository mailFollowerRepository, MailMessageCreator mailMessageCreator) {
    this.mailFollowerRepository = mailFollowerRepository;
    this.mailMessageCreator = mailMessageCreator;
  }

  @Override
  @Transactional
  public void sendNotification(User user, String subject, String body) {
    this.sendNotification(user, subject, body, null, null);
  }

  @Override
  @Transactional
  public void sendNotification(
      User user, String subject, String body, Long relatedId, Class<? extends Model> relatedModel) {

    long userId = user.getId();
    mailMessageCreator.persist(
        userId,
        body,
        subject,
        MailConstants.MESSAGE_TYPE_COMMENT,
        mailMessage -> {
          if (relatedId != null && relatedModel != null) {
            mailMessage.setRelatedId(relatedId);
            mailMessage.setRelatedModel(relatedModel.getName());
          }

          MailFlags flags = new MailFlags();
          flags.setMessage(mailMessage);
          flags.setUser(JpaRepository.of(User.class).find(userId));
          flags.setIsRead(Boolean.FALSE);
          mailMessage.addFlag(flags);
        });

    if (relatedId == null || relatedModel == null) {
      return;
    }

    MailFollower follower = mailFollowerRepository.findOne(JPA.find(relatedModel, relatedId), user);
    if (follower != null && Boolean.FALSE.equals(follower.getArchived())) {
      return;
    }

    if (follower == null) {
      follower = new MailFollower();
    }

    follower.setArchived(false);
    follower.setRelatedId(relatedId);
    follower.setRelatedModel(relatedModel.getName());
    follower.setUser(user);

    mailFollowerRepository.save(follower);
  }
}
