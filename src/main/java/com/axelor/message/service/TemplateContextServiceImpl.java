package com.axelor.message.service;

import com.axelor.message.db.TemplateContext;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptHelper;

public class TemplateContextServiceImpl implements TemplateContextService {
  @Override
  public Object computeTemplateContext(String groovyScript, Context values) {
    ScriptHelper scriptHelper = new GroovyScriptHelper(values);

    return scriptHelper.eval(groovyScript);
  }

  @Override
  public Object computeTemplateContext(TemplateContext templateContext, Context values) {
    return this.computeTemplateContext(templateContext.getValue(), values);
  }
}
