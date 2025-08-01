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
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.TemplateRepository;
import com.axelor.message.service.TemplateService;
import com.axelor.message.translation.ITranslation;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ExceptionHelper;
import java.util.Map;

public class TemplateController {

  public void generateDraftMessage(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      Template template = context.asType(Template.class);
      template = Beans.get(TemplateRepository.class).find(template.getId());
      String reference = null;
      String referenceId = null;
      if (template.getIsJson()) {
        Map<String, Object> jsonModelReference =
            (Map<String, Object>) context.get("jsonModelReference");
        Map<String, Object> jsonModelRecord = (Map<String, Object>) context.get("jsonModelRecord");
        if (jsonModelReference != null && jsonModelReference.containsKey("name")) {
          reference = (String) jsonModelReference.get("name");
        }
        if (jsonModelRecord != null && jsonModelRecord.containsKey("id")) {
          referenceId = jsonModelRecord.get("id").toString();
        }

      } else {
        reference = context.get("reference").toString();
        referenceId = context.get("referenceId").toString();
      }

      Message message =
          Beans.get(TemplateService.class).generateDraftMessage(template, reference, referenceId);
      response.setView(
          ActionView.define(I18n.get(ITranslation.MESSAGE_TEST_TEMPLATE))
              .model(Message.class.getName())
              .add("form", "message-form")
              .add("grid", "message-grid")
              .param("forceTitle", "true")
              .context("_message", message)
              .context("_templateMailAccount", template.getMailAccount())
              .map());
    } catch (Exception e) {
      ExceptionHelper.error(response, e);
    }
  }
}
