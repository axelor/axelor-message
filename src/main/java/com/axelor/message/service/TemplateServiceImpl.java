/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2026 Axelor (<http://axelor.com>).
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

import com.axelor.app.internal.AppFilter;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.exception.MessageExceptionMessage;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.utils.template.TemplateMaker;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.util.Iterator;
import java.util.Map;

public class TemplateServiceImpl implements TemplateService {

  public void checkTargetReceptor(Template template) {
    String target = template.getTarget();
    MetaModel metaModel = template.getMetaModel();

    if (Strings.isNullOrEmpty(target)) {
      return;
    }
    if (metaModel == null) {
      throw new IllegalStateException(I18n.get(MessageExceptionMessage.TEMPLATE_SERVICE_1));
    }

    try {
      this.validTarget(target, metaModel);
    } catch (Exception e) {
      throw new IllegalStateException(I18n.get(MessageExceptionMessage.TEMPLATE_SERVICE_2), e);
    }
  }

  private void validTarget(String target, MetaModel metaModel) throws ClassNotFoundException {
    Iterator<String> iter = Splitter.on(".").split(target).iterator();
    Property p = Mapper.of(Class.forName(metaModel.getFullName())).getProperty(iter.next());
    while (iter.hasNext() && p != null) {
      p = Mapper.of(p.getTarget()).getProperty(iter.next());
    }

    if (p == null) {
      throw new IllegalArgumentException();
    }
  }

  public String processSubject(
      String timeZone,
      Template template,
      Model bean,
      String beanName,
      Map<String, Object> context) {
    TemplateMaker maker = new TemplateMaker(timeZone, AppFilter.getLocale(), '$', '$');

    maker.setTemplate(template.getSubject());
    maker.setContext(bean, context, beanName);
    return maker.make();
  }

  public String processContent(
      String timeZone,
      Template template,
      Model bean,
      String beanName,
      Map<String, Object> context) {
    TemplateMaker maker = new TemplateMaker(timeZone, AppFilter.getLocale(), '$', '$');

    maker.setTemplate(template.getContent());
    maker.setContext(bean, context, beanName);
    return maker.make();
  }

  @SuppressWarnings("unchecked")
  public Message generateDraftMessage(Template template, String reference, String referenceId)
      throws ClassNotFoundException {
    Model modelObject = null;
    if (template.getIsJson()) {
      modelObject =
          Beans.get(MetaJsonModelRepository.class)
              .all()
              .filter("self.name = ?", reference)
              .fetchOne();
    } else {
      MetaModel metaModel =
          Beans.get(MetaModelRepository.class)
              .all()
              .filter("self.fullName = ?", reference)
              .fetchOne();
      if (metaModel == null) {
        return null;
      }
      String model = metaModel.getFullName();
      if (StringUtils.notEmpty(model)) {
        Class<? extends Model> modelClass = (Class<? extends Model>) Class.forName(model);
        modelObject = JPA.find(modelClass, Long.valueOf(referenceId));
      }
    }

    return Beans.get(TemplateMessageService.class).generateMessage(modelObject, template, true);
  }
}
