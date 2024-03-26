package com.axelor.message.registry;

import com.axelor.inject.Beans;
import com.axelor.message.service.MailMessageAction;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailMessageActionRegister {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  private static MailMessageActionRegister instance;
  private final Set<Class<? extends MailMessageAction>> mailActionClasses;

  public Set<Class<? extends MailMessageAction>> getMailActionClasses() {
    return mailActionClasses;
  }

  private MailMessageActionRegister() {
    this.mailActionClasses = new LinkedHashSet<>();
  }

  public void registerAction() {
    /*
        Beans.get(Injector.class)
            .getAllBindings()
            .entrySet()
            .forEach(entry -> LOG.debug(entry.toString()));
    */
    Beans.get(Injector.class).getAllBindings().keySet().stream()
        .map(Key::getTypeLiteral)
        .map(TypeLiteral::getRawType)
        .forEach(
            klass -> {
              Class<?>[] interfaces = klass.getInterfaces();
              boolean isMailMessageActionInterfaceExist =
                  Arrays.asList(interfaces).contains(MailMessageAction.class);
              if (isMailMessageActionInterfaceExist) {
                mailActionClasses.add((Class<? extends MailMessageAction>) klass);
              }
            });
  }

  public static MailMessageActionRegister getInstance() {
    if (instance == null) {
      instance = new MailMessageActionRegister();
    }
    return instance;
  }
}
