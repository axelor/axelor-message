package com.axelor.message.service.registry;

import com.axelor.inject.Beans;
import com.axelor.message.service.MessageAction;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.Set;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageActionRegister {

  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Set<Class<? extends MessageAction>> messageActions;

  private static final String AXELOR_BASE_PACKAGE = "com.axelor";

  public MessageActionRegister() {
    this.messageActions = new LinkedHashSet<>();
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
              if (interfaces.contains(MessageAction.class)) {
                messageActions.add((Class<? extends MessageAction>) klass);
              }
            });
  }

  private void filterOutAndStoreSubClasses() {
    messageActions.removeIf(this::isSubClassPresent);

    LOG.debug("Registered classes : ");
    messageActions.forEach(klass -> LOG.debug(klass.getCanonicalName()));
  }

  private boolean isSubClassPresent(Class<?> klass) {
    return messageActions.stream()
        .anyMatch(actionClass -> !actionClass.equals(klass) && klass.isAssignableFrom(actionClass));
  }

  public Set<Class<? extends MessageAction>> getMessageActions() {
    return messageActions;
  }
}
