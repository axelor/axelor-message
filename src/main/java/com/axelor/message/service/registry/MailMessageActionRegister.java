package com.axelor.message.service.registry;

import com.axelor.inject.Beans;
import com.axelor.message.service.MailMessageAction;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MailMessageActionRegister {
  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Set<Class<? extends MailMessageAction>> mailActionClasses;

  public MailMessageActionRegister() {
    this.mailActionClasses = new LinkedHashSet<>();
  }

  public void registerAction() {
    scanMailActionClasses();
    filterOutAndStoreSubClasses();
  }

  private void scanMailActionClasses() {

    Beans.get(Injector.class).getAllBindings().keySet().stream()
        .map(Key::getTypeLiteral)
        .map(TypeLiteral::getRawType)
        .filter(MailMessageAction.class::isAssignableFrom)
        .forEach(klass -> mailActionClasses.add(klass.asSubclass(MailMessageAction.class)));
  }

  private void filterOutAndStoreSubClasses() {
    mailActionClasses.removeIf(this::isSubClassPresent);

    LOG.debug("Registered classes : ");
    mailActionClasses.forEach(klass -> LOG.debug(klass.getCanonicalName()));
  }

  private boolean isSubClassPresent(Class<?> klass) {
    return mailActionClasses.stream()
        .anyMatch(actionClass -> !actionClass.equals(klass) && klass.isAssignableFrom(actionClass));
  }

  public Set<Class<? extends MailMessageAction>> getMailActionClasses() {
    return mailActionClasses;
  }
}
