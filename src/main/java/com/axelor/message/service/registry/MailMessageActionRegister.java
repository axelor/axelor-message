package com.axelor.message.service.registry;

import com.axelor.inject.Beans;
import com.axelor.message.service.MailMessageAction;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.util.LinkedHashSet;
import java.util.Set;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MailMessageActionRegister {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  private final Set<Class<? extends MailMessageAction>> mailActionClasses;
  /**
   * mailActionClassesCache: used to store temporary classes that implements or override action
   * classes
   */
  private final Set<Class<? extends MailMessageAction>> mailActionClassesCache;

  private static final String AXELOR_BASE_PACKAGE = "com.axelor";

  public Set<Class<? extends MailMessageAction>> getMailActionClasses() {
    return mailActionClasses;
  }

  public MailMessageActionRegister() {
    this.mailActionClasses = new LinkedHashSet<>();
    this.mailActionClassesCache = new LinkedHashSet<>();
  }

  public void registerAction() {
    scanMailActionClasses();
    filterOutAndStoreSubClasses();
  }

  private void scanMailActionClasses() {
    Reflections reflections =
        new Reflections(new ConfigurationBuilder().forPackage(AXELOR_BASE_PACKAGE));

    Beans.get(Injector.class).getAllBindings().keySet().stream()
        .map(Key::getTypeLiteral)
        .map(TypeLiteral::getRawType)
        .forEach(
            klass -> {
              Set<Class<?>> interfaces = reflections.get(ReflectionUtils.Interfaces.of(klass));
              if (interfaces.contains(MailMessageAction.class)) {
                mailActionClassesCache.add((Class<? extends MailMessageAction>) klass);
              }
            });
  }

  private void filterOutAndStoreSubClasses() {
    mailActionClassesCache.forEach(
        klass -> {
          if (Boolean.FALSE.equals(isSubClassPresent(klass))) {
            mailActionClasses.add(klass);
          }
        });

    LOG.debug("Registered classes : ");
    mailActionClasses.forEach(klass -> LOG.debug(klass.getCanonicalName()));
  }

  private boolean isSubClassPresent(Class<?> klass) {
    return mailActionClassesCache.stream()
        .anyMatch(
            actionClass ->
                Boolean.FALSE.equals(actionClass.equals(klass))
                    && klass.isAssignableFrom(actionClass));
  }
}
